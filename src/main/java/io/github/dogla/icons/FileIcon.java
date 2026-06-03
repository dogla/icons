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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * An {@link Icon} that reads its bytes from a file on the filesystem.
 * The file is read on every {@link #toBytes()} call — no caching — so
 * changes on disk are picked up immediately.
 */
public class FileIcon implements SizableIcon {

	private final File file;

	/**
	 * Creates an icon that reads from the given file.
	 *
	 * @param file the source file; need not exist at construction time
	 */
	public FileIcon(File file) {
		this.file = file;
	}

	/**
	 * Returns the file backing this icon.
	 *
	 * @return the file passed to the constructor
	 */
	public File getFile() {
		return file;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the file's bytes, or {@code null} if the file does not exist
	 *         or cannot be read
	 */
	@Override
	public byte[] toBytes() {
		if (!file.exists()) {
			return null;
		}
		try {
			return Files.readAllBytes(file.toPath());
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Bytes are read fresh from disk and then scaled via
	 * {@link Icons#scaleBytes} (assumes a PNG payload).
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		return Icons.scaleBytes(toBytes(), size);
	}

}
