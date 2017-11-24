package eu.eidas.auth.engine.metadata;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.xml.security.x509.X509Credential;
import org.opensaml.xml.signature.SignableXMLObject;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Interface responsible for signing and verifying Metadata objects.
 *
 * @since 1.1
 */
public interface MetadataSignerI {

    /**
     * Returns the X.509 certificate used to perform the digital signature of the Metadata content.
     * <p>
     * The returned credential must not contain a private key but only the public certificate.
     * <p>
     * This certificate is the one to be exposed in the list of trusted certificates being used to sign metadata
     * contents.
     *
     * @return the credential used to perform the digital signature of the Metadata content.
     */
    @Nullable
    X509Credential getPublicMetadataSigningCredential();

    /**
     * Signs the metadata with the metadata signature key, which is possibly a distinct key than the signature key.
     * <p>
     * This method MUST only be used to sign the metadata, do not use it to sign an Assertion, a Request or a Response.
     * <p>
     * To sign an Assertion, a Request or a Response, use {@link #sign(SignableXMLObject)} instead.
     *
     * @param signableObject the metadata object to sign.
     * @param <T> the type of the XML object to sign
     * @return the signed metadata
     * @throws EIDASSAMLEngineException in case of signature errors
     */
    @Nonnull
    <T extends SignableXMLObject> T signMetadata(@Nonnull T signableObject) throws EIDASSAMLEngineException;

    /**
     * Validates the digital signature present in the given signed metadata.
     *
     * @param signedMetadata the signed metadata to verify
     * @param <T> the type of the signed XML object of which the signature must be verified
     * @return the signed SAML object
     * @throws EIDASSAMLEngineException in case of an invalid signature
     */
    @Nonnull
    <T extends SignableXMLObject> T validateMetadataSignature(@Nonnull T signedMetadata)
            throws EIDASSAMLEngineException;
}
