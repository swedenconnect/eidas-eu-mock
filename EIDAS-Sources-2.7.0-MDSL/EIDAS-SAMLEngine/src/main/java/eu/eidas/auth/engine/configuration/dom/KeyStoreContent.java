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
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.xml.opensaml.CertificateUtil;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.stream.Stream;

/**
 * The decrypted content of a KeyStore
 *
 * @since 1.1
 */
public final class KeyStoreContent implements KeyContainer {

    @Nonnull
    private final ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries;

    @Nonnull
    private final ImmutableSet<X509Certificate> certificates;

    @Nonnull
    private final KeystorePurpose keystorePurpose;


    public KeyStoreContent(@Nonnull ImmutableSet<KeyStore.PrivateKeyEntry> privateKeyEntries,
                           @Nonnull ImmutableSet<X509Certificate> certificates,
                           @Nonnull String keystorePurpose) {
        Preconditions.checkNotNull(privateKeyEntries, "privateKeyEntries");
        Preconditions.checkNotNull(certificates, "certificates");
        this.privateKeyEntries = privateKeyEntries;
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
        throw new ProtocolEngineConfigurationException(
                "No private key entry matching serialNumber=" + serialNumber + " and issuer=" + issuer
                        + " found in configured keyStore");
    }


    @Nonnull
    public KeyStore.PrivateKeyEntry getMatchingPrivateKeyEntry(String serialNumber, String issuer)
            throws ProtocolEngineConfigurationException {
        return getMatchingPrivateKeyEntry(privateKeyEntries, serialNumber, issuer);
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
        throw new ProtocolEngineConfigurationException(
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
