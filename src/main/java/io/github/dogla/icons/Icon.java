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

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ServiceLoader;

/**
 * UI-toolkit-agnostic icon abstraction. An icon resolves to PNG bytes via
 * {@link #toBytes()} and can be auto-detected from common source types
 * (string, byte array, {@link java.io.File}, {@link java.awt.image.BufferedImage}, …)
 * through {@link #valueOf(Object)}.
 */
public interface Icon {

	/**
	 * Returns the icon rendered as PNG bytes.
	 *
	 * @return PNG bytes, or {@code null} if the icon cannot be rendered
	 *         (e.g. a missing source file or a failed download)
	 */
	byte[] toBytes();

	/**
	 * Returns the icon rendered as a Base64-encoded PNG string.
	 *
	 * @return Base64 encoding of {@link #toBytes()}, or {@code null} if
	 *         the icon cannot be rendered
	 */
	default String toBase64() {
		byte[] bytes = toBytes();
		if (bytes == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * Auto-detects an {@link Icon} for the given value. Falls back to
	 * {@link NullIcon} if no registered {@link IconFactory} can produce
	 * an icon. Equivalent to {@code valueOf(value, NullIcon.getInstance())}.
	 *
	 * @param value the source to wrap; may be a String (SVG, Iconify
	 *              descriptor, Base64), {@link java.io.File},
	 *              {@code byte[]}, {@link java.awt.image.BufferedImage},
	 *              or an existing {@link Icon} (returned as-is)
	 * @return the resolved icon, never {@code null}
	 */
	static Icon valueOf(Object value) {
		return valueOf(value, NullIcon.getInstance());
	}

	/**
	 * Auto-detects an {@link Icon} for the given value, falling back to the
	 * supplied default if no registered {@link IconFactory} can produce an
	 * icon (or produces only {@code null}).
	 *
	 * @param value       the source to wrap; same rules as {@link #valueOf(Object)}
	 * @param defaultIcon the icon to return when resolution fails; may be {@code null}
	 * @return the resolved icon, or {@code defaultIcon} if none of the
	 *         registered factories produced a non-null icon
	 */
	static Icon valueOf(Object value, Icon defaultIcon) {
		if (value instanceof Icon) {
			return (Icon) value;
		}
		for (IconFactory factory : IconFactoryCache.getFactories()) {
			if (factory.canHandle(value)) {
				Icon icon = factory.create(value);
				if (icon != null) {
					return icon;
				}
			}
		}
		return defaultIcon;
	}

	/** Cached ServiceLoader results to avoid classpath scanning on every valueOf() call. */
	final class IconFactoryCache {
		private static volatile List<IconFactory> factories;
		static List<IconFactory> getFactories() {
			if (factories == null) {
				List<IconFactory> loaded = new ArrayList<>();
				ServiceLoader.load(IconFactory.class).forEach(loaded::add);
				factories = loaded;
			}
			return factories;
		}
	}

}
