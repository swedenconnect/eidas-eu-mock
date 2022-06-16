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
package eu.eidas.node.logging.connector.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.EidasResponseMessageLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;

import static eu.eidas.auth.commons.EIDASValues.REFERER;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ConnectorIncomingEidasResponseLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));

    private MessageLoggerUtils messageLoggerUtils;
    private Cache<String, String> flowIdCache;

    public ConnectorIncomingEidasResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogMessages()) {

            final EidasResponseMessageLog.Builder messageLogBuilder = new EidasResponseMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString());
                messageLogBuilder.setOrigin(httpServletRequest.getHeader(REFERER.toString()));
                messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());

                final byte[] samlObj = getSamlResponseDecodedBytes(httpServletRequest);
                final String msgHashToLog = LoggingUtil.createMsgHash(samlObj);
                messageLogBuilder.setMsgHash(msgHashToLog);

                final IAuthenticationResponse authenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);
                messageLogBuilder.setMsgId(authenticationResponse.getId());
                messageLogBuilder.setStatusCode(authenticationResponse.getStatusCode());

                final String issuer = authenticationResponse.getIssuer();
                final String nodeId = messageLoggerUtils.getConnectorEntityId(issuer);
                messageLogBuilder.setNodeId(nodeId);

                final String inResponseToId = authenticationResponse.getInResponseToId();
                messageLogBuilder.setInResponseTo(inResponseToId);
                messageLogBuilder.setFlowId(flowIdCache.get(inResponseToId));
            } catch (EIDASSAMLEngineException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the incoming response because of ", e);
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
                        .paramName(EidasParameterKeys.SAML_RESPONSE)
                        .paramValue(samlResponse)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, samlResponse);
                }

            } catch (EIDASSAMLEngineException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "SAML Response is not valid");
            } catch (UnsupportedEncodingException | IllegalArgumentException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the incoming response because of ", e);
            }
        }
    }

    private byte[] getSamlResponseDecodedBytes(HttpServletRequest httpServletRequest) {
        final WebRequest webRequest = new IncomingRequest(httpServletRequest);
        final String samlResponseToken = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);
        final byte[] samlResponseDecodedBytes = EidasStringUtil.decodeBytesFromBase64(samlResponseToken);
        return samlResponseDecodedBytes;
    }


    /**
     * Setter for messageLoggerUtils.
     * @param messageLoggerUtils The fvalue to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * Setter for flowIdCache.
     * @param flowIdCache The fvalue to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
