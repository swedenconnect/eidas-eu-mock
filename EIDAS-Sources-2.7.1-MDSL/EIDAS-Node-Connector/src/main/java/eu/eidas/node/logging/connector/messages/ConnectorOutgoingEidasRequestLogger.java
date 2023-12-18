/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.node.logging.connector.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.connector.validation.ConnectorParameterValidator;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.EidasRequestMessageLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing eIDAS Request
 * to eIDAS Proxy-Service from eIDAS Connector
 *
 * @since 2.3
 */
public final class ConnectorOutgoingEidasRequestLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));

    private AUCONNECTORUtil springManagedAUCONNECTORUtil;
    private MessageLoggerUtils messageLoggerUtils;
    private Cache<String, String> flowIdCache;

    public ConnectorOutgoingEidasRequestLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogMessages()) {

            final EidasRequestMessageLog.Builder messageLogBuilder = new EidasRequestMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.CONNECTOR_SERVICE_REQUEST.toString());
                messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
                final byte[] msgObj = getSamlRequestDecodedBytes(httpServletRequest);
                final String msgHashToLog = LoggingUtil.createMsgHash(msgObj);
                messageLogBuilder.setMsgHash(msgHashToLog);

                messageLogBuilder.setNodeId(getNodeId(httpServletRequest));

                IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(msgObj);
                messageLogBuilder.setDestination(authenticationRequest.getDestination());

                final String msgId = authenticationRequest.getId();
                messageLogBuilder.setMsgId(msgId);

                final String flowId = flowIdCache.get(msgId);
                messageLogBuilder.setFlowId(flowId);
            } catch (NumberFormatException | EIDASSAMLEngineException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the outgoing request because of ", e);
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
                final byte[] msgObj = getSamlRequestDecodedBytes(httpServletRequest);
                Document samlResponseDocument = XmlSchemaUtil.validateSamlSchema(msgObj);
                final String characterEncoding = samlResponseDocument.getXmlEncoding();
                final String samlRequest = characterEncoding != null ? new String(msgObj, characterEncoding) : new String(msgObj);

                boolean valid = ConnectorParameterValidator
                        .paramName(EidasParameterKeys.SAML_REQUEST)
                        .paramValue(samlRequest)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, samlRequest);
                }

            } catch (EIDASSAMLEngineException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "SAML Request is not valid");
            } catch (UnsupportedEncodingException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the outgoing request because of ", e);
            }
        }
    }

    private byte[] getSamlRequestDecodedBytes(HttpServletRequest httpServletRequest) {
        final String samlRequestTokenSaml = (String) httpServletRequest.getAttribute(EidasParameterKeys.SAML_REQUEST.toString());
        if (samlRequestTokenSaml == null) {
            return new byte[0];
        }
        final byte[] msgObj = EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
        return msgObj;
    }

    private String getNodeId(HttpServletRequest httpServletRequest) {
        final String citizenCountryCode = (String) httpServletRequest.getAttribute(NodeParameterNames.CITIZEN_COUNTRY_CODE.toString());
        String issuer = springManagedAUCONNECTORUtil.loadConfigServiceMetadataURL(citizenCountryCode);
        return messageLoggerUtils.getConnectorEntityId(issuer);
    }

    /**
     * The MessageLoggerUtils setter.
     *
     * @param messageLoggerUtils: The value to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The {@link AUCONNECTORUtil} setter.
     * @param springManagedAUCONNECTORUtil: The value to be set.
     */
    public void setSpringManagedAUCONNECTORUtil(AUCONNECTORUtil springManagedAUCONNECTORUtil) {
        this.springManagedAUCONNECTORUtil = springManagedAUCONNECTORUtil;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache: The value to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
