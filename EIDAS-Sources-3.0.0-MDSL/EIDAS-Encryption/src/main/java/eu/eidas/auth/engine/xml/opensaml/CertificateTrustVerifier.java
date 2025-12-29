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
