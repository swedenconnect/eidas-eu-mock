package eu.eidas.auth.engine;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.FixedProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.samlengineconfig.CertificateConfigurationManager;
import eu.eidas.util.Preconditions;

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
     * @return instance of ProtocolEngine
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
                                                       @Nonnull CertificateConfigurationManager configManager,
                                                       @Nonnull ProtocolProcessorI protocolProcessor,
                                                       @Nonnull SamlEngineClock samlEngineClock)
            throws SamlEngineConfigurationException {
        ProtocolEngineConfigurationFactory protocolEngineConfigurationFactory =
                new ProtocolEngineConfigurationFactory(configManager);

        return createProtocolEngine(instanceName, protocolEngineConfigurationFactory, protocolProcessor,
                                    samlEngineClock);
    }

    @Nonnull
    public static ProtocolEngineI createProtocolEngine(@Nonnull String instanceName,
                                                       @Nonnull
                                                               ProtocolEngineConfigurationFactory protocolEngineConfigurationFactory,
                                                       @Nonnull ProtocolProcessorI protocolProcessor,
                                                       @Nonnull SamlEngineClock samlEngineClock)
            throws SamlEngineConfigurationException {
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
            throws SamlEngineConfigurationException {
        this(configurationFactory.getConfigurationMapAccessor());
    }

    public ProtocolEngineFactory(@Nonnull SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor)
            throws SamlEngineConfigurationException {
        Preconditions.checkNotNull(accessor, "accessor");
        this.accessor = accessor;
        ImmutableMap.Builder<String, ProtocolEngineI> builder = ImmutableMap.builder();
        try {
            for (final String instanceName : accessor.get().keySet()) {
                builder.put(instanceName, createProtocolEngine(instanceName));
            }
        } catch (IOException e) {
            throw new SamlEngineConfigurationException(
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
            public ProtocolEngineConfiguration get() throws SamlEngineConfigurationException {
                try {
                    return accessor.get().get(instanceName);
                } catch (IOException e) {
                    throw new SamlEngineConfigurationException(
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
