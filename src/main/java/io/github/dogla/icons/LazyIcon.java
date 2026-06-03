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

import java.util.function.Supplier;

/**
 * An {@link Icon} that defers resolution of the actual icon until the
 * first byte-producing call. Useful for expensive sources (file system
 * scans, native lookups) where some icons may never be rendered.
 * <p>
 * The supplier is invoked at most once; its result is cached even if it
 * was {@code null}.
 * </p>
 */
public class LazyIcon implements SizableIcon {

	private final Supplier<Icon> supplier;
	private volatile Icon resolved;
	private volatile boolean loaded;

	/**
	 * Creates a lazy icon backed by the given supplier.
	 *
	 * @param supplier produces the actual icon on first access; may return
	 *                 {@code null} to indicate "no icon available"
	 */
	public LazyIcon(Supplier<Icon> supplier) {
		this.supplier = supplier;
	}

	/**
	 * Returns the supplier backing this lazy icon.
	 *
	 * @return the supplier passed to the constructor
	 */
	public Supplier<Icon> getSupplier() {
		return supplier;
	}

	private Icon resolve() {
		if (!loaded) {
			resolved = supplier.get();
			loaded = true;
		}
		return resolved;
	}

	@Override
	public byte[] toBytes() {
		Icon icon = resolve();
		return icon != null ? icon.toBytes() : null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@link SizableIcon#toBytes(int)} when the resolved icon
	 * supports sizing; falls back to {@link #toBytes()} otherwise.
	 * </p>
	 */
	@Override
	public byte[] toBytes(int size) {
		Icon icon = resolve();
		if (icon == null) {
			return null;
		}
		if (icon instanceof SizableIcon) {
			return ((SizableIcon) icon).toBytes(size);
		}
		return icon.toBytes();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Delegates to {@link SizableIcon#toBase64(int)} when the resolved icon
	 * supports sizing; falls back to {@link Icon#toBase64()} otherwise.
	 * </p>
	 */
	@Override
	public String toBase64(int size) {
		Icon icon = resolve();
		if (icon == null) {
			return null;
		}
		if (icon instanceof SizableIcon) {
			return ((SizableIcon) icon).toBase64(size);
		}
		return icon.toBase64();
	}

}
