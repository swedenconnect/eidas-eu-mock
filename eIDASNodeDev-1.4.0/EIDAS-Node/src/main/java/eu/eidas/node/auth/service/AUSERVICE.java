/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.auth.service;

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.*;
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
import eu.eidas.auth.engine.core.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.engine.core.eidas.spec.RepresentativeLegalPersonSpec;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.utils.EidasNodeValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.Map;

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
                        .relayState(relayState)
                        .request(authnRequest)
                        .build();
        requestCorrelationMap.put(updatedRequestId, updatedStoredRequest);

        citizenService.checkMandatoryAttributes(authnRequest.getRequestedAttributes());

        citizenService.checkRepresentativeAttributes(authnRequest.getRequestedAttributes());

        //TODO START remove check of erroneous attributes after transition period of EID-423
        /* check if there is a tricking requesting erroneous and correct attributes also */
        ImmutableSet<AttributeDefinition<?>> attrDefs = authnRequest.getRequestedAttributes().getDefinitions();
        if ((attrDefs.contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS) && attrDefs.contains(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS)) ||
                (attrDefs.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION) && attrDefs.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER))) {
            LOG.error("BUSINESS EXCEPTION : Both LEGAL_ADDRESS and LEGAL_PERSON_ADDRESS or VAT_REGISTRATION and VAT_REGISTRATION_NUMBER requested");
            throw new EIDASServiceException(EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_ATTRIBUTE_LIST.errorMessage()));
        }
        //TODO END remove check of erroneous attributes after transition period of EID-423

        return authnRequest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IAuthenticationRequest processCitizenConsent(WebRequest webRequest,
                                                        @Nonnull StoredAuthenticationRequest storedRequest,
                                                        boolean askConsentType) {

        IAuthenticationRequest authnRequest = storedRequest.getRequest();
        String remoteAddress = storedRequest.getRemoteIpAddress();

        ImmutableAttributeMap consentedAttributes = authnRequest.getRequestedAttributes();

        if (askConsentType) {
            // construct citizen consent from the request
            CitizenConsent consent =
                    citizenService.getCitizenConsent(webRequest, authnRequest.getRequestedAttributes());

            // checks if all mandatory attributes are present in the consent
            citizenService.processCitizenConsent(consent, storedRequest, webRequest.getRemoteIpAddress());
            // updates the personalAttributeList, removing the attributes
            // without consent
            consentedAttributes =
                    citizenService.filterConsentedAttributes(consent, authnRequest.getRequestedAttributes());
            // If the personalAttributeList is empty then we must show a error
            // message.
            if (consentedAttributes.isEmpty()) {
                LOG.info("BUSINESS EXCEPTION : Attribute List is empty!");
                String errorCode = EidasErrors.get(EidasErrorKey.SERVICE_ATTR_NULL.errorCode());
                String errorMessage = EidasErrors.get(EidasErrorKey.SERVICE_ATTR_NULL.errorMessage());

                byte[] samlTokenFail = samlService.generateErrorAuthenticationResponse(authnRequest,
                                                                                       EIDASStatusCode.RESPONDER_URI.toString(),
                                                                                       errorCode, null, errorMessage,
                                                                                       remoteAddress, false);
                throw new ResponseCarryingServiceException(errorCode, errorMessage,
                                                           EidasStringUtil.encodeToBase64(samlTokenFail),
                                                           authnRequest.getAssertionConsumerServiceURL(),
                                                           storedRequest.getRelayState());
            }
        }

        citizenService.checkMandatoryAttributes(consentedAttributes);

        return citizenService.updateConsentedAttributes(authnRequest, consentedAttributes);
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
            LOG.info("ERROR : IdP response Level of Assurance is to low: requested="
                             + originalRequest.getLevelOfAssurance(),
                     " vs response=" + idpResponse.getLevelOfAssurance());
            return sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_RESPONSE_LOA_VALUE);
        }

       /* EID-423: wrong attribute name was implemented prior to 1.4, backward compatibility if for the Network only to ensure business continuity, the
        *  Specific must Request the right ones in the interface*/
        //TODO START remove check of erroneous attributes after transition period of EID-423
        ImmutableSet<AttributeDefinition<?>> suppliedAttributes = idpResponse.getAttributes().getDefinitions();
        if (suppliedAttributes != null && suppliedAttributes.contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS)) {
            LOG.error("BUSINESS EXCEPTION : "+LegalPersonSpec.Definitions.LEGAL_ADDRESS.getNameUri().toASCIIString()+" found in Response instead of "+LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS.getNameUri().toASCIIString());
            sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }
        if (suppliedAttributes != null && suppliedAttributes.contains(LegalPersonSpec.Definitions.VAT_REGISTRATION)) {
            LOG.error("BUSINESS EXCEPTION : "+LegalPersonSpec.Definitions.VAT_REGISTRATION.getNameUri().toASCIIString()+" found in Response instead of "+LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER.getNameUri().toASCIIString());
            sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }
        if (suppliedAttributes != null && suppliedAttributes.contains(RepresentativeLegalPersonSpec.Definitions.LEGAL_ADDRESS)) {
            LOG.error("BUSINESS EXCEPTION : "+LegalPersonSpec.Definitions.LEGAL_ADDRESS.getNameUri().toASCIIString()+" found in Response instead of "+RepresentativeLegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS.getNameUri().toASCIIString());
            sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }
        if (suppliedAttributes != null && suppliedAttributes.contains(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION)) {
            LOG.error("BUSINESS EXCEPTION : "+LegalPersonSpec.Definitions.VAT_REGISTRATION.getNameUri().toASCIIString()+" found in Response instead of "+RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER.getNameUri().toASCIIString());
            sendFailure(proxyServiceRequest, EidasErrorKey.INVALID_ATTRIBUTE_LIST);
        }
        //TODO END remove check of erroneous attributes after transition period of EID-423


        // update Response Attributes

        IAuthenticationRequest request = proxyServiceRequest.getRequest();
        ImmutableAttributeMap responseAttributes = idpResponse.getAttributes();

        ImmutableAttributeMap updatedResponseAttributes = updateResponseAttributes(request, responseAttributes);

        AuthenticationResponse.Builder authenticationResponseBuilder = AuthenticationResponse.builder();
        authenticationResponseBuilder.levelOfAssurance(idpResponse.getLevelOfAssurance())
                .attributes(updatedResponseAttributes)
                .inResponseTo(originalRequest.getId())
                .ipAddress(idpResponse.getIPAddress());

        authenticationResponseBuilder.id(SAMLEngineUtils.generateNCName())
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .issuer(getServiceMetadataUrl());
        serviceUtil.setMetadatUrlToAuthnResponse(getServiceMetadataUrl(), authenticationResponseBuilder);

        String currentIpAddress = webRequest.getRemoteIpAddress();

        return samlService.processIdpSpecificResponse(originalRequest, authenticationResponseBuilder.build(),
                                                      currentIpAddress, true);
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
            updatedResponseAttributes.put((AttributeDefinition) definition, (ImmutableSet) values);
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
                                                   storedRequest.getRelayState());
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
