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

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.regex.Pattern;

/**
 * Default icon factory handling all built-in icon types.
 * Registered via ServiceLoader.
 */
@SuppressWarnings("nls")
public class DefaultIconFactory implements IconFactory {

	private static final Pattern BASE64_PATTERN = Pattern.compile(
		"^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)?$"
	);

	@Override
	public boolean canHandle(Object value) {
		return value instanceof byte[]
			|| value instanceof File
			|| value instanceof BufferedImage
			|| value instanceof String;
	}

	@Override
	public Icon create(Object value) {
		// byte[] → BytesIcon
		if (value instanceof byte[]) {
			return new BytesIcon((byte[]) value);
		}

		// File → SvgIcon (if .svg) or FileIcon
		if (value instanceof File) {
			File file = (File) value;
			if (file.getName().toLowerCase().endsWith(".svg")) {
				try {
					return new SvgIcon(file);
				} catch (Exception e) {
					return null;
				}
			}
			return new FileIcon(file);
		}

		// BufferedImage → ImageIcon
		if (value instanceof BufferedImage) {
			return new ImageIcon((BufferedImage) value);
		}

		// String handling (order matters)
		if (value instanceof String) {
			String s = (String) value;
			if (s.isEmpty()) {
				return null;
			}

			// SVG content string
			String trimmed = s.trim();
			if (trimmed.startsWith("<svg") || trimmed.startsWith("<?xml")) {
				return new SvgIcon(s);
			}

			// Iconify: "iconify prefix:name" (legacy) or "prefix:name"
			if (s.startsWith("iconify ")) {
				try {
					return new IconifyIcon(s.substring(8));
				} catch (IllegalArgumentException e) {
					return null;
				}
			}
			int colonIndex = s.indexOf(':');
			if (colonIndex > 0 && colonIndex < s.length() - 1 && !s.startsWith("http")) {
				try {
					return new IconifyIcon(s);
				} catch (IllegalArgumentException e) {
					// fall through to Base64
				}
			}

			// Base64
			if (s.length() > 16 && BASE64_PATTERN.matcher(s).find()) {
				return new Base64Icon(s);
			}
		}

		return null;
	}

}
