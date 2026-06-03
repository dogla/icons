/*
 * Copyright (C) 2026 Dominik Glaser
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.dogla.icons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SVG icon loaded from the Iconify API (https://api.iconify.design/).
 * Extends {@link SvgIcon} with a 4-tier cache lookup:
 * <ol>
 * <li>In-memory (JVM-wide, static).</li>
 * <li>Classpath resource at {@code /iconify/<prefix>/<name>.svg} — for icons
 *     bundled into the application JAR.</li>
 * <li>Filesystem cache at {@link #getCacheDirectory()} — populated automatically
 *     when downloads succeed.</li>
 * <li>HTTP download from {@code https://api.iconify.design/} — unless the
 *     library is in {@linkplain #setOfflineMode offline mode}.</li>
 * </ol>
 * <p>
 * Descriptor format: {@code "prefix:name"} or {@code "prefix:name?size=24&fg=#fff&bg=#000"}
 * </p>
 */
@SuppressWarnings("nls")
public class IconifyIcon extends SvgIcon {

	private static final Logger logger = LoggerFactory.getLogger(IconifyIcon.class);

	/** Path prefix used for classpath resource lookup (tier 2). */
	private static final String RESOURCE_PREFIX = "/iconify/"; //$NON-NLS-1$

	private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
	private static final ConcurrentHashMap<String, String> svgCache = new ConcurrentHashMap<>();
	private static volatile File cacheDirectory;
	private static volatile boolean offlineMode;

    private final String prefix;
    private final String name;
    private final int defaultSize;
    private final String defaultFg;
    private final String defaultBg;

    /**
     * Creates an IconifyIcon from the given descriptor.
     *
     * @param descriptor format: {@code "prefix:name"} or {@code "prefix:name?size=24&fg=#fff&bg=#000"}
     */
	public IconifyIcon(String descriptor) {
        super(); // deferred SVG loading
        if (descriptor == null || descriptor.isEmpty()) {
            throw new IllegalArgumentException("Descriptor must not be null or empty");
        }

        String iconPart;
        String queryPart = null;

        int qIdx = descriptor.indexOf('?');
        if (qIdx >= 0) {
            iconPart = descriptor.substring(0, qIdx);
            queryPart = descriptor.substring(qIdx + 1);
        } else {
            iconPart = descriptor;
        }

        int colonIdx = iconPart.indexOf(':');
        if (colonIdx < 0) {
            throw new IllegalArgumentException("Descriptor must contain ':' to separate prefix and name: " + descriptor);
        }
        this.prefix = iconPart.substring(0, colonIdx);
        this.name = iconPart.substring(colonIdx + 1);

        // Parse query params
        int parsedSize = 24;
        String parsedFg = null;
        String parsedBg = null;

        if (queryPart != null && !queryPart.isEmpty()) {
            for (String param : queryPart.split("&")) {
                int eqIdx = param.indexOf('=');
                if (eqIdx < 0) {
					continue;
				}
                String key = param.substring(0, eqIdx).trim();
                String value = param.substring(eqIdx + 1).trim();
                switch (key) {
                    case "size":
                        try {
                            parsedSize = Integer.parseInt(value);
                        } catch (NumberFormatException e) {
                            // ignore, use default
                        }
                        break;
                    case "fg":
                        parsedFg = value;
                        break;
                    case "bg":
                        parsedBg = value;
                        break;
                }
            }
        }

        this.defaultSize = parsedSize;
        this.defaultFg = parsedFg;
        this.defaultBg = parsedBg;
    }

    /**
     * Returns the Iconify collection prefix.
     *
     * @return the prefix portion of the descriptor (e.g. {@code "fa6-solid"})
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Returns the Iconify icon name within its collection.
     *
     * @return the name portion of the descriptor (e.g. {@code "heart"})
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Triggers the 4-tier lookup (in-memory → classpath → filesystem → HTTP).
     * </p>
     */
    @Override
    public String getSvgContent() {
        return loadSvg();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Renders at the default size and colors parsed from the descriptor's
     * query string (or {@code size=24} and {@code null} colors if absent).
     * </p>
     */
    @Override
    public byte[] toBytes() {
        return toBytes(defaultSize, defaultFg, defaultBg);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses the descriptor's default {@code fg}/{@code bg} colors.
     * </p>
     */
    @Override
    public byte[] toBytes(int size) {
        return toBytes(size, defaultFg, defaultBg);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Parameters override the defaults parsed from the descriptor's query
     * string: a {@code size <= 0} or a {@code null} color falls back to the
     * descriptor default.
     * </p>
     */
    @Override
    public byte[] toBytes(int size, String fg, String bg) {
        int effectiveSize = (size <= 0) ? defaultSize : size;
        String effectiveFg = (fg != null) ? fg : defaultFg;
        String effectiveBg = (bg != null) ? bg : defaultBg;
        return super.toBytes(effectiveSize, effectiveFg, effectiveBg);
    }

    /**
     * Loads the SVG content using the 4-tier lookup described on the class.
     */
    private String loadSvg() {
        String cacheKey = prefix + ":" + name; //$NON-NLS-1$

        // Tier 1: in-memory cache
        String cached = svgCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // Tier 2: classpath resource (icons bundled into the application JAR)
        String fromResource = loadFromClasspath();
        if (fromResource != null) {
            svgCache.put(cacheKey, fromResource);
            return fromResource;
        }

        // Tier 3: filesystem cache
        File cacheDir = getCacheDirectory();
        File svgFile = (cacheDir != null) ? new File(new File(cacheDir, prefix), name + ".svg") : null; //$NON-NLS-1$
        if (svgFile != null && svgFile.exists()) {
            try {
                String content = new String(Files.readAllBytes(svgFile.toPath()), StandardCharsets.UTF_8);
                svgCache.put(cacheKey, content);
                return content;
            } catch (IOException e) {
                // fall through to download
            }
        }

        // Tier 4: HTTP download — skipped in offline mode
        if (offlineMode) {
            logger.warn("Icon '{}' not found in cache and offline mode is enabled", cacheKey); //$NON-NLS-1$
            return null;
        }
        return downloadAndCache(cacheKey, svgFile);
    }

    private String loadFromClasspath() {
        String resourcePath = RESOURCE_PREFIX + prefix + "/" + name + ".svg"; //$NON-NLS-1$ //$NON-NLS-2$
        try (InputStream in = IconifyIcon.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.warn("Failed to read classpath resource {}: {}", resourcePath, e.getMessage()); //$NON-NLS-1$
            return null;
        }
    }

    private String downloadAndCache(String cacheKey, File svgFile) {
        String url = "https://api.iconify.design/" + prefix + "/" + name + ".svg"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() == 200) {
                String content = response.body();
                if (svgFile != null) {
                    try {
                        Path parentPath = svgFile.getParentFile().toPath();
                        Files.createDirectories(parentPath);
                        Files.write(svgFile.toPath(), content.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        // Ignore caching errors — read-only target dir is fine
                    }
                }
                svgCache.put(cacheKey, content);
                return content;
            }
            // Do not cache error responses — allow retry on next call
            logger.error("Failed to download SVG from {}: HTTP {}", url, response.statusCode()); //$NON-NLS-1$
        } catch (IOException e) {
            logger.warn("Failed to download SVG from {}: {}", url, e.getMessage()); //$NON-NLS-1$
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }

    /**
     * Sets the directory used for caching downloaded SVG files (tier 3).
     * Pass {@code null} to restore the default resolution described in
     * {@link #getCacheDirectory()}.
     *
     * @param directory the cache directory; {@code null} restores the default
     */
    public static void setCacheDirectory(File directory) {
        cacheDirectory = directory;
    }

    /**
     * Returns the directory used for caching downloaded SVG files (tier 3).
     * Resolution order:
     * <ol>
     * <li>If {@link #setCacheDirectory(File)} was called with a non-null value, that wins.</li>
     * <li>If {@code src/main/resources} exists in the working directory, returns
     *     {@code src/main/resources/iconify} — so downloaded icons land in the
     *     project's resource folder and are bundled by the next build.</li>
     * <li>Otherwise returns a platform-conforming user cache directory
     *     under {@code dogla/icons/iconify}:
     *     <ul>
     *     <li>Windows: {@code %LOCALAPPDATA%\dogla\icons\iconify}</li>
     *     <li>macOS: {@code ~/Library/Caches/dogla/icons/iconify}</li>
     *     <li>Linux: {@code $XDG_CACHE_HOME/dogla/icons/iconify} (or {@code ~/.cache/dogla/icons/iconify})</li>
     *     </ul>
     * </li>
     * </ol>
     *
     * @return the resolved cache directory; never {@code null}
     */
    public static File getCacheDirectory() {
        if (cacheDirectory != null) {
            return cacheDirectory;
        }
        if (new File("src/main/resources").isDirectory()) { //$NON-NLS-1$
            return new File("src/main/resources/iconify"); //$NON-NLS-1$
        }
        return defaultUserCache("dogla/icons/iconify"); //$NON-NLS-1$
    }

    /**
     * Configures a platform-conforming user cache under
     * {@code <userCache>/<appName>/iconify}. Use this when your application
     * loads Iconify icons dynamically at runtime (e.g. based on user input)
     * and you want the cache to persist across restarts.
     *
     * @param appName your application name, used as the cache sub-folder
     */
    public static void useUserCacheDirectory(String appName) {
        if (appName == null || appName.isEmpty()) {
            throw new IllegalArgumentException("appName must not be null or empty"); //$NON-NLS-1$
        }
        cacheDirectory = defaultUserCache(appName + "/iconify"); //$NON-NLS-1$
    }

    /**
     * Enables or disables offline mode. When enabled, tier 4 (HTTP download)
     * is skipped — icons not present in tiers 1–3 resolve to {@code null}.
     * Useful for production builds that must not make outbound HTTP calls.
     *
     * @param enabled {@code true} to disable HTTP downloads
     */
    public static void setOfflineMode(boolean enabled) {
        offlineMode = enabled;
    }

    /**
     * Reports whether offline mode is currently active.
     *
     * @return {@code true} if HTTP downloads are currently disabled
     */
    public static boolean isOfflineMode() {
        return offlineMode;
    }

    private static File defaultUserCache(String relativePath) {
        String os = System.getProperty("os.name", "").toLowerCase(); //$NON-NLS-1$ //$NON-NLS-2$
        String home = System.getProperty("user.home"); //$NON-NLS-1$
        File base;
        if (os.contains("win")) { //$NON-NLS-1$
            String local = System.getenv("LOCALAPPDATA"); //$NON-NLS-1$
            base = new File((local != null && !local.isEmpty()) ? local : home);
        } else if (os.contains("mac")) { //$NON-NLS-1$
            base = new File(home, "Library/Caches"); //$NON-NLS-1$
        } else {
            String xdg = System.getenv("XDG_CACHE_HOME"); //$NON-NLS-1$
            base = (xdg != null && !xdg.isEmpty()) ? new File(xdg) : new File(home, ".cache"); //$NON-NLS-1$
        }
        return new File(base, relativePath);
    }
}
