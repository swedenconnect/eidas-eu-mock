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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.io.ResourceLocator;
import eu.eidas.auth.engine.configuration.KeyStoreType;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.DestroyFailedException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The KeyStore Configurator takes care of loading a keyStore from configured properties.
 * <p>
 * Configured passwords are used to decrypt the content into an instance of {@link KeyStoreContent}.
 */
public final class KeyStoreConfigurator {

    @Nonnull
    private final KeyStoreConfiguration keyStoreConfiguration;

    public static final KeyStoreConfigurationKeys DEFAULT_KEYSTORE_CONFIGURATION_KEYS =
            new KeyStoreConfigurationKeys(
                    KeyStoreKey.KEYSTORE_PATH.getKey(),
                    KeyStoreKey.KEYSTORE_TYPE.getKey(),
                    KeyStoreKey.KEYSTORE_PROVIDER.getKey(),
                    KeyStoreKey.KEYSTORE_PASSWORD.getKey(),
                    KeyStoreKey.KEYSTORE_PURPOSE.getKey(),
                    KeyStoreKey.KEY_ALIAS.getKey(),
                    KeyStoreKey.KEY_PASSWORD.getKey()
            );

    public static KeyStoreConfigurationKeys prefixPostfixConfigurationKeys(String prefix, String postfix) {
        final KeyStoreConfigurationKeys defaultKeys =  DEFAULT_KEYSTORE_CONFIGURATION_KEYS;
        return new KeyStoreConfigurationKeys(
                prefix + defaultKeys.keyStorePathConfigurationKey + postfix,
                prefix + defaultKeys.keyStoreTypeConfigurationKey + postfix,
                prefix + defaultKeys.keyStoreProviderConfigurationKey + postfix,
                prefix + defaultKeys.keyStorePasswordConfigurationKey + postfix,
                prefix + defaultKeys.keyStorePurposeConfigurationKey + postfix,
                prefix + defaultKeys.keyAliasConfigurationKey + postfix,
                prefix + defaultKeys.keyPasswordConfigurationKey + postfix
        );
    }

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreConfigurator.class);

    private static KeyStore.PrivateKeyEntry decryptPrivateKey(KeyStore keyStore, String alias, char[] password)
            throws NoSuchAlgorithmException, UnrecoverableEntryException, KeyStoreException, DestroyFailedException {
        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(password);
        try {
            KeyStore.Entry entry = keyStore.getEntry(alias, passwordProtection);
            // the entry can also be a symmetric key (without a certificate)
            if (entry instanceof KeyStore.PrivateKeyEntry) {
                return (KeyStore.PrivateKeyEntry) entry;
            }
        } finally {
            passwordProtection.destroy();
        }
        return null;
    }

    public static Set<X509Certificate> getCertificates(KeyStore keyStore)
            throws ProtocolEngineConfigurationException {
        try {
            Set<X509Certificate> certificates = new HashSet<>();
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                if (null != certificate) {
                    certificates.add(certificate);
                }
            }
            return Collections.unmodifiableSet(certificates);
        } catch (KeyStoreException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE, "", e);
        }
    }

    @Nonnull
    public static KeyStoreConfiguration getKeyStoreConfiguration(@Nonnull Map<String, String> properties,
                                                                 @Nonnull KeyStoreConfigurationKeys configurationKeys,
                                                                 @Nullable String defaultPath)
            throws ProtocolEngineConfigurationException {
        Preconditions.checkNotNull(properties, "properties");
        Preconditions.checkNotNull(configurationKeys, "configurationKeys");

        final String keyStoreType = properties.get(configurationKeys.getKeyStoreTypeConfigurationKey());
        String keyStorePath = properties.get(configurationKeys.getKeyStorePathConfigurationKey());
        onlyAcceptMissingKeystorePathForPKCS11(configurationKeys, keyStoreType, keyStorePath);

        if (StringUtils.isNotBlank(defaultPath)) {
            keyStorePath = defaultPath + keyStorePath;
        }

        final String keyStoreProvider = properties.get(configurationKeys.getKeyStoreProviderConfigurationKey());
        final String keyStorePasswordStr = properties.get(configurationKeys.getKeyStorePasswordConfigurationKey());
        final char[] keyStorePassword = toChars(keyStorePasswordStr);
        final String keyStorePurpose = properties.get(configurationKeys.getKeyStorePurposeConfigurationKey());
        final String keyAlias = properties.get(configurationKeys.getKeyAliasConfigurationKey());
        final String keyPasswordStr = properties.get(configurationKeys.getKeyPasswordConfigurationKey());
        final char[] keyPassword = toChars(keyPasswordStr);
        return new KeyStoreConfiguration(
                keyStorePath,
                keyStoreType,
                keyStoreProvider,
                keyStorePassword,
                keyStorePurpose,
                keyAlias,
                keyPassword
        );
    }

    private static void onlyAcceptMissingKeystorePathForPKCS11(KeyStoreConfigurationKeys configurationKeys, String keyStoreType, String keyStorePath) throws ProtocolEngineConfigurationException {
        if (!KeyStoreType.PKCS11.isEqualTo(keyStoreType) && StringUtils.isBlank(keyStorePath)) {
            String additionalInformation = "Missing KeyStore configuration key \"" +
                    configurationKeys.getKeyStorePathConfigurationKey() + "\"";
            LOG.error(additionalInformation);
            throw new ProtocolEngineConfigurationException(
                    EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    additionalInformation);
        }
    }

    public static KeyStoreContent getKeyStoreContent(KeyStore keyStore, char[] password, String purpose)
            throws ProtocolEngineConfigurationException {
        try {
            Set<KeyContainerEntry> keyContainerEntries = new HashSet<>();
            Set<X509Certificate> certificates = new HashSet<>();
            // if the keyStore contains other keys with different passwords, ignore them:
            UnrecoverableEntryException wrongPasswordException = null;
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    KeyStore.PrivateKeyEntry privateKeyEntry = null;
                    try {
                        privateKeyEntry = getPrivateKeyEntry(keyStore, alias, password);
                    } catch (UnrecoverableEntryException wrongPassword) {
                        wrongPasswordException = wrongPassword;
                    }
                    if (null != privateKeyEntry) {
                        keyContainerEntries.add(new KeyContainerEntry(privateKeyEntry, keyStore.getProvider().getName()));
                        certificates.add((X509Certificate) privateKeyEntry.getCertificate());
                        for (final Certificate certificate : privateKeyEntry.getCertificateChain()) {
                            certificates.add((X509Certificate) certificate);
                        }
                    }
                } else {
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (null != certificate) {
                        certificates.add(certificate);
                    }
                    Certificate[] certificateChain = keyStore.getCertificateChain(alias);
                    if (null != certificateChain) {
                        for (final Certificate cert : certificateChain) {
                            certificates.add((X509Certificate) cert);
                        }
                    }
                }
            }
            if (keyContainerEntries.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            return new KeyStoreContent(keyContainerEntries, Collections.unmodifiableSet(certificates), purpose);
        } catch (ProtocolEngineConfigurationException e) {
            throw e;
        } catch (KeyStoreException | UnrecoverableEntryException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    "Failed to load the keystore", e);
        }
    }

    public static Set<KeyStore.PrivateKeyEntry> getPrivateKeyEntries(KeyStore keyStore, char[] password)
            throws ProtocolEngineConfigurationException {
        try {
            Set<KeyStore.PrivateKeyEntry> keys = new HashSet<>();
            // if the keyStore contains other keys with different passwords, ignore them:
            UnrecoverableEntryException wrongPasswordException = null;
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                KeyStore.PrivateKeyEntry privateKeyEntry = null;
                try {
                    privateKeyEntry = getPrivateKeyEntry(keyStore, alias, password);
                } catch (UnrecoverableEntryException wrongPassword) {
                    wrongPasswordException = wrongPassword;
                }
                if (null != privateKeyEntry) {
                    keys.add(privateKeyEntry);
                }
            }
            if (keys.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            return Collections.unmodifiableSet(keys);
        } catch (ProtocolEngineConfigurationException e) {
            throw e;
        } catch (KeyStoreException | UnrecoverableEntryException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    "Failed to load the keystore", e);
        }
    }

    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore, String alias, char[] password)
            throws ProtocolEngineConfigurationException, UnrecoverableEntryException {
        try {
            if (keyStore.isKeyEntry(alias)) {
                return decryptPrivateKey(keyStore, alias, password);
            }
        } catch (UnrecoverableEntryException wrongPassword) {
            // The password does not match this alias
            throw wrongPassword;
        } catch (KeyStoreException | DestroyFailedException | NoSuchAlgorithmException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    "Failed to load the keystore ", e);
        }
        return null;
    }

    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore,
                                                              String serialNumber,
                                                              String issuer,
                                                              char[] password) throws ProtocolEngineConfigurationException {
        try {
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                if (keyStore.isKeyEntry(alias)) {
                    X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                    if (null == certificate || !CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                        continue;
                    }
                    KeyStore.PrivateKeyEntry entry = decryptPrivateKey(keyStore, alias, password);
                    if (null != entry) {
                        return entry;
                    }
                }
            }
            return null;
        } catch (KeyStoreException | NoSuchAlgorithmException | DestroyFailedException | UnrecoverableEntryException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE,
                    "Failed to load the keystore", e);
        }
    }

    @Nullable
    public static char[] toChars(@Nullable String passwordString) {
        char[] password;
        if (null == passwordString) {
            // The password must be null for PKCS11 keyStores
            password = null;
        } else {
            password = passwordString.toCharArray();
        }
        return password;
    }


    public KeyStoreConfigurator(@Nonnull KeyStoreConfiguration keyStoreConfiguration) {
        Preconditions.checkNotNull(keyStoreConfiguration, "keyStoreConfiguration");
        this.keyStoreConfiguration = keyStoreConfiguration;
    }

    public KeyStoreConfigurator(@Nonnull Map<String, String> properties,
                                @Nullable String defaultPath) throws ProtocolEngineConfigurationException {
        this(getKeyStoreConfiguration(properties, DEFAULT_KEYSTORE_CONFIGURATION_KEYS, defaultPath));
    }

    public KeyStoreConfigurator(@Nonnull Map<String, String> properties,
                                @Nonnull KeyStoreConfigurationKeys configurationKeys,
                                @Nullable String defaultPath) throws ProtocolEngineConfigurationException {
        this(getKeyStoreConfiguration(properties, configurationKeys, defaultPath));
    }

    /**
     * Loads the KeyStore.
     * @return the keyStore
     * @throws ProtocolEngineConfigurationException the configuration exception
     */
    public KeyStore loadKeyStore() throws ProtocolEngineConfigurationException {
        boolean traceEnabled = LOG.isTraceEnabled();
        String keyStoreType = keyStoreConfiguration.getKeyStoreType();
        if (null == keyStoreType) {
            keyStoreType = KeyStore.getDefaultType();
            if (traceEnabled) {
                LOG.trace("No keyStoreType configured for keyStore file \"" + keyStoreConfiguration.getKeyStorePath()
                                  + "\" using KeyStore.getDefaultType(): \"" + keyStoreType + "\"");
            }
        }
        try {
            if (traceEnabled) {
                LOG.trace("Loading keyStore file \"" + keyStoreConfiguration.getKeyStorePath() + "\", keyStoreType \""
                                  + keyStoreType + "\", keyStoreProvider \""
                                  + keyStoreConfiguration.getKeyStoreProvider() + "\"");
            }
            KeyStore keyStore;
            if (StringUtils.isBlank(keyStoreConfiguration.getKeyStoreProvider())) {
                keyStore = KeyStore.getInstance(keyStoreType);
            } else {
                keyStore = KeyStore.getInstance(keyStoreType, keyStoreConfiguration.getKeyStoreProvider());
            }

            if (KeyStoreType.PKCS11.isEqualTo(keyStoreType)) {
                keyStore.load(null, keyStoreConfiguration.getKeyStorePassword());
                return keyStore;
            }

            URL resource;
            try {
                resource = ResourceLocator.getResource(keyStoreConfiguration.getKeyStorePath());
            } catch (IOException ioe) {
                throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE, "",
                                                           ioe);
            }
            if (traceEnabled) {
                LOG.trace("Found keyStore file \"" + keyStoreConfiguration.getKeyStorePath() + "\", keyStoreType \""
                                  + keyStoreType + "\", keyStoreProvider \""
                                  + keyStoreConfiguration.getKeyStoreProvider() + "\" at \"" + resource.toExternalForm()
                                  + "\"");
            }
            try (InputStream fis = resource.openStream()) {
                keyStore.load(fis, keyStoreConfiguration.getKeyStorePassword());
                if (traceEnabled) {
                    LOG.trace(
                            "Loaded keyStore file \"" + keyStoreConfiguration.getKeyStorePath() + "\", keyStoreType \""
                                    + keyStoreType + "\", keyStoreProvider \""
                                    + keyStoreConfiguration.getKeyStoreProvider() + "\" from \""
                                    + resource.toExternalForm() + "\"");
                }
                return keyStore;
            }
        } catch (ProtocolEngineConfigurationException e) {
            LOG.error("Unable to load keyStore: " + e, e);
            throw e;
        } catch (KeyStoreException | NoSuchProviderException | IOException | NoSuchAlgorithmException | CertificateException e) {
            LOG.error("Unable to load keyStore: " + e, e);
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE, "", e);
        }
    }

    /**
     * Loads and decrypts the content of the configured KeyStore.
     * @return the content of the configured KeyStore
     * @throws ProtocolEngineConfigurationException the configuration exception
     */
    public KeyStoreContent loadKeyStoreContent() throws ProtocolEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getKeyStoreContent(keyStore, keyStoreConfiguration.getKeyPassword(), keyStoreConfiguration.getKeyStorePurpose());
    }



    /**
     * Loads and decrypts the content of the configured KeyStore and returns the key matching the configured alias.
     * @return the key matching the configured alias
     * @throws ProtocolEngineConfigurationException the configuration exception
     */
    public KeyStore.PrivateKeyEntry loadPrivateKeyAlias() throws ProtocolEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        try {
            return getPrivateKeyEntry(keyStore, keyStoreConfiguration.getKeyAlias(),
                                      keyStoreConfiguration.getKeyPassword());
        } catch (UnrecoverableEntryException e) {
            throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE, "", e);
        }
    }

    /**
     * Loads and decrypts the content of the configured KeyStore and returns all the keys matching the configured
     * password.
     * @return the keys matching the configured password
     * @throws ProtocolEngineConfigurationException the configuration exception
     */
    public Set<KeyStore.PrivateKeyEntry> loadPrivateKeyEntries() throws ProtocolEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getPrivateKeyEntries(keyStore, keyStoreConfiguration.getKeyPassword());
    }

    /**
     * Loads and decrypts the content of the configured KeyStore and returns the key matching the given serialNumber and
     * issuer.
     * @param serialNumber a {@link String}
     * @param issuer a {@link String}
     * @return the key matching the given serialNumber and issuer
     * @throws ProtocolEngineConfigurationException the configuration exception
     */
    public KeyStore.PrivateKeyEntry loadPrivateKeyEntry(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getPrivateKeyEntry(keyStore, serialNumber, issuer, keyStoreConfiguration.getKeyPassword());
    }

    public static final class KeyStoreConfiguration {

        @Nonnull
        private final String keyStorePath;

        @Nullable
        private final String keyStoreType;

        @Nullable
        private final String keyStoreProvider;

        @Nullable
        private final char[] keyStorePassword;

        @Nullable
        private final String keyStorePurpose;

        @Nullable
        private final String keyAlias;

        @Nullable
        private final char[] keyPassword;

        public KeyStoreConfiguration(@Nonnull String keyStorePath,
                                     @Nullable String keyStoreType,
                                     @Nullable String keyStoreProvider,
                                     @Nullable char[] keyStorePassword,
                                     @Nullable String keyStorePurpose,
                                     @Nullable String keyAlias,
                                     @Nullable char[] keyPassword) {
            this.keyStorePath = keyStorePath;
            this.keyStoreType = keyStoreType;
            this.keyStoreProvider = keyStoreProvider;
            this.keyStorePassword = keyStorePassword;
            this.keyStorePurpose = keyStorePurpose;
            this.keyAlias = keyAlias;
            this.keyPassword = keyPassword;
        }

        public String getKeyAlias() {
            return keyAlias;
        }

        public char[] getKeyPassword() {
            return keyPassword;
        }

        public char[] getKeyStorePassword() {
            return keyStorePassword;
        }

        @Nonnull
        public String getKeyStorePath() {
            return keyStorePath;
        }

        @Nullable
        public String getKeyStorePurpose() {
            return keyStorePurpose;
        }

        public String getKeyStoreProvider() {
            return keyStoreProvider;
        }

        public String getKeyStoreType() {
            return keyStoreType;
        }
    }

    public static final class KeyStoreConfigurationKeys {

        @Nonnull
        private final String keyStorePathConfigurationKey;

        @Nonnull
        private final String keyStoreTypeConfigurationKey;

        @Nonnull
        private final String keyStoreProviderConfigurationKey;

        @Nonnull
        private final String keyStorePasswordConfigurationKey;

        @Nonnull
        private final String keyStorePurposeConfigurationKey;

        @Nonnull
        private final String keyAliasConfigurationKey;

        @Nonnull
        private final String keyPasswordConfigurationKey;

        public KeyStoreConfigurationKeys(@Nonnull String keyStorePathConfigurationKey,
                                         @Nonnull String keyStoreTypeConfigurationKey,
                                         @Nonnull String keyStoreProviderConfigurationKey,
                                         @Nonnull String keyStorePasswordConfigurationKey,
                                         @Nonnull String keyStorePurposeConfigurationKey,
                                         @Nonnull String keyAliasConfigurationKey,
                                         @Nonnull String keyPasswordConfigurationKey) {

            Preconditions.checkNotBlank(keyStorePathConfigurationKey, "keyStorePathConfigurationKey");
            Preconditions.checkNotBlank(keyStoreTypeConfigurationKey, "keyStoreTypeConfigurationKey");
            Preconditions.checkNotBlank(keyStoreProviderConfigurationKey, "keyStoreProviderConfigurationKey");
            Preconditions.checkNotBlank(keyStorePasswordConfigurationKey, "keyStorePasswordConfigurationKey");
            Preconditions.checkNotBlank(keyStorePurposeConfigurationKey, "keyStorePurposeConfigurationKey");
            Preconditions.checkNotBlank(keyAliasConfigurationKey, "keyAliasConfigurationKey");
            Preconditions.checkNotBlank(keyPasswordConfigurationKey, "keyPasswordConfigurationKey");
            this.keyStorePathConfigurationKey = keyStorePathConfigurationKey;
            this.keyStoreTypeConfigurationKey = keyStoreTypeConfigurationKey;
            this.keyStoreProviderConfigurationKey = keyStoreProviderConfigurationKey;
            this.keyStorePasswordConfigurationKey = keyStorePasswordConfigurationKey;
            this.keyStorePurposeConfigurationKey = keyStorePurposeConfigurationKey;
            this.keyAliasConfigurationKey = keyAliasConfigurationKey;
            this.keyPasswordConfigurationKey = keyPasswordConfigurationKey;
        }

        @Nonnull
        public String getKeyAliasConfigurationKey() {
            return keyAliasConfigurationKey;
        }

        @Nonnull
        public String getKeyPasswordConfigurationKey() {
            return keyPasswordConfigurationKey;
        }

        @Nonnull
        public String getKeyStorePasswordConfigurationKey() {
            return keyStorePasswordConfigurationKey;
        }

        @Nonnull
        public String getKeyStorePathConfigurationKey() {
            return keyStorePathConfigurationKey;
        }

        @Nonnull
        public String getKeyStoreProviderConfigurationKey() {
            return keyStoreProviderConfigurationKey;
        }

        @Nonnull
        public String getKeyStoreTypeConfigurationKey() {
            return keyStoreTypeConfigurationKey;
        }

        @Nonnull
        public String getKeyStorePurposeConfigurationKey() {
            return keyStorePurposeConfigurationKey;
        }
    }
}
