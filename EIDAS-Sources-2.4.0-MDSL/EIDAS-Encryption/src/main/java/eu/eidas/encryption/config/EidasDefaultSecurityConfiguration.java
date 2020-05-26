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
 * limitations under the Licence.
 */

package eu.eidas.encryption.config;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.encryption.utils.DecryptionUtils;
import org.apache.xml.security.algorithms.JCEMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.xmlsec.DecryptionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.swedenconnect.opensaml.xmlsec.config.SAML2IntSecurityConfiguration;

import java.security.Provider;
import java.security.Security;
import java.util.Collections;

/**
 * Eidas Default Security configuration class.
 */
public class EidasDefaultSecurityConfiguration extends SAML2IntSecurityConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EidasDefaultSecurityConfiguration.class);
    private static final String EIDAS_OPENSAML_PROFILE_NAME = "eidas-opensaml-security-config";
    private static final EidasDefaultSecurityConfiguration INSTANCE = new EidasDefaultSecurityConfiguration();

    private EidasDefaultSecurityConfiguration() {
    }

    private static EidasDefaultSecurityConfiguration getInstance() {
        return INSTANCE;
    }

    @Override
    protected DecryptionConfiguration createDefaultDecryptionConfiguration() {
        return DecryptionUtils.buildDefaultDecryptionConfiguration(Collections.EMPTY_LIST);
    }

    @Override
    public String getProfileName() {
        return EIDAS_OPENSAML_PROFILE_NAME;
    }


    /**
     * Opensaml pre-initialization
     *
     * Adds the Bouncy Castle Provider
     * though call to {@link EidasDefaultSecurityConfiguration#addBouncyCastleSecurityProvider()}
     */
    public static void preInitialize() {
        getInstance().addBouncyCastleSecurityProvider();
    }

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
     * and update the signature configuration by setting BouncyCastle as Signer provider if BouncyCastle
     * is present as provider.
     *
     * @throws InitializationException if an error occurred in initializing a module
     */
    public static void postInitialize() throws InitializationException {
        getInstance().updateSecurityConfiguration();
        getInstance().updateSignatureConfiguration();
    }

    private synchronized void addBouncyCastleSecurityProvider() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider == null && isBouncyCastleProviderInstallationAllowed()) {
            LOG.info("{}: Crypto provider '{}' is not installed, installing it ...", this.getProfileName(),
                    BouncyCastleProvider.PROVIDER_NAME);
            Security.addProvider(new BouncyCastleProvider());
            LOG.info("{}: Crypto provider '{}' is installed", this.getProfileName(),
                    BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * This method is link to BouncyCastleBootstrap (EIDINT-3100).
     * Since there was no way to properly reuse the method, property is fetched here also.
     * @return true if BouncyCastle Security Provider may added to the Security providers, false otherwise.
     */
    private synchronized boolean isBouncyCastleProviderInstallationAllowed() {
        String value = System.getProperty(EidasParameterKeys.BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL.toString());
        boolean isBouncyCastleSecurityProviderInstallBlocked = Boolean.parseBoolean(value);
        return !isBouncyCastleSecurityProviderInstallBlocked;
    }

    /**
     * Override the default encryption, decryption and signature configuration service if defined.
     * @throws InitializationException if an error occurs while registering the new services.
     */
    private synchronized void updateSecurityConfiguration() throws InitializationException {
        LOG.info("Updating OpenSAML security configuration defaults using '{}' ...", this.getClass().getSimpleName());
        this.initOpenSAML();
        LOG.info("OpenSAML security configuration defaults updated with {}", this.getClass().getSimpleName());
    }

    /**
     * Set BouncyCastle Security provider as Default Signature Provider if BouncyCastle is installed.
     *
     * As OpenSaml xmlsec Signer provider is defined to be the ApacheSantuarioSignerProviderImpl, we
     * need to update the providerId in the JCEMapper from apache Santuario.
     */
    private synchronized void updateSignatureConfiguration() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            LOG.info("Setting BouncyCastle Provider as Signer provider");
            JCEMapper.setProviderId(BouncyCastleProvider.PROVIDER_NAME);
        }
    }
}
