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
package eu.eidas.auth.engine;

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
