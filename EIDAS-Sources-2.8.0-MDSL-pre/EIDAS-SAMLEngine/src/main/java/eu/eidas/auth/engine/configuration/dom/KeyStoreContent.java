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

import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;

/**
 * The decrypted content of a KeyStore
 *
 * @since 1.1
 */
public final class KeyStoreContent implements KeyContainer {

    @Nonnull
    private final ImmutableSet<KeyContainerEntry> keyContainerEntries;

    @Nonnull
    private final ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries;

    @Nonnull
    private final ImmutableSet<X509Certificate> certificates;

    @Nonnull
    private final KeystorePurpose keystorePurpose;

    @Deprecated
    public KeyStoreContent(@Nonnull Collection<KeyStore.PrivateKeyEntry> privateKeyEntries,
                           @Nonnull ImmutableSet<X509Certificate> certificates,
                           @Nonnull String keystorePurpose) {
        Preconditions.checkNotNull(privateKeyEntries, "privateKeyEntries");
        Preconditions.checkNotNull(certificates, "certificates");

        this.keyContainerEntries = privateKeyEntries.stream()
                .map(KeyContainerEntry::new)
                .collect(ImmutableSet.toImmutableSet());
        this.privateKeyEntries = privateKeyEntries.stream().collect(ImmutableSet.toImmutableSet());
        this.certificates = certificates;
        this.keystorePurpose = KeystorePurpose.fromString(keystorePurpose);
    }

    public KeyStoreContent(@Nonnull ImmutableSet<KeyContainerEntry> keyContainerEntries,
                           @Nonnull ImmutableSet<X509Certificate> certificates,
                           @Nonnull String keystorePurpose) {
        Preconditions.checkNotNull(keyContainerEntries, "keyContainerEntries");
        Preconditions.checkNotNull(certificates, "certificates");

        this.keyContainerEntries = keyContainerEntries;
        this.privateKeyEntries = keyContainerEntries.stream()
                .map(KeyContainerEntry::getPrivateKeyEntry)
                .collect(ImmutableSet.toImmutableSet());
        this.certificates = certificates;
        this.keystorePurpose = KeystorePurpose.fromString(keystorePurpose);
    }

    @Nonnull
    public static KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(
            @Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries, String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        for (final KeyStore.PrivateKeyEntry privateKeyEntry : privateKeyEntries) {
            X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
            if (CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                return privateKeyEntry;
            }
        }
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }

    @Nonnull
    public KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        return getMatchingPrivateKeyEntry(privateKeyEntries, serialNumber, issuer);
    }

    @Nonnull
    public KeyContainerEntry getMatchingKeyEntry(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        for (final KeyContainerEntry containerEntry : keyContainerEntries) {
            X509Certificate certificate = (X509Certificate) containerEntry.getPrivateKeyEntry().getCertificate();
            if (CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                return containerEntry;
            }
        }
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }

    @Nonnull
    public ImmutableSet<KeyStore.PrivateKeyEntry> getPrivateKeyEntries() {
        return privateKeyEntries;
    }

    @Nonnull
    public X509Certificate getMatchingCertificate(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        return getMatchingCertificate(certificates, serialNumber, issuer);
    }

    @Nonnull
    public ImmutableSet<X509Certificate> getCertificates() {
        return certificates;
    }

    @Nonnull
    public static X509Certificate getMatchingCertificate(@Nonnull ImmutableSet<X509Certificate> certificates,
                                                         String serialNumber,
                                                         String issuer) throws ProtocolEngineConfigurationException {
        for (final X509Certificate certificate : certificates) {
            if (CertificateUtil.matchesCertificate(serialNumber, issuer, certificate)) {
                return certificate;
            }
        }
        throw new ProtocolEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR,
                "No certificate matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }

    @Nonnull
    public KeystorePurpose getKeystorePurpose() {
        return keystorePurpose;
    }

    @Nonnull
    public boolean hasPurpose(KeystorePurpose purpose) {
        return keystorePurpose.equals(purpose);
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

        if (keyContainerEntries != null ? !keyContainerEntries.equals(that.keyContainerEntries)
                                      : that.keyContainerEntries != null) {
            return false;
        }
        return certificates != null ? certificates.equals(that.certificates) : that.certificates == null;
    }

    @Override
    public int hashCode() {
        int result = keyContainerEntries != null ? keyContainerEntries.hashCode() : 0;
        result = 31 * result + (certificates != null ? certificates.hashCode() : 0);
        return result;
    }

    public enum KeystorePurpose {
        KEYSTORE, // default
        TRUSTSTORE
        ;

        public static KeystorePurpose fromString(String input) {
            return Stream.of(KeystorePurpose.values())
                    .filter(purpose -> purpose.name().equalsIgnoreCase(input))
                    .findAny()
                    .orElse(KEYSTORE);
        }
    }
}
