package eu.eidas.sp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import eu.eidas.util.Preconditions;

/**
 * Sp ProtocolEngineFactory
 *
 * @since 1.1
 */
public final class SpProtocolEngineFactory extends ProtocolEngineFactory {

    /**
     * Initialization-on-demand holder idiom.
     * <p/>
     * See item 71 of Effective Java 2nd Edition.
     * <p/>
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom.
     */
    private static final class LazyHolder {

        private static final SpProtocolEngineFactory DEFAULT_SAML_ENGINE_FACTORY;

        private static final Exception INITIALIZATION_EXCEPTION;

        static {
            Exception initializationException = null;
            SpProtocolEngineFactory defaultProtocolEngineFactory = null;
            try {
                ProtocolEngineConfigurationFactory protocolEngineConfigurationFactory = new ProtocolEngineConfigurationFactory(Constants.SP_SAMLENGINE_FILE, null, SPUtil.getConfigFilePath());
                defaultProtocolEngineFactory =
                        new SpProtocolEngineFactory(protocolEngineConfigurationFactory);
            } catch (Exception ex) {
                initializationException = ex;
                LOG.error("Unable to instantiate default SAML engines: " + ex, ex);
            }
            DEFAULT_SAML_ENGINE_FACTORY = defaultProtocolEngineFactory;
            INITIALIZATION_EXCEPTION = initializationException;
        }

        static SpProtocolEngineFactory getDefaultSamlEngineFactory() {
            if (null == INITIALIZATION_EXCEPTION) {
                return DEFAULT_SAML_ENGINE_FACTORY;
            } else {
                throw new IllegalStateException(INITIALIZATION_EXCEPTION);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SpProtocolEngineFactory.class);

    @Nonnull
    public static SpProtocolEngineFactory getInstance() {
        return LazyHolder.getDefaultSamlEngineFactory();
    }

    /**
     * Returns a default ProtocolEngine instance matching the given name retrieved from the configuration file.
     *
     * @param instanceName the instance name
     * @return the ProtocolEngine instance matching the given name retrieved from the configuration file
     */
    @Nullable
    public static SpProtocolEngineI getSpProtocolEngine(@Nonnull String instanceName) {
        Preconditions.checkNotBlank(instanceName, "instanceName");
        return (SpProtocolEngineI) getInstance().getProtocolEngine(instanceName);
    }

    private SpProtocolEngineFactory(@Nonnull ProtocolEngineConfigurationFactory configurationFactory)
            throws SamlEngineConfigurationException {
        super(configurationFactory);
    }

    @Nonnull
    @Override
    protected ProtocolEngineI createProtocolEngine(@Nonnull ProtocolConfigurationAccessor configurationAccessor) {
        return new SpProtocolEngine(configurationAccessor);
    }
}