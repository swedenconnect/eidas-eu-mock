package eu.eidas.auth.engine.configuration.dom;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;

/**
 * The decrypted content of a KeyStore
 *
 * @since 1.1
 */
public final class KeyStoreContent {

    @Nonnull
    public static X509Certificate getMatchingCertificate(@Nonnull ImmutableSet<X509Certificate> certificates,
                                                         String serialNumber,
                                                         String issuer) throws SamlEngineConfigurationException {
        for (final X509Certificate certificate : certificates) {
            if (CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                return certificate;
            }
        }
        throw new SamlEngineConfigurationException(
                "No certificate matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }

    @Nonnull
    public static KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(
            @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries, String serialNumber, String issuer)
            throws SamlEngineConfigurationException {
        for (final KeyStore.PrivateKeyEntry privateKeyEntry : privateKeyEntries) {
            X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
            if (CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                return privateKeyEntry;
            }
        }
        throw new SamlEngineConfigurationException(
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }

    @Nonnull
    private final ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries;

    @Nonnull
    private final ImmutableSet<X509Certificate> certificates;

    public KeyStoreContent(@Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries,
                           @Nonnull ImmutableSet<X509Certificate> certificates) {
        Preconditions.checkNotNull(privateKeyEntries, "privateKeyEntries");
        Preconditions.checkNotNull(certificates, "certificates");
        this.privateKeyEntries = privateKeyEntries;
        this.certificates = certificates;
    }

    @Nonnull
    public ImmutableSet<X509Certificate> getCertificates() {
        return certificates;
    }

    @Nonnull
    public X509Certificate getMatchingCertificate(String serialNumber, String issuer)
            throws SamlEngineConfigurationException {
        return getMatchingCertificate(certificates, serialNumber, issuer);
    }

    @Nonnull
    public KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(String serialNumber, String issuer)
            throws SamlEngineConfigurationException {
        return getMatchingPrivateKeyEntry(privateKeyEntries, serialNumber, issuer);
    }

    @Nonnull
    public ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries() {
        return privateKeyEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KeyStoreContent that = (KeyStoreContent) o;

        if (privateKeyEntries != null ? !privateKeyEntries.equals(that.privateKeyEntries)
                                      : that.privateKeyEntries != null) {
            return false;
        }
        return certificates != null ? certificates.equals(that.certificates) : that.certificates == null;
    }

    @Override
    public int hashCode() {
        int result = privateKeyEntries != null ? privateKeyEntries.hashCode() : 0;
        result = 31 * result + (certificates != null ? certificates.hashCode() : 0);
        return result;
    }
}
