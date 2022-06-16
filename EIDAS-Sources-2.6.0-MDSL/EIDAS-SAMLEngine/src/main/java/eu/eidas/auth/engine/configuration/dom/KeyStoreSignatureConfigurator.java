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
package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.eidas.spec.EidasSignatureConstants;
import eu.eidas.auth.engine.core.impl.CertificateValidator;
import eu.eidas.auth.engine.core.impl.WhiteListConfigurator;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 * KeyStore-based SignatureConfigurator.
 *
 * @since 1.1
 */
public final class KeyStoreSignatureConfigurator {

    private static final String PROPERTY_PREFIX_DEFAULT = "";

    private KeyStore.PrivateKeyEntry getPrivateSigningKeyAndCertificate(Map<String, String> properties,
                                                                        String propertyPrefix,
                                                                        String defaultSerialNumber,
                                                                        String defaultIssuer,
                                                                        @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        String propPrefix = PROPERTY_PREFIX_DEFAULT;
        if (StringUtils.isNotEmpty(propertyPrefix)) {
            propPrefix = propertyPrefix;
        }
        String serialNumber = properties.get(propPrefix + SignatureKey.SERIAL_NUMBER.getKey());
        if (StringUtils.isBlank(serialNumber)) {
            serialNumber = defaultSerialNumber;
        }
        String issuer = properties.get(propPrefix + SignatureKey.ISSUER.getKey());
        if (StringUtils.isBlank(issuer)) {
            issuer = defaultIssuer;
        }

        KeyStoreConfigurator keyStoreConfigurator = getKeyStoreConfigurator(properties, defaultPath, propPrefix);

        return keyStoreConfigurator.loadPrivateKeyEntry(serialNumber, issuer);
    }

    private ImmutableSet<X509Certificate> getCertificates(Map<String, String> properties,
                                                          String propertyPrefix,
                                                          @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {

        String propPrefix = PROPERTY_PREFIX_DEFAULT;
        if (StringUtils.isNotEmpty(propertyPrefix)) {
            propPrefix = propertyPrefix;
        }

        KeyStoreConfigurator keyStoreConfigurator = getKeyStoreConfigurator(properties, defaultPath, propPrefix);

        return keyStoreConfigurator.loadKeyStoreContent().getCertificates();
    }

    private KeyStoreConfigurator getKeyStoreConfigurator(Map<String, String> properties, @Nullable String defaultPath, String propPrefix) throws ProtocolEngineConfigurationException {
        String keyStorePathConfigurationKey = propPrefix + KeyStoreKey.KEYSTORE_PATH.getKey();
        if (!properties.containsKey(keyStorePathConfigurationKey)) {
            keyStorePathConfigurationKey = KeyStoreKey.KEYSTORE_PATH.getKey();
        }

        String keyStoreTypeConfigurationKey = propPrefix + KeyStoreKey.KEYSTORE_TYPE.getKey();
        if (!properties.containsKey(keyStoreTypeConfigurationKey)) {
            keyStoreTypeConfigurationKey = KeyStoreKey.KEYSTORE_TYPE.getKey();
        }

        String keyStoreProviderConfigurationKey = propPrefix + KeyStoreKey.KEYSTORE_PROVIDER.getKey();
        if (!properties.containsKey(keyStoreProviderConfigurationKey)) {
            keyStoreProviderConfigurationKey = KeyStoreKey.KEYSTORE_PROVIDER.getKey();
        }

        String keyStorePasswordConfigurationKey = propPrefix + KeyStoreKey.KEYSTORE_PASSWORD.getKey();
        if (!properties.containsKey(keyStorePasswordConfigurationKey)) {
            keyStorePasswordConfigurationKey = KeyStoreKey.KEYSTORE_PASSWORD.getKey();
        }

        String keyAliasConfigurationKey = propPrefix + KeyStoreKey.KEY_ALIAS.getKey();
        if (!properties.containsKey(keyAliasConfigurationKey)) {
            keyAliasConfigurationKey = KeyStoreKey.KEY_ALIAS.getKey();
        }

        String keyPasswordConfigurationKey = propPrefix + KeyStoreKey.KEY_PASSWORD.getKey();
        if (!properties.containsKey(keyPasswordConfigurationKey)) {
            keyPasswordConfigurationKey = KeyStoreKey.KEY_PASSWORD.getKey();
        }

        KeyStoreConfigurator.KeyStoreConfigurationKeys keyStoreConfigurationKeys =
                new KeyStoreConfigurator.KeyStoreConfigurationKeys(keyStorePathConfigurationKey,
                                                                   keyStoreTypeConfigurationKey,
                                                                   keyStoreProviderConfigurationKey,
                                                                   keyStorePasswordConfigurationKey,
                                                                   keyAliasConfigurationKey,
                                                                   keyPasswordConfigurationKey);

        return new KeyStoreConfigurator(properties, keyStoreConfigurationKeys, defaultPath);
    }

    public SignatureConfiguration getSignatureConfiguration(Map<String, String> properties, @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        boolean checkedValidityPeriod = CertificateValidator.isCheckedValidityPeriod(properties);
        boolean disallowedSelfSignedCertificate = CertificateValidator.isDisallowedSelfSignedCertificate(properties);
        boolean responseSignAssertions = Boolean.parseBoolean(
                StringUtils.trim(SignatureKey.RESPONSE_SIGN_ASSERTIONS.getAsString(properties)));
        boolean requestSignWithKey = Boolean.parseBoolean(
                StringUtils.trim(SignatureKey.REQUEST_SIGN_WITH_KEY_VALUE.getAsString(properties)));
        boolean responseSignWithKey = Boolean.parseBoolean(
                StringUtils.trim(SignatureKey.RESPONSE_SIGN_WITH_KEY_VALUE.getAsString(properties)));

        String serialNumber = SignatureKey.SERIAL_NUMBER.getAsString(properties);
        String issuer = SignatureKey.ISSUER.getAsString(properties);
        KeyStoreContent keyStoreContent = new KeyStoreConfigurator(properties, defaultPath).loadKeyStoreContent();
        KeyStore.PrivateKeyEntry signatureKeyAndCertificate =
                keyStoreContent.getMatchingPrivateKeyEntry(serialNumber, issuer);
        ImmutableSet<X509Certificate> trustedCertificates = keyStoreContent.getCertificates();
        String signatureAlgorithmWhiteListStr = SignatureKey.SIGNATURE_ALGORITHM_WHITE_LIST.getAsString(properties);
        ImmutableSet<String> allowedSignatureAlgorithmWhitelist = WhiteListConfigurator.getAllowedAlgorithms(
                EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST,
                EidasSignatureConstants.DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST,
                signatureAlgorithmWhiteListStr);
        signatureAlgorithmWhiteListStr = String.join(EIDASValues.SEMICOLON.toString(), allowedSignatureAlgorithmWhitelist);
        String signatureAlgorithm = SignatureKey.SIGNATURE_ALGORITHM.getAsString(properties);
        String metadataSignatureAlgorithm = SignatureKey.METADATA_SIGNATURE_ALGORITHM.getAsString(properties);

        String digestMethodAlgorithmWhiteListStr = SignatureKey.DIGEST_METHOD_ALGORITHM_WHITELIST.getAsString(properties);
        ImmutableSet<String> digestMethodAlgorithmWhiteList = WhiteListConfigurator
                .getAllowedAlgorithms(EidasSignatureConstants.DEFAULT_DIGEST_ALGORITHM_WHITE_LIST, digestMethodAlgorithmWhiteListStr);
        String digestMethodAlgorithm = SignatureKey.DIGEST_METHOD_ALGORITHM.getAsString(properties);
        KeyStore.PrivateKeyEntry metadataSigningKeyAndCertificate =
                getPrivateSigningKeyAndCertificate(properties, SignatureKey.METADATA_PREFIX.getKey(), serialNumber,
                                                   issuer, defaultPath);

        ImmutableSet<X509Certificate> metadataKeystoreCertificates =
                getCertificates(properties, SignatureKey.METADATA_PREFIX.getKey(),
                        defaultPath);

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
}
