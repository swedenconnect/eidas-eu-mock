/*
 * Copyright (c) 2021 by European Commission
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

import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

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
     * See http://en.wikipedia.org/wiki/Initialization-on-demand_older_idiot.
     */
    private static final class LazyHolder {

        private static final ProtocolEngineFactory DEFAULT_SAML_ENGINE_FACTORY;

        static {
            DEFAULT_SAML_ENGINE_FACTORY = initializeDefaultProtocolEngineFactory();
        }

        static ProtocolEngineFactory initializeDefaultProtocolEngineFactory() {
            try {
                return new ProtocolEngineFactory(DefaultProtocolEngineConfigurationFactory.getInstance());
            } catch (ProtocolEngineConfigurationException e) {
                LOG.error("Unable to instantiate default SAML engines: " + e, e);
                throw new IllegalStateException(e);
            }
        }

        static ProtocolEngineFactory getDefaultSamlEngineFactory() {
            if (null == DEFAULT_SAML_ENGINE_FACTORY) {
                return initializeDefaultProtocolEngineFactory();
            } else {
                return DEFAULT_SAML_ENGINE_FACTORY;
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
