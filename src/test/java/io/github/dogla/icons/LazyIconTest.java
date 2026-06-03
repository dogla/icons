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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class LazyIconTest {

	@Test
	void supplierIsNotInvokedAtConstruction() {
		AtomicInteger calls = new AtomicInteger();
		new LazyIcon(() -> {
			calls.incrementAndGet();
			return new BytesIcon(new byte[] { 1, 2, 3 });
		});
		assertEquals(0, calls.get());
	}

	@Test
	void supplierIsInvokedOnlyOnce() {
		AtomicInteger calls = new AtomicInteger();
		LazyIcon icon = new LazyIcon(() -> {
			calls.incrementAndGet();
			return new BytesIcon(new byte[] { 1, 2, 3 });
		});

		icon.toBytes();
		icon.toBytes();
		icon.toBytes(32);

		assertEquals(1, calls.get());
	}

	@Test
	void delegatesToBytesIcon() {
		byte[] payload = new byte[] { 9, 8, 7 };
		LazyIcon icon = new LazyIcon(() -> new BytesIcon(payload));

		assertArrayEquals(payload, icon.toBytes());
	}

	@Test
	void delegatesSizableCall() {
		LazyIcon icon = new LazyIcon(() -> new BytesIcon(new byte[] { 1, 2, 3 }));
		// BytesIcon is a SizableIcon — toBytes(int) routes through it.
		// Non-image bytes can't actually be scaled, so the call should at
		// least return non-null (the original bytes).
		assertArrayEquals(new byte[] { 1, 2, 3 }, icon.toBytes(16));
	}

	@Test
	void supplierReturningNullPropagates() {
		LazyIcon icon = new LazyIcon(() -> null);
		assertNull(icon.toBytes());
		assertNull(icon.toBytes(32));
		assertNull(icon.toBase64(32));
	}

	@Test
	void fallsBackToToBytesForNonSizableIcon() {
		// A plain Icon that does NOT implement SizableIcon — toBytes(size)
		// on the LazyIcon must fall back to the unsized variant.
		byte[] payload = new byte[] { 1, 2, 3 };
		Icon plain = () -> payload;
		LazyIcon icon = new LazyIcon(() -> plain);

		assertArrayEquals(payload, icon.toBytes(32));
	}

}
