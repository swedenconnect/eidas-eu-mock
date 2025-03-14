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

package eu.eidas.auth.engine.xml.opensaml.dss;

import static eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams.DISABLE_REVOCATION_CHECK;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.security.x509.BasicX509Credential;

import eu.eidas.auth.engine.xml.opensaml.CertificateVerifierParams;
import eu.eidas.auth.engine.xml.opensaml.exception.CertificateRevokedException;
import eu.eidas.auth.engine.xml.opensaml.exception.MissingCertificateRevocationDataException;
import eu.eidas.auth.engine.xml.opensaml.exception.UntrustedCertificateException;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.pki.jaxb.JAXBPKILoader;
import eu.europa.esig.dss.pki.jaxb.model.JAXBCertEntity;
import eu.europa.esig.dss.pki.jaxb.model.JAXBCertEntityRepository;
import eu.europa.esig.dss.pki.x509.revocation.crl.PKICRLSource;
import eu.europa.esig.dss.pki.x509.revocation.ocsp.PKIOCSPSource;
import org.opensaml.security.x509.X509Credential;

/**
 * Test class for {@link DSSCertificateTrustVerifier}
 * Verify typical use case for certificate validation.
 * Use locally generated PKI to mock CRL and OCSP response.
 * It does not test loading over the internet.
 * */
public class DSSCertificateTrustVerifierTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private final CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

    private static DSSCertificateTrustVerifier verifier;

    private static JAXBCertEntityRepository certificateRepository;

    public DSSCertificateTrustVerifierTest() throws CertificateException {}

    @BeforeClass
    public static void init() {
        certificateRepository = new JAXBCertEntityRepository();

        // Load an XML file containing PKI configuration
        final File pkiFile = new File("src/test/resources/certificates/pki/pki-factory-config.xml");

        // Init a JAXBPKILoader to load PKI from XML file
        final JAXBPKILoader builder = new JAXBPKILoader();

        // Initialize a content of the PKI from XML file and load created entries to the
        // repository
        builder.persistPKI(certificateRepository, pkiFile);

        verifier = new DSSCertificateTrustVerifier(new PKIOCSPSource(certificateRepository), new PKICRLSource(certificateRepository));
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * Certificate is selfsigned and untrusted
     * <p>
     * Must fail.
     */
    @Test
    public void emptyTrustAnchor() {
        this.thrown.expect(UntrustedCertificateException.class);

        final BasicX509Credential target = loadPkiCredential("root-ca");

        verifier.verify(target, Collections.emptyList(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * Certificate is on a trusted chain but revoked
     * <p>
     * Must fail.
     */
    @Test
    public void testCrlIntermediateCaRevoked() {
        this.thrown.expect(CertificateRevokedException.class);

        final BasicX509Credential target = loadPkiCredential("good-user-revoked-ca");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * The intermediate CA that signed the certificate is revoked. However, as intermediate CA is the trust anchor, revocation data is not checked and thus the verification is a success
     * <p>
     * Must succeed.
     */
    @Test
    public void testIntermediateCaRevokedTrusted() {
        final BasicX509Credential target = loadPkiCredential("good-user-revoked-ca");

        verifier.verify(target, loadPkiCertificates("revoked-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * The intermediate CA that signed the certificate is revoked. However, as intermediate CA is the trust anchor, revocation data is not checked and thus the verification is a success.
     * Check with root also trusted to ensure it does not perform revocation check and stop on the "first" trusted anchor
     * <p>
     * Must succeed.
     */
    @Test
    public void testIntermediateCaRevokedTrustedAndRootTrusted() {
        final BasicX509Credential target = loadPkiCredential("good-user-revoked-ca");

        verifier.verify(target, loadPkiCertificates("revoked-ca", "root-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * The intermediate CA that signed the certificate is revoked. However, as it is direct trust, revocation data is not checked and thus the verification is a success
     * <p>
     * Must succeed.
     */
    @Test
    public void testIntermediateCaRevokedUserDirectTrust() {
        final BasicX509Credential target = loadPkiCredential("good-user-revoked-ca");

       verifier.verify(target, loadPkiCertificates("good-user-revoked-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the leaf (user) has been (OCSP) revoked by the grandparent
     * <p>
     * Must fail.
     */
    @Test
    public void testOcspSignedByRootRevokedUser() {
        this.thrown.expect(CertificateRevokedException.class);

        final BasicX509Credential target = loadPkiCredential("revoked-user-root-ocsp-response");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the leaf (user) has been (OCSP) revoked by the parent
     * <p>
     * Must fail.
     */
    @Test
    public void testRevokedUser() {
        this.thrown.expect(CertificateRevokedException.class);

        final BasicX509Credential target = loadPkiCredential("revoked-user");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When revocation has been disabled and
     * the leaf (user) has been (OCSP) revoked by the parent
     * <p>
     * Must succeed.
     */
    @Test
    public void testRevokedUserDisableRevocationCheck() {
        final BasicX509Credential target = loadPkiCredential("revoked-user");

        verifier.verify(target, getPkiTrustAnchors(), DISABLE_REVOCATION_CHECK);
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * The certificate is revoked. However, as it is directly trusted, revocation data is not checked and thus the verification is a success
     * <p>
     * Must succeed.
     */
    @Test
    public void testRevokedUserTrusted() {
        final BasicX509Credential target = loadPkiCredential("revoked-user");

        verifier.verify(target, loadPkiCertificates("revoked-user", "good-ca", "root-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate is not part of the trust chain.
     * <p>
     * Must fail.
     */
    @Test
    public void testUntrustedChain() throws Exception {
        this.thrown.expect(UntrustedCertificateException.class);

        final BasicX509Credential target = loadPkiCredential("good-user");

        verifier.verify(target, Collections.singletonList(this.loadCertificate("classpath:/certificates/pki/intermediate-ca.crt")), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate is part of the trust chain.
     * <p>
     * Must succeed.
     */
    @Test
    public void testTrustedRoot() {
        final BasicX509Credential target = loadPkiCredential("good-user");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the intermediate is trusted and the certificate is part of the trust chain.
     * <p>
     * Must succeed.
     */
    @Test
    public void testTrustedIntermediate() {
        final BasicX509Credential target = loadPkiCredential("good-user");

        verifier.verify(target, loadPkiCertificates("good-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate itself is trusted.
     * <p>
     * Must succeed.
     */
    @Test
    public void testDirectTrust() {
        final BasicX509Credential target = loadPkiCredential("good-user");

        verifier.verify(target, loadPkiCertificates("good-user"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate itself is trusted and the only certificate inside the chain (cannot build the chain up to root certificate).
     * <p>
     * Must succeed.
     */
    @Test
    public void testDirectTrustNoChain() {
        final BasicX509Credential target = loadPkiCredential("good-user");
        target.setEntityCertificateChain(Collections.singletonList(target.getEntityCertificate()));

        verifier.verify(target, loadPkiCertificates("good-user"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate has a crl defined.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCrlValidCert() {
        final BasicX509Credential target = loadPkiCredential("good-user-crl");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate has a crl revoked.
     * <p>
     * Must fail.
     */
    @Test
    public void testCrlRevoked() {
        this.thrown.expect(CertificateRevokedException.class);

        final BasicX509Credential target = loadPkiCredential("revoked-user-crl");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate has crl and ocsp defined.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCrlAndOcspValidCert() {
        final BasicX509Credential target = loadPkiCredential("good-user-crl-ocsp");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * Chain not specified, thus it cannot build the path to trust anchor (because AIA is not enabled).
     * The validation will fail as trust anchor is not identified.
     * <p>
     * Must fail.
     */
    @Test
    public void testNoChain() {
        this.thrown.expect(UntrustedCertificateException.class);

        final BasicX509Credential target = loadPkiCredential("good-user");
        target.setEntityCertificateChain(Collections.singletonList(target.getEntityCertificate()));

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * CA of the verified certificate is not in the chain. However, as the intermediate CA is trusted (and not the root),
     * it can still build the chain to trust anchor using information inside certificate and provided trust anchor.
     * <p>
     * Must succeed.
     */
    @Test
    public void testCaNotInChain() {
        final BasicX509Credential target = loadPkiCredential("good-user");
        target.setEntityCertificateChain(Collections.singletonList(target.getEntityCertificate()));

        verifier.verify(target, loadPkiCertificates("good-ca"), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate has ocsp defined in an unreachable location.
     * <p>
     * Must succeed.
     */
    @Test
    public void testOcspResponseFailSoftFailEnabled() {
        final BasicX509Credential target = loadPkiCredential("good-user-ocsp-only-fail");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate has ocsp defined in an unreachable location and there is no soft fail.
     * <p>
     * Must fail.
     */
    @Test
    public void testOcspResponseFailSoftFailDisabled() {
        this.thrown.expect(MissingCertificateRevocationDataException.class);

        final BasicX509Credential target = loadPkiCredential("good-user-ocsp-only-fail");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams(true, false));
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the OCSP is unreachable, the verifier fallback on the provided CRL to perform revocation check.
     * <p>
     * Must succeed.
     */
    @Test
    public void testOcspResponseFailFallbackCrl() {
        final BasicX509Credential target = loadPkiCredential("good-user-ocsp-fail");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams(true, false));
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate does not have revocation data (OCSP and CRL not defined in attributes) and soft fail is enabled.
     * <p>
     * Must succeed.
     */
    @Test
    public void noRevocationSoftFailEnabled() {
        final BasicX509Credential target = loadPkiCredential("good-user-no-revocation");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams());
    }

    /**
     * Test method for
     * {@link DSSCertificateTrustVerifier#verify(X509Credential, Collection, CertificateVerifierParams)}
     * When the certificate does not have revocation data (OCSP and CRL not defined in attributes) and soft fail is disabled.
     * <p>
     * Must fail.
     */
    @Test
    public void noRevocationSoftFailDisabled() {
        this.thrown.expect(MissingCertificateRevocationDataException.class);

        final BasicX509Credential target = loadPkiCredential("good-user-no-revocation");

        verifier.verify(target, getPkiTrustAnchors(), new CertificateVerifierParams(true, false));
    }

    private static BasicX509Credential loadPkiCredential(final String subject) {
        final JAXBCertEntity certEntity = certificateRepository.getCertEntityBySubject(subject);

        final X509Certificate x509Certificate = certEntity.getCertificateToken().getCertificate();

        final List<X509Certificate> chainCertificates = certEntity.getCertificateChain().stream()
                .map(CertificateToken::getCertificate)
                .collect(Collectors.toList());

        final BasicX509Credential credential = new BasicX509Credential(x509Certificate);
        credential.setEntityCertificateChain(chainCertificates);

        return credential;
    }

    private static Collection<X509Certificate> getPkiTrustAnchors() {
        return certificateRepository.getTrustAnchors().stream()
                .map(JAXBCertEntity::getCertificateToken)
                .map(CertificateToken::getCertificate)
                .collect(Collectors.toList());
    }

    private static List<X509Certificate> loadPkiCertificates(final String... subjects) {
        final List<X509Certificate> certificates = new ArrayList<>(subjects.length);

        for (final String subject : subjects) {
            final X509Certificate x509Certificate = certificateRepository.getCertEntityBySubject(subject).getCertificateToken().getCertificate();
            certificates.add(x509Certificate);
        }

        return certificates;
    }

    private X509Certificate loadCertificate(String pathname) throws Exception {
        final URI uri = new URI(pathname);

        final InputStream inputStream;

        if (uri.getScheme().equals("classpath")) {
            inputStream = this.getClass().getResourceAsStream(uri.getPath());
        } else {
            inputStream = uri.toURL().openStream();
        }

        final Certificate certificate = this.certificateFactory.generateCertificate(inputStream);
        if (certificate instanceof X509Certificate) {
            return (X509Certificate) certificate;
        }

        return null;
    }
}
