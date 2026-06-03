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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SvgIconTest {

	private static final String MIN_SVG =
		"<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'>" +
		"<rect width='24' height='24' fill='currentColor'/></svg>";

	@Test
	void rendersSvgStringToPngOfRequestedSize() throws Exception {
		SvgIcon icon = new SvgIcon(MIN_SVG);
		byte[] png = icon.toBytes(48);

		assertNotNull(png);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		assertNotNull(img);
		assertEquals(48, img.getWidth());
		assertEquals(48, img.getHeight());
	}

	@Test
	void rendersSvgFromFile(@TempDir Path tmp) throws Exception {
		Path svg = tmp.resolve("test.svg");
		Files.writeString(svg, MIN_SVG, StandardCharsets.UTF_8);

		SvgIcon icon = new SvgIcon(svg.toFile());
		byte[] png = icon.toBytes();

		assertNotNull(png);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		assertNotNull(img);
	}

	@Test
	void missingSvgFileThrowsIOException() {
		assertThrows(IOException.class, () -> new SvgIcon(new File("does-not-exist.svg")));
	}

	@Test
	void nullSvgContentReturnsNullBytes() {
		SvgIcon icon = new SvgIcon((String) null);
		assertNull(icon.toBytes());
		assertNull(icon.toBytes(32));
		assertNull(icon.toBytes(32, "#ff0000", null));
	}

	@Test
	void differentForegroundColorsProduceDifferentPngs() {
		SvgIcon icon = new SvgIcon(MIN_SVG);
		byte[] red = icon.toBytes(32, "#ff0000", null);
		byte[] blue = icon.toBytes(32, "#0000ff", null);

		assertNotNull(red);
		assertNotNull(blue);
		// currentColor is replaced — two distinct fills must yield two
		// distinct PNG byte streams.
		assertFalse(Arrays.equals(red, blue), "Expected different bytes for different fg colors");
	}

	@Test
	void backgroundColorAffectsRendering() {
		// A circle leaves the corners of the viewport empty — the bg color
		// shows through there. A full-viewport rect would hide it.
		String circleSvg =
			"<svg xmlns='http://www.w3.org/2000/svg' width='24' height='24' viewBox='0 0 24 24'>" +
			"<circle cx='12' cy='12' r='6' fill='currentColor'/></svg>";
		SvgIcon icon = new SvgIcon(circleSvg);

		byte[] withBg = icon.toBytes(32, "#ff0000", "#00ff00");
		byte[] withoutBg = icon.toBytes(32, "#ff0000", null);

		assertNotNull(withBg);
		assertNotNull(withoutBg);
		assertFalse(Arrays.equals(withBg, withoutBg), "Expected background color to affect output");
	}

	@Test
	void nonPositiveSizeFallsBackToDefault() throws Exception {
		SvgIcon icon = new SvgIcon(MIN_SVG);
		byte[] png = icon.toBytes(0);
		assertNotNull(png);

		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		// Default size is 24
		assertEquals(24, img.getWidth());
		assertEquals(24, img.getHeight());
	}

}
