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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class IconsScaleTest {

	@Test
	void scaleNullReturnsNull() {
		assertNull(Icons.scaleBytes(null, 32));
	}

	@Test
	void scaleNonPositiveSizeReturnsInput() {
		byte[] payload = new byte[] { 1, 2, 3 };
		assertSame(payload, Icons.scaleBytes(payload, 0));
		assertSame(payload, Icons.scaleBytes(payload, -5));
	}

	@Test
	void scaleNonPngReturnsOriginalBytes() {
		byte[] garbage = "not a png".getBytes();
		assertSame(garbage, Icons.scaleBytes(garbage, 32));
	}

	@Test
	void scaleSameSizeIsNoOp() throws Exception {
		byte[] png = createPng(16, 16);
		assertSame(png, Icons.scaleBytes(png, 16));
	}

	@Test
	void scaleToDifferentSizeProducesPng() throws Exception {
		byte[] png = createPng(16, 16);
		byte[] scaled = Icons.scaleBytes(png, 32);
		assertNotNull(scaled);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(scaled));
		assertNotNull(img);
		assertArrayEquals(new int[] { 32, 32 }, new int[] { img.getWidth(), img.getHeight() });
	}

	private static byte[] createPng(int width, int height) throws Exception {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(Color.RED);
			g.fillRect(0, 0, width, height);
		} finally {
			g.dispose();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(img, "png", baos);
		return baos.toByteArray();
	}

}
