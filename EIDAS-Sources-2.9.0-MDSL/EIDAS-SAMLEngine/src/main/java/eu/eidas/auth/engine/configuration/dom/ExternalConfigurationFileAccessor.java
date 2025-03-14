/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.auth.commons.io.PropertiesConverter;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * ExternalConfigurationFile Accessor
 *
 * @since 1.1
 */
final class ExternalConfigurationFileAccessor {

    static <T> SingletonAccessor<T> newAccessor(@Nonnull final String instanceName,
                                                @Nonnull final String configurationName,
                                                @Nonnull String externalConfigurationFile,
                                                @Nullable String defaultPath,
                                                @Nonnull final Map<String, String> staticParameters,
                                                @Nonnull final Map<String, String> defaultParameters,
                                                @Nonnull final MapConverter<T> mapConverter) {
        SingletonAccessor<T> accessor =
                SingletonAccessors.newPropertiesAccessor(externalConfigurationFile, defaultPath, new PropertiesConverter<T>() {

                    @Nonnull
                    @Override
                    public Properties marshal(@Nonnull T t) {
                        throw new UnsupportedOperationException();
                    }

                    @Nonnull
                    @Override
                    public T unmarshal(@Nonnull Properties properties) {
                        try {
                            Map<String, String> validParameters =
                                    DOMConfigurationParser.validateParameters(instanceName, configurationName,
                                            propertiesToMap(properties));

                            Map<String, String> allParameters = new LinkedHashMap<String, String>();
                            allParameters.putAll(defaultParameters);
                            allParameters.putAll(staticParameters);
                            allParameters.putAll(validParameters);

                            return mapConverter.convert(Map.copyOf(allParameters));
                        } catch (ProtocolEngineConfigurationException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
        return accessor;
    }

    /**
     * Converts a {@link Properties} object into an unmodifiable {@link Map}.
     *
     * @param properties The {@link Properties} object to convert.
     * @return An unmodifiable {@link Map} containing the property keys and their corresponding values.
     */
    private static <T> Map<String, String> propertiesToMap(Properties properties) {
        Map<String, String> map = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
        return Collections.unmodifiableMap(map);
    }

    private ExternalConfigurationFileAccessor() {
    }
}
