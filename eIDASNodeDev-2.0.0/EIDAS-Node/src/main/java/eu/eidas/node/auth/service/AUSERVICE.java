/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
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
package eu.eidas.node.auth.service;

import java.security.InvalidParameterException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.utils.EidasNodeValidationUtil;

/**
 * The AUSERVICE class deals with the requests coming from the Connector. This class communicates with the IdP and APs
 * in order to authenticate the citizen, validate the attributes provided by him/her, and to request the values of the
 * citizen's attributes.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.82 $, $Date: 2011-07-07 20:53:51 $
 * @see ISERVICEService
 */
public final class AUSERVICE implements ISERVICEService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUSERVICE.class.getName());

    /**
     * Service for citizen related operations.
     */
    private ISERVICECitizenService citizenService;

    /**
     * Service for SAML related operations.
     */
    private ISERVICESAMLService samlService;

    /**
     * Service's Util class.
     */
    private AUSERVICEUtil serviceUtil;

    private String serviceMetadataUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    public IAuthenticationRequest processAuthenticationRequest(@Nonnull WebRequest webRequest,
                                                               @Nullable String relayState,
                                                               @Nonnull
                                                                       CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap,
                                                               @Nonnull String remoteIpAddress) {

        String stringSamlToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);

        if (stringSamlToken == null) {
            LOG.info("BUSINESS EXCEPTION : SAML Token is null");
            throw new InvalidParameterEIDASException(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
        }

        byte[] samlToken = EidasStringUtil.decodeBytesFromBase64(stringSamlToken);

        // validate samlToken and populate AuthenticationData
        IAuthenticationRequest authnRequest =
                samlService.processConnectorRequest(webRequest.getMethod().getValue(), samlToken, remoteIpAddress,
                                                    relayState);

        LOG.trace("Validating destination");
        NormalParameterValidator.paramName(EidasErrorKey.SERVICE_REDIRECT_URL.toString())
                .paramValue(authnRequest.getDestination())
                .validate();

        // TODO: should we add an indirection in the returned SAML Request ID here
        // TODO: to prevent that the SAML Request ID sent to the IdP is the same as the one sent by the Connector

        String updatedRequestId = authnRequest.getId();
        StoredAuthenticationRequest updatedStoredRequest =
                new StoredAuthenticationRequest.Builder().remoteIpAddress(webRequest.getRemoteIpAddress())
                        .request(authnRequest)
                        .build();
        requestCorrelationMap.put(updatedRequestId, updatedStoredRequest);

        citizenService.checkMandatoryAttributes(authnRequest.getRequestedAttributes());

        citizenService.checkRepresentativeAttributes(authnRequest.getRequestedAttributes());

        return authnRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IResponseMessage processIdpResponse(@Nonnull WebRequest webRequest,
                                               @Nonnull StoredAuthenticationRequest proxyServiceRequest,
                                               @Nonnull ILightResponse idpResponse) {

        IAuthenticationRequest originalRequest = proxyServiceRequest.getRequest();

        if (null == idpResponse || idpResponse.getStatus().isFailure()) {
            LOG.info("ERROR : IdP response Personal Attribute List is null!");
            return sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }

        // checks if all mandatory attributes have values.
        if (!samlService.checkMandatoryAttributes(originalRequest.getRequestedAttributes(),
                                                  idpResponse.getAttributes())) {
            LOG.info("BUSINESS EXCEPTION : Mandatory attribute is missing!");
            return sendFailure(proxyServiceRequest, EidasErrorKey.ATT_VERIFICATION_MANDATORY);
        }

        // check minimum data set
        if (!samlService.checkMandatoryAttributeSet(idpResponse.getAttributes())) {
            LOG.info("ERROR : IdP response Personal Attribute List is missing mandatory values!");
            return sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : idpResponse
                .getAttributes().getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            AttributeValueMarshaller<?> attributeValueMarshaller = definition.getAttributeValueMarshaller();
            for (final AttributeValue<?> attributeValue : values) {
                String value;
                try {
                    value = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
                    if (!NormalParameterValidator.paramName(EidasParameterKeys.ATTRIBUTE_VALUE).paramValue(value).isValid()) {
                        throw new InvalidParameterException("Invalid Length value :" + value + " for Parameter " + definition.getFriendlyName() + ". Check also the value for the parameter " + EidasParameterKeys.ATTRIBUTE_VALUE + " in the configuration file " + EidasParameters.getPropertiesFilename());
                    }
                } catch (AttributeValueMarshallingException e) {
                    // TODO improve this:
                    throw new IllegalStateException(e);
                }
            }
        }
        if (!EidasNodeValidationUtil.isLoAValid(LevelOfAssuranceComparison.MINIMUM,
                                                originalRequest.getLevelOfAssurance(),
                                                idpResponse.getLevelOfAssurance())) {
            LOG.error("ERROR : IdP response Level of Assurance is to low: requested="
                             + originalRequest.getLevelOfAssurance(),
                     " vs response=" + idpResponse.getLevelOfAssurance());
            return sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_RESPONSE_LOA_VALUE);
/*            throw new EidasNodeException(
                    EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE.errorMessage()));*/
        }

        // update Response Attributes

        IAuthenticationRequest request = proxyServiceRequest.getRequest();
        ImmutableAttributeMap responseAttributes = idpResponse.getAttributes();

        ImmutableAttributeMap updatedResponseAttributes = updateResponseAttributes(request, responseAttributes);

        AuthenticationResponse.Builder authenticationResponseBuilder = AuthenticationResponse.builder();
        authenticationResponseBuilder.levelOfAssurance(idpResponse.getLevelOfAssurance())
                .attributes(updatedResponseAttributes)
                .inResponseTo(originalRequest.getId())
                .ipAddress(idpResponse.getIPAddress())
                .id(SAMLEngineUtils.generateNCName())
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .issuer(getServiceMetadataUrl())
                .subject(idpResponse.getSubject())
                .subjectNameIdFormat(idpResponse.getSubjectNameIdFormat());
        serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authenticationResponseBuilder);

        String currentIpAddress = webRequest.getRemoteIpAddress();

        return samlService.processIdpSpecificResponse(originalRequest, authenticationResponseBuilder.build(),
                                                      currentIpAddress);
    }

    @Nonnull
    private ImmutableAttributeMap updateResponseAttributes(@Nonnull IAuthenticationRequest request,
                                                           @Nonnull ImmutableAttributeMap responseAttributes) {

        // As per the spec:
        // The uniqueness identifier consists of:
        // 1. The first part is the Nationality Code of the identifier
        // \uF0B7 This is one of the ISO 3166-1 alpha-2 codes, followed by a slash (\u201C/\u201C))
        // 2. The second part is the Nationality Code of the destination country or international organization1
        // \uF0B7 This is one of the ISO 3166-1 alpha-2 codes, followed by a slash (\u201C/\u201C)
        // 3. The third part a combination of readable characters
        // \uF0B7 This uniquely identifies the identity asserted in the country of origin but does not necessarily reveal
        // any discernible correspondence with the subject's actual identifier (for example, username, fiscal number etc)
        // Example: ES/AT/02635542Y (Spanish eIDNumber for an Austrian SP)

        String originCountryCode = request.getOriginCountryCode();
        String proxyServiceCountryCode = samlService.getCountryCode();

        String identifierPrefix = proxyServiceCountryCode + "/" + originCountryCode + "/";

        boolean modified = false;

        ImmutableAttributeMap.Builder updatedResponseAttributes = ImmutableAttributeMap.builder();

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : responseAttributes
                .getAttributeMap()
                .entrySet()) {
            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();
            if (definition.isUniqueIdentifier()) {
                AttributeValueMarshaller<?> attributeValueMarshaller = definition.getAttributeValueMarshaller();
                ImmutableSet.Builder<AttributeValue<?>> updatedValues = ImmutableSet.builder();
                boolean modifiedValues = false;
                for (final AttributeValue<?> attributeValue : values) {
                    String value;
                    try {
                        value = attributeValueMarshaller.marshal((AttributeValue) attributeValue);
                    } catch (AttributeValueMarshallingException e) {
                        // TODO improve this:
                        throw new IllegalStateException(e);
                    }
                    if (!value.startsWith(identifierPrefix)) {
                        modified = true;
                        modifiedValues = true;
                        AttributeValue<?> updated = null;
                        try {
                            updated = attributeValueMarshaller.unmarshal(identifierPrefix + value,
                                                                         attributeValue.isNonLatinScriptAlternateVersion());
                        } catch (AttributeValueMarshallingException e) {
                            // TODO improve this:
                            throw new IllegalStateException(e);
                        }
                        updatedValues.add(updated);
                    } else {
                        updatedValues.add(attributeValue);
                    }
                }
                if (modifiedValues) {
                    values = updatedValues.build();
                }
            }
            updatedResponseAttributes.put(definition, (ImmutableSet) values);
        }

        if (modified) {
            return updatedResponseAttributes.build();
        }

        return responseAttributes;
    }

    private IResponseMessage sendFailure(StoredAuthenticationRequest storedRequest, EidasErrorKey error) {
        IAuthenticationRequest originalRequest = storedRequest.getRequest();
        String errorCode = EidasErrors.get(error.errorCode());
        String errorMessage = EidasErrors.get(error.errorMessage());
        if (null == errorCode) {
            errorCode = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode());
        }
        if (null == errorMessage) {
            errorMessage = EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage());
        }
        byte[] samlTokenFail = samlService.generateErrorAuthenticationResponse(originalRequest,
                                                                               EIDASStatusCode.RESPONDER_URI.toString(),
                                                                               errorCode, null, errorMessage,
                                                                               storedRequest.getRemoteIpAddress(),
                                                                               true);
        throw new ResponseCarryingServiceException(errorCode, errorMessage,
                                                   EidasStringUtil.encodeToBase64(samlTokenFail),
                                                   originalRequest.getAssertionConsumerServiceURL(),
                                                   storedRequest.getRequest().getRelayState());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String generateSamlTokenFail(IAuthenticationRequest authData,
                                        String statusCode,
                                        EidasErrorKey error,
                                        String ipUserAddress) {

        return generateSamlTokenFail(authData, statusCode, EidasErrors.get(error.errorCode()),
                                     EIDASSubStatusCode.REQUEST_DENIED_URI.toString(),
                                     EidasErrors.get(error.errorMessage()), ipUserAddress, false);
    }

    @Override
    public String generateSamlTokenFail(IAuthenticationRequest originalRequest,
                                        String statusCode,
                                        String errorCode,
                                        String subCode,
                                        String errorMessage,
                                        String ipUserAddress,
                                        boolean isAuditable) {
        byte[] samlTokenFail =
                samlService.generateErrorAuthenticationResponse(originalRequest, statusCode, errorCode, subCode,
                                                                errorMessage, ipUserAddress, isAuditable);

        return EidasStringUtil.encodeToBase64(samlTokenFail);
    }

    /**
     * Generates a exception with an embedded SAML token.
     *
     * @param webRequest A map of parameters to generate the error token.
     * @see Map
     */
    private void sendErrorPage(WebRequest webRequest) {
        if (webRequest.getRequestState().getErrorCode() != null) {
            String exErrorCode = EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode());
            String exErrorMessage = EidasErrors.get(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage());
            throw new EIDASServiceException(exErrorCode, exErrorMessage, null);
        }
    }

    /**
     * Setter for citizenService.
     *
     * @param nCitizenService The new citizenService value.
     * @see ISERVICECitizenService
     */
    public void setCitizenService(final ISERVICECitizenService nCitizenService) {
        this.citizenService = nCitizenService;
    }

    /**
     * Getter for citizenService.
     *
     * @return The citizenService value.
     * @see ISERVICECitizenService
     */
    public ISERVICECitizenService getCitizenService() {
        return citizenService;
    }

    /**
     * Setter for samlService.
     *
     * @param nSamlService The new samlService value.
     * @see ISERVICESAMLService
     */
    public void setSamlService(final ISERVICESAMLService nSamlService) {
        this.samlService = nSamlService;
    }

    /**
     * Getter for samlService.
     *
     * @return The samlService value.
     * @see ISERVICESAMLService
     */
    public ISERVICESAMLService getSamlService() {
        return samlService;
    }

    /**
     * Getter for serviceMetadataUrl
     *
     * @return serviceMetadataUrl value
     */
    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    /**
     * Setter for serviceMetadataUrl.
     *
     * @param serviceMetadataUrl The service metadata url value.
     */
    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    /**
     * Getter for serviceUtil
     *
     * @return The serviceUtil value
     * @see AUSERVICEUtil
     */
    public AUSERVICEUtil getServiceUtil() {
        return serviceUtil;
    }

    /**
     * Setter for serviceUtil.
     *
     * @param serviceUtil The new serviceUtil value.
     */
    public void setServiceUtil(AUSERVICEUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

}
