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
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The decrypted content of a KeyStore
 *
 * @since 1.1
 */
public final class KeyStoreContent implements KeyContainer {

    @Nonnull
    private final Set<KeyContainerEntry> keyContainerEntries;

    @Nonnull
    private final Set<KeyStore.PrivateKeyEntry> privateKeyEntries;

    @Nonnull
    private final Set<X509Certificate> certificates;

    @Nonnull
    private final KeystorePurpose keystorePurpose;

    public KeyStoreContent(@Nonnull Set<KeyContainerEntry> keyContainerEntries,
                           @Nonnull Set<X509Certificate> certificates,
                           @Nonnull String keystorePurpose) {
        Preconditions.checkNotNull(keyContainerEntries, "keyContainerEntries");
        Preconditions.checkNotNull(certificates, "certificates");

        this.keyContainerEntries = keyContainerEntries;
        this.privateKeyEntries = keyContainerEntries.stream()
                .map(KeyContainerEntry::getPrivateKeyEntry)
                .collect(Collectors.toUnmodifiableSet());
        this.certificates = certificates;
        this.keystorePurpose = KeystorePurpose.fromString(keystorePurpose);
    }

    @Nonnull
    public static KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(
            @Nonnull Set<KeyStore.PrivateKeyEntry> privateKeyEntries, String serialNumber, String issuer)
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
    public Set<KeyStore.PrivateKeyEntry> getPrivateKeyEntries() {
        return privateKeyEntries;
    }

    @Nonnull
    public X509Certificate getMatchingCertificate(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        return getMatchingCertificate(certificates, serialNumber, issuer);
    }

    @Nonnull
    public Set<X509Certificate> getCertificates() {
        return certificates;
    }

    @Nonnull
    public static X509Certificate getMatchingCertificate(@Nonnull Set<X509Certificate> certificates,
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
