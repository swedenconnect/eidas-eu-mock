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

package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.RecommendedSecurityProviders;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/**
 * Tests for the {@link CertificateUtil}.
 */
public class CertificateUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

    private final BasicX509Credential selfSignedCredential = loadCredential(certificateFactory, "src/test/resources/certificates/selfSigned.crt");

    private final BasicX509Credential leafCredentialSignedByIntermediateCa = loadCredential(certificateFactory, "src/test/resources/certificates/pki/leaf.crt");
    private final BasicX509Credential intermediateCaCredentialSignedByRootCa = loadCredential(certificateFactory, "src/test/resources/certificates/pki/intermediate-ca.crt");
    private final BasicX509Credential rootCaCredential = loadCredential(certificateFactory, "src/test/resources/certificates/pki/root-ca.crt");

    private final BasicX509Credential unstrustedCredential = loadCredential(certificateFactory, "src/test/resources/certificates/untrustedCertificate.crt");
    private final BasicX509Credential unstrustedSimilarToLeafCredential = loadCredential(certificateFactory, "src/test/resources/certificates/unstrustedSimilarToLeaf.crt");

    private final BasicX509Credential bpCl3CaG2STBSCredential = loadCredential(certificateFactory, "src/test/resources/certificates/BPCl3CaG2STBS.cer");

    public CertificateUtilTest() throws CertificateException, IOException {
    }

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is a self-signed one (selfsigned)
     * when the trusted credential is the one related to the signing key (selfsigned),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckSelfSignedSigningCredentialTrustingSelfSignedSigningCredential() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        final BasicX509Credential selfSignedCredential = getSelfSignedCredential();
        trustedCredentials.add(selfSignedCredential);

        CertificateUtil.checkChainTrust(selfSignedCredential, trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the one related to the signing key (metadatanode)
     * when the trusted credential is the one related to the signing key (metadatanode),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckMetadataSigningCredentialTrustingMetadataSigningCredential() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        final BasicX509Credential selfSignedCredential = getSelfSignedCredential();
        trustedCredentials.add(selfSignedCredential);

        CertificateUtil.checkChainTrust(selfSignedCredential, trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the one related to the signing key (leaf)
     * when the trusted credential is the one related to the signing key (rootCA),
     * <p/>
     * Must fail.
     */
    @Test
    public void checkChainTrustCheckMetadataSigningCredentialTrustingRootCACredential() throws CertificateException {
        thrown.expect(CertificateException.class);
        thrown.expectMessage("untrusted certificate");

        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getLeafCredentialSignedByIntermediateCa());
        CertificateUtil.checkChainTrust(getRootCaCredential(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is (metadatanode)
     * when the trusted credential is the intermediate CA (intermediateCAMetadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckMetadataSigningCredentialTrustingIntermediateCa() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getIntermediateCaCredentialSignedByRootCa());
        CertificateUtil.checkChainTrust(getLeafCredentialSignedByIntermediateCa(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is (metadatanode)
     * when the trusted credential is the intermediate CA (intermediateCAMetadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckMetadataSigningCredentialTrustingIntermediateCaRootCA() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getRootCaCredential());
        trustedCredentials.add(getIntermediateCaCredentialSignedByRootCa());
        CertificateUtil.checkChainTrust(getLeafCredentialSignedByIntermediateCa(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is (metadatanode)
     * when the trusted credential is the intermediate CA (intermediateCAMetadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckMetadataSigningCredentialTrustingAllChain() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getRootCaCredential());
        trustedCredentials.add(getIntermediateCaCredentialSignedByRootCa());
        trustedCredentials.add(getLeafCredentialSignedByIntermediateCa());
        CertificateUtil.checkChainTrust(getLeafCredentialSignedByIntermediateCa(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the one related to the signing key (metadatanode)
     * when the trusted credential is the one related to the signing key (rootCAMetadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustCheckIntermediateCaCredentialTrustingRootCACredential() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getRootCaCredential());
        CertificateUtil.checkChainTrust(getIntermediateCaCredentialSignedByRootCa(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the intermediate CA (intermediateCAMetadata)
     * when the trusted credential is the intermediate CA (intermediateCAMetadata),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkChainTrustIntermediateCaCredentialTrustingIntermediateCa() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        BasicX509Credential intermediateCaCredential = getIntermediateCaCredentialSignedByRootCa();
        trustedCredentials.add(intermediateCaCredential);

        CertificateUtil.checkChainTrust(intermediateCaCredential, trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the intermediate CA (intermediateCAMetadata)
     * when there are no trusted credentials,
     * <p/>
     * Must fail.
     */
    @Test
    public void checkChainTrustEmptyTrustedCredentials() throws CertificateException {
        thrown.expect(CertificateException.class);
        thrown.expectMessage("untrusted certificate");

        ArrayList<Credential> credentials = new ArrayList<>();
        CertificateUtil.checkChainTrust(getLeafCredentialSignedByIntermediateCa(), credentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust should not be trusted.
     *
     * <p/>
     * Must fail.
     */
    @Test
    public void checkChainTrustUnstrustedCertificate() throws CertificateException {
        thrown.expect(CertificateException.class);
        thrown.expectMessage("untrusted certificate");

        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getLeafCredentialSignedByIntermediateCa());
        trustedCredentials.add(getIntermediateCaCredentialSignedByRootCa());
        trustedCredentials.add(getRootCaCredential());

        CertificateUtil.checkChainTrust(getUnstrustedCredential(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust should not be trusted
     * but the related certificate is quite similar to the trusted {@link CertificateUtilTest#leafCredentialSignedByIntermediateCa}
     * e.g. the IssuerDN, SubjectDn, Serial Number are equal
     *
     *
     * <p/>
     * Must fail.
     */
    @Test
    public void checkChainTrustSameIssuerUnstrustedCertificate() throws CertificateException {
        thrown.expect(CertificateException.class);
        thrown.expectMessage("untrusted certificate");

        BasicX509Credential leafCredentialInstance = getLeafCredentialSignedByIntermediateCa();
        BasicX509Credential intermediateCACredentialInstance = getIntermediateCaCredentialSignedByRootCa();
        BasicX509Credential unstrustedSimilarToLeafCredentialInstance = getUnstrustedSimilarToLeafCredential();

        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(leafCredentialInstance);
        trustedCredentials.add(intermediateCACredentialInstance);
        trustedCredentials.add(getRootCaCredential());

        X509Certificate unstrustedSimilarToLeafCertificate = unstrustedSimilarToLeafCredentialInstance.getEntityCertificate();
        X509Certificate intermediateCAEntityCertificate = intermediateCACredentialInstance.getEntityCertificate();
        X509Certificate leafCredentialEntityCertificate = leafCredentialInstance.getEntityCertificate();

        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getIssuerDN(), leafCredentialEntityCertificate.getIssuerDN());
        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getSubjectDN(), leafCredentialEntityCertificate.getSubjectDN());
        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getSerialNumber(), leafCredentialEntityCertificate.getSerialNumber());

        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getIssuerDN(), intermediateCAEntityCertificate.getSubjectDN());

        Assert.assertNotEquals(unstrustedSimilarToLeafCertificate, leafCredentialEntityCertificate);

        CertificateUtil.checkChainTrust(unstrustedSimilarToLeafCredentialInstance, trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify successful matching of a certificate using RFC 1779 encoding.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_SuccessWithRFC1779Encoding() {
        String issuer = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);
        boolean result = CertificateUtil.matchesCertificate(serialNumber, issuer, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed due to consistent RFC 1779 encoding handling.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching fails when the issuer string is modified.
     * <p>
     * Must fail.
     */
    @Test
    public void testMatchesCertificate_ExactMatchWithModifiedIssuerFails() {
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);
        String modifiedIssuer = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=NTRNO-999999999, C=NO";

        boolean result = CertificateUtil.matchesCertificate(serialNumber, modifiedIssuer, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertFalse("Expected match to fail due to non-matching issuer string.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching succeeds when the issuer matches either the certificate's issuer or subject field.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_MatchWithSubject() {
        String subjectMatchingIssuer = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, subjectMatchingIssuer, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed when issuer matches certificate's subject field.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify successful matching when the certificate uses ORG_ID in the issuer.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_SuccessWithOrgId() {
        String issuerWithOrgId = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, ORG_ID=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, issuerWithOrgId, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed with ORG_ID field correctly mapped.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching fails when ORG_ID is not correctly mapped.
     * <p>
     * Must fail.
     */
    @Test
    public void testMatchesCertificate_FailWithInvalidOrgId() {
        String invalidOrgIdIssuer = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, ORG_ID=INVALID-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, invalidOrgIdIssuer, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertFalse("Expected match to fail due to invalid ORG_ID value.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify successful matching when OID.2.5.4.97 is used instead of ORG_ID.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_SuccessWithOidMapping() {
        String issuerWithOid = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, issuerWithOid, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed with OID mapping for ORG_ID.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching fails when the OID is incorrect.
     * <p>
     * Must fail.
     */
    @Test
    public void testMatchesCertificate_FailWithIncorrectOidMapping() {
        String incorrectOidIssuer = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=INVALID-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, incorrectOidIssuer, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertFalse("Expected match to fail due to incorrect OID value.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching succeeds when both ORG_ID and OID.2.5.4.97 are supported.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_SuccessWithBothOrgIdAndOid() {
        String issuerWithOrgId = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, ORG_ID=NTRNO-983163327, C=NO";
        String issuerWithOid = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, OID.2.5.4.97=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean resultOrgId = CertificateUtil.matchesCertificate(serialNumber, issuerWithOrgId, bpCl3CaG2STBSCredential.getEntityCertificate());
        boolean resultOid = CertificateUtil.matchesCertificate(serialNumber, issuerWithOid, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed with ORG_ID in issuer.", resultOrgId);
        Assert.assertTrue("Expected match to succeed with OID in issuer.", resultOid);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching succeeds with case-insensitive OID mapping.
     * <p>
     * Must succeed.
     */
    @Test
    public void testMatchesCertificate_SuccessWithCaseInsensitiveOid() {
        String issuerWithLowerCaseOid = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, oid.2.5.4.97=NTRNO-983163327, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, issuerWithLowerCaseOid, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertTrue("Expected match to succeed with case-insensitive OID mapping.", result);
    }

    /**
     * Test method for
     * {@link CertificateUtil#matchesCertificate(String, String, X509Certificate)}
     * to verify that matching fails when neither ORG_ID nor OID.2.5.4.97 is present in the issuer.
     * <p>
     * Must fail.
     */
    @Test
    public void testMatchesCertificate_WithoutOrgIdOrOid() {
        String issuerWithoutOrgIdOrOid = "CN=Buypass Class 3 Root CA G2 ST, O=Buypass AS, C=NO";
        String serialNumber = bpCl3CaG2STBSCredential.getEntityCertificate().getSerialNumber().toString(16);

        boolean result = CertificateUtil.matchesCertificate(serialNumber, issuerWithoutOrgIdOrOid, bpCl3CaG2STBSCredential.getEntityCertificate());

        Assert.assertFalse("Expected match to fail without ORG_ID or OID attributes.", result);
    }

    /**
     * Loads the certificate contained in the file at {@param pathname} into a {@link BasicX509Credential} instance.
     *
     * @param certificateFactory the factory used to generate the certificate instance.
     * @param pathname           the path to the file containing the certificate
     * @return the {@link BasicX509Credential} instance corresponding to the certificate
     * @throws IOException          if a problem occurs while opening the file related to the {@param pathname}
     * @throws CertificateException if a certificate instance could not be generated from the file input stream of the certificate
     */
    private BasicX509Credential loadCredential(CertificateFactory certificateFactory, String pathname) throws IOException, CertificateException {
        X509Certificate x509Certificate = loadCertificate(certificateFactory, pathname);
        return new BasicX509Credential(x509Certificate);
    }

    /**
     * Loads the certificate contained in the file at {@param pathname} into a {@link X509Certificate} instance.
     *
     * @param certificateFactory the factory used to generate the certificate instance.
     * @param pathname the path to the file containing the certificate
     * @return the {@link X509Certificate} instance corresponding to the certificate
     * @throws IOException if a problem occurs while opening the file related to the {@param pathname}
     * @throws CertificateException if a certificate instance could not be generated from the file input stream of the certificate
     */
    private X509Certificate loadCertificate(CertificateFactory certificateFactory, String pathname) throws IOException, CertificateException {
        File file =new File(pathname);
        try (FileInputStream fileInputStream = new FileInputStream(file)){
            Certificate certificate = certificateFactory.generateCertificate(fileInputStream);
            if (certificate instanceof X509Certificate) {
                return (X509Certificate) certificate;
            }
        }

        return null;
    }

    public BasicX509Credential getSelfSignedCredential() {
        return selfSignedCredential;
    }

    public BasicX509Credential getLeafCredentialSignedByIntermediateCa() {
        return leafCredentialSignedByIntermediateCa;
    }

    public BasicX509Credential getIntermediateCaCredentialSignedByRootCa() {
        return intermediateCaCredentialSignedByRootCa;
    }

    public BasicX509Credential getRootCaCredential() {
        return rootCaCredential;
    }

    public BasicX509Credential getUnstrustedCredential() {
        return unstrustedCredential;
    }

    public BasicX509Credential getUnstrustedSimilarToLeafCredential() {
        return unstrustedSimilarToLeafCredential;
    }


}