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

import java.nio.file.Files;
import java.nio.file.Path;

/** Dumps the NullIcon at a few common sizes for visual inspection. */
public class NullIconPreview {

	public static void main(String[] args) throws Exception {
		Path outDir = Path.of("target");
		Files.createDirectories(outDir);
		int[] sizes = { 16, 32, 64, 128 };
		for (int size : sizes) {
			byte[] png = NullIcon.getInstance().toBytes(size);
			Path out = outDir.resolve("null-icon-" + size + ".png");
			Files.write(out, png);
			System.out.println("Wrote " + out + " (" + png.length + " bytes)");
		}
	}

}
