/*
 * Copyright (c) 2025 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.node.auth.util.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


public class FileUtils {

    /**
     * Copies the files from a source path to a destination path
     *
     * @param sourcePath      the source path
     * @param destinationPath the destination path
     * @throws IOException if an I/O error is thrown when accessing the starting file.
     */
    public static void copyFolder(Path sourcePath, Path destinationPath) throws IOException {
        Files.walk(sourcePath)
                .forEach(source -> copy(source, destinationPath.resolve(sourcePath.relativize(source))));
    }

    private static void copy(Path sourcePath, Path destinationPath) {
        try {
            Files.copy(sourcePath, destinationPath, REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

}
