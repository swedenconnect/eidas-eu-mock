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
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.util.Preconditions;

/**
 * A ReloadableProperties implementation, each time {@link #getProperties()} is invoked, properties are checked for
 * modifications and can be reloaded.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class ReloadableProperties {

    @Nonnull
    private final SingletonAccessor<Properties> accessor;

    /**
     * Creates a new ReloadableProperties instance.
     *
     * @param fileName the name of the properties file.
     * @param defaultPath the default path
     */
    public ReloadableProperties(@Nonnull String fileName, @Nullable String defaultPath) {
        Preconditions.checkNotNull(fileName, "fileName");
        accessor = SingletonAccessors.newPropertiesAccessor(fileName, defaultPath, PropertiesConverter.IdemConverter.INSTANCE);
    }

    /**
     * Returns the current snapshot of the Properties.
     * <p>
     * Do not keep a reference to the returned object as it is not reloadable itself.
     *
     * @return the current snapshot of the Properties.
     * @throws IOException if the properties cannot be reloaded
     */
    @Nonnull
    public Properties getProperties() throws IOException {
        return accessor.get();
    }

    public void setProperties(@Nonnull Properties newValue) throws IOException {
        Preconditions.checkNotNull(newValue, "newValue");
        accessor.set(newValue);
    }
}
