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
package eu.eidas.auth.engine.core;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.engine.metadata.HighLevelMetadataParamsI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.ZonedDateTime;
import java.util.SortedSet;

/**
 * The {@code ProtocolProcessorI} interface is the low-level interface responsible for generating the correct protocol
 * message bytes and instantiating the correct implementations of {@link IAuthenticationRequest} and {@link
 * IAuthenticationResponse}.
 *
 * @since 1.1
 */
public interface ProtocolProcessorI {

    /**
     * Checks whether all mandatory attributes for the person type are present
     *
     * @param immutableAttributeMap the attribute map.
     * @return true if all mandatory attributes for the person type are present
     */
    boolean checkMandatoryAttributes(@Nullable ImmutableAttributeMap immutableAttributeMap);

    /**
     * Checks if representation response rules are respected.
     *
     * @param immutableAttributeMap the attribute map of attributes.
     * @return true if representation response rules are respected, false otherwise
     */
    boolean checkRepresentationResponse(@Nullable ImmutableAttributeMap  immutableAttributeMap);

    /**
     * Checks whether the attribute map satisfy the rules of representation
     *
     * @param immutableAttributeMap the attribute map.
     * @return true or false
     */
    boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap  immutableAttributeMap);

    /**
     * Ensures that the generated request is complete and is not missing information required by this protocol.
     *
     * @param request the current request
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    void checkRequestSanity(@Nonnull IAuthenticationRequest request) throws EIDASSAMLEngineException;

    /**
     * configuration for the generator and processor
     */
    void configure(ProtocolSignerI protocolSigner);

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
    SortedSet<AttributeDefinition<?>> getAllSupportedAttributes();

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
     * Verifies if the incoming request (received by the Proxy Service) is valid and conform.
     *
     * @param authnRequest the incoming request
     * @param httpMethod the current HTTP method (verb) or {@code null} if the HTTP binding is not relevant
     * @return {@code true} if the given request is valid and can be processed, returns {@code false} otherwise.
     * @throws EIDASSAMLEngineException in case of errors
     */
    boolean isAcceptableHttpRequest(@Nonnull IAuthenticationRequest authnRequest, @Nullable String httpMethod)
            throws EIDASSAMLEngineException;

    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML error response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @param request the request data
     * @param response the response data
     * @param ipAddress the ip address
     * @param samlCoreProperties the saml engine's core properties
     * @param currentTime the current time
     *
     * @since 1.4
     *
     * @return the response
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    Response marshallErrorResponse(@Nonnull IAuthenticationRequest request,
                                   @Nonnull IAuthenticationResponse response,
                                   @Nonnull String ipAddress,
                                   @Nonnull SamlEngineCoreProperties samlCoreProperties,
                                   @Nonnull final ZonedDateTime currentTime)
            throws EIDASSAMLEngineException;

    /**
     * Validates and completes if necessary the given {@link IAuthenticationRequest} which is to be sent by this
     * processor (e.g. acting as a Connector).
     * <p>
     * In particular, it is expected that this method generates a new instance of an {@link IAuthenticationRequest}
     * customized for the protocol in use and that the returned instance possesses a newly generated ID, independent of
     * the ID of the given {@code request}.
     * <p>
     * The returned instance is complete and ready to be marshalled by {@link #marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, ZonedDateTime)}
     * <p>
     * If the given {@link IAuthenticationRequest} is not valid, throws an EIDASSAMLEngineException.
     *
     * @param requestToBeSent the request data
     * @param serviceIssuer the issuer of a request
     * @param samlCoreProperties the saml engine's core properties
     *
     * @since 1.1.1
     *
     * @return the request
     * @throws EIDASSAMLEngineException if the request could not be generated
     *
     */
    @Nonnull
    IAuthenticationRequest createProtocolRequestToBeSent(@Nonnull IAuthenticationRequest requestToBeSent,
                                                         @Nonnull String serviceIssuer,
                                                         @Nonnull SamlEngineCoreProperties samlCoreProperties)
            throws EIDASSAMLEngineException;

    /**
     * Converts an {@link IEidasAuthenticationRequest} into the appropriate SAML request.
     * <p>
     * The returned SAML request is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @param eidasRequestToBeSent the eidas request data
     * @param serviceIssuer the issuer of a request
     * @param samlCoreProperties the saml engine's core properties
     * @param currentTime the current time
     *
     * @since 2.5
     *
     * @return the request
     * @throws EIDASSAMLEngineException if the request could not be generated
     */
    @Nonnull
    AuthnRequest marshallRequest(@Nonnull IEidasAuthenticationRequest eidasRequestToBeSent,
                                 @Nonnull String serviceIssuer,
                                 @Nonnull SamlEngineCoreProperties samlCoreProperties,
                                 @Nonnull final ZonedDateTime currentTime) throws EIDASSAMLEngineException;
    
    /**
     * Converts the given kind of {@link IAuthenticationResponse} into the appropriate SAML response.
     * <p>
     * The returned SAML response is not encrypted and is not signed, encryption and signature are handled by the {@link
     * eu.eidas.auth.engine.ProtocolEngineI} itself.
     *
     * @param request the request data
     * @param response the response data
     * @param ipAddress the ip address
     * @param samlCoreProperties the saml engine's core properties
     * @param currentTime the current time
     *
     * @since 1.4
     *
     * @return the response
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    Response marshallResponse(@Nonnull IAuthenticationRequest request,
                              @Nonnull IAuthenticationResponse response,
                              @Nonnull String ipAddress,
                              @Nonnull SamlEngineCoreProperties samlCoreProperties,
                              @Nonnull final ZonedDateTime currentTime) throws EIDASSAMLEngineException;



    /**
     *
     * @param errorResponse the response data
     * @param samlErrorResponse the response
     * @param ipAddress the ip address
     * @param coreProperties the saml engine's core properties
     *
     * @since 1.1
     *
     * @return the response data
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    IAuthenticationResponse unmarshallErrorResponse(@Nonnull IAuthenticationResponse errorResponse,
                                                    @Nonnull Response samlErrorResponse,
                                                    @Nonnull String ipAddress,
                                                    @Nonnull SamlEngineCoreProperties coreProperties)
            throws EIDASSAMLEngineException;

    /**
     *
     * @param citizenCountryCode the citizen Country Code
     * @param samlRequest the request data
     * @param originCountryCode the country code
     *
     * @since 1.1
     *
     * @return the request data
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    IAuthenticationRequest unmarshallRequest(@Nonnull String citizenCountryCode,
                                             @Nonnull AuthnRequest samlRequest,
                                             @Nullable String originCountryCode) throws EIDASSAMLEngineException;

    /**
     * Converts the given SAML response into the appropriate kind of {@link IAuthenticationResponse}.
     *
     * @param response the response data
     * @param verifyBearerIpAddress true, if is IP validation
     * @param userIpAddress the ip address
     * @param beforeSkewTimeInMillis the skew time for notBefore (value to be added)
     * @param afterSkewTimeInMillis  the skew time for notOnOrAfter (value to be added)
     * @param now the current time
     * @param audienceRestriction the restriction based on URI
     *
     * @since 1.1
     *
     * @return the response data
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    IAuthenticationResponse unmarshallResponse(@Nonnull Response response,
                                               boolean verifyBearerIpAddress,
                                               @Nullable String userIpAddress,
                                               long beforeSkewTimeInMillis,
                                               long afterSkewTimeInMillis,
                                               @Nonnull ZonedDateTime now,
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

    public String getCountryCode(SignableSAMLObject signableSAMLObject)throws EIDASSAMLEngineException;

    /**
     * Returns the metadata EntityDescriptor fetched from the given URL
     * with the fetcher, signer and clock as configured in the saml engine
     *
     * @param metadataUrl metadata URL
     * @return metadata EntityDescriptor as {@link HighLevelMetadataParamsI} extends
     * @throws EIDASSAMLEngineException
     */
    @Nonnull
    HighLevelMetadataParamsI getMetadataParameters(@Nonnull String metadataUrl) throws EIDASSAMLEngineException;

    /**
     * Evaluates if the metadata cache needs to be prematurely refreshed.
     * Returns the metadata EntityDescriptor fetched from the origin of the SAML Message
     * with the fetcher, signer and clock as configured in the saml engine
     *
     * @param samlMessage incoming SAML Request or Response
     * @return metadata EntityDescriptor as {@link HighLevelMetadataParamsI} extends
     * @throws EIDASSAMLEngineException
     */
    @Nonnull
    HighLevelMetadataParamsI getMetadataParameters(SignableSAMLObject samlMessage) throws EIDASSAMLEngineException;
}
