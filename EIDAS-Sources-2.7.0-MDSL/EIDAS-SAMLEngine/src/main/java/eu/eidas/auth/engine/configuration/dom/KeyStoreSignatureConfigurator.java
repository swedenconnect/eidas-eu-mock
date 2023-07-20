/*
 * Copyright (c) 2023 by European Commission
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

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.core.impl.WhiteListConfigurator;
import se.idsec.eidas.cef.trustconfig.EidasTrustedCertificates;

import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.trim;

/**
 * KeyStore-based SignatureConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreSignatureConfigurator {

    /**
     * This is a customized object which append the trusted certificate list with certificates hold in a PEM file
     * The location of the PEM file is determined by the environment variable "EIDAS_TRUSTED_CERTS_FILE".
     * This file can hold 1 or more trusted certificates.
     */
    private static final EidasTrustedCertificates externalTrustConfig = new EidasTrustedCertificates();

    private KeyStoreConfigurator getKeyStoreConfigurator(Map<String, String> properties, @Nullable String defaultPath, String propPrefix) throws ProtocolEngineConfigurationException {
        final String keyStorePathConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEYSTORE_PATH, propPrefix);
        final String keyStoreTypeConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEYSTORE_TYPE, propPrefix);
        final String keyStoreProviderConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEYSTORE_PROVIDER, propPrefix);
        final String keyStorePasswordConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEYSTORE_PASSWORD, propPrefix);
        final String keyStorePurposeConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEYSTORE_PURPOSE, propPrefix);
        final String keyAliasConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEY_ALIAS, propPrefix);
        final String keyPasswordConfigurationKey = tryConfigurationKeyPreferPrefix(properties, KeyStoreKey.KEY_PASSWORD, propPrefix);

        KeyStoreConfigurator.KeyStoreConfigurationKeys keyStoreConfigurationKeys = new KeyStoreConfigurator.KeyStoreConfigurationKeys(
                keyStorePathConfigurationKey,
                keyStoreTypeConfigurationKey,
                keyStoreProviderConfigurationKey,
                keyStorePasswordConfigurationKey,
                keyStorePurposeConfigurationKey,
                keyAliasConfigurationKey,
                keyPasswordConfigurationKey
        );

        return new KeyStoreConfigurator(properties, keyStoreConfigurationKeys, defaultPath);
    }

    private String tryConfigurationKeyPreferPrefix(Map<String, String> properties, KeyStoreKey keyStoreKey, String propPrefix) {
        final String prefixedKeyStoreKey = propPrefix + keyStoreKey.getKey();
        if (properties.containsKey(prefixedKeyStoreKey)) {
            return prefixedKeyStoreKey;
        } else {
            return keyStoreKey.getKey();
        }
    }

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
        keystoreContentList.addAll(fetchLegacyConfiguration(properties, defaultPath));
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
        /*
         * Customized addition by SE for injecting trusted MDSL and PEM certificates
         */
        final ImmutableSet<X509Certificate> trustedCertificates = externalTrustConfig.addTrustedCertificates(trustStoreContent.getCertificates(), properties);

        final KeyStore.PrivateKeyEntry signatureKeyAndCertificate = keyStoreContent.getMatchingPrivateKeyEntry(serialNumber, issuer);


        final String metadataPrefix = SignatureKey.METADATA_PREFIX.getKey();
        final String metadataSerialNumberKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.SERIAL_NUMBER, metadataPrefix);
        final String metadataSerialNumber = properties.get(metadataSerialNumberKey);
        final String metadataIssuerKey = tryConfigurationKeyPreferPrefix(properties, SignatureKey.ISSUER, metadataPrefix);
        final String metadataIssuer = properties.get(metadataIssuerKey);

        final ImmutableSet<X509Certificate> metadataKeystoreCertificates = keyStoreContent.getCertificates();
        final KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate = keyStoreContent.getMatchingPrivateKeyEntry(metadataSerialNumber, metadataIssuer);


        SignatureConfiguration.Builder signatureConfigurationBuilder = new SignatureConfiguration.Builder();
        signatureConfigurationBuilder.setDigestAlgorithm(digestMethodAlgorithm);
        signatureConfigurationBuilder.setDigestMethodAlgorithmWhiteList(digestMethodAlgorithmWhiteList);
        signatureConfigurationBuilder.setSignatureAlgorithm(signatureAlgorithm);
        signatureConfigurationBuilder.setSignatureAlgorithmWhiteList(signatureAlgorithmWhiteListStr);
        signatureConfigurationBuilder.setSignatureKeyAndCertificate(signatureKeyAndCertificate);
        signatureConfigurationBuilder.setTrustedCertificates(trustedCertificates);
        signatureConfigurationBuilder.setMetadataSignatureAlgorithm(metadataSignatureAlgorithm);
        signatureConfigurationBuilder.setMetadataSigningKeyAndCertificate(metadataSigningKeyAndCertificate);
        signatureConfigurationBuilder.setMetadataKeystoreCertificates(metadataKeystoreCertificates);
        signatureConfigurationBuilder.setCheckedValidityPeriod(checkedValidityPeriod);
        signatureConfigurationBuilder.setDisallowedSelfSignedCertificate(disallowedSelfSignedCertificate);
        signatureConfigurationBuilder.setRequestSignWithKey(requestSignWithKey);
        signatureConfigurationBuilder.setResponseSignWithKey(responseSignWithKey);
        signatureConfigurationBuilder.setResponseSignAssertions(responseSignAssertions);

        return signatureConfigurationBuilder.build();

    }

    private List<KeyStoreContent> fetchLegacyConfiguration(Map<String, String> properties, String defaultPath) throws ProtocolEngineConfigurationException {
        ArrayList<KeyStoreContent> legacyKeyStores = new ArrayList<>();

        if(properties.containsKey(KeyStoreConfigurator.DEFAULT_KEYSTORE_CONFIGURATION_KEYS.getKeyStoreTypeConfigurationKey())) {
            final KeyStoreContent defaultKeyStoreContent = new KeyStoreConfigurator(properties, defaultPath).loadKeyStoreContent();
            legacyKeyStores.add(defaultKeyStoreContent);
        }

        final String metadataPrefix = SignatureKey.METADATA_PREFIX.getKey();
        if (properties.containsKey(KeyStoreConfigurator.prefixPostfixConfigurationKeys(metadataPrefix,"").getKeyStoreTypeConfigurationKey())) {
            final KeyStoreContent metadataKeyStoreContent = getKeyStoreConfigurator(properties, defaultPath, metadataPrefix).loadKeyStoreContent();
            legacyKeyStores.add(metadataKeyStoreContent);
        }
        return legacyKeyStores;
    }

    private static KeyStoreConfigurator.KeyStoreConfigurationKeys getNumberPrefixConfigurationKeys(int prefixCounter) {
        return KeyStoreConfigurator.prefixPostfixConfigurationKeys(prefixCounter + ".","");
    }
}
