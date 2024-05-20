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

import static org.apache.commons.lang.StringUtils.trim;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.core.impl.WhiteListConfigurator;

/**
 * KeyStore-based SignatureConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreSignatureConfigurator {

    private String tryConfigurationKeyPreferPrefix(Map<String, String> properties, SignatureKey signatureKey, String propPrefix) {
        final String prefixedSignatureKey = propPrefix + signatureKey.getKey();
        if (properties.containsKey(prefixedSignatureKey)) {
            return prefixedSignatureKey;
        } else {
            return signatureKey.getKey();
        }
    }

    public SignatureConfiguration getSignatureConfiguration(Map<String, String> properties, @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseSignAssertions = Boolean.parseBoolean(trim(SignatureKey.RESPONSE_SIGN_ASSERTIONS.getAsString(properties)));
        boolean requestSignWithKey =     Boolean.parseBoolean(trim(SignatureKey.REQUEST_SIGN_WITH_KEY_VALUE.getAsString(properties)));
        boolean responseSignWithKey =    Boolean.parseBoolean(trim(SignatureKey.RESPONSE_SIGN_WITH_KEY_VALUE.getAsString(properties)));

        final String enableCertificateRevocationCheckingStr = trim(SignatureKey.ENABLE_CERTIFICATE_REVOCATION_CHECKING.getAsString(properties));
        final String enableCertificateRevocationSoftFailStr = trim(SignatureKey.ENABLE_CERTIFICATE_REVOCATION_SOFT_FAIL.getAsString(properties));

        boolean enableCertificateRevocationChecking = Boolean.parseBoolean(enableCertificateRevocationCheckingStr);
        boolean enableCertificateRevocationSoftFail = Boolean.parseBoolean(enableCertificateRevocationSoftFailStr);

        final String signatureAlgorithm =         SignatureKey.SIGNATURE_ALGORITHM.getAsString(properties);
        final String metadataSignatureAlgorithm = SignatureKey.METADATA_SIGNATURE_ALGORITHM.getAsString(properties);
        final String digestMethodAlgorithm =      SignatureKey.DIGEST_METHOD_ALGORITHM.getAsString(properties);

        final ImmutableSet<String> allowedSignatureAlgorithmWhitelist = WhiteListConfigurator.getAllowedAlgorithms(
                EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST,
                EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST,
                SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getAsString(properties)
        );
        final String signatureAlgorithmWhiteListStr = String.join(EIDASValues.SEMICOLON.toString(), allowedSignatureAlgorithmWhitelist);

        final ImmutableSet<String> digestMethodAlgorithmWhiteList = WhiteListConfigurator.getAllowedAlgorithms(
                EidasSignatureConstants.DEFAULT_DIGEST_ALGORITHM_WHITE_LIST,
                SignatureKey.DIGEST_METHOD_ALGORITHM_WHITELIST.getAsString(properties)
        );

        final ArrayList<KeyStoreContent> keystoreContentList = new ArrayList<>();
        int i = 1;
        while (properties.containsKey(i + "." + KeyStoreKey.KEYSTORE_TYPE.getKey())) {
            final KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, getNumberPrefixConfigurationKeys(i), defaultPath).loadKeyStoreContent();
            keystoreContentList.add(keyStoreContent);
            i++;
        }

        final KeyContainer keyStoreContent = new ListKeystoreContent(keystoreContentList);
        final String serialNumber = SignatureKey.SERIAL_NUMBER.getAsString(properties);
        final String issuer =       SignatureKey.ISSUER.getAsString(properties);

        final KeyContainer trustStoreContent = new ListKeystoreContent(keystoreContentList)
                .subset(KeyStoreContent.KeystorePurpose.TRUSTSTORE);
        final ImmutableSet<X509Certificate> trustedCertificates = trustStoreContent.getCertificates();
        final KeyContainerEntry signatureKeyAndCertificate = keyStoreContent.getMatchingKeyEntry(serialNumber, issuer);


        final String metadataPrefix = SignatureKey.METADATA_PREFIX.getKey();
        final String metadataSerialNumberKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.SERIAL_NUMBER, metadataPrefix);
        final String metadataSerialNumber = properties.get(metadataSerialNumberKey);
        final String metadataIssuerKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.ISSUER, metadataPrefix);
        final String metadataIssuer = properties.get(metadataIssuerKey);

        final ImmutableSet<X509Certificate> metadataKeystoreCertificates = keyStoreContent.getCertificates();
        final KeyContainerEntry metadataSigningKeyAndCertificate = keyStoreContent.getMatchingKeyEntry(metadataSerialNumber, metadataIssuer);


        SignatureConfiguration.Builder signatureConfigurationBuilder = new SignatureConfiguration.Builder();
        signatureConfigurationBuilder.setDigestAlgorithm(digestMethodAlgorithm);
        signatureConfigurationBuilder.setDigestMethodAlgorithmWhiteList(digestMethodAlgorithmWhiteList);
        signatureConfigurationBuilder.setSignatureAlgorithm(signatureAlgorithm);
        signatureConfigurationBuilder.setSignatureAlgorithmWhiteList(signatureAlgorithmWhiteListStr);
        signatureConfigurationBuilder.setSignatureKeyAndCertificate(signatureKeyAndCertificate.getPrivateKeyEntry());
        signatureConfigurationBuilder.setSignatureKeyProvider(signatureKeyAndCertificate.getKeyProvider());
        signatureConfigurationBuilder.setTrustedCertificates(trustedCertificates);
        signatureConfigurationBuilder.setMetadataSignatureAlgorithm(metadataSignatureAlgorithm);
        signatureConfigurationBuilder.setMetadataSigningKeyAndCertificate(metadataSigningKeyAndCertificate.getPrivateKeyEntry());
        signatureConfigurationBuilder.setMetadataSignatureKeyProvider(metadataSigningKeyAndCertificate.getKeyProvider());
        signatureConfigurationBuilder.setMetadataKeystoreCertificates(metadataKeystoreCertificates);
        signatureConfigurationBuilder.setCheckedValidityPeriod(checkedValidityPeriod);
        signatureConfigurationBuilder.setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate);
        signatureConfigurationBuilder.setRequestSignWithKey(requestSignWithKey);
        signatureConfigurationBuilder.setResponseSignWithKey(responseSignWithKey);
        signatureConfigurationBuilder.setResponseSignAssertions(responseSignAssertions);
        signatureConfigurationBuilder.setEnableCertificateRevocationChecking(enableCertificateRevocationChecking);
        signatureConfigurationBuilder.setEnableCertificateRevocationSoftFail(enableCertificateRevocationSoftFail);

        return signatureConfigurationBuilder.build();

    }

    private static KeyStoreConfigurator.KeyStoreConfigurationKeys getNumberPrefixConfigurationKeys(int prefixCounter) {
        return KeyStoreConfigurator.prefixPostfixConfigurationKeys(prefixCounter + ".","");
    }
}
