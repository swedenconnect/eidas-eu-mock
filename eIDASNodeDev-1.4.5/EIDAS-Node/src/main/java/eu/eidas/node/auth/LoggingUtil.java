package eu.eidas.node.auth;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.Correlated;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.util.logging.LoggingMarkerMDC;
import eu.eidas.util.logging.LoggingSanitizer;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import static org.bouncycastle.util.encoders.Base64.decode;

public class LoggingUtil {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtil.class);

    /**
     * Connector's processAuthenticationResponse class.
     */
    private AUCONNECTORUtil connectorUtil;
    /**
     * Country Code of this ProxyService.
     */
    private String countryCode;
    private String samlServiceInstance;
    private String samlEngineInstanceName;
    private boolean samlAuditable;
    private ProtocolEngineFactory nodeProtocolEngineFactory;
    private MetadataFetcherI metadataFetcher;


    public enum OperationTypes {RECEIVES, GENERATES, SENDS}
    /**
     * Getter for connectorUtil.
     *
     * @return The connectorUtil value.
     * @see AUCONNECTORUtil
     */
    public AUCONNECTORUtil getConnectorUtil() {
        return connectorUtil;
    }

    /**
     * Setter for connectorUtil.
     *
     * @param connectorUtil The new connectorUtil value.
     * @see AUCONNECTORUtil
     */
    public void setConnectorUtil(AUCONNECTORUtil connectorUtil) {
        this.connectorUtil = connectorUtil;
    }

    /**
     * Getter for countryCode.
     *
     * @return The countryCode value.
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Setter for countryCode.
     *
     * @param code The countryCode to set.
     */
    public void setCountryCode(String code) {
        this.countryCode = code;
    }

    public String getSamlServiceInstance() {
        return samlServiceInstance;
    }

    public void setSamlServiceInstance(String samlServiceInstance) {
        this.samlServiceInstance = samlServiceInstance;
    }

    public String getSamlEngineInstanceName() {
        return samlEngineInstanceName;
    }

    public void setSamlEngineInstanceName(String samlEngineInstanceName) {
        this.samlEngineInstanceName = samlEngineInstanceName;
    }

    public boolean isSamlAuditable() {
        return samlAuditable;
    }

    public void setSamlAuditable(boolean samlAuditable) {
        this.samlAuditable = samlAuditable;
    }

    public ProtocolEngineFactory getNodeProtocolEngineFactory() {
        return nodeProtocolEngineFactory;
    }

    public void setNodeProtocolEngineFactory(ProtocolEngineFactory nodeProtocolEngineFactory) {
        this.nodeProtocolEngineFactory = nodeProtocolEngineFactory;
    }

    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    public ProtocolEngineI getSamlEngine(@Nonnull String instanceName) {
        return nodeProtocolEngineFactory.getProtocolEngine(instanceName);
    }

    private String getEntityId(String metaDataUrl) {
        if (metaDataUrl == null) return null;

        String entityId = null;
        if ((null != metadataFetcher) && (null != metaDataUrl)) {
            try {
                entityId = metadataFetcher.getEntityDescriptor(metaDataUrl,
                        (MetadataSignerI) getSamlEngine(samlServiceInstance).getSigner()).getEntityID();
            } catch (EIDASSAMLEngineException e) {
                e.printStackTrace();
            }

        }
        return entityId;
    }

    private String getNodeId(OperationTypes operationType, String issuer) {

        String entityId = getConnectorUtil().loadConfigServiceMetadataURL(countryCode);
        if (issuer != null)
            entityId = getEntityId(issuer);

        return operationType.compareTo(OperationTypes.GENERATES) == 0 ? "N/A" : entityId;
    }

    ;

    private String getOriginatingMsgId(OperationTypes operationType, String correlationId) {
        return operationType.compareTo(OperationTypes.GENERATES) != 0 ? "N/A" : correlationId;

    }

    private String parseOrigin(OperationTypes operationType, String origin) {
        if (operationType.compareTo(OperationTypes.SENDS) == 0)
            return "N/A";

        if (origin == null) return "N/A";

        return origin.indexOf("?") == -1 ? origin : origin.substring(0, origin.indexOf("?"));
    }
    public IAuthenticationRequest getIAuthenticationRequest(byte[] samlObj, String citizenCountryCode, String componentRequest) throws EIDASSAMLEngineException {
        String samlEngine = getSamlServiceInstance();
        if (componentRequest.equals("ProxyService"))
            samlEngine = getSamlEngineInstanceName();

        ProtocolEngineI protocolEngine = nodeProtocolEngineFactory.getProtocolEngine(samlEngine);
        IAuthenticationRequest serviceProviderRequest =
                protocolEngine.unmarshallRequestAndValidate(samlObj, citizenCountryCode, null, false);

        return serviceProviderRequest;
    }
    public void prepareAndSaveAuthenticationRequestLog(Logger logger, String opType, HttpServletRequest request, String componentRequest, OperationTypes operationType) {
        if (!isSamlAuditable()) return;

            // Obtains the parameters from httpRequest
            WebRequest webRequest = new IncomingRequest(request);

            String origin = request.getHeader("referer");

            String citizenCountryCode = webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY);
            citizenCountryCode = (citizenCountryCode == null) ? countryCode : citizenCountryCode;

            String samlRequestFromSp = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);

            byte[] samlObj = decode(samlRequestFromSp);

            String errorMessage = null;
            String issuer = null;
            IAuthenticationRequest serviceProviderRequest = null;
        try {
            serviceProviderRequest =
                    getIAuthenticationRequest(samlObj, citizenCountryCode, componentRequest);
            issuer = serviceProviderRequest.getIssuer();
        } catch (EIDASSAMLEngineException e) {
            errorMessage = e.getErrorMessage();
            LOGGER.info("BUSINESS EXCEPTION : Error validating SAMLToken", e);
        }
            saveAuthenticationRequestLog(logger, opType, issuer, samlObj, serviceProviderRequest, "N/A", origin, operationType);
    }

    public void saveAuthenticationRequestLog(Logger logger, String opType, String issuer, byte[] samlObj, IAuthenticationRequest request, String correlationId, String origin, OperationTypes operationType) {
        if (!isSamlAuditable()) return;

        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, appendRequestToLog(opType, issuer, samlObj, request, correlationId, origin, operationType));
    }

    private String appendRequestToLog(String opType, String issuer, byte[] samlObj, IAuthenticationRequest authnRequest, String correlationId, String origin, OperationTypes operationType) {
        String destination = "N/A";
        String msgId = "N/A";
        if (authnRequest != null){
            msgId = authnRequest.getId();
            destination = authnRequest.getDestination();
        }
        String hashClassName = null;
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(samlObj, hashClassName);

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nTimestamp     ").append(DateUtil.currentTimeStamp().toString()).append(",\n");

        stringBuilder.append("OpType        ").append(opType).append(",\n");
        String nodeId = "N/A";
        String originWithoutCrlf = "N/A";
        String destinationWithoutCrlf = "N/A";
        if (!operationType.equals(OperationTypes.GENERATES)) {
            nodeId = getNodeId(operationType, issuer);
            originWithoutCrlf = LoggingSanitizer.removeCRLFInjection(parseOrigin(operationType, origin));
            destinationWithoutCrlf = LoggingSanitizer.removeCRLFInjection(destination);
        }
        stringBuilder.append("NodeId        ").append(nodeId).append(",\n");
        stringBuilder.append("Origin        ").append(originWithoutCrlf).append(",\n");
        stringBuilder.append("Destination   ").append(destinationWithoutCrlf)
                .append(",\n");

        stringBuilder.append("samlHash      ").append(org.apache.xml.security.utils.Base64.encode(tokenHash))
                .append(",\n");
        correlationId = getOriginatingMsgId(operationType, correlationId);
        stringBuilder.append("originatingMsgId ").append(correlationId).append(",\n");

        stringBuilder.append("msgId         ").append(msgId);

        return stringBuilder.toString();
    }

    public IAuthenticationResponse getIAuthenticationResponse(byte[] samlObj, String remoteIpAddress, String componentRequest) throws EIDASSAMLEngineException {

        String samlEngine = getSamlServiceInstance();
        if (componentRequest.equals("ProxyService"))
            samlEngine = getSamlEngineInstanceName();

        ProtocolEngineI protocolEngine = nodeProtocolEngineFactory.getProtocolEngine(samlEngine);

        Correlated unmarshalledResponse = protocolEngine.unmarshallResponse(samlObj, null, false);

        IAuthenticationResponse authenticationResponse = protocolEngine.validateUnmarshalledResponse(unmarshalledResponse, remoteIpAddress,
                    0, 0, null);
        return authenticationResponse;
    }

    public IAuthenticationResponse getIAuthenticationResponse(HttpServletRequest request, String componentRequest) {
        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(request);
        String samlResponse = request.getParameter(EidasParameterKeys.SAML_RESPONSE.toString());
        byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlResponse);
        IAuthenticationResponse iAuthenticationResponse = null;
        try{
            iAuthenticationResponse = getIAuthenticationResponse(samlObj, webRequest.getRemoteIpAddress(), componentRequest);
        } catch (EIDASSAMLEngineException e) {
            LOGGER.info("BUSINESS EXCEPTION : Error validating SAMLToken", e);
        }
        return iAuthenticationResponse;
    }

    public void prepareAndSaveAuthenticationResponseLog(Logger logger, String opType, HttpServletRequest request, String componentRequest, OperationTypes operationType) {

        if (!isSamlAuditable()) return;


        String origin = request.getHeader("referer");
        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(request);

        String samlResponse = request.getParameter(EidasParameterKeys.SAML_RESPONSE.toString());

        byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlResponse);
        String issuer = null;
        String errorMessage = null;
        IAuthenticationResponse authenticationResponse = null;
        try {
            authenticationResponse = getIAuthenticationResponse(samlObj, webRequest.getRemoteIpAddress(), componentRequest);
            issuer = authenticationResponse.getIssuer();
        } catch (EIDASSAMLEngineException e) {
            errorMessage = e.getErrorMessage();
            LOGGER.info("BUSINESS EXCEPTION : Error validating SAMLToken", e);
        }
        saveAuthenticationResponseLog(logger, opType, issuer, samlObj, authenticationResponse, "N/A", origin, errorMessage, operationType);
    }

    public void saveAuthenticationResponseLog(Logger logger, String opType, String originIssuer, byte[] samlObj, IAuthenticationResponse responseMessage, String correlationId, String origin, String errorMessage, OperationTypes operationType) {
        if (!isSamlAuditable()) return;

        final String msg = appendResponseToLog(opType, originIssuer, samlObj, responseMessage, correlationId, origin, errorMessage, operationType);
        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, msg);
    }

    private String appendResponseToLog(String opType, String originIssuer, byte[] samlObj, IAuthenticationResponse responseMessage, String correlationId, String origin, String errorMessage, OperationTypes operationType) {

        String inResponseTo = "N/A";
        String message = "N/A";
        String msgId = "N/A";
        if (responseMessage != null){
            message = responseMessage.getStatusCode();
            inResponseTo = responseMessage.getInResponseToId();
            msgId = responseMessage.getId();
        }

        String hashClassName = null;
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(samlObj, hashClassName);

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nTimestamp     ").append(DateUtil.currentTimeStamp().toString()).append(",\n");

        stringBuilder.append("OpType        ").append(opType).append(",\n");
        String nodeId = "N/A";
        String originWithoutCrlf = "N/A";
        String destinationWithoutCrlf = "N/A";
        if (!operationType.equals(OperationTypes.GENERATES)) {
            nodeId = getNodeId(operationType, originIssuer);
            originWithoutCrlf = LoggingSanitizer.removeCRLFInjection(parseOrigin(operationType, origin));
            try {
                Response samlResponse = (Response) OpenSamlHelper.unmarshall(samlObj);
                destinationWithoutCrlf = LoggingSanitizer.removeCRLFInjection(samlResponse.getDestination());
            } catch (UnmarshallException e) {
                e.printStackTrace();
            }
        }

        stringBuilder.append("NodeId        ").append(nodeId).append(",\n");
        stringBuilder.append("Origin        ").append(originWithoutCrlf).append(",\n");
        stringBuilder.append("Destination   ").append(destinationWithoutCrlf)
                .append(",\n");

        stringBuilder.append("inResponseTo  ").append(inResponseTo).append(",\n");
        stringBuilder.append("statusCode    ").append(message).append(",\n");
        stringBuilder.append("samlHash      ").append(org.apache.xml.security.utils.Base64.encode(tokenHash))
                .append(",\n");
        correlationId = getOriginatingMsgId(operationType, correlationId);
        stringBuilder.append("originatingMsgId ").append(correlationId).append(",\n");

        stringBuilder.append("msgId         ").append(msgId);

        return stringBuilder.toString();
    }

    public void prepareAndSaveResponseToLog(Logger logger, String opType, String originIssuer, byte[] samlObj, String correlationId, String origin, String errorMessage, OperationTypes operationType) {

        if (!isSamlAuditable()) return;

        // hack to look inside what was really generated:
        Response samlResponse = null;
        try {
            samlResponse = (Response) OpenSamlHelper.unmarshall(samlObj);

        } catch (UnmarshallException e) {
            e.printStackTrace();
        }
        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, writeResponseToLog(opType, originIssuer, samlObj, samlResponse, correlationId, origin, errorMessage, operationType));

    }

    private String writeResponseToLog(String opType, String originIssuer, byte[] samlObj, Response responseMessage, String correlationId, String origin, String errorMessage, OperationTypes operationType) {

        String message = responseMessage.getStatus().getStatusCode().getValue();

        String hashClassName = null;
        byte[] tokenHash = EidasDigestUtil.hashPersonalToken(samlObj, hashClassName);

        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("\nTimestamp     ").append(DateUtil.currentTimeStamp().toString()).append(",\n");

        stringBuilder.append("OpType        ").append(opType).append(",\n");
        String nodeId = "N/A";
        String originWithoutCrlf = "N/A";
        String destinationWithoutCrlf = "N/A";
        if (!operationType.equals(OperationTypes.GENERATES)) {
            nodeId = getNodeId(operationType, originIssuer);
            originWithoutCrlf = LoggingSanitizer.removeCRLFInjection(parseOrigin(operationType, origin));
            destinationWithoutCrlf = LoggingSanitizer.removeCRLFInjection(responseMessage.getDestination());
        }

        stringBuilder.append("NodeId        ").append(nodeId).append(",\n");
        stringBuilder.append("Origin        ").append(originWithoutCrlf).append(",\n");
        stringBuilder.append("Destination   ").append(destinationWithoutCrlf)
                .append(",\n");

        stringBuilder.append("inResponseTo  ").append(responseMessage.getInResponseTo()).append(",\n");
        stringBuilder.append("statusCode    ").append(message).append(",\n");
        stringBuilder.append("samlHash      ").append(org.apache.xml.security.utils.Base64.encode(tokenHash))
                .append(",\n");
        correlationId = getOriginatingMsgId(operationType, correlationId);
        stringBuilder.append("originatingMsgId ").append(correlationId).append(",\n");

        stringBuilder.append("msgId         ").append(responseMessage.getID());

        return stringBuilder.toString();
    }

}
