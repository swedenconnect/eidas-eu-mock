/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and    limitations under the License.
 */
package eu.eidas.node.auth.specific;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.PersonType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IEIDASConfigurationProxy;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidSessionEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.engine.Correlated;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.auth.specific.IAUService;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.WhitelistUtil;

/**
 * This class is specific and should be modified by each member state if they want to use any different settings.
 */
@SuppressWarnings("PMD")
public final class SpecificEidasService implements IAUService {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SpecificEidasService.class);

    private static final String NOT_AVAILABLE_COUNTRY = "NA";

    /**
     * Specific configurations.
     */
    private IEIDASConfigurationProxy specificProps;

    private Properties serviceProperties;

    private ProtocolEngineFactory protocolEngineFactory;

    private String serviceMetadataUrl;

    private String serviceRequesterMetadataUrl;

    private Boolean serviceMetadataActive;

    private String callBackURL;

    private CorrelationMap<StoredLightRequest> proxyServiceRequestCorrelationMap;

    private CorrelationMap<StoredAuthenticationRequest> specificIdpRequestCorrelationMap;

    private String idpMetadataUrl;

    private String idpMetadataWhitelist;

    public String getIdpMetadataWhitelist() {
		return idpMetadataWhitelist;
	}

	public void setIdpMetadataWhitelist(String idpMetadataWhitelist) {
		this.idpMetadataWhitelist = idpMetadataWhitelist;
	}

	public ProtocolEngineFactory getProtocolEngineFactory() {
        return protocolEngineFactory;
    }

    public void setProtocolEngineFactory(ProtocolEngineFactory protocolEngineFactory) {
        this.protocolEngineFactory = protocolEngineFactory;
    }

    public Properties getServiceProperties() {
        return serviceProperties;
    }

    public void setServiceProperties(Properties nodeProps) {
        this.serviceProperties = nodeProps;
    }

    private ProtocolEngineI getProtocolEngine() {
        return getProtocolEngineFactory().getProtocolEngine(getSamlEngine());
    }

    private String samlEngine;

    public String getSamlEngine() {
        return samlEngine;
    }

    public void setSamlEngine(String samlEngine) {
        this.samlEngine = samlEngine;
    }

    public IEIDASConfigurationProxy getSpecificProps() {
        return specificProps;
    }

    public void setSpecificProps(IEIDASConfigurationProxy specificProps) {
        this.specificProps = specificProps;
    }

    public String getServiceMetadataUrl() {
        return serviceMetadataUrl;
    }

    public void setServiceMetadataUrl(String serviceMetadataUrl) {
        this.serviceMetadataUrl = serviceMetadataUrl;
    }

    public Boolean getServiceMetadataActive() {
        return serviceMetadataActive;
    }

    public void setServiceMetadataActive(Boolean serviceMetadataActive) {
        this.serviceMetadataActive = serviceMetadataActive;
    }

    public String getServiceRequesterMetadataUrl() {
        return serviceRequesterMetadataUrl;
    }

    public void setServiceRequesterMetadataUrl(String serviceRequesterMetadataUrl) {
        this.serviceRequesterMetadataUrl = serviceRequesterMetadataUrl;
    }

    @Override
    public CorrelationMap<StoredLightRequest> getProxyServiceRequestCorrelationMap() {
        return proxyServiceRequestCorrelationMap;
    }

    public void setProxyServiceRequestCorrelationMap(CorrelationMap<StoredLightRequest> proxyServiceRequestCorrelationMap) {
        this.proxyServiceRequestCorrelationMap = proxyServiceRequestCorrelationMap;
    }

    @Override
    public CorrelationMap<StoredAuthenticationRequest> getSpecificIdpRequestCorrelationMap() {
        return specificIdpRequestCorrelationMap;
    }

    public void setSpecificIdpRequestCorrelationMap(CorrelationMap<StoredAuthenticationRequest> specificIdpRequestCorrelationMap) {
        this.specificIdpRequestCorrelationMap = specificIdpRequestCorrelationMap;
    }

    @Override
    public String getCallBackURL() {
        return callBackURL;
    }

    public void setCallBackURL(String callBackURL) {
        this.callBackURL = callBackURL;
    }

    public String getIdpMetadataUrl() {
        return idpMetadataUrl;
    }

    public void setIdpMetadataUrl(String idpMetadataUrl) {
        this.idpMetadataUrl = idpMetadataUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] prepareCitizenAuthentication(ILightRequest lightRequest,
                                               ImmutableAttributeMap modifiedRequestedAttributes,
                                               Map<String, Object> parameters,
                                               Map<String, Object> attrHeaders) {

        String destination = (String) parameters.get(EidasParameterKeys.IDP_URL.toString());
        String citizenCountryCode = (String) parameters.get(EidasParameterKeys.CITIZEN_COUNTRY_CODE.toString());
        String citizenIpAddress = (String) parameters.get(EidasParameterKeys.CITIZEN_IP_ADDRESS.toString());
        String serviceProviderName = (String) parameters.get(EidasParameterKeys.SERVICE_PROVIDER_NAME.toString());
        String eidasLoa = (String) parameters.get(EidasParameterKeys.EIDAS_SERVICE_LOA.toString());
        String eidasNameidFormat = (String) parameters.get(EidasParameterKeys.EIDAS_NAMEID_FORMAT.toString());
        String serviceProviderType = (String) parameters.get(EidasParameterKeys.SERVICE_PROVIDER_TYPE.toString());

        return generateAuthenticationRequest(lightRequest, modifiedRequestedAttributes, destination,
                eidasLoa, eidasNameidFormat, citizenCountryCode, citizenIpAddress,
                serviceProviderName, serviceProviderType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AuthenticationExchange processAuthenticationResponse(byte[] responseFromIdp) {

        try {

            ProtocolEngineI protocolEngine = getProtocolEngine();
            Correlated idpSamlResponse = protocolEngine.unmarshallResponse(responseFromIdp,WhitelistUtil.metadataWhitelist(idpMetadataWhitelist), true);

            String specificRequestId = idpSamlResponse.getInResponseToId();

            if (StringUtils.isBlank(specificRequestId)) {
                LOG.error("Invalid IdP Response \"" + idpSamlResponse.getId() + "\": inResponseTo request not found \""
                        + specificRequestId + "\"");
                throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.INVALID_SESSION.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_SESSION.errorMessage()));
            }

            StoredAuthenticationRequest specificRequest = specificIdpRequestCorrelationMap.get(specificRequestId);
            //clean up
            specificIdpRequestCorrelationMap.remove(specificRequestId);

            if (null == specificRequest) {
                LOG.info("BUSINESS EXCEPTION : Session is missing or invalid!");
                throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.INVALID_SESSION.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_SESSION.errorMessage()));
            }

            IAuthenticationResponse authenticationResponse =
                    protocolEngine.validateUnmarshalledResponse(idpSamlResponse, specificRequest.getRemoteIpAddress(),
                            0, 0, null);// Skew time from IDP is set to 0
            validateSpecificResponse(authenticationResponse, specificRequest);

            return new AuthenticationExchange(specificRequest, authenticationResponse);

        } catch (EIDASSAMLEngineException e) {
            String code = "0";
            String message = "Validation Autentication Response.";
            EidasErrorKey err = null;
            if (EidasErrorKey.isErrorCode(e.getErrorCode())) {
                err = EidasErrorKey.fromCode(e.getErrorCode());
                message = EidasErrors.get(err.errorMessage());
                code = EidasErrors.get(err.errorCode());
            }
            LOG.info("ERROR : Error validating SAML Autentication Response from IdP", e.getMessage());
            LOG.debug("ERROR : Error validating SAML Autentication Response from IdP", e);
            if (err != null && !err.isShowToUser()) {
                throw new EIDASServiceException(
                        EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorCode()),
                        EidasErrors.get(EidasErrorKey.IDP_SAML_RESPONSE.errorMessage()));
            } else {
                throw new EIDASServiceException(code, message, e, "");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] generateErrorAuthenticationResponse(String inResponseTo,
                                                      String issuer,
                                                      String assertionConsumerServiceURL,
                                                      String code,
                                                      String subcode,
                                                      String message,
                                                      String ipUserAddress) {
        byte[] responseBytes;
        try {
            // create SAML token
            EidasAuthenticationRequest.Builder request = new EidasAuthenticationRequest.Builder();
            request.id(inResponseTo);
            request.issuer(issuer);
            request.assertionConsumerServiceURL(assertionConsumerServiceURL);
            AuthenticationResponse.Builder error = new AuthenticationResponse.Builder();
            error.statusCode(code);
            error.subStatusCode(subcode);
            error.statusMessage(message);

            IResponseMessage responseMessage = generateResponseErrorMessage(request.build(), ipUserAddress, getProtocolEngine(), error);

            responseBytes = responseMessage.getMessageBytes();
        } catch (EIDASSAMLEngineException e) {
            LOG.info("ERROR : Error generating SAMLToken", e.getMessage());
            LOG.debug("ERROR : Error generating SAMLToken", e);
            throw new InternalErrorEIDASException("0", "Error generating SAMLToken", e);
        }
        return responseBytes;
    }

    private IResponseMessage generateResponseErrorMessage(IAuthenticationRequest authData, String ipUserAddress, ProtocolEngineI engine, AuthenticationResponse.Builder eidasAuthnResponseError) throws EIDASSAMLEngineException {
        final List<String> includeAssertionApplicationIdentifiers = getIncludeAssertionApplicationIdentifiers();
        return  engine.generateResponseErrorMessage(authData, eidasAuthnResponseError.build(), ipUserAddress, includeAssertionApplicationIdentifiers);
    }

    private List<String> getIncludeAssertionApplicationIdentifiers() {
        String property = serviceProperties.getProperty(EidasParameterKeys.INCLUDE_ASSERTION_FAIL_RESPONSE_APPLICATION_IDENTIFIERS.toString());
        return EidasStringUtil.getTokens(property);
    }

    private boolean haveExpectedName(ImmutableAttributeMap original, URI nameUri, int arraySize) {
        boolean attrNotFound = true;
        for (int i = 1; i <= arraySize; i++) {
            String derivedId = specificProps.getEidasParameterValue(EIDASValues.DERIVE_ATTRIBUTE.index(i));
            String derivedUri = specificProps.getEidasParameterValue(EIDASValues.DERIVE_ATTRIBUTE.uri(i));
            if (nameUri != null && nameUri.toASCIIString().equals(derivedUri) && original.getDefinitionsByFriendlyName(derivedId) != null) {
                attrNotFound = false;
            }
        }
        return attrNotFound;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compareAttributeLists(ImmutableAttributeMap original, ImmutableAttributeMap modified) {

        if (original == null || modified == null) {
            LOG.info("ERROR : At least one list is null!");
            return false;
        }

        int nNames = Integer.parseInt(
                specificProps.getEidasParameterValue(EidasParameterKeys.DERIVE_ATTRIBUTE_NUMBER.toString()));

        for (final AttributeDefinition<?> attributeDefinition : modified.getDefinitions()) {

            if (original.getDefinitionByNameUri(attributeDefinition.getNameUri()) == null
                    && !PersonType.REPV_LEGAL_PERSON.equals(attributeDefinition.getPersonType())
                    && !PersonType.REPV_NATURAL_PERSON.equals(attributeDefinition.getPersonType())) {
                if (haveExpectedName(original, attributeDefinition.getNameUri(), nNames)) {
                    LOG.info("ERROR : Element is not present on original list: " + attributeDefinition.getNameUri());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Generates a SAML Request.
     *
     * @param destination                 The URL of destination.
     * @param modifiedRequestedAttributes request these from IdP
     * @return byte[] containing the SAML Request.
     */
    private byte[] generateAuthenticationRequest(ILightRequest lightRequest,
                                                 ImmutableAttributeMap modifiedRequestedAttributes,
                                                 String destination,
                                                 String eidasLoa,
                                                 String eidasNameidFormat,
                                                 String citizenCountryCode,
                                                 String citizenIpAddress,
                                                 String serviceProviderName,
                                                 String serviceProviderType) {
        try {
            EidasAuthenticationRequest.Builder builder = EidasAuthenticationRequest.builder();

            //Technical specification related : Level of assurance hardcoded to minimum
            builder.levelOfAssuranceComparison(LevelOfAssuranceComparison.MINIMUM.stringValue());
            builder.assertionConsumerServiceURL(null);
            builder.binding(EidasSamlBinding.EMPTY.getName());
            if (getServiceMetadataActive() && StringUtils.isNotBlank(getServiceRequesterMetadataUrl())) {
                builder.issuer(getServiceRequesterMetadataUrl());
            }
            builder.serviceProviderCountryCode(NOT_AVAILABLE_COUNTRY);
            builder.spType(serviceProviderType);
            builder.destination(destination);
            builder.providerName(serviceProviderName);
            builder.requestedAttributes(modifiedRequestedAttributes);
            builder.nameIdFormat(eidasNameidFormat);

            //Adding missing mandatory values
            builder.id(SAMLEngineUtils.generateNCName());
            builder.citizenCountryCode(citizenCountryCode);
            builder.levelOfAssurance(eidasLoa);

            IRequestMessage generatedSpecificRequest = getProtocolEngine().generateRequestMessage(builder.build(), getIdpMetadataUrl());

            IAuthenticationRequest generatedRequest = generatedSpecificRequest.getRequest();

            String specificRequestSamlId = generatedRequest.getId();

            // store the correlation between the specific request ID and the original ILightRequest
            proxyServiceRequestCorrelationMap.put(specificRequestSamlId, StoredLightRequest.builder()
                    .remoteIpAddress(citizenIpAddress)
                    .request(lightRequest)
                    .build());

            // store the correlation between the specific request ID and the corresponding specific request instance
            specificIdpRequestCorrelationMap.put(specificRequestSamlId,
                    new StoredAuthenticationRequest.Builder().request(generatedRequest)
                            .remoteIpAddress(citizenIpAddress)
                            .build());
            return generatedSpecificRequest.getMessageBytes();

        } catch (EIDASSAMLEngineException e) {
            LOG.info("Error generating SAML Token for Authentication Request", e.getMessage());
            LOG.debug("Error generating SAML Token for Authentication Request", e);
            throw new InternalErrorEIDASException("0", "error genereating SAML Token for Authentication Request", e);
        }
    }

    /**
     * Validates a given {@link AuthenticationResponse}.
     *
     * @param specificResponse The {@link AuthenticationResponse} to validate.
     */
    private StoredAuthenticationRequest validateSpecificResponse(IAuthenticationResponse specificResponse,
                                                                 StoredAuthenticationRequest specificRequest) {
        String issuer = specificRequest.getRequest().getIssuer();
        String audienceRestriction = specificResponse.getAudienceRestriction();

        if (audienceRestriction != null && !audienceRestriction.equals(issuer)) {
            LOG.error("Mismatch in response AudienceRestriction=\"" + audienceRestriction + "\" vs request issuer=\""
                    + issuer + "\"");
            throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.SESSION.errorCode()),
                    EidasErrors.get(EidasErrorKey.SESSION.errorMessage()));
        }
        return specificRequest;
    }
}
