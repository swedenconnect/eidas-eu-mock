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

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Marshals and unmarshals one instance of a type T to an OutputStream and from a InputStream.
 *
 * @param <T> the type converted from the file and persisted into the file.
 * @since 1.1
 */
public interface StreamMarshaller<T> {

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
    void marshal(@Nonnull T newValue, @Nonnull OutputStream output) throws IOException;

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
    T unmarshal(@Nonnull InputStream input) throws IOException;
}
