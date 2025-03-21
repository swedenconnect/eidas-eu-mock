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
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import javax.annotation.Nonnull;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Interface responsible for signing and verifying Metadata objects.
 *
 * @since 1.1
 */
public interface MetadataSignerI {

    /**
     * Signs the metadata with the metadata signature key, which is possibly a distinct key than the signature key.
     * <p>
     * This method MUST only be used to sign the metadata, do not use it to sign an Assertion, a Request or a Response.
     * <p>
     *
     * @param signableObject the metadata object to sign.
     * @param <T>            the type of the XML object to sign
     * @return the signed metadata
     * @throws EIDASMetadataException in case of signature errors
     */
    @Nonnull
    <T extends SignableXMLObject> T signMetadata(@Nonnull T signableObject) throws EIDASMetadataException;

    /**
     * Validates the digital signature present in the given signed metadata.
     *
     * @param signedMetadata the signed metadata to verify
     * @param <T>            the type of the signed XML object of which the signature must be verified
     * @return the signed SAML object
     * @throws EIDASMetadataException in case of an invalid signature
     */
    @Nonnull
    <T extends SignableXMLObject> T validateMetadataSignature(@Nonnull T signedMetadata)
            throws EIDASMetadataException;

    /**
     * Tries to match the trust chain in cached metadata against the trust store
     *
     * @param trustChain the list of certificates forming the trust chain of the cached metadata
     * @return {@code true} if at least one certificate in the trust chain matches a certificate in the trust store, {@code false} otherwise
     */
    @Nonnull
    boolean checkMetadataTrustAnchorAgainstTrustStore(@Nonnull List<X509Certificate> trustChain);
}
