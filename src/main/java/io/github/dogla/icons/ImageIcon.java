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
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * An {@link Icon} backed by an in-memory {@link BufferedImage}. The PNG
 * encoding is computed lazily on the first {@link #toBytes()} call and
 * cached; returned arrays are defensive copies.
 */
public class ImageIcon implements SizableIcon {

	private final BufferedImage image;
	private volatile byte[] cachedBytes;

	/**
	 * Creates an icon backed by the given image. The image is held by
	 * reference — callers should treat it as immutable afterwards.
	 *
	 * @param image the source image
	 */
	public ImageIcon(BufferedImage image) {
		this.image = image;
	}

	/**
	 * Returns the underlying image.
	 *
	 * @return the {@link BufferedImage} passed to the constructor
	 */
	public BufferedImage getImage() {
		return image;
	}

	@Override
	public byte[] toBytes() {
		if (cachedBytes == null) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos); //$NON-NLS-1$
				cachedBytes = baos.toByteArray();
			} catch (IOException e) {
				return null;
			}
		}
		return cachedBytes.clone();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The cached PNG is decoded and scaled via {@link Icons#scaleBytes}.
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		return Icons.scaleBytes(toBytes(), size);
	}

}
