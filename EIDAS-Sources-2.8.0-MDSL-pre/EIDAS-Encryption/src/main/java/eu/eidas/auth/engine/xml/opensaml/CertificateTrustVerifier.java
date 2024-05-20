package eu.eidas.auth.engine.xml.opensaml;

import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.opensaml.security.x509.X509Credential;

import eu.eidas.auth.engine.xml.opensaml.exception.CertificateRevokedException;
import eu.eidas.auth.engine.xml.opensaml.exception.MissingCertificateRevocationDataException;
import eu.eidas.auth.engine.xml.opensaml.exception.UntrustedCertificateException;

public interface CertificateTrustVerifier {

    /**
     * Verify if the certificate is trusted (verify path can be build up to a trust anchor and check the revocation data of certificates).
     *
     * @param target the {@link X509Credential} to be evaluated
     * @param trustAnchors the list of certificates that are trusted by the node (aka. trust anchors)
     * @param params the configuration for revocation checking
     * @throws CertificateRevokedException if one or more certificate is revoked.
     * @throws MissingCertificateRevocationDataException if one or more revocation data is missing (during validation) for certificate that must be checked.
     * @throws UntrustedCertificateException if the credential is not trusted.
     */
    void verify(@Nonnull final X509Credential target, @Nonnull final Collection<X509Certificate> trustAnchors, @Nonnull final CertificateVerifierParams params) throws CertificateRevokedException, MissingCertificateRevocationDataException, UntrustedCertificateException;

}
