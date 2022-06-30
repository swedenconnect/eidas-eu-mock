/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.commons.io;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Marshals and unmarshals a type T to and from a File.
 *
 * @param <T> the type converted from the file and persisted into the file.
 * @since 1.1
 */
public interface FileMarshaller<T> {

    /**
     * Marshals the given newValue into a file.
     *
     * @param newValue the value to marshal into the file
     * @param output the destination file
     * @throws IOException if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    void marshal(@Nonnull T newValue, @Nonnull File output) throws IOException;

    /**
     * Unmarshals the given file into an instance of T.
     *
     * @param input the file to unmarshal
     * @return a new instance of T unmarshalled from the given file.
     * @throws IOException if the file does not exist,
     *                   is a directory rather than a regular file,
     *                   or for some other reason cannot be opened for
     *                   reading.
     */
    T unmarshal(@Nonnull File input) throws IOException;
}
