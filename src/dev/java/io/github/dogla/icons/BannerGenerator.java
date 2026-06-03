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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Generates the banner image for the README using the icons library itself.
 */
@SuppressWarnings("nls")
public class BannerGenerator {

	private static final String[] ICON_NAMES = {
		"fa6-solid:heart",
		"fa6-solid:star",
		"fa6-solid:gear",
		"fa6-solid:house",
		"fa6-solid:magnifying-glass",
		"fa6-solid:bell",
		"fa6-solid:envelope",
		"fa6-solid:bookmark",
		"fa6-solid:calendar",
		"fa6-solid:cloud",
		"fa6-solid:code",
		"fa6-solid:lock",
		"mdi:lightning-bolt",
		"mdi:palette",
		"mdi:rocket-launch",
		"mdi:puzzle",
	};

	private static final String[] COLORS = {
		"#E91E63", "#F44336", "#FF9800", "#4CAF50",
		"#2196F3", "#9C27B0", "#00BCD4", "#FF5722",
		"#3F51B5", "#009688", "#FFC107", "#607D8B",
		"#E91E63", "#4CAF50", "#2196F3", "#FF9800",
	};

	/**
	 * Creates the banner.png used in the readme.
	 * 
	 * @param args args are ignored
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int iconSize = 40;
		int padding = 12;
		int cols = ICON_NAMES.length;
		int gridWidth = cols * (iconSize + padding) + padding;
		int totalHeight = 120;

		BufferedImage banner = new BufferedImage(gridWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = banner.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Background
		g.setColor(new Color(0x1a, 0x1a, 0x2e));
		g.fillRoundRect(0, 0, gridWidth, totalHeight, 16, 16);

		// Title
		g.setColor(Color.WHITE);
		g.setFont(new Font("SansSerif", Font.BOLD, 20));
		String title = "Icons for Java";
		int titleWidth = g.getFontMetrics().stringWidth(title);
		g.drawString(title, (gridWidth - titleWidth) / 2, 30);

		// Subtitle
		g.setColor(new Color(0xaa, 0xaa, 0xcc));
		g.setFont(new Font("SansSerif", Font.PLAIN, 11));
		String subtitle = "200,000+ Iconify icons as PNG bytes \u2022 No UI dependency \u2022 JSVG rendering";
		int subWidth = g.getFontMetrics().stringWidth(subtitle);
		g.drawString(subtitle, (gridWidth - subWidth) / 2, 48);

		// Icons row
		int iconY = 60;
		for (int i = 0; i < ICON_NAMES.length; i++) {
			int x = padding + i * (iconSize + padding);
			try {
				IconifyIcon icon = new IconifyIcon(ICON_NAMES[i] + "?fg=" + COLORS[i]);
				byte[] png = icon.toBytes(iconSize);
				if (png != null) {
					BufferedImage iconImg = ImageIO.read(new ByteArrayInputStream(png));
					if (iconImg != null) {
						g.drawImage(iconImg, x, iconY, null);
					}
				}
			} catch (Exception e) {
				System.err.println("Failed to render: " + ICON_NAMES[i] + " - " + e.getMessage());
			}
		}

		g.dispose();

		File output = new File("docs/banner.png");
		ImageIO.write(banner, "png", output);
		System.out.println("Banner generated: " + output.getAbsolutePath() + " (" + banner.getWidth() + "x" + banner.getHeight() + ")");
	}
}
