/*
 * Copyright (c) 2020 by European Commission
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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
import java.security.Security;
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

    public CertificateUtilTest() throws CertificateException, IOException {
    }

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
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
     * {@link CertificateUtil#checkExplicitTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the one related to the signing key (metadatanode)
     * when the trusted credential is the one related to the signing key (metadatanode),
     * <p/>
     * Must succeed.
     */
    @Test
    public void checkExplicitTrust() throws CertificateException {
        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getLeafCredentialSignedByIntermediateCa());
        CertificateUtil.checkExplicitTrust(getLeafCredentialSignedByIntermediateCa(), trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkExplicitTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust is the one related to the signing key (metadatanode)
     * when the trusted credential is the one related to the signing key (intermediateCAMetadata),
     * <p/>
     * Must fail.
     */
    @Test
    public void checkExplicitTrustDifferentCredentialsFail() throws CertificateException {
        thrown.expect(CertificateException.class);
        thrown.expectMessage("untrusted certificate");

        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(getLeafCredentialSignedByIntermediateCa());
        CertificateUtil.checkExplicitTrust(getIntermediateCaCredentialSignedByRootCa(), trustedCredentials);
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