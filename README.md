# Icons

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.dogla/icons.svg)](https://central.sonatype.com/artifact/io.github.dogla/icons)

<p align="center">
  <picture>
    <img src="docs/banner.png" alt="Icons Library" width="600"/>
  </picture>
</p>

Lightweight, UI-toolkit-agnostic icon library for Java 17+.
Delivers icons as PNG bytes -- use them with Swing, JavaFX, SWT, or any framework.

## Features

- **200,000+ icons** via [Iconify](https://iconify.design) integration (FontAwesome, Material Design, Lucide, and [many more](https://icon-sets.iconify.design))
- **No UI dependency** in the API -- icons are plain `byte[]` (PNG)
- **4-tier caching** -- in-memory → classpath bundle → filesystem → HTTP, with an offline mode for production builds
- **Platform-aware user cache** -- dynamic icons persist between runs in the OS-conforming user cache directory
- **JSVG rendering** -- fast, lightweight SVG-to-PNG (no Apache Batik)
- **SPI extensible** -- register custom `IconFactory` implementations via ServiceLoader
- **Visible fallback** -- unresolved icons render as a pastel-red `?` placeholder instead of `null`

## Quick Start

```xml
<dependency>
    <groupId>io.github.dogla</groupId>
    <artifactId>icons</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
// Iconify icon (downloaded + cached automatically)
Icon icon = Icon.valueOf("fa6-regular:bookmark");
byte[] png = icon.toBytes();

// With size and colors
IconifyIcon heart = new IconifyIcon("fa6-solid:heart?size=32&fg=#ff0000");
byte[] png32 = heart.toBytes(32);

// SVG icon from string or file
Icon svgIcon = new SvgIcon("<svg viewBox='0 0 24 24'>...</svg>");
Icon svgFile = new SvgIcon(new File("logo.svg"));
byte[] colored = ((ColorableIcon) svgIcon).toBytes(48, "#ff0000", "#ffffff");

// Base64 encoded
String base64 = icon.toBase64();

// From existing data
Icon fromBytes = new BytesIcon(pngBytes);
Icon fromBase64 = new Base64Icon(base64String);
Icon fromFile = new FileIcon(new File("icon.png"));

// Lazy loading (resolved on first access)
Icon lazy = new LazyIcon(() -> loadExpensiveIcon());

// Windows native file icons
Icon fileIcon = Icons.extractFileIconAsIcon(new File("C:/Program Files/app.exe"));

// Scale any icon to a specific size
byte[] scaled = Icons.scaleBytes(pngBytes, 48);

// Auto-detect: Icon.valueOf() handles strings, byte[], files, etc.
Icon auto1 = Icon.valueOf("fa6-regular:star");           // → IconifyIcon
Icon auto2 = Icon.valueOf("<svg>...</svg>");              // → SvgIcon
Icon auto3 = Icon.valueOf("iVBORw0KGgo...");             // → Base64Icon
Icon auto4 = Icon.valueOf(new File("logo.svg"));          // → SvgIcon
Icon auto5 = Icon.valueOf(new File("photo.png"));         // → FileIcon
```

## Icon Types

All icon implementations support `toBytes(int size)` to render at a specific size — vector sources re-render, raster sources are scaled.

| Class | Input | Sizable | Colorable |
|-------|-------|---------|-----------|
| `IconifyIcon` | Iconify descriptor (`prefix:name`) | yes (SVG) | yes |
| `SvgIcon` | SVG string or `.svg` file | yes (SVG) | yes |
| `Base64Icon` | Base64-encoded PNG string | yes (pixel scaling) | no |
| `BytesIcon` | Raw PNG `byte[]` | yes (pixel scaling) | no |
| `FileIcon` | Image `File` | yes (pixel scaling) | no |
| `ImageIcon` | `BufferedImage` | yes (pixel scaling) | no |
| `LazyIcon` | `Supplier<Icon>` | delegates | delegates |
| `NullIcon` | -- (placeholder) | yes (rendered) | no |

`Icon.valueOf()` auto-detects the type: SVG strings → `SvgIcon`, `prefix:name` → `IconifyIcon`, `.svg` files → `SvgIcon`, Base64 → `Base64Icon`, `byte[]` → `BytesIcon`, other files → `FileIcon`. If nothing matches, `Icon.valueOf(value)` returns `NullIcon.getInstance()` — a visible pastel-red `?` placeholder, so missing icons stand out without crashing the UI. Pass your own fallback via `Icon.valueOf(value, myFallback)`.

## Iconify

Everything in this section is specific to `IconifyIcon` — the integration with [Iconify](https://iconify.design) for on-demand SVG icons.

### Descriptor Format

```
prefix:name[?size=N&fg=#hex&bg=#hex]
```

Examples:
- `fa6-regular:bookmark` -- FontAwesome 6 regular bookmark
- `mdi:home?size=48&fg=#ffffff` -- Material Design home, 48px, white
- `lucide:settings?fg=#333333&bg=#f0f0f0` -- Lucide settings with colors

Browse available icons at [icon-sets.iconify.design](https://icon-sets.iconify.design).

### Caching

`IconifyIcon` resolves SVGs through a 4-tier lookup, top to bottom:

1. **In-memory** -- JVM-wide static map (process lifetime).
2. **Classpath** -- `/iconify/<prefix>/<name>.svg` from the application JAR.
3. **Filesystem** -- under `IconifyIcon.getCacheDirectory()`; populated automatically when downloads succeed.
4. **HTTP** -- `https://api.iconify.design/<prefix>/<name>.svg`. Disabled when `setOfflineMode(true)` is set.

Three workflows are supported out of the box.

#### Bundle icons into your app (static set known at build time)

Icons end up on the classpath (tier 2) and ship inside your JAR. No HTTP calls in production.

```java
// 1) During development, run your app once. By default,
//    downloaded SVGs land in src/main/resources/iconify/...
//    (the default cache dir when src/main/resources exists).
new IconifyIcon("fa6-solid:heart").toBytes(24);

// 2) Commit the files under src/main/resources/iconify/, rebuild.
//    Subsequent runs serve them from the classpath - no network.

// 3) Optional: guarantee no HTTP in production.
IconifyIcon.setOfflineMode(true);
```

#### Dynamic icons (descriptors chosen by the end user at runtime)

Icons land in a platform-conforming user cache (tier 3) so a download happens only the first time.

```java
// Once, at application startup:
IconifyIcon.useUserCacheDirectory("MyApp");
```

Resolves to:
- Windows: `%LOCALAPPDATA%\MyApp\iconify\`
- macOS: `~/Library/Caches/MyApp/iconify/`
- Linux: `~/.cache/MyApp/iconify/` (honours `$XDG_CACHE_HOME`)

#### Custom cache location

```java
IconifyIcon.setCacheDirectory(new File("C:\\ProgramData\\MyApp\\iconify"));
```

#### Default cache directory

If neither `setCacheDirectory` nor `useUserCacheDirectory` is called, `getCacheDirectory()` returns:

| Condition | Default |
|---|---|
| `src/main/resources/` exists next to the working directory | `src/main/resources/iconify` (dev/bundle mode) |
| Otherwise | platform user cache at `dogla/icons/iconify` |

## SPI Extensibility

Register custom `IconFactory` implementations via `META-INF/services/io.github.dogla.icons.IconFactory`:

```java
public class MyIconFactory implements IconFactory {
    public boolean canHandle(Object value) { return value instanceof MyType; }
    public Icon create(Object value) { return new MyIcon((MyType) value); }
}
```

## Utilities (`Icons` class)

```java
// Scale PNG bytes to a specific size (bicubic interpolation)
byte[] scaled = Icons.scaleBytes(pngBytes, 48);

// Extract native Windows file icon as PNG bytes (cached by extension)
byte[] icon = Icons.extractFileIcon(new File("document.pdf"));

// Extract as lazy Icon (resolved in background)
Icon icon = Icons.extractFileIconAsIcon(new File("app.exe"));
```

## Framework Integration

Since icons are plain `byte[]` (PNG), they work with any Java UI toolkit:

### Swing

```java
Icon icon = new IconifyIcon("fa6-solid:gear?fg=#333333");
byte[] png = icon.toBytes(24);
javax.swing.ImageIcon swingIcon = new javax.swing.ImageIcon(png);
JLabel label = new JLabel(swingIcon);
```

### JavaFX

```java
Icon icon = new IconifyIcon("mdi:home?fg=#ffffff");
byte[] png = icon.toBytes(32);
javafx.scene.image.Image fxImage = new javafx.scene.image.Image(new ByteArrayInputStream(png));
ImageView view = new ImageView(fxImage);
```

### SWT

```java
Icon icon = new IconifyIcon("lucide:settings?fg=#000000");
byte[] png = icon.toBytes(16);
org.eclipse.swt.graphics.Image swtImage = new org.eclipse.swt.graphics.Image(
    display, new org.eclipse.swt.graphics.ImageData(new ByteArrayInputStream(png)));
label.setImage(swtImage);
```

### Headless / Web

```java
Icon icon = new IconifyIcon("fa6-brands:github");
String base64 = icon.toBase64(64);
String imgTag = "<img src='data:image/png;base64," + base64 + "'/>";
```

## Requirements

- Java 17+
- Windows (for `Icons.extractFileIcon()` -- all other features are cross-platform)

## License

[Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
