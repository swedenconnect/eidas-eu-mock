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
package eu.eidas.node.logging.service.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.logging.IFullMessageLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingConstants;
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
 * to log the outgoing {@link ILightRequest}
 * to MS's Specific Proxy-Service from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceOutgoingLightRequestLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));
    private MessageLoggerUtils messageLoggerUtils;
    private SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService;
    private Cache<String, String> flowIdCache;
    private String lightTokenProxyserviceRequestNodeId;

    public ProxyServiceOutgoingLightRequestLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {

        if (getMessageLoggerUtils().isLogMessages()) {
            final LightRequestMessageLog.Builder messageLogBuilder = new LightRequestMessageLog.Builder();
            try {
                messageLogBuilder.setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString());
                messageLogBuilder.setNodeId(getLightTokenProxyserviceRequestNodeId());
                messageLogBuilder.setOrigin(LoggingConstants.NOT_APPLICABLE);
                messageLogBuilder.setDestination(getMessageLoggerUtils().getProxyServiceRedirectUrl());

                final String tokenBase64 = extractToken(httpServletRequest);
                final String bltHashToLog = LoggingUtil.createBltHash(tokenBase64);
                messageLogBuilder.setBltHash(bltHashToLog);

                final ILightRequest iLightRequest = getSpringManagedSpecificCommunicationLoggingService().
                        getRequest(tokenBase64, getMessageLoggerUtils().retrieveProxyServiceAttributes());

                byte[] msgObj = iLightRequest.toString().getBytes();
                final String msgHashToLog = LoggingUtil.createMsgHash(msgObj);
                messageLogBuilder.setMsgHash(msgHashToLog);

                final String msgId = iLightRequest.getId();
                messageLogBuilder.setMsgId(msgId);
                final String flowId = getFlowIdCache().get(msgId);
                messageLogBuilder.setFlowId(flowId);
            } catch (SpecificCommunicationException | InvalidParameterEIDASException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the outgoing light request because of ", e);
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
                final String tokenBase64 = extractToken(httpServletRequest);
                final String lightRequest = getSpringManagedSpecificCommunicationLoggingService().getRequest(tokenBase64);
                boolean valid = NormalParameterValidator
                        .paramName("LightRequest")
                        .paramValue(lightRequest)
                        .marker(LoggingMarkerMDC.FULL_MSG_EXCHANGE)
                        .isValid();
                if (valid) {
                    fullLogger.info(LoggingMarkerMDC.FULL_MSG_EXCHANGE, lightRequest);
                }
            } catch (SpecificCommunicationException e) {
                fullLogger.error(LoggingMarkerMDC.FULL_MSG_EXCHANGE, "Could not log the outgoing light request because of ", e);
            }
        }
    }

    /**
     * Helper method to get token from the httpServletRequest.
     *
     * @param httpServletRequest: The request containing the token.
     */
    private String extractToken(HttpServletRequest httpServletRequest) {
        return (String) httpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString());
    }

    /**
     * MessageLoggerUtils setter
     *
     * @param messageLoggerUtils: the messageLoggerUtils to be set
     */
    public final void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * Communication service setter
     *
     * @param springManagedSpecificCommunicationLoggingService: the Communication service to be set
     */
    public final void setSpringManagedSpecificCommunicationLoggingService(
            SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService) {

        this.springManagedSpecificCommunicationLoggingService = springManagedSpecificCommunicationLoggingService;
    }

    public void setLightTokenProxyserviceRequestNodeId(String lightTokenProxyserviceRequestNodeId) {
        this.lightTokenProxyserviceRequestNodeId = lightTokenProxyserviceRequestNodeId;
    }

    /**
     * flowIdCache setter.
     *
     * @param flowIdCache The flowIdCache to set.
     */
    public final void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

    private MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    private SpecificCommunicationLoggingService getSpringManagedSpecificCommunicationLoggingService() {
        return springManagedSpecificCommunicationLoggingService;
    }

    private Cache<String, String> getFlowIdCache() {
        return flowIdCache;
    }

    private String getLightTokenProxyserviceRequestNodeId() {
        return lightTokenProxyserviceRequestNodeId;
    }
}
