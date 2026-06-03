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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileIconTest {

	@Test
	void readsBytesFromExistingFile(@TempDir Path tmp) throws Exception {
		byte[] payload = new byte[] { 1, 2, 3, 4, 5 };
		Path file = tmp.resolve("blob.bin");
		Files.write(file, payload);

		File expected = file.toFile();
		FileIcon icon = new FileIcon(expected);

		assertArrayEquals(payload, icon.toBytes());
		assertEquals(expected, icon.getFile());
	}

	@Test
	void nonExistentFileReturnsNull() {
		FileIcon icon = new FileIcon(new File("does-not-exist.bin"));
		assertNull(icon.toBytes());
		assertNull(icon.toBytes(32));
	}

	@Test
	void scalesPngOnDemand(@TempDir Path tmp) throws Exception {
		Path png = tmp.resolve("dot.png");
		Files.write(png, createPng(16, 16));

		FileIcon icon = new FileIcon(png.toFile());
		byte[] scaled = icon.toBytes(48);

		assertNotNull(scaled);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(scaled));
		assertEquals(48, img.getWidth());
		assertEquals(48, img.getHeight());
	}

	private static byte[] createPng(int w, int h) throws Exception {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, w, h);
		} finally {
			g.dispose();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		return baos.toByteArray();
	}

}
