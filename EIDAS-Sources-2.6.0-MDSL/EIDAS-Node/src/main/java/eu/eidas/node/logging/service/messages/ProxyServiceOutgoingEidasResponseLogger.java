/*
 * Copyright (c) 2021 by European Commission
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
package eu.eidas.node.logging.service.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.EidasResponseMessageLog;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceOutgoingEidasResponseLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    public ProxyServiceOutgoingEidasResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {

        if (messageLoggerUtils.isLogMessages()) {
            final EidasResponseMessageLog.Builder messageLogBuilder = new EidasResponseMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString());
                messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
                messageLogBuilder.setDestination(getDestinationUrl(httpServletRequest));

                final byte[] samlObj = getSamlResponseDecodedBytes(httpServletRequest);
                final String msgHashToLog = LoggingUtil.createMsgHash(samlObj);
                messageLogBuilder.setMsgHash(msgHashToLog);

                final IAuthenticationResponse authenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);
                final String inResponseToId = authenticationResponse.getInResponseToId();
                final String issuer = messageLoggerUtils.getIssuer(inResponseToId, httpServletRequest);
                final String nodeId = messageLoggerUtils.getProxyServiceEntityId(issuer);
                messageLogBuilder.setNodeId(nodeId);

                messageLogBuilder.setFlowId(flowIdCache.get(inResponseToId));
                messageLogBuilder.setMsgId(authenticationResponse.getId());

                messageLogBuilder.setInResponseTo(inResponseToId);
                messageLogBuilder.setStatusCode(authenticationResponse.getStatusCode());
            } catch (EIDASSAMLEngineException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the outgoing response because of ", e);
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());
                return;
            }

            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());
        }
    }

    @Override
    public void logFullMessage(HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogCompleteMessage()) {
            try {
                final byte[] msgObj = getSamlResponseDecodedBytes(httpServletRequest);
                Document samlResponseDocument = XmlSchemaUtil.validateSamlSchema(msgObj);
                final String characterEncoding = samlResponseDocument.getXmlEncoding();
                final String samlResponse = characterEncoding != null ? new String(msgObj, characterEncoding) : new String(msgObj);
                boolean valid = NormalParameterValidator
                        .paramName("SAMLResponse")
                        .paramValue(samlResponse)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, samlResponse);
                }

            } catch (EIDASSAMLEngineException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "SAML Response is not valid");
            } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the outgoing response because of ", e);
            }
        }
    }

    private byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
        final String samlResponseToken = getSamlResponseToken(httpServletRequest);
        if (samlResponseToken == null) {
            return new byte[0];
        }
        final byte[] samlResponseDecodedBytes = EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
        return samlResponseDecodedBytes;
    }

    private String getDestinationUrl(HttpServletRequest httpServletRequest) {
        String destinationUrl = (String) httpServletRequest.getAttribute(NodeParameterNames.REDIRECT_URL.toString());

        if (StringUtils.isEmpty(destinationUrl)) {
            destinationUrl = (String) httpServletRequest.getAttribute(EidasParameterKeys.ERROR_REDIRECT_URL.toString());
        }

        destinationUrl = removeSessionIdFromUrl(destinationUrl);

        return destinationUrl;
    }

    private String removeSessionIdFromUrl(String destinationUrl) {
        return destinationUrl.replaceAll(";.*", "");
    }

    private String getSamlResponseToken(HttpServletRequest httpServletRequest) {
        String samlResponseTokenSaml = (String) httpServletRequest.getAttribute(NodeParameterNames.SAML_TOKEN.toString());

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            samlResponseTokenSaml = (String) httpServletRequest.getAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString());
        }

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            samlResponseTokenSaml = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);
        }

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            samlResponseTokenSaml = ((ResponseCarryingServiceException) httpServletRequest.getAttribute("exception")).getSamlTokenFail();
        }

        return samlResponseTokenSaml;
    }


    /**
     * The messageLoggerUtils setter.
     *
     * @param messageLoggerUtils: The messageLoggerUils to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache The flowIdCache to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

}
