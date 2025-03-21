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

import eu.eidas.auth.commons.io.FileMarshaller;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.commons.io.StreamMarshaller;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Reloadable ConfigurationMap.
 *
 * @since 1.1
 */
public final class ReloadableProtocolConfigurationMap {

    static Map<String, ProtocolEngineConfiguration> unmarshalStream(@Nonnull String configurationFileName,
                                                                    @Nullable String defaultPath,
                                                                    @Nonnull InputStream input,
                                                                    @Nullable String overrideFileName)
            throws IOException {
        try {
            InstanceMap instanceMap = DOMConfigurationParser.parseConfiguration(configurationFileName, input);
            return DOMConfigurator.getProtocolConfigurationMap(instanceMap, defaultPath, overrideFileName);
        } catch (ProtocolEngineConfigurationException e) {
            throw new IOException(e);
        }
    }

    @Nonnull
    private final SingletonAccessor<Map<String, ProtocolEngineConfiguration>> accessor;

    /**
     * @param configurationFileName the name of the configuration properties file.
     * @param overrideFileName      the configuration properties file name containing overriding properties if any, otherwise
     *                              {@code null}.
     * @param defaultPath           optional path to the configuration properties file.
     * @since 1.1
     */
    public ReloadableProtocolConfigurationMap(@Nonnull final String configurationFileName,
                                              @Nullable final String overrideFileName,
                                              @Nullable final String defaultPath) {
        Preconditions.checkNotNull(configurationFileName, "configurationFileName");

        SingletonAccessor<Map<String, ProtocolEngineConfiguration>> fileAccessor =
                SingletonAccessors.newFileAccessor(configurationFileName, defaultPath,
                        new FileMarshaller<Map<String, ProtocolEngineConfiguration>>() {

                            @Override
                            public void marshal(@Nonnull
                                                Map<String, ProtocolEngineConfiguration> newValue,
                                                @Nonnull File output)
                                    throws UnsupportedEncodingException {
                                throw new UnsupportedEncodingException();
                            }

                            @Override
                            public Map<String, ProtocolEngineConfiguration> unmarshal(
                                    @Nonnull File input) throws IOException {
                                try (InputStream inputStream = new FileInputStream(input)) {
                                    return unmarshalStream(configurationFileName, defaultPath,
                                            new BufferedInputStream(
                                                    new FileInputStream(input)),
                                            overrideFileName);
                                }
                            }
                        },
                        new StreamMarshaller<Map<String, ProtocolEngineConfiguration>>() {

                            @Override
                            public void marshal(@Nonnull
                                                Map<String, ProtocolEngineConfiguration> newValue,
                                                @Nonnull OutputStream output)
                                    throws UnsupportedEncodingException {
                                throw new UnsupportedEncodingException();
                            }

                            @Override
                            public Map<String, ProtocolEngineConfiguration> unmarshal(
                                    @Nonnull InputStream input) throws IOException {
                                return unmarshalStream(configurationFileName, defaultPath, input,
                                        overrideFileName);
                            }
                        });
        // each interface component in the returned configuration can be cached as a reference in client code,
        // therefore each such interface must be a proxy capable of reloading its configuration
        accessor = new ReloadableProtocolConfigurationProxyMapAccessor(fileAccessor);
    }

    @Nonnull
    public SingletonAccessor<Map<String, ProtocolEngineConfiguration>> getAccessor() {
        return accessor;
    }

    @Nonnull
    public Map<String, ProtocolEngineConfiguration> getConfigurationMap() {
        try {
            return accessor.get();
        } catch (IOException e) {
            if (e.getCause() instanceof ProtocolEngineConfigurationException) {
                throw new EIDASSAMLEngineRuntimeException((ProtocolEngineConfigurationException) e.getCause());
            }
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }
}
