/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.auth.engine.core.impl;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The base abstract class for implementations of {@link ProtocolCipherI}.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolCipher implements ProtocolCipherI {

    public static final ImmutableSet<String> DEFAULT_ALLOWED_ALGORITHMS =
            ImmutableSet.of("http://www.w3.org/2009/xmlenc11#aes128-gcm", "http://www.w3.org/2009/xmlenc11#aes192-gcm",
                            "http://www.w3.org/2009/xmlenc11#aes256-gcm");

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractProtocolCipher.class);

    static {
        BouncyCastleBootstrap.bootstrap();
    }

    private final boolean checkedValidityPeriod;

    private final boolean disallowedSelfSignedCertificate;

    private final boolean responseEncryptionMandatory;

    @Nonnull
    private final ImmutableSet<String> encryptionAlgorithmWhiteList;

    protected AbstractProtocolCipher(@Nonnull EncryptionConfiguration encryptionConfiguration)
            throws SamlEngineConfigurationException {
        this(encryptionConfiguration.isCheckedValidityPeriod(),
             encryptionConfiguration.isDisallowedSelfSignedCertificate(),
             encryptionConfiguration.isResponseEncryptionMandatory(),
             encryptionConfiguration.getEncryptionAlgorithmWhiteList());
    }

    protected AbstractProtocolCipher(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseEncryptionMandatory,
                                     @Nonnull ImmutableSet<String> encryptionAlgorithmWhiteList) {
        Preconditions.checkNotNull(encryptionAlgorithmWhiteList, "encryptionAlgorithmWhiteList");

        this.checkedValidityPeriod = checkedValidityPeriod;
        this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
        this.responseEncryptionMandatory = responseEncryptionMandatory;
        this.encryptionAlgorithmWhiteList = encryptionAlgorithmWhiteList;
    }

    protected AbstractProtocolCipher(boolean checkedValidityPeriod,
                                     boolean disallowedSelfSignedCertificate,
                                     boolean responseEncryptionMandatory,
                                     @Nullable String encryptionAlgorithmWhiteList)
            throws SamlEngineConfigurationException {
        try {
            this.checkedValidityPeriod = checkedValidityPeriod;
            this.disallowedSelfSignedCertificate = disallowedSelfSignedCertificate;
            this.responseEncryptionMandatory = responseEncryptionMandatory;

            this.encryptionAlgorithmWhiteList =
                    WhiteListConfigurator.getAllowedAlgorithms(DEFAULT_ALLOWED_ALGORITHMS, encryptionAlgorithmWhiteList);
            LOG.trace("AbstractProtocolCipher loaded.");
        } catch (Exception e) {
            LOG.error("AbstractProtocolCipher init: " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    @Nonnull
    protected ImmutableSet<String> getEncryptionAlgorithmWhiteList() {
        return encryptionAlgorithmWhiteList;
    }

    @Nonnull
    protected String getJcaProviderNameWithDefault(@Nullable String jcaProviderName) {
        String bc = BouncyCastleBootstrap.BOUNCY_CASTLE_PROVIDER.getName();
        if (StringUtils.isBlank(jcaProviderName)) {
            jcaProviderName = bc;
        }
        if (bc.equals(jcaProviderName)) {
            reInstallSecurityProvider();
        }
        return jcaProviderName;
    }

    @Override
    public boolean isCheckedValidityPeriod() {
        return checkedValidityPeriod;
    }

    @Override
    public boolean isDisallowedSelfSignedCertificate() {
        return disallowedSelfSignedCertificate;
    }

    @Override
    public boolean isResponseEncryptionMandatory() {
        return responseEncryptionMandatory;
    }

    protected void reInstallSecurityProvider() {
        BouncyCastleBootstrap.reInstallSecurityProvider();
    }

    protected void validateEncryptionAlgorithm(@Nonnull String encryptionAlgorithm) throws EIDASSAMLEngineException {
        if (StringUtils.isBlank(encryptionAlgorithm) || !encryptionAlgorithmWhiteList.contains(
                encryptionAlgorithm.trim())) {
            LOG.error("Invalid encryption algorithm: \"" + encryptionAlgorithm
                              + "\" (allowed algorithms in white list are: " + encryptionAlgorithmWhiteList + ")");
            throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_ENCRYPTION_ALGORITHM.errorCode(), EidasErrorKey.INVALID_ENCRYPTION_ALGORITHM.errorMessage());
        }
    }
}
