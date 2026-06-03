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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class NullIconTest {

	@Test
	void isSingleton() {
		assertSame(NullIcon.getInstance(), NullIcon.getInstance());
	}

	@Test
	void toBytesReturnsValidPng() throws Exception {
		byte[] png = NullIcon.getInstance().toBytes();
		assertNotNull(png);
		assertPngMagic(png);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		assertNotNull(img);
	}

	@Test
	void toBytesAtRequestedSize() throws Exception {
		byte[] png = NullIcon.getInstance().toBytes(48);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		assertEquals(48, img.getWidth());
		assertEquals(48, img.getHeight());
	}

	@Test
	void nonPositiveSizeFallsBackToDefault() throws Exception {
		byte[] png = NullIcon.getInstance().toBytes(0);
		BufferedImage img = ImageIO.read(new ByteArrayInputStream(png));
		// Default size is 24
		assertEquals(24, img.getWidth());
		assertEquals(24, img.getHeight());
	}

	@Test
	void toBase64ReturnsNonNull() {
		assertNotNull(NullIcon.getInstance().toBase64());
	}

	@Test
	void repeatedCallsReturnDefensiveCopies() {
		byte[] first = NullIcon.getInstance().toBytes(32);
		byte[] second = NullIcon.getInstance().toBytes(32);
		assertArrayEquals(first, second);
		assertNotSame(first, second);
	}

	private static void assertPngMagic(byte[] png) {
		assertEquals((byte) 0x89, png[0]);
		assertEquals((byte) 0x50, png[1]);
		assertEquals((byte) 0x4E, png[2]);
		assertEquals((byte) 0x47, png[3]);
	}

}
