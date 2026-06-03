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

import java.util.Base64;

/**
 * An {@link Icon} backed by a Base64-encoded PNG string. Decoding happens
 * on the first {@link #toBytes()} call and is cached for subsequent calls;
 * returned arrays are defensive copies.
 */
public class Base64Icon implements SizableIcon {

	private final String base64;
	private volatile byte[] cachedBytes;

	/**
	 * Creates an icon from the given Base64-encoded PNG.
	 *
	 * @param base64 Base64-encoded PNG bytes; must be valid Base64
	 */
	public Base64Icon(String base64) {
		this.base64 = base64;
	}

	/**
	 * Returns the encoded source string.
	 *
	 * @return the Base64 string passed to the constructor
	 */
	public String getBase64() {
		return base64;
	}

	@Override
	public byte[] toBytes() {
		if (cachedBytes == null) {
			cachedBytes = Base64.getDecoder().decode(base64);
		}
		return cachedBytes.clone();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The decoded PNG is scaled via {@link Icons#scaleBytes}.
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		return Icons.scaleBytes(toBytes(), size);
	}

}
