/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core;

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.SamlBindingUri;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.opensaml.saml2.common.Extensions;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;
import org.opensaml.xml.XMLObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

/**
 * Parses or generates a SAML format (either eIDAS or another format).
 *
 * @deprecated since 1.1, use {@link ProtocolProcessorI} instead.
 */
@Deprecated
public interface ExtensionProcessorI {

    /**
     * @deprecated since 1.1
     */
    @Deprecated
    void addRequestedAuthnContext(IAuthenticationRequest request, AuthnRequest authnRequestAux)
            throws EIDASSAMLEngineException;

    /**
     * @param  samlRequest
     * @return a parsed request extracted from the SAML extensions
     * @throws EIDASSAMLEngineException
     * @deprecated since 1.1
     */
    @Nonnull
    @Deprecated
    IAuthenticationRequest processExtensions(@Nonnull String citizenCountryCode,
                                             @Nonnull AuthnRequest samlRequest,
                                             @Nonnull String originCountryCode,
                                             @Nullable X509Certificate trustedCertificate)
            throws EIDASSAMLEngineException;

    /**
     * Converts the given kind of {@link IAuthenticationRequest} into the appropriate SAML request.
     * <p>
     * The returned SAML request is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     */
    @Nonnull
    AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest request,
                                 @Nonnull String serviceIssuer,
                                 @Nonnull SamlEngineCoreProperties samlCoreProperties) throws EIDASSAMLEngineException;

    /**
     * @since 1.1
     */
    @Nonnull
    IAuthenticationRequest unmarshallRequest(@Nonnull String citizenCountryCode,
                                             @Nonnull AuthnRequest samlRequest,
                                             @Nullable String originCountryCode) throws EIDASSAMLEngineException;

    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML error response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     */
    @Nonnull
    Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                   @Nonnull IAuthenticationResponse response,
                                   @Nonnull String ipAddress,
                                   @Nonnull SamlEngineCoreProperties samlCoreProperties)
            throws EIDASSAMLEngineException;

    /**
     * @since 1.1
     */
    @Nonnull
    IAuthenticationResponse unmarshallErrorResponse(@Nonnull IAuthenticationResponse errorResponse,
                                                    @Nonnull Response samlErrorResponse,
                                                    @Nonnull String ipAddress,
                                                    @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException;

    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     */
    @Nonnull
    Response marshallResponse(@Nonnull IAuthenticationRequest request,
                              @Nonnull IAuthenticationResponse response,
                              @Nonnull String ipAddress,
                              @Nonnull SamlEngineCoreProperties samlCoreProperties) throws EIDASSAMLEngineException;

    /**
     * Converts the given SAML response into the appropriate kind of {@link IAuthenticationResponse}.
     *
     * @since 1.1
     */
    @Nonnull
    IAuthenticationResponse unmarshallResponse(@Nonnull Response response,
                                               boolean verifyBearerIpAddress,
                                               @Nullable String userIpAddress,
                                               long beforeSkewTimeInMillis,
                                               long afterSkewTimeInMillis,
                                               @Nonnull DateTime now,
                                               @Nullable String audienceRestriction) throws EIDASSAMLEngineException;

    /**
     * @return the id of the configured request validator
     */
    String getRequestValidatorId();

    /**
     * @return the id of the configured response validator
     */
    String getResponseValidatorId();

    /**
     * @param samlCoreProperties the configuration properties
     * @param request the request for which the extension will be generated
     * @return a SAML extension based on the request
     * @throws EIDASSAMLEngineException
     * @deprecated since 1.1
     */
    @Deprecated
    Extensions generateExtensions(SamlEngineCoreProperties samlCoreProperties, IAuthenticationRequest request)
            throws EIDASSAMLEngineException;

    SAMLExtensionFormat getFormat();

    /**
     * configuration for the generator and processor
     */
    void configureExtension();

    AttributeRegistry getMinimumDataSetAttributes();

    AttributeRegistry getAdditionalAttributes();

    ImmutableSortedSet<AttributeDefinition<?>> getAllSupportedAttributes();

    /**
     * verify if the request is compatible with the processor
     *
     * @param request
     * @return
     */
    boolean isValidRequest(AuthnRequest request);

    /**
     * Validate parameters from authentication request.
     *
     * @param request the request.
     * @param serviceIssuer the target service issuer URI (metadata URL).
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated since 1.1
     */
    @Deprecated
    @Nonnull
    IAuthenticationRequest validateAuthenticationRequest(@Nonnull IAuthenticationRequest request,
                                                         @Nonnull String serviceIssuer) throws EIDASSAMLEngineException;

    /**
     * Ensures that the generated request is complete and is not missing information required by this protocol.
     *
     * @param request the current request
     */
    void checkRequestSanity(IAuthenticationRequest request) throws EIDASSAMLEngineException;

    /**
     * Generate attribute from a list of values.
     *
     * @param attributeDefinition the name of the attribute.
     * @param values the value of the attribute.
     * @return the attribute
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated since 1.1
     */
    @Deprecated
    @Nonnull
    Attribute generateAttrSimple(@Nonnull AttributeDefinition<?> attributeDefinition,
                                 @Nonnull Collection<String> values) throws EIDASSAMLEngineException;

    /**
     * Generate attribute from a map of values.
     * <p/>
     * TODO: this is STORK only.
     *
     * @param attributeDefinition the name of the attribute.
     * @param status the status of the parameter: "Available", "NotAvailable" or "Withheld" (only for STORK).
     * @param values the value of the attribute.
     * @param isHashing the is hashing with "SHA-512" algorithm.
     * @return the attribute
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated since 1.1
     */
    @Deprecated
    Attribute generateAttrComplex(AttributeDefinition<?> attributeDefinition,
                                  String status,
                                  Map<String, String> values,
                                  boolean isHashing) throws EIDASSAMLEngineException;

    @Nullable
    AttributeDefinition<?> getAttributeDefinitionNullable(@Nonnull String name);

    /**
     * Computes SAML binding from http binding
     *
     * @param  request
     * @return
     */
    @Nullable
    String getProtocolBinding(@Nonnull IAuthenticationRequest request, @Nonnull SamlEngineCoreProperties defaultValues);


    /**
     * Checks whether the attribute map contains at least one of the mandatory eIDAS attribute set (either for a natural
     * [person or for a legal person)
     *
     * @param immutableAttributeMap
     */
    boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap);

    @Nonnull
    IAuthenticationRequest updateRequestWithConsent(@Nonnull IAuthenticationRequest authnRequest,
                                                    @Nonnull ImmutableAttributeMap consentedAttributes);

    /**
     * Register the namespace on the request SAML XML object.
     *
     * @param xmlObject
     * @deprecated since 1.1
     */
    @Deprecated
    void registerRequestNamespace(@Nonnull XMLObject xmlObject);

    /**
     * Register the namespace on the response SAML XML object.
     *
     * @param xmlObject
     * @deprecated since 1.1
     */
    @Deprecated
    void registerResponseNamespace(@Nonnull XMLObject xmlObject);

    /**
     * Returns the encryption certificate to be used to encrypt a response for the given requester
     *
     * @return the encryption certificate to be used to encrypt a response for the given requester
     */
    @Nullable
    X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException;

    /**
     * Returns the certificate that must be used to perform the signature validation for the given issuer for a request
     * coming from a Connector (for example, with the SAML metadata role "ServiceProvider").
     *
     * @param issuer the issuer of a request or a response
     * @return the certificate that must be used to perform the signature validation for the given issuer
     * @throws EIDASSAMLEngineException in case of errors
     */
    @Nullable
    X509Certificate getRequestSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException;

    /**
     * Returns the certificate that must be used to perform the signature validation for the given issuer for a response
     * coming from a Proxy Service (for example, with the SAML metadata role "IdentityProvider").
     *
     * @param issuer the issuer of a request or a response
     * @return the certificate that must be used to perform the signature validation for the given issuer
     * @throws EIDASSAMLEngineException in case of errors
     */
    @Nullable
    X509Certificate getResponseSignatureCertificate(@Nonnull String issuer) throws EIDASSAMLEngineException;

    /**
     * Returns the ProxyService URL where to send requests for the given issuer (metadata URL) and SAML binding URI
     * (optional method).
     * <p>
     * This method can return {@code null} when it is not supported.
     *
     * @param issuer the target service issuer (metadata URL)
     * @param bindingUri the kind of SAML binding
     * @return the service URL
     * @throws EIDASSAMLEngineException
     * @since 1.1
     */
    @Nullable
    String getServiceUrl(@Nonnull String issuer, @Nonnull SamlBindingUri bindingUri) throws EIDASSAMLEngineException;

    /**
     * Verifies if the incoming request (received by the Proxy Service) is valid and conform.
     *
     * @param authnRequest the incoming request
     * @param httpMethod the current HTTP method (verb) or {@code null} if the HTTP binding is not relevant
     * @return {@code true} if the given request is valid and can be processed, returns {@code false} otherwise.
     * @throws EIDASSAMLEngineException
     */
    boolean isAcceptableHttpRequest(@Nonnull IAuthenticationRequest authnRequest, @Nullable String httpMethod)
            throws EIDASSAMLEngineException;
}
