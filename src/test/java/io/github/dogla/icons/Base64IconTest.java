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
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.Base64;

import org.junit.jupiter.api.Test;

class Base64IconTest {

	@Test
	void decodesBase64ToBytes() {
		byte[] expected = new byte[] { 1, 2, 3, 4, 5 };
		String encoded = Base64.getEncoder().encodeToString(expected);

		Base64Icon icon = new Base64Icon(encoded);

		assertArrayEquals(expected, icon.toBytes());
		assertEquals(encoded, icon.getBase64());
	}

	@Test
	void toBase64ReencodesFromDecodedBytes() {
		byte[] payload = new byte[] { 42, 0, -1, 17 };
		String encoded = Base64.getEncoder().encodeToString(payload);

		Base64Icon icon = new Base64Icon(encoded);

		assertEquals(encoded, icon.toBase64());
	}

	@Test
	void repeatedCallsReturnDefensiveCopies() {
		byte[] payload = new byte[] { 1, 2, 3, 4 };
		Base64Icon icon = new Base64Icon(Base64.getEncoder().encodeToString(payload));

		byte[] first = icon.toBytes();
		byte[] second = icon.toBytes();

		assertArrayEquals(payload, first);
		assertArrayEquals(payload, second);
		assertNotSame(first, second);
	}

	@Test
	void mutatingReturnedBytesDoesNotAffectIcon() {
		byte[] payload = new byte[] { 7, 8, 9, 10 };
		Base64Icon icon = new Base64Icon(Base64.getEncoder().encodeToString(payload));

		byte[] first = icon.toBytes();
		for (int i = 0; i < first.length; i++) {
			first[i] = 0;
		}

		assertArrayEquals(payload, icon.toBytes());
	}

}
