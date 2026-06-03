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
 * An {@link Icon} that can be rendered at a specific pixel size. Vector
 * sources (SVG, Iconify) re-render at the requested size; raster sources
 * scale the underlying PNG.
 */
public interface SizableIcon extends Icon {

	/**
	 * Renders the icon at the given square pixel size.
	 *
	 * @param size width and height in pixels; values {@code <= 0}
	 *             use the implementation's default size
	 * @return PNG bytes, or {@code null} if the icon cannot be rendered
	 */
	byte[] toBytes(int size);

	/**
	 * Returns the icon as a Base64-encoded PNG string at the given size.
	 *
	 * @param size width and height in pixels
	 * @return Base64 encoding of {@link #toBytes(int)}, or {@code null}
	 *         if the icon cannot be rendered
	 */
	default String toBase64(int size) {
		byte[] bytes = toBytes(size);
		if (bytes == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(bytes);
	}

}
