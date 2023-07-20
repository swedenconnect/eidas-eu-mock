/*
 * Copyright (c) 2023 by European Commission
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
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.connector.validation.ConnectorParameterValidator;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.LightRequestMessageLog;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming {@link ILightRequest}
 * to eIDAS Connector from MS's Specific Connector
 *
 * @since 2.3
 */
public final class ConnectorIncomingLightRequestLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));

    private SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService;
    private MessageLoggerUtils messageLoggerUtils;
    private Cache<String, String> flowIdCache;
    private String lightTokenConnectorRequestNodeId;

    public ConnectorIncomingLightRequestLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (getMessageLoggerUtils().isLogMessages()) {

            final LightRequestMessageLog.Builder messageLogBuilder = new LightRequestMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.SPECIFIC_EIDAS_CONNECTOR_REQUEST.toString());
                messageLogBuilder.setNodeId(getLightTokenConnectorRequestNodeId());
                messageLogBuilder.setOrigin(httpServletRequest.getHeader(EIDASValues.REFERER.toString()));
                messageLogBuilder.setDestination(httpServletRequest.getRequestURL().toString());

                final WebRequest webRequest = new IncomingRequest(httpServletRequest);
                final String tokenBase64 = webRequest.getEncodedLastParameterValue(EidasParameterKeys.TOKEN);
                final String bltHashToLog = LoggingUtil.createBltHash(tokenBase64);
                messageLogBuilder.setBltHash(bltHashToLog);

                final ILightRequest iLightRequest = getSpringManagedSpecificCommunicationLoggingService()
                        .getRequest(tokenBase64, getMessageLoggerUtils().retrieveConnectorAttributes());

                final String msgId = iLightRequest.getId();
                final String flowId = SAMLEngineUtils.generateNCName();
                getFlowIdCache().put(msgId, flowId);
                messageLogBuilder.setFlowId(flowId);
                messageLogBuilder.setMsgId(msgId);

                byte[] msgObj = iLightRequest.toString().getBytes();
                final String msgHashToLog = LoggingUtil.createMsgHash(msgObj);
                messageLogBuilder.setMsgHash(msgHashToLog);
            } catch (SpecificCommunicationException | InvalidParameterEIDASException | IllegalArgumentException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the incoming light request because of ", e);
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());
                return;
            }

            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());

        }
    }

    @Override
    public void logFullMessage(HttpServletRequest httpServletRequest) {
        if (getMessageLoggerUtils().isLogCompleteMessage()) {
            try {
                final WebRequest webRequest = new IncomingRequest(httpServletRequest);
                final String tokenBase64 = webRequest.getEncodedLastParameterValue(EidasParameterKeys.TOKEN);
                final SpecificCommunicationLoggingService loggingService = getSpringManagedSpecificCommunicationLoggingService();
                final String lightRequest = loggingService.getRequest(tokenBase64);
                boolean valid = ConnectorParameterValidator
                        .paramName(EidasParameterKeys.LIGHT_REQUEST)
                        .paramValue(lightRequest)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, lightRequest);
                }
            } catch (SpecificCommunicationException | IllegalArgumentException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the incoming light request because of ", e);
            }
        }
    }

    public SpecificCommunicationLoggingService getSpringManagedSpecificCommunicationLoggingService() {
        return springManagedSpecificCommunicationLoggingService;
    }

    public void setSpringManagedSpecificCommunicationLoggingService(SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService) {
        this.springManagedSpecificCommunicationLoggingService = springManagedSpecificCommunicationLoggingService;
    }

    public MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    public Cache<String, String> getFlowIdCache() {
        return flowIdCache;
    }

    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

    public String getLightTokenConnectorRequestNodeId() {
        return lightTokenConnectorRequestNodeId;
    }

    public void setLightTokenConnectorRequestNodeId(String lightTokenConnectorRequestNodeId) {
        this.lightTokenConnectorRequestNodeId = lightTokenConnectorRequestNodeId;
    }
}
