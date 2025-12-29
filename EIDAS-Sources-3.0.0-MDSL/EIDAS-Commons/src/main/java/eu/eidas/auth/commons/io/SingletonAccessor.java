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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Accessor and mutator of a singleton instance.
 *
 * @since 1.1
 */
public interface SingletonAccessor<T> {

    /**
     * Retrieves the current value.
     *
     * @return the current value.
     * @throws IOException if an exception happens while retrieving the value.
     */
    @Nullable
    T get() throws IOException;

    /**
     * Sets the new value.
     *
     * @param newValue the new value to set.
     * @throws IOException if an exception happens while setting the value.
     */
    void set(@Nonnull T newValue) throws IOException;
}
