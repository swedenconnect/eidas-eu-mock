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
package eu.eidas.auth.engine.configuration.dom;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
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
            throws ProtocolEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseEncryptionMandatory = Boolean.parseBoolean(
                StringUtils.trim(EncryptionKey.RESPONSE_ENCRYPTION_MANDATORY.getAsString(properties)));
        boolean isAssertionEncryptWithKey = Boolean.parseBoolean(
                StringUtils.trim(EncryptionKey.ASSERTION_ENCRYPTION_WITH_KEY.getAsString(properties)));

        String jcaProviderName = StringUtils.trim(EncryptionKey.JCA_PROVIDER_NAME.getAsString(properties));

        String dataEncryptionAlgorithm =
                StringUtils.trim(EncryptionKey.DATA_ENCRYPTION_ALGORITHM.getAsString(properties));

        String keyEncryptionAlgorithm =
                StringUtils.trim(EncryptionKey.KEY_ENCRYPTION_ALGORITHM.getAsString(properties));
        String keyEncryptionAlgorithmForKeyAgreement =
                StringUtils.trim(EncryptionKey.KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT.getAsString(properties));

        String encryptionAlgorithmWhiteList =
                StringUtils.trim(EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST.getAsString(properties));

        KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, defaultPath).loadKeyStoreContent();

        return new EncryptionConfiguration(checkedValidityPeriod, disallowedSelfSignedCertificate,
                                           responseEncryptionMandatory, 
                                           isAssertionEncryptWithKey,
                                           keyStoreContent.getPrivateKeyEntries(),
                                           keyStoreContent.getCertificates(), dataEncryptionAlgorithm,
                                           keyEncryptionAlgorithm, keyEncryptionAlgorithmForKeyAgreement,
                                           jcaProviderName, encryptionAlgorithmWhiteList);
    }
}
