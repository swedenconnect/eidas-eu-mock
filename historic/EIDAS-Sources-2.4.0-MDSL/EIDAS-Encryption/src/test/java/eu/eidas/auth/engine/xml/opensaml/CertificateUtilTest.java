/*
 * Copyright (c) 2018 by European Commission
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

import org.junit.Assert;
import org.junit.Before;
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


    private static BasicX509Credential leafCredential;
    private static BasicX509Credential intermediateCaCredential;
    private static BasicX509Credential rootCaCredential;

    private static BasicX509Credential unstrustedCredential;
    private static BasicX509Credential unstrustedSimilarToLeafCredential;

    @Before
    public void setUp() throws Exception {
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

        leafCredential = loadCredential(certificateFactory, "src/test/resources/certificates/pki/leaf.crt");
        intermediateCaCredential = loadCredential(certificateFactory, "src/test/resources/certificates/pki/intermediate-ca.crt");
        rootCaCredential = loadCredential(certificateFactory, "src/test/resources/certificates/pki/root-ca.crt");

        unstrustedCredential = loadCredential(certificateFactory, "src/test/resources/certificates/untrustedCertificate.crt");
        unstrustedSimilarToLeafCredential = loadCredential(certificateFactory, "src/test/resources/certificates/unstrustedSimilarToLeaf.crt");
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
        trustedCredentials.add(leafCredential);
        CertificateUtil.checkChainTrust(leafCredential, trustedCredentials);
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
        trustedCredentials.add(leafCredential);
        CertificateUtil.checkChainTrust(rootCaCredential, trustedCredentials);
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
        trustedCredentials.add(intermediateCaCredential);
        CertificateUtil.checkChainTrust(leafCredential, trustedCredentials);
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
        trustedCredentials.add(rootCaCredential);
        trustedCredentials.add(intermediateCaCredential);
        CertificateUtil.checkChainTrust(leafCredential, trustedCredentials);
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
        trustedCredentials.add(rootCaCredential);
        trustedCredentials.add(intermediateCaCredential);
        trustedCredentials.add(leafCredential);
        CertificateUtil.checkChainTrust(leafCredential, trustedCredentials);
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
        trustedCredentials.add(rootCaCredential);
        CertificateUtil.checkChainTrust(intermediateCaCredential, trustedCredentials);
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
        CertificateUtil.checkChainTrust(leafCredential, credentials);
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
        trustedCredentials.add(leafCredential);
        trustedCredentials.add(intermediateCaCredential);
        trustedCredentials.add(rootCaCredential);

        CertificateUtil.checkChainTrust(unstrustedCredential, trustedCredentials);
    }

    /**
     * Test method for
     * {@link CertificateUtil#checkChainTrust(X509Credential, Iterable)}
     * when the credential to be checked for trust should not be trusted
     * but the related certificate is quite similar to the trusted {@link CertificateUtilTest#leafCredential}
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

        BasicX509Credential leafCredential = CertificateUtilTest.leafCredential;
        BasicX509Credential intermediateCACredential = CertificateUtilTest.intermediateCaCredential;
        BasicX509Credential unstrustedSimilarToLeafCredential = CertificateUtilTest.unstrustedSimilarToLeafCredential;

        ArrayList<Credential> trustedCredentials = new ArrayList<>();
        trustedCredentials.add(leafCredential);
        trustedCredentials.add(intermediateCACredential);
        trustedCredentials.add(rootCaCredential);

        X509Certificate unstrustedSimilarToLeafCertificate = unstrustedSimilarToLeafCredential.getEntityCertificate();
        X509Certificate intermediateCAEntityCertificate = intermediateCACredential.getEntityCertificate();
        X509Certificate leafCredentialEntityCertificate = leafCredential.getEntityCertificate();

        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getIssuerDN(), leafCredentialEntityCertificate.getIssuerDN());
        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getSubjectDN(), leafCredentialEntityCertificate.getSubjectDN());
        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getSerialNumber(), leafCredentialEntityCertificate.getSerialNumber());

        Assert.assertEquals(unstrustedSimilarToLeafCertificate.getIssuerDN(), intermediateCAEntityCertificate.getSubjectDN());

        Assert.assertNotEquals(unstrustedSimilarToLeafCertificate, leafCredentialEntityCertificate);

        CertificateUtil.checkChainTrust(unstrustedSimilarToLeafCredential, trustedCredentials);
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
        trustedCredentials.add(leafCredential);
        CertificateUtil.checkExplicitTrust(leafCredential, trustedCredentials);
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
        trustedCredentials.add(leafCredential);
        CertificateUtil.checkExplicitTrust(intermediateCaCredential, trustedCredentials);
    }

   /**
     * Loads the certificate contained in the file at {@param pathname} into a {@link BasicX509Credential} instance.
     *
     * @param certificateFactory the factory used to generate the certificate instance.
     * @param pathname the path to the file containing the certificate
     * @return the {@link BasicX509Credential} instance corresponding to the certificate
     * @throws IOException if a problem occurs while opening the file related to the {@param pathname}
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

}