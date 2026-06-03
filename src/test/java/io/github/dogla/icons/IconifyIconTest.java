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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Offline tests for {@link IconifyIcon}. Tier 4 (HTTP download) is never
 * exercised: tests either route through the classpath (tier 2), through a
 * temp-directory file-system cache (tier 3), or rely on
 * {@link IconifyIcon#setOfflineMode(boolean) offline mode}.
 */
class IconifyIconTest {

	private static final String MIN_SVG =
		"<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'>" +
		"<rect width='24' height='24' fill='currentColor'/></svg>";

	@BeforeEach
	@AfterEach
	void resetState() {
		IconifyIcon.setCacheDirectory(null);
		IconifyIcon.setOfflineMode(false);
	}

	// ---------------------------------------------------------------- parsing

	@Test
	void parsesPrefixAndName() {
		IconifyIcon icon = new IconifyIcon("fa6-solid:heart");
		assertEquals("fa6-solid", icon.getPrefix());
		assertEquals("heart", icon.getName());
	}

	@Test
	void parsesNameContainingColons() {
		IconifyIcon icon = new IconifyIcon("prefix:weird:name");
		assertEquals("prefix", icon.getPrefix());
		assertEquals("weird:name", icon.getName());
	}

	@Test
	void parsesQueryString() {
		IconifyIcon icon = new IconifyIcon("mdi:home?size=48&fg=#ffffff&bg=#000000");
		assertEquals("mdi", icon.getPrefix());
		assertEquals("home", icon.getName());
	}

	@Test
	void invalidQuerySizeIsIgnored() {
		IconifyIcon icon = new IconifyIcon("mdi:home?size=abc");
		assertEquals("home", icon.getName());
	}

	@Test
	void nullDescriptorThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IconifyIcon(null));
	}

	@Test
	void emptyDescriptorThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IconifyIcon(""));
	}

	@Test
	void descriptorWithoutColonThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IconifyIcon("noColon"));
	}

	// ----------------------------------------------------- cache configuration

	@Test
	void setCacheDirectoryIsReturnedByGetCacheDirectory(@TempDir File tmp) {
		IconifyIcon.setCacheDirectory(tmp);
		assertSame(tmp, IconifyIcon.getCacheDirectory());
	}

	@Test
	void defaultCacheDirectoryPicksProjectResourceFolder() {
		// Tests run from the project root where src/main/resources exists,
		// so the default resolution should pick the dev-mode resource folder.
		File dir = IconifyIcon.getCacheDirectory();
		assertNotNull(dir);
		String path = dir.getPath().replace('\\', '/');
		assertTrue(path.endsWith("src/main/resources/iconify"),
			"Expected dev-mode resource folder, got: " + dir);
	}

	@Test
	void useUserCacheDirectoryRoutesUnderAppName() {
		IconifyIcon.useUserCacheDirectory("MyTestApp");
		File dir = IconifyIcon.getCacheDirectory();
		assertNotNull(dir);
		String path = dir.getPath().replace('\\', '/');
		assertTrue(path.contains("MyTestApp/iconify"),
			"Expected app-scoped iconify path, got: " + dir);
	}

	@Test
	void useUserCacheDirectoryNullThrows() {
		assertThrows(IllegalArgumentException.class, () -> IconifyIcon.useUserCacheDirectory(null));
	}

	@Test
	void useUserCacheDirectoryEmptyThrows() {
		assertThrows(IllegalArgumentException.class, () -> IconifyIcon.useUserCacheDirectory(""));
	}

	// ----------------------------------------------------------- classpath tier

	@Test
	void rendersFromClasspathResource() {
		// src/test/resources/iconify/test-cp/icon.svg is shipped with the test sources.
		IconifyIcon icon = new IconifyIcon("test-cp:icon");
		byte[] png = icon.toBytes(32);
		assertPngBytes(png);
	}

	@Test
	void classpathTierWorksEvenInOfflineMode(@TempDir File emptyCache) {
		// No filesystem cache hit (empty dir), no HTTP. The icon must still
		// load via the classpath resource.
		IconifyIcon.setCacheDirectory(emptyCache);
		IconifyIcon.setOfflineMode(true);

		IconifyIcon icon = new IconifyIcon("test-cp:icon");
		byte[] png = icon.toBytes(32);
		assertPngBytes(png);
	}

	// -------------------------------------------------------- filesystem tier

	@Test
	void rendersFromFileSystemCacheWithoutNetwork(@TempDir Path tmp) throws Exception {
		// Unique prefix/name avoids the static in-memory cache colliding with
		// other tests running in the same JVM.
		String prefix = "test-fs-cache";
		String name = "render-icon";
		Path svgPath = tmp.resolve(prefix).resolve(name + ".svg");
		Files.createDirectories(svgPath.getParent());
		Files.writeString(svgPath, MIN_SVG, StandardCharsets.UTF_8);

		IconifyIcon.setCacheDirectory(tmp.toFile());

		IconifyIcon icon = new IconifyIcon(prefix + ":" + name);
		byte[] png = icon.toBytes(32);
		assertPngBytes(png);
	}

	// ------------------------------------------------------------ offline mode

	@Test
	void isOfflineModeReflectsSetter() {
		assertFalse(IconifyIcon.isOfflineMode());
		IconifyIcon.setOfflineMode(true);
		assertTrue(IconifyIcon.isOfflineMode());
		IconifyIcon.setOfflineMode(false);
		assertFalse(IconifyIcon.isOfflineMode());
	}

	@Test
	void offlineModeReturnsNullForUnknownIcon(@TempDir File emptyCache) {
		IconifyIcon.setCacheDirectory(emptyCache);
		IconifyIcon.setOfflineMode(true);

		// Unique prefix to ensure no leftover hit in the static in-memory cache.
		IconifyIcon icon = new IconifyIcon("test-offline-prefix:never-cached-name");
		assertNull(icon.toBytes(16));
	}

	// ------------------------------------------------------------------- util

	private static void assertPngBytes(byte[] png) {
		assertNotNull(png, "Expected a non-null PNG byte array");
		// PNG magic: 89 50 4E 47
		assertEquals((byte) 0x89, png[0]);
		assertEquals((byte) 0x50, png[1]);
		assertEquals((byte) 0x4E, png[2]);
		assertEquals((byte) 0x47, png[3]);
	}

}
