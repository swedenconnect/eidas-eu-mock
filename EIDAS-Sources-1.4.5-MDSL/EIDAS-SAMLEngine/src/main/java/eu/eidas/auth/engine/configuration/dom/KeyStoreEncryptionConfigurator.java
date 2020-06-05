package eu.eidas.auth.engine.configuration.dom;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.impl.CertificateValidator;

import javax.annotation.Nullable;

/**
 * KeyStore-based EncryptionConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreEncryptionConfigurator {

    public EncryptionConfiguration getEncryptionConfiguration(Map<String, String> properties,
                                                              @Nullable String defaultPath)
            throws SamlEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseEncryptionMandatory = Boolean.parseBoolean(
                StringUtils.trim(EncryptionKey.RESPONSE_ENCRYPTION_MANDATORY.getAsString(properties)));

        String jcaProviderName = StringUtils.trim(EncryptionKey.JCA_PROVIDER_NAME.getAsString(properties));

        String dataEncryptionAlgorithm =
                StringUtils.trim(EncryptionKey.DATA_ENCRYPTION_ALGORITHM.getAsString(properties));

        String keyEncryptionAlgorithm =
                StringUtils.trim(EncryptionKey.KEY_ENCRYPTION_ALGORITHM.getAsString(properties));

        String encryptionAlgorithmWhiteList =
                StringUtils.trim(EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST.getAsString(properties));

        KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, defaultPath).loadKeyStoreContent();

        return new EncryptionConfiguration(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                           responseEncryptionMandatory, keyStoreContent.getPrivateKeyEntries(),
                                           keyStoreContent.getCertificates(), dataEncryptionAlgorithm,
                                           keyEncryptionAlgorithm, jcaProviderName, encryptionAlgorithmWhiteList);
    }
}
