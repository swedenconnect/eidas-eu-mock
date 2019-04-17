package eu.eidas.node.auth.specific;

import eu.eidas.auth.commons.DateUtil;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasDigestUtil;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.util.logging.LoggingMarkerMDC;
import eu.eidas.util.logging.LoggingSanitizer;
import org.opensaml.saml2.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Properties;

public class LoggingUtil {
    /**
     * Logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingUtil.class);

    /**
     * Country Code of this ProxyService.
     */
    private String countryCode;

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

    private String samlEngine;

    public void setSamlEngine(String samlEngine) {
        this.samlEngine = samlEngine;
    }

    public String getSamlEngine() {
        return samlEngine;
    }

    private boolean samlAuditable;

    public boolean isSamlAuditable() {
        return samlAuditable;
    }

    public void setSamlAuditable(boolean samlAuditable) {
        this.samlAuditable = samlAuditable;
    }

    private ProtocolEngineFactory protocolEngineFactory;

    public ProtocolEngineFactory getProtocolEngineFactory() {
        return protocolEngineFactory;
    }

    public void setProtocolEngineFactory(ProtocolEngineFactory protocolEngineFactory) {
        this.protocolEngineFactory = protocolEngineFactory;
    }


    private MetadataFetcherI metadataFetcher;

    public MetadataFetcherI getMetadataFetcher() {
        return metadataFetcher;
    }

    public void setMetadataFetcher(MetadataFetcherI metadataFetcher) {
        this.metadataFetcher = metadataFetcher;
    }

    public ProtocolEngineI getSamlEngine(@Nonnull String instanceName) {
        return protocolEngineFactory.getProtocolEngine(instanceName);
    }

    private Properties configs;

    public Properties getConfigs() {
        return configs;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

    private String getEntityId(String metaDataUrl){
        String entityId = null;
        if ((null != metadataFetcher)&&(null != metaDataUrl)) {
            try {
                entityId = metadataFetcher.getEntityDescriptor(metaDataUrl,
                        (MetadataSignerI) getSamlEngine(samlEngine).getSigner()).getEntityID();
            } catch (EIDASSAMLEngineException e) {
                e.printStackTrace();
            }

        }
        return entityId;
    }

    public String loadConfigServiceURL(final String serviceId) {
        return loadServiceAttribute(serviceId, "url");
    }

    public String loadConfigServiceMetadataURL(final String pepId) {
        return loadServiceAttribute(pepId, "metadata.url");
    }

    private String loadServiceAttribute(final String pepId, String paramName) {
        String retVal = null;
        final int nServices = Integer.parseInt(configs.getProperty(EidasParameterKeys.EIDAS_NUMBER.toString()));
        LOGGER.debug("Number of Service: " + nServices);

        // load URL
        for (int i = 1; i <= nServices && retVal == null; i++) {
            final String serviceCons = EIDASValues.EIDAS_SERVICE_PREFIX.index(i);
            if (configs.containsKey(serviceCons) && configs.getProperty(serviceCons).equals(pepId)) {
                retVal = configs.getProperty(EIDASValues.EIDAS_SERVICE_PREFIX.attribute(paramName, i));
                LOGGER.debug("Service URL " + retVal);
            }
        }

        return retVal;
    }

    public enum OperationTypes { RECEIVES, GENERATES, SENDS };

    private String getNodeId(OperationTypes operationType, String issuer) {

        String entityId = "N/A";
        if (issuer != null)
            entityId = getEntityId(issuer);

        return operationType.compareTo(OperationTypes.GENERATES) == 0 ? "N/A" : entityId;
    }
    private String getOriginatingMsgId(OperationTypes operationType, String correlationId){
        return operationType.compareTo(OperationTypes.GENERATES) != 0 ? "N/A" : correlationId;
    }
    private String parseOrigin(OperationTypes operationType, String origin){
        if (operationType.compareTo(OperationTypes.SENDS) == 0)
            return "N/A";

        if (origin==null) return "N/A";

        return origin.indexOf("?") == -1 ? origin : origin.substring(0, origin.indexOf("?"));
    }

    public IAuthenticationRequest getIAuthenticationRequest(byte[] samlTokenBytes, String citizenCountryCode) throws EIDASSAMLEngineException {
        String samlEngine = getSamlEngine();
        ProtocolEngineI protocolEngine = protocolEngineFactory.getProtocolEngine(samlEngine);

        IAuthenticationRequest serviceProviderRequest =
                protocolEngine.unmarshallRequestAndValidate(samlTokenBytes, citizenCountryCode, null, false);

        return serviceProviderRequest;
    }

    public void prepareAndSaveAuthenticationRequestLog(Logger logger, String opType, String originIssuer, byte[] samlTokenBytes, String correlationId, String origin, String citizenCountryCode, OperationTypes operationType) {
        if (!isSamlAuditable()) return;
        IAuthenticationRequest serviceProviderRequest = null;
        try {
            serviceProviderRequest =
                    getIAuthenticationRequest(samlTokenBytes, citizenCountryCode);


        } catch (EIDASSAMLEngineException e) {
            LOGGER.error("Error processing the Authentication Request", e);
        }
        saveAuthenticationRequestLog(logger, opType, originIssuer, samlTokenBytes, serviceProviderRequest, correlationId, origin, operationType);

    }

    public void saveAuthenticationRequestLog(Logger logger, String opType, String originIssuer, byte[] samlObj, IAuthenticationRequest request, String correlationId, String origin, OperationTypes operationType) {
        if (!isSamlAuditable()) return;

        final String msg = appendRequestToLog(opType, originIssuer, samlObj, request, correlationId, origin, operationType);
        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, msg);
    }

    private String appendRequestToLog(String opType, String originIssuer, byte[] samlObj, IAuthenticationRequest authnRequest, String correlationId, String origin, OperationTypes operationType) {
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
            nodeId = getNodeId(operationType, originIssuer);
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

    public void saveAuthenticationResponseLog(Logger logger, String opType, byte[] samlObj, IAuthenticationResponse responseMessage, String correlationId, String origin, String errorMessage, OperationTypes operationType) {
        if (!isSamlAuditable()) return;

        logger.info(LoggingMarkerMDC.SAML_EXCHANGE, appendResponseToLog(opType, samlObj, responseMessage, correlationId, origin, errorMessage, operationType));
    }

    private String appendResponseToLog(String opType, byte[] samlObj, IAuthenticationResponse responseMessage, String correlationId, String origin, String errorMessage, OperationTypes operationType) {

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
            nodeId = getNodeId(operationType, responseMessage.getIssuer());
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
