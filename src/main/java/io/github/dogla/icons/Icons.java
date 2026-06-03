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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.MultiResolutionImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

/**
 * Utility class for icon operations: PNG scaling and native file-icon
 * extraction (Windows).
 */
@SuppressWarnings("nls")
public final class Icons {

	private Icons() {
		// no instances
	}

	// -------------------------------------------------------------------------
	// Scaling
	// -------------------------------------------------------------------------

	/**
	 * Scales PNG bytes to the given square size using bicubic interpolation.
	 * Returns the original bytes if already the correct size or if scaling fails.
	 *
	 * @param pngBytes the source PNG bytes
	 * @param size     the target width and height in pixels
	 * @return scaled PNG bytes
	 */
	public static byte[] scaleBytes(byte[] pngBytes, int size) {
		if (pngBytes == null || size <= 0) {
			return pngBytes;
		}
		try {
			BufferedImage original = ImageIO.read(new ByteArrayInputStream(pngBytes));
			if (original == null) {
				return pngBytes;
			}
			if (original.getWidth() == size && original.getHeight() == size) {
				return pngBytes;
			}
			BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = scaled.createGraphics();
			try {
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g.drawImage(original, 0, 0, size, size, null);
			} finally {
				g.dispose();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaled, "png", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			return pngBytes;
		}
	}

	// -------------------------------------------------------------------------
	// Native file icon extraction (Windows)
	// -------------------------------------------------------------------------

	private static final ConcurrentHashMap<String, byte[]> FILE_ICON_CACHE = new ConcurrentHashMap<>();

	/**
	 * Extracts the native file icon as PNG bytes (Windows only).
	 * Icons are cached by file extension (or by absolute path for .exe/.lnk files).
	 *
	 * @param file the file
	 * @return PNG bytes, or null if extraction fails
	 */
	public static byte[] extractFileIcon(File file) {
		String extension = getExtension(file.getName());
		String cacheKey;
		if ("exe".equalsIgnoreCase(extension) || "lnk".equalsIgnoreCase(extension)) {
			cacheKey = file.getAbsolutePath();
		} else {
			cacheKey = extension;
		}
		return FILE_ICON_CACHE.computeIfAbsent(cacheKey, k -> extractFileIconBytes(file, 32));
	}

	/**
	 * Extracts a native file icon as a lazy {@link Icon} (Windows only).
	 *
	 * @param file the file
	 * @return a LazyIcon that resolves to a BytesIcon
	 */
	public static Icon extractFileIconAsIcon(File file) {
		return new LazyIcon(() -> {
			byte[] bytes = extractFileIcon(file);
			return bytes != null ? new BytesIcon(bytes) : null;
		});
	}

	@SuppressWarnings("restriction")
	private static byte[] extractFileIconBytes(File file, int preferredSize) {
		try {
			sun.awt.shell.ShellFolder sf = sun.awt.shell.ShellFolder.getShellFolder(file);
			Image image = sf.getIcon(false);

			if (image instanceof MultiResolutionImage) {
				MultiResolutionImage mri = (MultiResolutionImage) image;
				Image matched = null;
				List<Image> variants = mri.getResolutionVariants();
				for (Image img : variants) {
					int w = img.getWidth(null);
					if (w <= preferredSize) {
						if (matched == null || matched.getWidth(null) < w) {
							matched = img;
						}
					}
				}
				image = matched != null ? matched : image;
			}

			if (image != null) {
				BufferedImage bi;
				if (image instanceof BufferedImage) {
					bi = (BufferedImage) image;
				} else {
					bi = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
					java.awt.Graphics g = bi.getGraphics();
					try {
						g.drawImage(image, 0, 0, null);
					} finally {
						g.dispose();
					}
				}
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ImageIO.write(bi, "png", bos);
				return bos.toByteArray();
			}
		} catch (FileNotFoundException e) {
			// file doesn't exist
		} catch (Exception e) {
			// extraction failed
		}
		return null;
	}

	private static String getExtension(String fileName) {
		int dot = fileName.lastIndexOf('.');
		return dot >= 0 ? fileName.substring(dot + 1) : "";
	}

}
