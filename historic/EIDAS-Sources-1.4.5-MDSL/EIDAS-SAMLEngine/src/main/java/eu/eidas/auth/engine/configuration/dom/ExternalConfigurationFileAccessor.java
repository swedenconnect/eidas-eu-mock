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
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;

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
                        } catch (SamlEngineConfigurationException e) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                });
        return accessor;
    }

    private ExternalConfigurationFileAccessor() {
    }
}
