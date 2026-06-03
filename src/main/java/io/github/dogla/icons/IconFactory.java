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

/**
 * SPI for plugging custom source-to-{@link Icon} conversions into
 * {@link Icon#valueOf(Object)}. Implementations are discovered through
 * {@link java.util.ServiceLoader} via the
 * {@code META-INF/services/io.github.dogla.icons.IconFactory} file.
 */
public interface IconFactory {

	/**
	 * Reports whether this factory recognizes the given value as a source
	 * it can convert. Should be cheap — usually an {@code instanceof} or a
	 * lightweight string check.
	 *
	 * @param value the candidate source (may be {@code null})
	 * @return {@code true} if {@link #create(Object)} should be attempted
	 */
	boolean canHandle(Object value);

	/**
	 * Produces an {@link Icon} for the given value. Called only after
	 * {@link #canHandle(Object)} returned {@code true}, though
	 * implementations may still return {@code null} (e.g. when a referenced
	 * file does not exist) — in that case {@link Icon#valueOf(Object, Icon)}
	 * tries the next factory.
	 *
	 * @param value the source previously accepted by {@link #canHandle(Object)}
	 * @return the resolved icon, or {@code null} if conversion failed
	 */
	Icon create(Object value);

}
