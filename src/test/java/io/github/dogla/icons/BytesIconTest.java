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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class BytesIconTest {

	@Test
	void toBytesReturnsCopy() {
		byte[] payload = new byte[] { 10, 20, 30 };
		BytesIcon icon = new BytesIcon(payload);

		byte[] first = icon.toBytes();
		byte[] second = icon.toBytes();

		assertArrayEquals(payload, first);
		assertArrayEquals(payload, second);
		assertNotSame(payload, first);
		assertNotSame(first, second);
	}

	@Test
	void mutatingConstructorArgDoesNotAffectIcon() {
		byte[] payload = new byte[] { 1, 2, 3 };
		BytesIcon icon = new BytesIcon(payload);

		payload[0] = 99;

		assertArrayEquals(new byte[] { 1, 2, 3 }, icon.toBytes());
	}

	@Test
	void nullBytesAreTolerated() {
		BytesIcon icon = new BytesIcon(null);
		assertNull(icon.toBytes());
		assertNull(icon.getBytes());
	}

}
