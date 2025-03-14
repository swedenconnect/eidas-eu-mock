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

package eu.eidas.encryption.config;

import eu.eidas.encryption.support.EidasECDHKeyAgreementProcessor;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.agreement.KeyAgreementProcessor;
import org.opensaml.xmlsec.agreement.KeyAgreementProcessorRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eidas Default Security configuration class.
 */
public class EidasDefaultSecurityConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EidasDefaultSecurityConfiguration.class);
    private static final EidasDefaultSecurityConfiguration INSTANCE = new EidasDefaultSecurityConfiguration();

    private EidasDefaultSecurityConfiguration() {
    }

    private static EidasDefaultSecurityConfiguration getInstance() {
        return INSTANCE;
    }

    /**
     * Opensaml pre-initialization
     */
    public static void preInitialize() { }

    /**
     * Opensaml initialization of OpenSAML library modules using the Java Services API.
     *
     * @throws InitializationException if an error occurred in initializing a module
     */
    public static void initialize() throws InitializationException {
        InitializationService.initialize();
    }

    /**
     * OpenSaml post-initialization to apply the configuration defaults for this instance.
     * Override OpenSaml default security configuration with eIDAS default security configuration
     *
     * @throws InitializationException if an error occurred in initializing a module
     */
    public static void postInitialize() throws InitializationException {
        getInstance().updateSecurityConfiguration();
    }

    /**
     * Override the default encryption, decryption and signature configuration service if defined.
     * @throws InitializationException if an error occurs while registering the new services.
     */
    private synchronized void updateSecurityConfiguration() throws InitializationException {
        LOG.info("Updating OpenSAML security configuration defaults using '{}' ...", this.getClass().getSimpleName());
        updateKeyAgreementProcessor();
        LOG.info("OpenSAML security configuration defaults updated with {}", this.getClass().getSimpleName());
    }

    private synchronized void updateKeyAgreementProcessor() {
        KeyAgreementProcessorRegistry keyAgreementProcessorRegistry = ConfigurationService.get(KeyAgreementProcessorRegistry.class);
        KeyAgreementProcessor eidasECDHkeyAgreementProcessor = new EidasECDHKeyAgreementProcessor();
        keyAgreementProcessorRegistry.register(eidasECDHkeyAgreementProcessor);
    }

}
