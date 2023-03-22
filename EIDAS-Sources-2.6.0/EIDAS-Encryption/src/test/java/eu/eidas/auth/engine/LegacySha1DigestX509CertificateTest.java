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

package eu.eidas.auth.engine;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.security.x509.X509Credential;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.times;

/**
 * Test class for {@link LegacySha1DigestX509Certificate}
 */
public class LegacySha1DigestX509CertificateTest {

    private X509Certificate x509Certificate;
    private LegacySha1DigestX509Certificate legacySha1DigestX509Certificate;

    @Before
    public void setUp() throws Exception {
        final List<X509Certificate> x509Certificates = loadCertificatesFromKeystore("src/test/resources/keystores/test.p12");
        x509Certificate = x509Certificates.get(0);
        legacySha1DigestX509Certificate = new LegacySha1DigestX509Certificate(x509Certificate);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getIssuerX500Principal()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getIssuerX500Principal() {
        final X500Principal actualIssuerX500Principal = legacySha1DigestX509Certificate.getIssuerX500Principal();
        Assert.assertEquals(x509Certificate.getIssuerX500Principal(), actualIssuerX500Principal);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSubjectX500Principal()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getSubjectX500Principal() {
        final X500Principal actualSubjectX500Principal = legacySha1DigestX509Certificate.getSubjectX500Principal();
        Assert.assertEquals(x509Certificate.getSubjectX500Principal(), actualSubjectX500Principal);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getExtendedKeyUsage()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getExtendedKeyUsage() throws CertificateParsingException {
        final List<String> actualExtendedKeyUsage = legacySha1DigestX509Certificate.getExtendedKeyUsage();
        Assert.assertEquals(x509Certificate.getExtendedKeyUsage(), actualExtendedKeyUsage);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSubjectAlternativeNames()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getSubjectAlternativeNames() throws CertificateParsingException {
        final Collection<List<?>> actualSubjectAlternativeNames = legacySha1DigestX509Certificate.getSubjectAlternativeNames();
        Assert.assertEquals(x509Certificate.getSubjectAlternativeNames(), actualSubjectAlternativeNames);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getIssuerAlternativeNames()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getIssuerAlternativeNames() throws CertificateParsingException {
        final Collection<List<?>> actualIssuerAlternativeNames = legacySha1DigestX509Certificate.getIssuerAlternativeNames();
        Assert.assertEquals(x509Certificate.getIssuerAlternativeNames(), actualIssuerAlternativeNames);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#verify(PublicKey)}
     * <p>
     * Must succeed.
     */
    @Test
    public void verify() throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        legacySha1DigestX509Certificate.verify(x509Certificate.getPublicKey());
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#verify(PublicKey)}
     * <p>
     * Must succeed.
     */
    @Test
    public void verifyWithProvider() throws CertificateException, NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        try {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                Security.addProvider(new BouncyCastleProvider());
            }

            legacySha1DigestX509Certificate.verify(x509Certificate.getPublicKey(), BouncyCastleProvider.PROVIDER_NAME);
        } finally {
            if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
                Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
            }
        }
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getType()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getType() {
        final String actualType = legacySha1DigestX509Certificate.getType();
        Assert.assertEquals(x509Certificate.getType(), actualType);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#equals(Object)} 
     * <p>
     * Must succeed.
     */
    @Test
    public void testEquals() {
        final LegacySha1DigestX509Certificate legacySha1DigestX509Certificate1 = new LegacySha1DigestX509Certificate(x509Certificate);
        final LegacySha1DigestX509Certificate legacySha1DigestX509Certificate2 = new LegacySha1DigestX509Certificate(x509Certificate);

        Assert.assertEquals(legacySha1DigestX509Certificate1, legacySha1DigestX509Certificate2);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#hashCode()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void testHashCode() {
        final LegacySha1DigestX509Certificate legacySha1DigestX509Certificate1 = new LegacySha1DigestX509Certificate(x509Certificate);
        final LegacySha1DigestX509Certificate legacySha1DigestX509Certificate2 = new LegacySha1DigestX509Certificate(x509Certificate);

        Assert.assertEquals(legacySha1DigestX509Certificate1.hashCode(), legacySha1DigestX509Certificate2.hashCode());
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getCertificate()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getCertificate() {
        final X509Certificate actualCertificates =  legacySha1DigestX509Certificate.getCertificate();
        Assert.assertEquals(x509Certificate, actualCertificates);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#checkValidity()}
     * <p>
     * Must succeed.
     */
    @Test
    public void checkValidityNow() throws CertificateNotYetValidException, CertificateExpiredException {
        X509Certificate x509Certificate = Mockito.mock(X509Certificate.class);
        final LegacySha1DigestX509Certificate legacySha1DigestX509Certificate = new LegacySha1DigestX509Certificate((x509Certificate));

        legacySha1DigestX509Certificate.checkValidity();

        Mockito.verify(x509Certificate, times(1)).checkValidity();
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#checkValidity(Date)}
     * <p>
     * Must succeed.
     */
    @Test
    public void checkValidity() throws CertificateNotYetValidException, CertificateExpiredException {
        final Date onDate = Date.from(Instant.ofEpochMilli(1632734313178L));
        legacySha1DigestX509Certificate.checkValidity(onDate);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getVersion()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getVersion() {
        final int actualVersion = legacySha1DigestX509Certificate.getVersion();
        Assert.assertEquals(x509Certificate.getVersion(), actualVersion);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSerialNumber()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSerialNumber() {
        final BigInteger actualSerialNumber = legacySha1DigestX509Certificate.getSerialNumber();
        Assert.assertEquals(x509Certificate.getSerialNumber(), actualSerialNumber);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getIssuerDN()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getIssuerDN() {
        final Principal actualIssuerDN = legacySha1DigestX509Certificate.getIssuerDN();
        Assert.assertEquals(x509Certificate.getIssuerDN(), actualIssuerDN);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSubjectDN()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSubjectDN() {
        final Principal actualSubjectDN = legacySha1DigestX509Certificate.getSubjectDN();
        Assert.assertEquals(x509Certificate.getSubjectDN(), actualSubjectDN);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getNotBefore()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getNotBefore() {
        final Date actualNotBefore = legacySha1DigestX509Certificate.getNotBefore();
        Assert.assertEquals(x509Certificate.getNotBefore(), actualNotBefore);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getNotAfter()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getNotAfter() {
        final Date actualNotAfter = legacySha1DigestX509Certificate.getNotAfter();
        Assert.assertEquals(x509Certificate.getNotAfter(), actualNotAfter);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getTBSCertificate()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getTBSCertificate() throws CertificateEncodingException {
        final byte[] actualTBSCertificate = legacySha1DigestX509Certificate.getTBSCertificate();
        Assert.assertArrayEquals(x509Certificate.getTBSCertificate(), actualTBSCertificate);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSignature()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSignature() {
        final byte[] actualSignature = legacySha1DigestX509Certificate.getSignature();
        Assert.assertArrayEquals(x509Certificate.getSignature(), actualSignature);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSigAlgName()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSigAlgName() {
        final String actualSigAlgName = legacySha1DigestX509Certificate.getSigAlgName();
        Assert.assertEquals(x509Certificate.getSigAlgName(), actualSigAlgName);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSigAlgOID()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSigAlgOID() {
        final String actualSigAlgOID = legacySha1DigestX509Certificate.getSigAlgOID();
        Assert.assertEquals(x509Certificate.getSigAlgOID(), actualSigAlgOID);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSigAlgParams()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSigAlgParams() {
        final byte[] actualSigAlgName = legacySha1DigestX509Certificate.getSigAlgParams();
        Assert.assertArrayEquals(x509Certificate.getSigAlgParams(), actualSigAlgName);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getIssuerUniqueID()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getIssuerUniqueID() {
        final boolean[] actualIssuerUniqueID = legacySha1DigestX509Certificate.getIssuerUniqueID();
        Assert.assertArrayEquals(x509Certificate.getIssuerUniqueID(), actualIssuerUniqueID);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSubjectUniqueID()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getSubjectUniqueID() {
        final boolean[] actualSubjectUniqueID = legacySha1DigestX509Certificate.getSubjectUniqueID();
        Assert.assertArrayEquals(x509Certificate.getSubjectUniqueID(), actualSubjectUniqueID);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getKeyUsage()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getKeyUsage() {
        final boolean[] actualKeyUsage = legacySha1DigestX509Certificate.getKeyUsage();
        Assert.assertArrayEquals(x509Certificate.getKeyUsage(), actualKeyUsage);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getBasicConstraints()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getBasicConstraints() {
        final int actualBasicConstraints = legacySha1DigestX509Certificate.getBasicConstraints();
        Assert.assertEquals(x509Certificate.getBasicConstraints(), actualBasicConstraints);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getEncoded()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getEncoded() throws CertificateEncodingException {
        final byte[] actualEncoded = legacySha1DigestX509Certificate.getEncoded();
        Assert.assertArrayEquals(x509Certificate.getEncoded(), actualEncoded);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getSubjectX500Principal(X509Credential)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testToString() {
        final String actualToString = legacySha1DigestX509Certificate.toString();
        Assert.assertEquals(x509Certificate.toString(), actualToString);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getPublicKey()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getPublicKey() {
        final PublicKey actualPublicKey = legacySha1DigestX509Certificate.getPublicKey();
        Assert.assertEquals(x509Certificate.getPublicKey(), actualPublicKey);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#hasUnsupportedCriticalExtension()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void hasUnsupportedCriticalExtension() {
        final boolean actualHasUnsupportedCriticalExtension = legacySha1DigestX509Certificate.hasUnsupportedCriticalExtension();
        Assert.assertEquals(x509Certificate.hasUnsupportedCriticalExtension(), actualHasUnsupportedCriticalExtension);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getCriticalExtensionOIDs()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getCriticalExtensionOIDs() {
        final Set<String> actualCriticalExtensionOIDs = legacySha1DigestX509Certificate.getCriticalExtensionOIDs();
        Assert.assertEquals(x509Certificate.getCriticalExtensionOIDs(), actualCriticalExtensionOIDs);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getNonCriticalExtensionOIDs()} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getNonCriticalExtensionOIDs() {
        final Set<String> actualNonCriticalExtensionOIDs = legacySha1DigestX509Certificate.getNonCriticalExtensionOIDs();
        Assert.assertEquals(x509Certificate.getNonCriticalExtensionOIDs(), actualNonCriticalExtensionOIDs);
    }

    /**
     * Test method for
     * {@link LegacySha1DigestX509Certificate#getExtensionValue(String)} 
     * <p>
     * Must succeed.
     */
    @Test
    public void getExtensionValue() {
        final byte[] expectedExtensionValue = x509Certificate.getExtensionValue("2.5.29.19");
        final byte[] actualExtensionValue = legacySha1DigestX509Certificate.getExtensionValue("2.5.29.19");
        Assert.assertArrayEquals(expectedExtensionValue, actualExtensionValue);
    }

    private static List<X509Certificate> loadCertificatesFromKeystore(String keystorePath) throws Exception {
        final String keystorePass = "local-demo";
        final char[] keystorePassCharArray = keystorePass.toCharArray();

        final List<X509Certificate> certificates = new ArrayList<>();

        final KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(new FileInputStream(keystorePath), keystorePassCharArray);

        KeyStore.PasswordProtection passwordProtection = new KeyStore.PasswordProtection(keystorePassCharArray);
        try {
            Enumeration<String> keyAliases = keystore.aliases();
            while (keyAliases.hasMoreElements()) {
                String keyAlias = keyAliases.nextElement();
                if (keystore.isKeyEntry(keyAlias)) {

                    KeyStore.Entry keyStoreEntry = keystore.getEntry(keyAlias, passwordProtection);
                    if (keyStoreEntry instanceof KeyStore.PrivateKeyEntry) {
                        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStoreEntry;
                        X509Certificate certificate = (X509Certificate) privateKeyEntry.getCertificate();
                        certificates.add(certificate);
                    }
                }
            }
        } finally {
            passwordProtection.destroy();
            return certificates;
        }
    }
}