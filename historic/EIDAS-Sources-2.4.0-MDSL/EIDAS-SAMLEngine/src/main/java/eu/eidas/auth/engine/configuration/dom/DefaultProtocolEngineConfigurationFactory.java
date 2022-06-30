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
 * limitations under the Licence
 */
package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableMap;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * Default ProtocolEngineConfiguration Factory
 *
 * @since 1.1
 */
public final class DefaultProtocolEngineConfigurationFactory {

    /**
     * Initialization-on-demand holder idiom.
     * <p/>
     * See item 71 of Effective Java 2nd Edition.
     * <p/>
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom.
     */
    private static final class LazyHolder {

        private static final ProtocolEngineConfigurationFactory DEFAULT_CONFIGURATION_FACTORY;

        private static final Exception INITIALIZATION_EXCEPTION;

        static {
            Exception initializationException = null;
            ProtocolEngineConfigurationFactory factory = null;
            try {
                factory = new ProtocolEngineConfigurationFactory(DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE, null, null);
            } catch (Exception ex) {
                LOG.error("Problem initializing the static configurations: " + ex, ex);
                initializationException = ex;
            }
            DEFAULT_CONFIGURATION_FACTORY = factory;
            INITIALIZATION_EXCEPTION = initializationException;
        }

        static ProtocolEngineConfigurationFactory getDefaultConfigurationFactory() {
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
    private static final Logger LOG = LoggerFactory.getLogger(DefaultProtocolEngineConfigurationFactory.class);

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the name of the instance
     * @return the corresponding configuration
     * @throws ProtocolEngineConfigurationException the SAML engine configuration exception
     */
    @Nonnull
    public static ProtocolEngineConfiguration getDefaultConfiguration(@Nonnull String instanceName)
            throws ProtocolEngineConfigurationException {
        return getInstance().getConfiguration(instanceName);
    }

    /**
     * Returns all the available configurations retrieved from the default configuration file {@link
     * DOMConfigurationParser#DEFAULT_CONFIGURATION_FILE}.
     *
     * @return all the available configurations retrieved from the default configuration file {@link
     * DOMConfigurationParser#DEFAULT_CONFIGURATION_FILE}.
     */
    public static SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> getDefaultConfigurations() {
        return getInstance().getConfigurationMapAccessor();
    }

    public static ProtocolEngineConfigurationFactory getInstance() {
        return LazyHolder.getDefaultConfigurationFactory();
    }

    private DefaultProtocolEngineConfigurationFactory() {
    }
}
