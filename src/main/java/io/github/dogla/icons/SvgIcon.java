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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;

import javax.imageio.ImageIO;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.LoaderContext;
import com.github.weisj.jsvg.parser.SVGLoader;
import com.github.weisj.jsvg.view.FloatSize;
import com.github.weisj.jsvg.view.ViewBox;

/**
 * Icon backed by SVG content, rendered to PNG via JSVG.
 * Supports foreground/background color injection.
 */
public class SvgIcon implements ColorableIcon {

	private static final int DEFAULT_SIZE = 24;

	private final String svgContent;

	/**
	 * Creates an SvgIcon from an SVG content string.
	 *
	 * @param svgContent the SVG XML content
	 */
	public SvgIcon(String svgContent) {
		this.svgContent = svgContent;
	}

	/**
	 * Creates an SvgIcon from an SVG file.
	 *
	 * @param svgFile the SVG file
	 * @throws IOException if the file cannot be read
	 */
	public SvgIcon(File svgFile) throws IOException {
		this.svgContent = Files.readString(svgFile.toPath());
	}

	/**
	 * Subclass constructor that defers SVG content resolution.
	 * Subclasses must override {@link #getSvgContent()}.
	 */
	protected SvgIcon() {
		this.svgContent = null;
	}

	/**
	 * Returns the raw SVG content. Subclasses (e.g. {@link IconifyIcon})
	 * can override to load SVG lazily.
	 *
	 * @return the SVG XML content, or {@code null} if it could not be obtained
	 */
	public String getSvgContent() {
		return svgContent;
	}

	@Override
	public byte[] toBytes() {
		return toBytes(DEFAULT_SIZE, null, null);
	}

	@Override
	public byte[] toBytes(int size) {
		return toBytes(size, null, null);
	}

	@Override
	public byte[] toBytes(int size, String fg, String bg) {
		String svg = getSvgContent();
		if (svg == null) {
			return null;
		}
		int effectiveSize = size > 0 ? size : DEFAULT_SIZE;
		String processed = processSvgColors(svg, fg);
		return renderToPng(processed, effectiveSize, bg);
	}

	@Override
	public String toBase64(int size, String fg, String bg) {
		byte[] bytes = toBytes(size, fg, bg);
		return bytes != null ? Base64.getEncoder().encodeToString(bytes) : null;
	}

	/**
	 * Processes SVG colors: sets the fill attribute and replaces currentColor references.
	 */
	protected String processSvgColors(String svg, String fg) {
		String effectiveColor = (fg != null) ? fg : "#000000"; //$NON-NLS-1$
		svg = svg.replaceFirst("<svg ", "<svg fill='" + effectiveColor + "' "); //$NON-NLS-1$ //$NON-NLS-2$
		svg = svg.replace("currentColor", effectiveColor); //$NON-NLS-1$
		return svg;
	}

	/**
	 * Renders the SVG content to PNG bytes at the given size.
	 *
	 * @param svgContent the SVG XML to render
	 * @param size       width and height in pixels
	 * @param bg         optional background color as a CSS hex string; {@code null}
	 *                   leaves the background transparent
	 * @return PNG bytes, or {@code null} if rendering failed
	 */
	protected byte[] renderToPng(String svgContent, int size, String bg) {
		try {
			byte[] svgBytes = svgContent.getBytes(StandardCharsets.UTF_8);
			InputStream inputStream = new ByteArrayInputStream(svgBytes);

			SVGLoader loader = new SVGLoader();
			SVGDocument doc = loader.load(inputStream, null, LoaderContext.createDefault());
			if (doc == null) {
				return null;
			}

			FloatSize docSize = doc.size();
			float scale;
			if (docSize.width <= 0 || docSize.height <= 0) {
				scale = 1.0f;
			} else {
				scale = Math.min(size / docSize.width, size / docSize.height);
			}

			float scaledWidth = docSize.width * scale;
			float scaledHeight = docSize.height * scale;
			float offsetX = (size - scaledWidth) / 2.0f;
			float offsetY = (size - scaledHeight) / 2.0f;

			BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2d = image.createGraphics();
			try {
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

				if (bg != null && !bg.isEmpty()) {
					g2d.setColor(Color.decode(bg));
					g2d.fillRect(0, 0, size, size);
				}

				g2d.translate(offsetX, offsetY);
				g2d.scale(scale, scale);
				doc.render(null, g2d, new ViewBox(docSize));
			} finally {
				g2d.dispose();
			}

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(image, "png", baos); //$NON-NLS-1$
			return baos.toByteArray();
		} catch (IOException e) {
			return null;
		}
	}

}
