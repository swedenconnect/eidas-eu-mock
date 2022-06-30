package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.DestroyFailedException;

import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.io.ResourceLocator;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;

/**
 * The KeyStore Configurator takes care of loading a keyStore from configured properties.
 * <p>
 * Configured passwords are used to decrypt the content into an instance of {@link KeyStoreContent}.
 *
 * @since 1.1
 */
public final class KeyStoreConfigurator {

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
        private final String keyAlias;

        @Nullable
        private final char[] keyPassword;

        public KeyStoreConfiguration(@Nonnull String keyStorePath,
                                     @Nullable String keyStoreType,
                                     @Nullable String keyStoreProvider,
                                     @Nullable char[] keyStorePassword,
                                     @Nullable String keyAlias,
                                     @Nullable char[] keyPassword) {
            Preconditions.checkNotBlank(keyStorePath, "keyStorePath");
            this.keyStorePath = keyStorePath;
            this.keyStoreType = keyStoreType;
            this.keyStoreProvider = keyStoreProvider;
            this.keyStorePassword = keyStorePassword;
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
        private final String keyAliasConfigurationKey;

        @Nonnull
        private final String keyPasswordConfigurationKey;

        public KeyStoreConfigurationKeys(@Nonnull String keyStorePathConfigurationKey,
                                         @Nonnull String keyStoreTypeConfigurationKey,
                                         @Nonnull String keyStoreProviderConfigurationKey,
                                         @Nonnull String keyStorePasswordConfigurationKey,
                                         @Nonnull String keyAliasConfigurationKey,
                                         @Nonnull String keyPasswordConfigurationKey) {

            Preconditions.checkNotBlank(keyStorePathConfigurationKey, "keyStorePathConfigurationKey");
            Preconditions.checkNotBlank(keyStoreTypeConfigurationKey, "keyStoreTypeConfigurationKey");
            Preconditions.checkNotBlank(keyStoreProviderConfigurationKey, "keyStoreProviderConfigurationKey");
            Preconditions.checkNotBlank(keyStorePasswordConfigurationKey, "keyStorePasswordConfigurationKey");
            Preconditions.checkNotBlank(keyAliasConfigurationKey, "keyAliasConfigurationKey");
            Preconditions.checkNotBlank(keyPasswordConfigurationKey, "keyPasswordConfigurationKey");
            this.keyStorePathConfigurationKey = keyStorePathConfigurationKey;
            this.keyStoreTypeConfigurationKey = keyStoreTypeConfigurationKey;
            this.keyStoreProviderConfigurationKey = keyStoreProviderConfigurationKey;
            this.keyStorePasswordConfigurationKey = keyStorePasswordConfigurationKey;
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
    }

    public static final KeyStoreConfigurationKeys DEFAULT_KEYSTORE_CONFIGURATION_KEYS =
            new KeyStoreConfigurationKeys(KeyStoreKey.KEYSTORE_PATH.getKey(), KeyStoreKey.KEYSTORE_TYPE.getKey(),
                                          KeyStoreKey.KEYSTORE_PROVIDER.getKey(),
                                          KeyStoreKey.KEYSTORE_PASSWORD.getKey(), KeyStoreKey.KEY_ALIAS.getKey(),
                                          KeyStoreKey.KEY_PASSWORD.getKey());

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

    public static ImmutableSet<X509Certificate> getCertificates(KeyStore keyStore)
            throws SamlEngineConfigurationException {
        try {
            ImmutableSet.Builder<X509Certificate> certificates = ImmutableSet.builder();
            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements(); ) {
                String alias = aliases.nextElement();
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                if (null != certificate) {
                    certificates.add(certificate);
                }
            }
            return certificates.build();
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    }

    @Nonnull
    public static KeyStoreConfiguration getKeyStoreConfiguration(@Nonnull Map<String, String> properties,
                                                                 @Nonnull KeyStoreConfigurationKeys configurationKeys,
                                                                 @Nullable String defaultPath)
            throws SamlEngineConfigurationException {
        Preconditions.checkNotNull(properties, "properties");
        Preconditions.checkNotNull(configurationKeys, "configurationKeys");

        String keyStorePath;
        if (StringUtils.isNotBlank(defaultPath)) {
            keyStorePath = defaultPath + properties.get(configurationKeys.getKeyStorePathConfigurationKey());
        } else {
            keyStorePath = properties.get(configurationKeys.getKeyStorePathConfigurationKey());
        }

        if (StringUtils.isBlank(keyStorePath)) {
            String msg = "Missing KeyStore configuration key \"" + configurationKeys.getKeyStorePathConfigurationKey()
                    + "\"";
            LOG.error(msg);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), msg);
        }
        String keyStoreType = properties.get(configurationKeys.getKeyStoreTypeConfigurationKey());
        String keyStoreProvider = properties.get(configurationKeys.getKeyStoreProviderConfigurationKey());
        String keyStorePasswordStr = properties.get(configurationKeys.getKeyStorePasswordConfigurationKey());
        char[] keyStorePassword = toChars(keyStorePasswordStr);
        String keyAlias = properties.get(configurationKeys.getKeyAliasConfigurationKey());
        String keyPasswordStr = properties.get(configurationKeys.getKeyPasswordConfigurationKey());
        char[] keyPassword = toChars(keyPasswordStr);
        return new KeyStoreConfiguration(keyStorePath, keyStoreType, keyStoreProvider, keyStorePassword, keyAlias,
                                         keyPassword);
    }

    public static KeyStoreContent getKeyStoreContent(KeyStore keyStore, char[] password)
            throws SamlEngineConfigurationException {
        try {
            ImmutableSet.Builder<KeyStore.PrivateKeyEntry> privateKeys = ImmutableSet.builder();
            ImmutableSet.Builder<X509Certificate> certificates = ImmutableSet.builder();
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
                        privateKeys.add(privateKeyEntry);
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
            ImmutableSet<KeyStore.PrivateKeyEntry> entries = privateKeys.build();
            if (entries.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            return new KeyStoreContent(entries, certificates.build());
        } catch (SamlEngineConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    }

    public static ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries(KeyStore keyStore, char[] password)
            throws SamlEngineConfigurationException {
        try {
            ImmutableSet.Builder<KeyStore.PrivateKeyEntry> keys = ImmutableSet.builder();
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
            ImmutableSet<KeyStore.PrivateKeyEntry> entries = keys.build();
            if (entries.isEmpty() && null != wrongPasswordException) {
                // If there is only keys with different passwords, the given password is probably incorrect:
                throw wrongPasswordException;
            }
            return entries;
        } catch (SamlEngineConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    }

    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore, String alias, char[] password)
            throws SamlEngineConfigurationException, UnrecoverableEntryException {
        try {
            if (keyStore.isKeyEntry(alias)) {
                return decryptPrivateKey(keyStore, alias, password);
            }
        } catch (UnrecoverableEntryException wrongPassword) {
            // The password does not match this alias
            throw wrongPassword;
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
        return null;
    }

    public static KeyStore.PrivateKeyEntry getPrivateKeyEntry(KeyStore keyStore,
                                                              String serialNumber,
                                                              String issuer,
                                                              char[] password) throws SamlEngineConfigurationException {
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
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
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

    @Nonnull
    private final KeyStoreConfiguration keyStoreConfiguration;

    public KeyStoreConfigurator(@Nonnull KeyStoreConfiguration keyStoreConfiguration) {
        Preconditions.checkNotNull(keyStoreConfiguration, "keyStoreConfiguration");
        this.keyStoreConfiguration = keyStoreConfiguration;
    }

    public KeyStoreConfigurator(@Nonnull Map<String, String> properties,
                                @Nullable String defaultPath) throws SamlEngineConfigurationException {
        this(getKeyStoreConfiguration(properties, DEFAULT_KEYSTORE_CONFIGURATION_KEYS, defaultPath));
    }

    public KeyStoreConfigurator(@Nonnull Map<String, String> properties,
                                @Nonnull KeyStoreConfigurationKeys configurationKeys,
                                @Nullable String defaultPath)
            throws SamlEngineConfigurationException {
        this(getKeyStoreConfiguration(properties, configurationKeys, defaultPath));
    }

    /**
     * Loads the KeyStore.
     */
    public KeyStore loadKeyStore() throws SamlEngineConfigurationException {
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
            URL resource;
            try {
                resource = ResourceLocator.getResource(keyStoreConfiguration.getKeyStorePath());
            } catch (IOException ioe) {
                throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                           EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(),
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
        } catch (SamlEngineConfigurationException e) {
            LOG.error("Unable to load keyStore: " + e, e);
            throw e;
        } catch (Exception e) {
            LOG.error("Unable to load keyStore: " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    }

    /**
     * Loads and decrypts the content of the configured KeyStore.
     *
     * @throws SamlEngineConfigurationException the configuration exception
     */
    public KeyStoreContent loadKeyStoreContent() throws SamlEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getKeyStoreContent(keyStore, keyStoreConfiguration.getKeyPassword());
    }

    /**
     * Loads and decrypts the content of the configured KeyStore and returns the key matching the configured alias.
     *
     * @throws SamlEngineConfigurationException the configuration exception
     */
    public KeyStore.PrivateKeyEntry loadPrivateKeyAlias() throws SamlEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        try {
            return getPrivateKeyEntry(keyStore, keyStoreConfiguration.getKeyAlias(),
                                      keyStoreConfiguration.getKeyPassword());
        } catch (UnrecoverableEntryException e) {
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_INVALID_KEYSTORE.errorMessage(), e);
        }
    }

    /**
     * Loads and decrypts the content of the configured KeyStore and returns all the keys matching the configured
     * password.
     *
     * @throws SamlEngineConfigurationException the configuration exception
     */
    public ImmutableSet<KeyStore.PrivateKeyEntry> loadPrivateKeyEntries() throws SamlEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getPrivateKeyEntries(keyStore, keyStoreConfiguration.getKeyPassword());
    }

    /**
     * Loads and decrypts the content of the configured KeyStore and returns the key matching the given serialNumber and
     * issuer.
     *
     * @throws SamlEngineConfigurationException the configuration exception
     */
    public KeyStore.PrivateKeyEntry loadPrivateKeyEntry(String serialNumber, String issuer)
            throws SamlEngineConfigurationException {
        KeyStore keyStore = loadKeyStore();
        return getPrivateKeyEntry(keyStore, serialNumber, issuer, keyStoreConfiguration.getKeyPassword());
    }
}
