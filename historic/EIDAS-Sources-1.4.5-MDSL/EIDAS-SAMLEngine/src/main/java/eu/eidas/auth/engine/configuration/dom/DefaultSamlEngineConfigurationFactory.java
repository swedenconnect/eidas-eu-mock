package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;

/**
 * SamlEngineConfiguration Factory.
 * <p>
 * Remove this class in 1.2.
 *
 * @since 1.1
 * @deprecated since 1.1, use {@link DefaultProtocolEngineConfigurationFactory} instead.
 */
@Deprecated
public final class DefaultSamlEngineConfigurationFactory {

    /**
     * Initialization-on-demand holder idiom.
     * <p/>
     * See item 71 of Effective Java 2nd Edition.
     * <p/>
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom.
     */
    private static final class LazyHolder {

        private static final SamlEngineConfigurationFactory DEFAULT_CONFIGURATION_FACTORY;

        private static final Exception INITIALIZATION_EXCEPTION;

        static {
            Exception initializationException = null;
            SamlEngineConfigurationFactory factory = null;
            try {
                factory = new SamlEngineConfigurationFactory(DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE);
            } catch (Exception ex) {
                LOG.error("Problem initializing the static configurations: " + ex, ex);
                initializationException = ex;
            }
            DEFAULT_CONFIGURATION_FACTORY = factory;
            INITIALIZATION_EXCEPTION = initializationException;
        }

        static SamlEngineConfigurationFactory getDefaultConfigurationFactory() {
            if (null == INITIALIZATION_EXCEPTION) {
                return DEFAULT_CONFIGURATION_FACTORY;
            } else {
                throw new IllegalStateException(INITIALIZATION_EXCEPTION);
            }
        }
    }

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DefaultSamlEngineConfigurationFactory.class);

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the name of the instance
     */
    @Nonnull
    public static SamlEngineConfiguration getDefaultConfiguration(@Nonnull String instanceName)
            throws SamlEngineConfigurationException {
        return getInstance().getConfiguration(instanceName);
    }

    /**
     * Returns all the available configurations retrieved from the default configuration file {@link
     * DOMConfigurationParser#DEFAULT_CONFIGURATION_FILE}.
     *
     * @return all the available configurations retrieved from the default configuration file {@link
     * DOMConfigurationParser#DEFAULT_CONFIGURATION_FILE}.
     */
    public static SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> getDefaultConfigurations() {
        return getInstance().getConfigurationMapAccessor();
    }

    public static SamlEngineConfigurationFactory getInstance() {
        return LazyHolder.getDefaultConfigurationFactory();
    }

    private DefaultSamlEngineConfigurationFactory() {
    }
}
