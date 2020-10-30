/*
 * Copyright (c) 2020 by European Commission
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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingConstants;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.LoggingUtil;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.logging.messages.LightResponseMessageLog;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing {@link ILightResponse}
 * to MS's Specific Connector from eIDAS Connector
 *
 * @since 2.3
 */
public final class ConnectorOutgoingLightResponseLogger implements IMessageLogger, IFullMessageLogger {


    private SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService;

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    private String lightTokenConnectorResponseNodeId;

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));

    public ConnectorOutgoingLightResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogMessages()) {

            final LightResponseMessageLog.Builder messageLogBuilder = new LightResponseMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString());
                messageLogBuilder.setNodeId(getLightTokenConnectorResponseNodeId());
                messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
                messageLogBuilder.setDestination(getMessageLoggerUtils().getConnectorRedirectUrl());

                final String tokenBase64 = (String) httpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString());
                final String bltHashToLog = LoggingUtil.createBltHash(tokenBase64);
                messageLogBuilder.setBltHash(bltHashToLog);

                final ILightResponse iLightResponse = getSpringManagedSpecificCommunicationLoggingService()
                        .getResponse(tokenBase64, getMessageLoggerUtils().retrieveConnectorAttributes());

                final String inResponseToId = iLightResponse.getInResponseToId();
                messageLogBuilder.setInResponseTo(inResponseToId);
                final String flowId = getFlowIdCache().get(inResponseToId);
                messageLogBuilder.setFlowId(flowId);

                messageLogBuilder.setMsgId(iLightResponse.getId());

                byte[] msgObj = iLightResponse.toString().getBytes();
                final String msgHashToLog = LoggingUtil.createMsgHash(msgObj);
                messageLogBuilder.setMsgHash(msgHashToLog);

                final String statusCode = iLightResponse.getStatus().getStatusCode();
                messageLogBuilder.setStatusCode(statusCode);
            } catch (Exception e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the outgoing light response because of ", e);
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
                final String tokenBase64 = (String) httpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString());
                final SpecificCommunicationLoggingService loggingService = getSpringManagedSpecificCommunicationLoggingService();
                final String lightResponse = loggingService.getResponse(tokenBase64);
                boolean valid = NormalParameterValidator
                        .paramName(EidasParameterKeys.LIGHT_RESPONSE)
                        .paramValue(lightResponse)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, lightResponse);
                }
            } catch (SpecificCommunicationException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the outgoing light response because of ", e);
            }
        }
    }

    /**
     * The messageLoggerUtils setter.
     *
     * @param messageLoggerUtils: The value to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The specific connector communication logging service setter.
     * @param springManagedSpecificCommunicationLoggingService: The value to be set.
     */
    public void setSpringManagedSpecificCommunicationLoggingService(SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService) {
        this.springManagedSpecificCommunicationLoggingService = springManagedSpecificCommunicationLoggingService;
    }

    public void setLightTokenConnectorResponseNodeId(String lightTokenConnectorResponseNodeId) {
        this.lightTokenConnectorResponseNodeId = lightTokenConnectorResponseNodeId;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache: The value to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

    private String getLightTokenConnectorResponseNodeId() {
        return lightTokenConnectorResponseNodeId;
    }

    private SpecificCommunicationLoggingService getSpringManagedSpecificCommunicationLoggingService() {
        return springManagedSpecificCommunicationLoggingService;
    }

    private MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    private Cache<String, String> getFlowIdCache() {
        return flowIdCache;
    }
}
