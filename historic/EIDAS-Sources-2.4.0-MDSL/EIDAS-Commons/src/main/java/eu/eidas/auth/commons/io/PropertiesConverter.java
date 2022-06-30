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

import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSortedMap;

import eu.eidas.auth.commons.collections.PrintSortedProperties;

/**
 * Marshals and unmarshals to and from properties.
 *
 * @since 1.1
 */
public interface PropertiesConverter<T> {

    enum IdemConverter implements PropertiesConverter<Properties> {

        INSTANCE;

        @Nonnull
        @Override
        public Properties marshal(@Nonnull Properties value) {
            return value;
        }

        @Nonnull
        @Override
        public Properties unmarshal(@Nonnull Properties properties) {
            return properties;
        }
    }

    enum MapPropertiesConverter implements PropertiesConverter<NavigableMap<String, String>> {

        INSTANCE;

        @Nonnull
        @Override
        public Properties marshal(@Nonnull NavigableMap<String, String> value) {
            Properties properties = new PrintSortedProperties();
            //noinspection UseOfPropertiesAsHashtable
            properties.putAll(value);
            return properties;
        }

        @Nonnull
        @Override
        public NavigableMap<String, String> unmarshal(@Nonnull Properties properties) {
            //noinspection unchecked,rawtypes
            return ImmutableSortedMap.<String, String>copyOf((Map) properties);
        }
    }

    @Nonnull
    Properties marshal(@Nonnull T value);

    @Nonnull
    T unmarshal(@Nonnull Properties properties);
}
