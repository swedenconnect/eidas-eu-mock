package eu.eidas.auth.engine;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;

/**
 * ProtocolEngineFactory
 *
 * @since 1.1
 */
public final class DefaultProtocolEngineFactory {

    /**
     * Initialization-on-demand holder idiom.
     * <p/>
     * See item 71 of Effective Java 2nd Edition.
     * <p/>
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom.
     */
    private static final class LazyHolder {

        private static final ProtocolEngineFactory DEFAULT_SAML_ENGINE_FACTORY;

        private static final Exception INITIALIZATION_EXCEPTION;

        static {
            Exception initializationException = null;
            ProtocolEngineFactory defaultProtocolEngineFactory = null;
            try {
                defaultProtocolEngineFactory =
                        new ProtocolEngineFactory(DefaultProtocolEngineConfigurationFactory.getInstance());
            } catch (Exception ex) {
                initializationException = ex;
                LOG.error("Unable to instantiate default SAML engines: " + ex, ex);
            }
            DEFAULT_SAML_ENGINE_FACTORY = defaultProtocolEngineFactory;
            INITIALIZATION_EXCEPTION = initializationException;
        }

        static ProtocolEngineFactory getDefaultSamlEngineFactory() {
            if (null == INITIALIZATION_EXCEPTION) {
                return DEFAULT_SAML_ENGINE_FACTORY;
            } else {
                throw new IllegalStateException(INITIALIZATION_EXCEPTION);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DefaultProtocolEngineFactory.class);

    @Nonnull
    public static ProtocolEngineFactory getInstance() {
        return LazyHolder.getDefaultSamlEngineFactory();
    }

    private DefaultProtocolEngineFactory() {
    }
}
