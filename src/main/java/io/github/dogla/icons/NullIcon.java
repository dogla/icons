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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

/**
 * Visible placeholder {@link Icon} returned by {@link Icon#valueOf(Object)}
 * when no registered {@link IconFactory} could produce an icon. Renders a
 * pastel-red rounded square with a centered "?" mark so missing icons are
 * easy to spot in the UI without being visually aggressive.
 * <p>
 * Implementations of {@link Icon#valueOf(Object, Icon)} can pass an
 * application-specific fallback to replace this default.
 * </p>
 */
public final class NullIcon implements SizableIcon {

	private static final int DEFAULT_SIZE = 24;

	/** Material Design Red 200 — pastel red, easy on the eye. */
	private static final Color BACKGROUND = new Color(0xEF, 0x9A, 0x9A);
	private static final Color FOREGROUND = Color.WHITE;

	private static final NullIcon NULL = new NullIcon();
	private static final ConcurrentHashMap<Integer, byte[]> RENDER_CACHE = new ConcurrentHashMap<>();

	/**
	 * Returns the shared singleton instance.
	 *
	 * @return the singleton {@link NullIcon}
	 */
	public static NullIcon getInstance() {
		return NULL;
	}

	private NullIcon() {
		// singleton
	}

	@Override
	public byte[] toBytes() {
		return toBytes(DEFAULT_SIZE);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Renders a pastel-red {@code size x size} placeholder. Renders are
	 * cached per size; returned arrays are defensive copies.
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		int effective = size > 0 ? size : DEFAULT_SIZE;
		byte[] cached = RENDER_CACHE.computeIfAbsent(effective, NullIcon::render);
		return cached.clone();
	}

	private static byte[] render(int size) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		try {
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			int corner = Math.max(2, size / 5);
			g.setColor(BACKGROUND);
			g.fillRoundRect(0, 0, size, size, corner, corner);

			// Centered "?" — scales with the icon size
			int fontSize = Math.max(8, (int) Math.round(size * 0.7));
			g.setColor(FOREGROUND);
			g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
			FontMetrics fm = g.getFontMetrics();
			String glyph = "?"; //$NON-NLS-1$
			int textX = (size - fm.stringWidth(glyph)) / 2;
			int textY = (size - fm.getHeight()) / 2 + fm.getAscent();
			g.drawString(glyph, textX, textY);
		} finally {
			g.dispose();
		}

		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(img, "png", baos); //$NON-NLS-1$
			return baos.toByteArray();
		} catch (IOException e) {
			// In-memory PNG encoding should never fail for an ARGB BufferedImage.
			return new byte[0];
		}
	}

}
