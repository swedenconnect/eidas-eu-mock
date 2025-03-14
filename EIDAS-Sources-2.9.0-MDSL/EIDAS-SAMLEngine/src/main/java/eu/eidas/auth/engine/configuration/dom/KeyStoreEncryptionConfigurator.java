/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasEncryptionConstants;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.core.impl.WhiteListConfigurator;

import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.ASSERTION_ENCRYPTION_WITH_KEY;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.DATA_ENCRYPTION_ALGORITHM;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.ENCRYPTION_ALGORITHM_WHITE_LIST;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.JCA_PROVIDER_NAME;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.KEY_ENCRYPTION_AGREEMENT_METHOD_ALGORITHM;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT_DIGEST;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT_MGF;
import static eu.eidas.auth.engine.configuration.dom.EncryptionKey.RESPONSE_ENCRYPTION_MANDATORY;
import static org.apache.commons.lang.StringUtils.trim;

/**
 * KeyStore-based EncryptionConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreEncryptionConfigurator {

    public static EncryptionConfiguration getEncryptionConfiguration(Map<String, String> properties,
                                                              @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseEncryptionMandatory = Boolean.parseBoolean(trim(RESPONSE_ENCRYPTION_MANDATORY.getAsString(properties)));
        boolean isAssertionEncryptWithKey = Boolean.parseBoolean(trim(ASSERTION_ENCRYPTION_WITH_KEY.getAsString(properties)));

        String jcaProviderName =         trim(JCA_PROVIDER_NAME.getAsString(properties));
        String dataEncryptionAlgorithm = trim(DATA_ENCRYPTION_ALGORITHM.getAsString(properties));
        
        String encryptionMethodKeyTransport =       trim(KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT.getAsString(properties));
        String messageDigestKeyTransport =          trim(KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT_DIGEST.getAsString(properties));
        String maskGenerationFunctionKeyTransport = trim(KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT_MGF.getAsString(properties));

        String keyEncryptionAlgorithmForKeyAgreement = trim(KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT.getAsString(properties));
        String keyEncryptionAgreementMethodAlgorithm = trim(KEY_ENCRYPTION_AGREEMENT_METHOD_ALGORITHM.getAsString(properties));
        Set<String> allowedEncryptionAlgorithms = WhiteListConfigurator.getAllowedAlgorithms(
                EidasEncryptionConstants.DEFAULT_ENCRYPTION_ALGORITHM_WHITE_LIST,
                EidasEncryptionConstants.DEFAULT_ENCRYPTION_ALGORITHM_WHITE_LIST,
                trim(ENCRYPTION_ALGORITHM_WHITE_LIST.getAsString(properties))
        );
        String encryptionAlgorithmWhiteList = String.join(EIDASValues.SEMICOLON.toString(), allowedEncryptionAlgorithms);

        ArrayList<KeyStoreContent> keystoreContentList = new ArrayList<>();
        int i = 1;
        while(properties.containsKey(i + "." + KeyStoreKey.KEYSTORE_TYPE.getKey())) {
            final KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, getNumberPrefixConfigurationKeys(i), defaultPath).loadKeyStoreContent(); // default pass
            keystoreContentList.add(keyStoreContent);
            i++;
        }

        EncryptionConfiguration.Builder encryptionConfigurationBuilder = new EncryptionConfiguration.Builder()
                .setResponseEncryptionMandatory(responseEncryptionMandatory)
                .setCheckedValidityPeriod(checkedValidityPeriod)
                .setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate)
                .setAssertionEncryptWithKey(isAssertionEncryptWithKey)
                .setDataEncryptionAlgorithm(dataEncryptionAlgorithm)
                .setKeyEncryptionAlgorithm(encryptionMethodKeyTransport)
                .setMessageDigestKeyTransport(messageDigestKeyTransport)
                .setMaskGenerationFunctionKeyTransport(maskGenerationFunctionKeyTransport)
                .setKeyEncryptionAlgorithmForKeyAgreement(keyEncryptionAlgorithmForKeyAgreement)
                .setKeyEncryptionAgreementMethodAlgorithm(keyEncryptionAgreementMethodAlgorithm)
                .setJcaProviderName(jcaProviderName)
                .setEncryptionAlgorithmWhiteList(encryptionAlgorithmWhiteList);

        if (!keystoreContentList.isEmpty()){

            KeyContainer keyStoreContent = new ListKeystoreContent(keystoreContentList);

            final Set<X509Certificate> encryptionCertificates = keyStoreContent.getCertificates();
            final Set<KeyStore.PrivateKeyEntry> decryptionKeyAndCertificates = keyStoreContent.getPrivateKeyEntries();

            encryptionConfigurationBuilder
                    .setDecryptionKeyAndCertificates(decryptionKeyAndCertificates)
                    .setEncryptionCertificates(encryptionCertificates);

        }
        return encryptionConfigurationBuilder.build();
    }

    private static KeyStoreConfigurator.KeyStoreConfigurationKeys getNumberPrefixConfigurationKeys(int prefixCounter) {
        return KeyStoreConfigurator.prefixPostfixConfigurationKeys(prefixCounter + ".","");
    }
}
