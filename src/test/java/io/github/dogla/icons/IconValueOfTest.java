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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.io.File;

import org.junit.jupiter.api.Test;

/**
 * Tests the auto-detection logic of {@link Icon#valueOf(Object)} and the
 * registered {@link DefaultIconFactory}.
 */
class IconValueOfTest {

	/** A tiny 1x1 transparent PNG, base64-encoded. */
	private static final String PNG_1X1_BASE64 =
		"iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

	@Test
	void existingIconIsReturnedAsIs() {
		Icon original = new BytesIcon(new byte[] { 1, 2, 3 });
		assertSame(original, Icon.valueOf(original));
	}

	@Test
	void byteArrayBecomesBytesIcon() {
		byte[] payload = new byte[] { 1, 2, 3, 4 };
		Icon icon = Icon.valueOf(payload);
		assertInstanceOf(BytesIcon.class, icon);
	}

	@Test
	void bufferedImageBecomesImageIcon() {
		BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
		Icon icon = Icon.valueOf(img);
		assertInstanceOf(ImageIcon.class, icon);
	}

	@Test
	void svgStringBecomesSvgIcon() {
		Icon icon = Icon.valueOf("<svg viewBox='0 0 10 10'><rect width='10' height='10'/></svg>");
		assertInstanceOf(SvgIcon.class, icon);
	}

	@Test
	void base64StringBecomesBase64Icon() {
		Icon icon = Icon.valueOf(PNG_1X1_BASE64);
		assertInstanceOf(Base64Icon.class, icon);
	}

	@Test
	void prefixedStringBecomesIconifyIcon() {
		Icon icon = Icon.valueOf("fa6-solid:heart");
		assertInstanceOf(IconifyIcon.class, icon);
		IconifyIcon iconify = (IconifyIcon) icon;
		assertEquals("fa6-solid", iconify.getPrefix());
		assertEquals("heart", iconify.getName());
	}

	@Test
	void emptyStringFallsBackToDefaultIcon() {
		assertSame(NullIcon.getInstance(), Icon.valueOf(""));
	}

	@Test
	void unhandledTypeFallsBackToDefaultIcon() {
		assertSame(NullIcon.getInstance(), Icon.valueOf(123));
	}

	@Test
	void unhandledTypeUsesCustomDefault() {
		Icon custom = new BytesIcon(new byte[] { 0 });
		assertSame(custom, Icon.valueOf(123, custom));
	}

	@Test
	void emptyStringUsesCustomDefault() {
		Icon custom = new BytesIcon(new byte[] { 0 });
		assertSame(custom, Icon.valueOf("", custom));
	}

	@Test
	void nonExistentSvgFileFallsBackToDefaultIcon() {
		assertSame(NullIcon.getInstance(), Icon.valueOf(new File("does-not-exist.svg")));
	}

	@Test
	void nonExistentSvgFileUsesCustomDefault() {
		Icon custom = new BytesIcon(new byte[] { 0 });
		assertSame(custom, Icon.valueOf(new File("does-not-exist.svg"), custom));
	}

}
