/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.auth.engine;

import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.FixedProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Factory creating {@link ProtocolEngine} instances.
 * <p>
 * Protocol engines are created from a {@link eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration} which are
 * created by a {@link eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory}.
 * <p>
 * One can create one's own ProtocolEngineConfigurationFactory and use it to create one's own ProtocolEngineFactory.
 * <p>
 * There is a default ProtocolEngineFactory: {@link eu.eidas.auth.engine.DefaultProtocolEngineFactory} which uses the
 * default configuration files.
 * <p>
 * One can use the convenient method {@link #getDefaultProtocolEngine(String)} to obtain the default ProtocolEngine
 * created from the default configuration files.
 *
 * @since 1.1
 */
public class ProtocolEngineFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ProtocolEngineFactory.class);

    /**
     * Creates an instance of ProtocolEngine.
     *
     * @param nameInstance the name instance
     * @param protocolProcessor the {@link ProtocolProcessorI}
     * @return instance of ProtocolEngine
     * @throws EIDASSAMLEngineException if the SAMLEngine con not be initialized
     */
    @Nonnull
    public static ProtocolEngineI createProtocolEngine(@Nonnull String nameInstance,
                                                       @Nonnull ProtocolProcessorI protocolProcessor)
            throws EIDASSAMLEngineException {
        return createProtocolEngine(nameInstance, DefaultProtocolEngineConfigurationFactory.getInstance(),
                                    protocolProcessor, new SamlEngineSystemClock());
    }

    @Nonnull
    public static ProtocolEngineI createProtocolEngine(@Nonnull String instanceName,
                                                       @Nonnull
                                                               ProtocolEngineConfigurationFactory protocolEngineConfigurationFactory,
                                                       @Nonnull ProtocolProcessorI protocolProcessor,
                                                       @Nonnull SamlEngineClock samlEngineClock)
            throws ProtocolEngineConfigurationException {
        LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "creating new ProtocolEngine instance: {} ", instanceName);

        ProtocolEngineConfiguration preConfiguration =
                protocolEngineConfigurationFactory.getConfiguration(instanceName);

        protocolProcessor.configure();

        ProtocolEngineConfiguration configuration = ProtocolEngineConfiguration.builder(preConfiguration)
                .protocolProcessor(protocolProcessor)
                .clock(samlEngineClock)
                .build();

        ProtocolEngineI samlEngine = new ProtocolEngine(new FixedProtocolConfigurationAccessor(configuration));

        LOG.info(AbstractProtocolEngine.SAML_EXCHANGE, "created instance: {} ", samlEngine);

        return samlEngine;
    }

    /**
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @return the ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public static ProtocolEngineI getDefaultProtocolEngine(@Nonnull String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return DefaultProtocolEngineFactory.getInstance().getProtocolEngine(instanceName);
    }

    private final SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor;

    private final ImmutableMap<String, ProtocolEngineI> engines;

    public ProtocolEngineFactory(@Nonnull ProtocolEngineConfigurationFactory configurationFactory)
            throws ProtocolEngineConfigurationException {
        this(configurationFactory.getConfigurationMapAccessor());
    }

    public ProtocolEngineFactory(@Nonnull SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(accessor, "accessor");
        this.accessor = accessor;
        ImmutableMap.Builder<String, ProtocolEngineI> builder = ImmutableMap.builder();
        try {
            for (final String instanceName : accessor.get().keySet()) {
                builder.put(instanceName, createProtocolEngine(instanceName));
            }
        } catch (IOException e) {
            throw new ProtocolEngineConfigurationException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()), e);
        }
        engines = builder.build();
    }

    @Nonnull
    private ProtocolEngineI createProtocolEngine(@Nonnull final String instanceName) {
        return createProtocolEngine(new ProtocolConfigurationAccessor() {

            @Nonnull
            @Override
            public ProtocolEngineConfiguration get() throws ProtocolEngineConfigurationException {
                try {
                    return accessor.get().get(instanceName);
                } catch (IOException e) {
                    throw new ProtocolEngineConfigurationException(
                            EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                            EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()), e);
                }
            }
        });
    }

    @Nonnull
    protected ProtocolEngineI createProtocolEngine(@Nonnull ProtocolConfigurationAccessor configurationAccessor) {
        return new ProtocolEngine(configurationAccessor);
    }

    /**
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @return the ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public ProtocolEngineI getProtocolEngine(@Nonnull String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return engines.get(instanceName.trim());
    }
}
