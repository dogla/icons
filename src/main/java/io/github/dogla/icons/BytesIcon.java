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

/**
 * An {@link Icon} backed by a raw PNG byte array. Defensive copies are made
 * on construction and on every accessor, so callers cannot mutate the
 * internal state.
 */
public class BytesIcon implements SizableIcon {

	private final byte[] bytes;

	/**
	 * Creates an icon from the given PNG bytes.
	 *
	 * @param bytes raw PNG bytes; may be {@code null}
	 */
	public BytesIcon(byte[] bytes) {
		this.bytes = bytes != null ? bytes.clone() : null;
	}

	/**
	 * Returns a defensive copy of the underlying bytes.
	 *
	 * @return a fresh copy of the byte array, or {@code null} if the icon
	 *         was constructed with {@code null}
	 */
	public byte[] getBytes() {
		return bytes != null ? bytes.clone() : null;
	}

	@Override
	public byte[] toBytes() {
		return bytes != null ? bytes.clone() : null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The raw bytes are decoded as PNG and scaled via {@link Icons#scaleBytes}.
	 * Non-PNG payloads are returned unchanged.
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		return Icons.scaleBytes(toBytes(), size);
	}

}
