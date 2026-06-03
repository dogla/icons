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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageIconTest {

	@Test
	void encodesBufferedImageAsPng() throws Exception {
		BufferedImage src = createImage(20, 10, Color.RED);
		ImageIcon icon = new ImageIcon(src);

		byte[] png = icon.toBytes();

		assertNotNull(png);
		BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(png));
		assertEquals(20, decoded.getWidth());
		assertEquals(10, decoded.getHeight());
		assertSame(src, icon.getImage());
	}

	@Test
	void repeatedCallsReturnEqualButDefensiveCopies() {
		ImageIcon icon = new ImageIcon(createImage(8, 8, Color.GREEN));

		byte[] first = icon.toBytes();
		byte[] second = icon.toBytes();

		assertArrayEquals(first, second, "Expected equal bytes from cached encoding");
		assertNotSame(first, second, "Expected defensive copy on each call");
	}

	@Test
	void mutatingReturnedBytesDoesNotAffectIcon() {
		ImageIcon icon = new ImageIcon(createImage(4, 4, Color.RED));

		byte[] first = icon.toBytes();
		byte[] copy = first.clone();
		// Corrupt the returned buffer — the icon must keep its own state intact.
		for (int i = 0; i < first.length; i++) {
			first[i] = 0;
		}

		byte[] second = icon.toBytes();
		assertArrayEquals(copy, second, "Internal cache must be isolated from caller mutations");
	}

	@Test
	void scaledOutputMatchesRequestedSize() throws Exception {
		ImageIcon icon = new ImageIcon(createImage(8, 8, Color.BLUE));

		byte[] scaled = icon.toBytes(32);

		assertNotNull(scaled);
		BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(scaled));
		assertEquals(32, decoded.getWidth());
		assertEquals(32, decoded.getHeight());
	}

	private static BufferedImage createImage(int w, int h, Color color) {
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setColor(color);
			g.fillRect(0, 0, w, h);
		} finally {
			g.dispose();
		}
		return img;
	}

}
