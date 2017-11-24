/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
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
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;

/**
 * The {@code ProtocolProcessorI} interface is the low-level interface responsible for generating the correct protocol
 * message bytes and instantiating the correct implementations of {@link IAuthenticationRequest} and {@link
 * IAuthenticationResponse}.
 *
 * @since 1.1
 */
public interface ProtocolProcessorI {

    /**
     * Checks whether the attribute map contains at least one of the mandatory eIDAS attribute set (either for a natural
     * [person or for a legal person)
     *
     * @param immutableAttributeMap the attribute map.
     */
    boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap);

    /**
     * Checks whether the attribute map satisfy the rules of representation
     *
     * @param immutableAttributeMap the attribute map.
     */
    boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap  immutableAttributeMap);

    /**
     * Ensures that the generated request is complete and is not missing information required by this protocol.
     *
     * @param request the current request
     */
    void checkRequestSanity(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException;

    /**
     * configuration for the generator and processor
     */
    void configure();

    /**
     * Returns the registry of sector-specific attributes.
     *
     * @return the registry of sector-specific attributes.
     */
    AttributeRegistry getAdditionalAttributes();

    /**
     * Returns all the attributes supported i.e. both standard and sector-specific attributes.
     *
     * @return all the attributes supported i.e. both standard and sector-specific attributes.
     */
    ImmutableSortedSet<AttributeDefinition<?>> getAllSupportedAttributes();

    /**
     * Looks for the given attribute full name URI in all the supported attributes and returns the matching {@link
     * AttributeDefinition} if any, returns {@code null} otherwise.
     *
     * @param name the full name URI of the attribute to search for
     * @return the matching {@link AttributeDefinition} if any, returns {@code null} otherwise
     */
    @Nullable
    AttributeDefinition<?> getAttributeDefinitionNullable(@Nonnull String name);

    /**
     * Returns the encryption certificate to be used to encrypt a response for the given requester
     *
     * @return the encryption certificate to be used to encrypt a response for the given requester
     */
    @Nullable
    X509Certificate getEncryptionCertificate(@Nullable String requestIssuer) throws EIDASSAMLEngineException;

    /**
     * // TODO to be removed
     *
     * @deprecated since 1.1
     */
    @Deprecated
    SAMLExtensionFormat getFormat();

    /**
     * Returns the registry for standard attributes.
     *
     * @return the registry for standard attributes.
     */
    AttributeRegistry getMinimumDataSetAttributes();

    /**
     * Computes SAML binding from http binding
     *
     * @param request the current request
     * @param defaultValues the core properties
     * @return the SAML protocol binding if any, otherwise {@code null}.
     */
    @Nullable
    String getProtocolBinding(@Nonnull IAuthenticationRequest request, @Nonnull SamlEngineCoreProperties defaultValues);

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
     * @return the id of the configured request validator
     */
    String getRequestValidatorId();

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
     * @return the id of the configured response validator
     */
    String getResponseValidatorId();

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


    /**
     * TODO to be removed
     *
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML error response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     */
    @Nonnull
    @Deprecated
    Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                   @Nonnull IAuthenticationResponse response,
                                   @Nonnull String ipAddress,
                                   @Nonnull SamlEngineCoreProperties samlCoreProperties)
            throws EIDASSAMLEngineException;


    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML error response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.4
     *
     */
    @Nonnull
    Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                   @Nonnull IAuthenticationResponse response,
                                   @Nonnull String ipAddress,
                                   @Nonnull SamlEngineCoreProperties samlCoreProperties,
                                   @Nonnull final DateTime currentTime)
            throws EIDASSAMLEngineException;

    /**
     * Validates and completes if necessary the given {@link IAuthenticationRequest} which is to be sent by this
     * processor (e.g. acting as a Connector).
     * <p>
     * In particular, it is expected that this method generates a new instance of an {@link IAuthenticationRequest}
     * customized for the protocol in use and that the returned instance possesses a newly generated ID, independent of
     * the ID of the given {@code request}.
     * <p>
     * The returned instance is complete and ready to be marshalled by {@link #marshallRequest(IAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}.
     * <p>
     * If the given {@link IAuthenticationRequest} is not valid, throws an EIDASSAMLEngineException.
     *
     * @since 1.1.1
     */
    @Nonnull
    IAuthenticationRequest createProtocolRequestToBeSent(@Nonnull IAuthenticationRequest requestToBeSent,
                                                         @Nonnull String serviceIssuer,
                                                         @Nonnull SamlEngineCoreProperties samlCoreProperties)
            throws EIDASSAMLEngineException;


    /**
     * TODO to be removed
     *
     * Converts the given kind of {@link IAuthenticationRequest} into the appropriate SAML request.
     * <p>
     * The returned SAML request is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallRequest(IAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     */
    @Nonnull
    @Deprecated
    AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest requestToBeSent,
                                 @Nonnull String serviceIssuer,
                                 @Nonnull SamlEngineCoreProperties samlCoreProperties) throws EIDASSAMLEngineException;

    /**
     * Converts the given kind of {@link IAuthenticationRequest} into the appropriate SAML request.
     * <p>
     * The returned SAML request is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.4
     */
    @Nonnull
    AuthnRequest marshallRequest(@Nonnull IAuthenticationRequest requestToBeSent,
                                 @Nonnull String serviceIssuer,
                                 @Nonnull SamlEngineCoreProperties samlCoreProperties,
                                 @Nonnull final DateTime currentTime) throws EIDASSAMLEngineException;

    /**
     * TODO to be removed
     *
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.1
     *
     * @deprecated since 1.4
     * Use {@link ProtocolProcessorI#marshallResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     */
    @Nonnull
    @Deprecated
    Response marshallResponse(@Nonnull IAuthenticationRequest request,
                              @Nonnull IAuthenticationResponse response,
                              @Nonnull String ipAddress,
                              @Nonnull SamlEngineCoreProperties samlCoreProperties) throws EIDASSAMLEngineException;
    
    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @since 1.4
     */
    @Nonnull
    Response marshallResponse(@Nonnull IAuthenticationRequest request,
                              @Nonnull IAuthenticationResponse response,
                              @Nonnull String ipAddress,
                              @Nonnull SamlEngineCoreProperties samlCoreProperties,
                              @Nonnull final DateTime currentTime) throws EIDASSAMLEngineException;



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
     * @since 1.1
     */
    @Nonnull
    IAuthenticationRequest unmarshallRequest(@Nonnull String citizenCountryCode,
                                             @Nonnull AuthnRequest samlRequest,
                                             @Nullable String originCountryCode) throws EIDASSAMLEngineException;

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
     * Updates the given request with the attributes the user consents to send.
     *
     * @param request the current request
     * @param consentedAttributes the attributes the user consents to send.
     * @return the updated request which takes into account the user's consent
     */
    @Nonnull
    IAuthenticationRequest updateRequestWithConsent(@Nonnull IAuthenticationRequest request,
                                                    @Nonnull ImmutableAttributeMap consentedAttributes);


}
