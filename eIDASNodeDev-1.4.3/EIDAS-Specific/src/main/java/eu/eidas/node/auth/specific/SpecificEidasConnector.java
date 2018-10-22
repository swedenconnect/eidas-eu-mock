package eu.eidas.node.auth.specific;

import static eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils.generateNCName;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;
import java.util.Properties;

import eu.eidas.auth.commons.EidasStringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IEIDASConfigurationProxy;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidSessionEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.BinaryAuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.specific.IAUConnector;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.WhitelistUtil;

/**
 * This class is specific on the connector side and should be modified by each member state if they want to use any
 * different settings.
 *
 * @since 1.1
 */
public class SpecificEidasConnector implements IAUConnector {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificEidasConnector.class);

    private String samlEngine;

    /**
     * The sp return URL
     */
    private String spUrl;

    /**
     * Specific configurations.
     */
    private IEIDASConfigurationProxy specificProps;

    private Properties configs;

    private ProtocolEngineFactory protocolEngineFactory;

    private CorrelationMap<StoredAuthenticationRequest> specificSpRequestCorrelationMap;

    private CorrelationMap<StoredLightRequest> connectorRequestCorrelationMap;

    private MetadataFetcherI metadataFetcher;

    private String spMetadataWhitelist;
    
    public String getSpMetadataWhitelist() {
		return spMetadataWhitelist;
	}

	public void setSpMetadataWhitelist(String spMetadataWhitelist) {
		this.spMetadataWhitelist = spMetadataWhitelist;
	}

	public ProtocolEngineFactory getProtocolEngineFactory() {
        return protocolEngineFactory;
    }

    public void setProtocolEngineFactory(ProtocolEngineFactory protocolEngineFactory) {
        this.protocolEngineFactory = protocolEngineFactory;
    }

    public CorrelationMap<StoredAuthenticationRequest> getSpecificSpRequestCorrelationMap() {
        return specificSpRequestCorrelationMap;
    }

    public void setSpecificSpRequestCorrelationMap(CorrelationMap<StoredAuthenticationRequest> specificSpRequestCorrelationMap) {
        this.specificSpRequestCorrelationMap = specificSpRequestCorrelationMap;
    }

    public CorrelationMap<StoredLightRequest> getConnectorRequestCorrelationMap() {
        return connectorRequestCorrelationMap;
    }

    public void setConnectorRequestCorrelationMap(CorrelationMap<StoredLightRequest> connectorRequestCorrelationMap) {
        this.connectorRequestCorrelationMap = connectorRequestCorrelationMap;
    }

    public IEIDASConfigurationProxy getSpecificProps() {
        return specificProps;
    }

    public void setSpecificProps(IEIDASConfigurationProxy specificProps) {
        this.specificProps = specificProps;
    }

    public Properties getConfigs() {
        return configs;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    public String getSamlEngine() {
        return samlEngine;
    }

    public void setSamlEngine(String samlEngine) {
        this.samlEngine = samlEngine;
    }

    public String getSpUrl() {
        return spUrl;
    }

    public void setSpUrl(String spUrl) {
        this.spUrl = spUrl;
    }

    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    // Implement this unique ID generation based on the specific protocol.
    // It is an override point only if the ID cannot be presented at XXXProtocolProcessor level.
    protected String generateLRId(IAuthenticationRequest serviceProviderRequest) {
        String id = generateNCName();
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ILightRequest processAuthenticationRequest(WebRequest webRequest, byte[] requestFromSP) {

        String ipAddress = webRequest.getRemoteIpAddress();
        String relayState = webRequest.getRelayState();
        String citizenCountryCode = webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY);
        try {

            ProtocolEngineI protocolEngine = protocolEngineFactory.getProtocolEngine(getSamlEngine());

            IAuthenticationRequest serviceProviderRequest =
                    protocolEngine.unmarshallRequestAndValidate(requestFromSP, citizenCountryCode,WhitelistUtil.metadataWhitelist(spMetadataWhitelist));

            /* uncomment this code if specific action is required for SP type validation, otherwise it will be checked in the Connector Node */
            //validateSPType((EidasAuthenticationRequest) serviceProviderRequest);

            // Get the assertionConsumerUrl from metadata and validate
            String assertionConsumerUrl = MetadataUtil.getAssertionConsumerUrlFromMetadata(metadataFetcher,
                                                                                           (MetadataSignerI) protocolEngine
                                                                                                   .getSigner(),
                                                                                           serviceProviderRequest);
            NormalParameterValidator.paramName(EidasParameterKeys.SP_URL)
                    .paramValue(assertionConsumerUrl)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SPREDIRECT)
                    .validate();
            // set the assertionConsumerUrl in the Service Provider Authentication Request.
            serviceProviderRequest =
                    EidasAuthenticationRequest.builder((IEidasAuthenticationRequest) serviceProviderRequest)
                            .assertionConsumerServiceURL(assertionConsumerUrl)
                            .build();

            String lightRequestId = generateLRId(serviceProviderRequest);

            LightRequest lightRequest = LightRequest.builder(serviceProviderRequest).id(lightRequestId).build();

            specificSpRequestCorrelationMap.put(lightRequest.getId(), StoredAuthenticationRequest.builder()
                    .remoteIpAddress(ipAddress)
                    .request(serviceProviderRequest)
                    .relayState(relayState)
                    .build());
            connectorRequestCorrelationMap.put(lightRequest.getId(), StoredLightRequest.builder()
                    .remoteIpAddress(ipAddress)
                    .request(lightRequest)
                    .relayState(relayState)
                    .build());

            return lightRequest;

        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error processing the Authentication Request", e);
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SAML.errorMessage()), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BinaryAuthenticationExchange generateAuthenticationResponse(ILightResponse lightResponse,
                                                                       boolean signAssertionParam) {

        StoredAuthenticationRequest storedAuthenticationRequest = getStoredAuthenticationRequest(lightResponse);
        IAuthenticationRequest spAuthenticationRequest = storedAuthenticationRequest.getRequest();

        String ipAddress = null;
        //TODO EIDINT-1271 - ip address usage
        if (StringUtils.isNotBlank(lightResponse.getIPAddress())) {
            ipAddress = storedAuthenticationRequest.getRemoteIpAddress();
        }
        IAuthenticationResponse authenticationResponse =
                AuthenticationResponse.builder().lightResponse(lightResponse).build();

        try {
            IResponseMessage responseMessage;
            LOG.trace("Checking status code");

            if (lightResponse.getStatus().isFailure()) {
                LOG.info("ERROR : Auth not succeed!");

                String statusMessage = lightResponse.getStatus().getStatusMessage();

                responseMessage =
                        generateErrorAuthenticationResponse(spAuthenticationRequest, ipAddress, authenticationResponse,
                                                            statusMessage);
            } else {

                ProtocolEngineI protocolEngine = protocolEngineFactory.getProtocolEngine(getSamlEngine());

                // Check if the specific SAML engine configuration is different than the default one injected by spring
                boolean generateSignedAssertion = ((ProtocolEngine) protocolEngine).getCoreProperties()== null ? signAssertionParam :
                        Boolean.parseBoolean(((ProtocolEngine) protocolEngine).getCoreProperties().getProperty(EidasParameterKeys.RESPONSE_SIGN_ASSERTION.toString()));

                responseMessage =
                        protocolEngine.generateResponseMessage(spAuthenticationRequest, authenticationResponse,
                                generateSignedAssertion, ipAddress);
            }

            return new BinaryAuthenticationExchange(storedAuthenticationRequest, responseMessage);

        } catch (EIDASSAMLEngineException e) {
            LOG.error("Error generating SAML Response", e);
            throw new InternalErrorEIDASException("0", "error genereating SAML Response", e);
        }
    }

    private IResponseMessage generateErrorAuthenticationResponse(IAuthenticationRequest request,
                                                                 String ipUserAddress,
                                                                 IAuthenticationResponse response,
                                                                 String message) {
        try {
            // Generate SAMLResponse Fail.
            String inResponseTo = request.getId();

            AuthenticationResponse.Builder samlResponseFail = new AuthenticationResponse.Builder();
            samlResponseFail.id(response.getId());
            samlResponseFail.statusCode(response.getStatusCode());
            samlResponseFail.subStatusCode(response.getSubStatusCode());
            samlResponseFail.statusMessage(message);
            samlResponseFail.issuer(response.getIssuer());
            samlResponseFail.inResponseTo(inResponseTo);

            ProtocolEngineI protocolEngine = protocolEngineFactory.getProtocolEngine(getSamlEngine());

            IResponseMessage responseMessage = generateResponseErrorMessage(request, ipUserAddress, protocolEngine, samlResponseFail);

            LOG.info("Generating ERROR SAML Response to request with ID {}, error is {} {}", inResponseTo,
                     response.getSubStatusCode(), message);

            return responseMessage;

        } catch (EIDASSAMLEngineException e) {
            LOG.info("Error generating SAML Response", e);
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML.errorMessage()), e);
        }
    }

    private IResponseMessage generateResponseErrorMessage(IAuthenticationRequest authData, String ipUserAddress, ProtocolEngineI engine, AuthenticationResponse.Builder eidasAuthnResponseError) throws EIDASSAMLEngineException {
        final List<String> includeAssertionApplicationIdentifiers = getIncludeAssertionApplicationIdentifiers();
        return  engine.generateResponseErrorMessage(authData, eidasAuthnResponseError.build(), ipUserAddress, includeAssertionApplicationIdentifiers);
    }

    private List<String> getIncludeAssertionApplicationIdentifiers() {
        String property = configs.getProperty(EidasParameterKeys.INCLUDE_ASSERTION_FAIL_RESPONSE_APPLICATION_IDENTIFIERS.toString());
        return EidasStringUtil.getTokens(property);
    }

    private StoredAuthenticationRequest getStoredAuthenticationRequest(ILightResponse lightResponse) {

        String inResponseTo = lightResponse.getInResponseToId();
        StoredAuthenticationRequest authenticationRequest = specificSpRequestCorrelationMap.get(inResponseTo);

        if (null == authenticationRequest) {
            LOG.error("Invalid InResponseTo: \"" + inResponseTo + "\"");
            throw new InvalidSessionEIDASException(EidasErrors.get(EidasErrorKey.SESSION.errorCode()),
                                                   EidasErrors.get(EidasErrorKey.SESSION.errorMessage()));
        }
        return authenticationRequest;
    }

    private void validateSPType(EidasAuthenticationRequest eidasAuthenticationRequest) {
        SpType spType = SpType.fromString(eidasAuthenticationRequest.getSpType());
        String metadataSpType = configs.getProperty(EIDASValues.EIDAS_SPTYPE.toString());

        if ((isNotBlank(metadataSpType) && spType != null && !metadataSpType.equalsIgnoreCase(spType.toString()))
                || (isBlank(metadataSpType) && spType == null)) {
            LOG.error("BUSINESS EXCEPTION : SPType "+ spType +" is not supported ");
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorCode()),
                                         EidasErrors.get(EidasErrorKey.CONNECTOR_INVALID_SPTYPE.errorMessage()));
        }
    }
}
