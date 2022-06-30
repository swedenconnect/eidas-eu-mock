/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import eu.eidas.auth.commons.io.PropertiesConverter;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;

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
                                                @Nonnull final ImmutableMap<String, String> staticParameters,
                                                @Nonnull final ImmutableMap<String, String> overrideParameters,
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
                            ImmutableMap<String, String> validParameters =
                                    DOMConfigurationParser.validateParameters(instanceName, configurationName,
                                                                              Maps.fromProperties(properties));

                            Map<String, String> allParameters = new LinkedHashMap<String, String>();
                            allParameters.putAll(staticParameters);
                            allParameters.putAll(validParameters);
                            allParameters.putAll(overrideParameters);

                            return mapConverter.convert(ImmutableMap.copyOf(allParameters));
                        } catch (ProtocolEngineConfigurationException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
        return accessor;
    }

    private ExternalConfigurationFileAccessor() {
    }
}
