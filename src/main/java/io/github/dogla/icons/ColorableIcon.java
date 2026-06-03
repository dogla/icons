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
 * A {@link SizableIcon} whose foreground and background colors can be
 * injected at render time. Typically implemented by vector-based icons
 * (SVG, Iconify) where {@code currentColor} can be substituted.
 */
public interface ColorableIcon extends SizableIcon {

	/**
	 * Renders the icon at the given size with explicit foreground and
	 * background colors.
	 *
	 * @param size width and height in pixels
	 * @param fg   foreground color as a CSS-style hex string
	 *             (e.g. {@code "#ff0000"}); {@code null} keeps the icon's default
	 * @param bg   background color in the same format; {@code null} leaves
	 *             the background transparent
	 * @return PNG bytes, or {@code null} if the icon cannot be rendered
	 */
	byte[] toBytes(int size, String fg, String bg);

	/**
	 * Returns the colored, sized icon as a Base64-encoded PNG string.
	 *
	 * @param size width and height in pixels
	 * @param fg   foreground color (CSS hex), or {@code null}
	 * @param bg   background color (CSS hex), or {@code null} for transparent
	 * @return Base64 encoding of {@link #toBytes(int, String, String)},
	 *         or {@code null} if the icon cannot be rendered
	 */
	default String toBase64(int size, String fg, String bg) {
		byte[] bytes = toBytes(size, fg, bg);
		if (bytes == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(bytes);
	}

}
