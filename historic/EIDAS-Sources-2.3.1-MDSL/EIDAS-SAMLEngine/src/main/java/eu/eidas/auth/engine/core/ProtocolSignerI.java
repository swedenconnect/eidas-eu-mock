/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.auth.engine.core;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import org.opensaml.security.SecurityException;
import org.opensaml.security.x509.X509Credential;
import org.opensaml.xmlsec.signature.RSAKeyValue;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Represents the abstraction responsible for performing digital signatures and verifying them.
 * <p>
 * The {@code ProtocolSignerI} is used to sign requests, responses and assertions but not metadata.
 *
 * @since 1.1
 */
public interface ProtocolSignerI {

    /**
     * Returns the X.509 certificate used to perform the digital signature of SAML requests and responses.
     * <p>
     * The returned credential must not contain a private key but only the public certificate.
     * <p>
     * This certificate is the one to be exposed in the metadata content as signing certificate.
     *
     * @return the credential used to perform the digital signature of SAML requests and responses.
     */
    @Nonnull
    X509Credential getPublicSigningCredential();

    /**
     * Signs the given object (an Assertion, a Request or a Response) with the signature key.
     * <p>
     * This method MUST not be used to sign the metadata, to sign the metadata, use {@link
     * eu.eidas.auth.engine.metadata.MetadataSignerI#signMetadata(SignableXMLObject)} instead.
     *
     * @param signableObject the SAML object to sign (an Assertion, a Request or a Response).
     * @param onlyKeyInfoNoCert flag to put only the RSAKeyValue instead of the full X509Data in the Signature.
     * @param <T> the type of the XML object to sign
     * @return the signed SAML object (an Assertion, a Request or a Response)
     * @throws EIDASSAMLEngineException in case of signature errors
     */
    @Nonnull
    <T extends SignableXMLObject> T sign(@Nonnull T signableObject, boolean onlyKeyInfoNoCert) throws EIDASSAMLEngineException;

    /**
     * Signs the given object (an Assertion, a Request or a Response) with the signature key.
     * <p>
     * This method MUST not be used to sign the metadata, to sign the metadata, use {@link
     * eu.eidas.auth.engine.metadata.MetadataSignerI#signMetadata(SignableXMLObject)} instead.
     *
     * @param signableObject the SAML object to sign (an Assertion, a Request or a Response).
     * @param <T> the type of the XML object to sign
     * @return the signed SAML object (an Assertion, a Request or a Response)
     * @throws EIDASSAMLEngineException in case of signature errors
     */
    @Nonnull
    <T extends SignableXMLObject> T sign(@Nonnull T signableObject) throws EIDASSAMLEngineException;

    /**
     * Validates the digital signature present in the given signed object (an Assertion, a Request or a Response).
     * <p>
     * This method MUST not be used to validate the signature of a metadata, to do so use {@link
     * eu.eidas.auth.engine.metadata.MetadataSignerI#validateMetadataSignature(SignableXMLObject)} instead.
     * <p>
     * The optional parameter {code trustedCertificates} can be used to specify a given Collection of trusted
     * certificates the given signed object must have been signed with when this can be known in advance, for instance
     * when a metadata is used.
     * <p>
     * If the Collection of trusted signing certificates cannot be known or when the implementation does not rely on a
     * dynamic or external trust source, {@code null} or an empty Collection can be passed as argument for the {code
     * trustedCertificates}.
     *
     * @param signedObject the signed XML object to verify (an Assertion, a Request or a Response)
     * @param trustedCertificates the collection of trusted certificates the signature must have been performed with
     * according to the MetaData for example, or {@code null} or an empty collection to use the internal trust instead.
     * @param <T> the type of the signed XML object of which the signature must be verified
     * @return the signed SAML object (an Assertion, a Request or a Response)
     * @throws EIDASSAMLEngineException in case of an invalid signature
     */
    @Nonnull
    <T extends SignableXMLObject> T validateSignature(@Nonnull T signedObject,
                                                      @Nullable Collection<X509Certificate> trustedCertificates)
            throws EIDASSAMLEngineException;

    /**
     * Returns whether to sign response assertions or not.
     *
     * @return {@code true} when response assertions must be signed, returns {@code false} otherwise..
     */
    boolean isResponseSignAssertions();

    /**
     * Returns whether to sign request only with the Key , or use the certificate instead
     *
     * @return {@code true} when sign request only with the Key , or use the certificate instead
     */
    boolean isRequestSignWithKey();

    /**
     * Returns whether to sign response only with the Key , or use the certificate instead
     *
     * @return {@code true} when sign response only with the Key , or use the certificate instead
     */
    boolean isResponseSignWithKey();


	X509Credential getTrustedCertificateFromRSAKeyValue(RSAKeyValue signatureRsaKeyValue) throws SecurityException, EIDASSAMLEngineException;
}
