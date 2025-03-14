/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.logging.logger;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.LoggingUtil;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.LightRequestMessageLog;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * Logs {@link ILightRequest}
 * <p>
 * Implements {@link IMessageLogger} to log brief highlights under {@link EIDASValues#EIDAS_PACKAGE_REQUEST_LOGGER_VALUE}
 * Implements {@link IFullMessageLogger} log the raw message under {@link EIDASValues#EIDAS_PACKAGE_LOGGING_FULL}
 */
public abstract class LightRequestLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));
    private final Logger commsLogger = LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE, this.getClass()));

    private SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService;
    private MessageLoggerUtils messageLoggerUtils;
    private String lightTokenRequestNodeId;
    private String loggingHashDigestAlgorithm;
    private String loggingHashDigestProvider;

    @Override
    public final void logMessage(final HttpServletRequest httpServletRequest) {
        if (getMessageLoggerUtils().isLogMessages()) {

            final LightRequestMessageLog.Builder messageLogBuilder = new LightRequestMessageLog.Builder();
            try {
                setMessageVector(messageLogBuilder, httpServletRequest);

                final String bltHashToLog = getBinaryLightTokenHash(httpServletRequest);
                final ILightRequest iLightRequest = getLightRequest(httpServletRequest);

                final String msgId = iLightRequest.getId();
                final String msgHashToLog = LoggingUtil.createMsgHash(iLightRequest.toString().getBytes());
                final String flowId = messageLoggerUtils.trackMessageFlow(msgId);
                messageLogBuilder.setFlowId(flowId);
                messageLogBuilder.setMsgId(msgId);
                messageLogBuilder.setMsgHash(msgHashToLog);
                messageLogBuilder.setBltHash(bltHashToLog);
            } catch (SpecificCommunicationException | InvalidParameterEIDASException | IllegalArgumentException e) {
                commsLogger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the light request because of ", e);
                commsLogger.error(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());
                return;
            }
            commsLogger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageLogBuilder.build());
        }
    }

    @Override
    public void logFullMessage(HttpServletRequest httpServletRequest) {
        if (getMessageLoggerUtils().isLogCompleteMessage()) {
            try {
                final String lightRequest = getRawLightRequest(httpServletRequest);
                boolean valid = NormalParameterValidator
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

    /**
     * Creates a message header for logging
     * (subject, from, who, to)
     * {@link LightRequestMessageLog.Builder#setOpType(String)} Human Readable description of the use-case.
     * {@link LightRequestMessageLog.Builder#setOrigin(String)} The origin of the incoming message.
     * {@link LightRequestMessageLog.Builder#setNodeId(String)} Identifier for the counterparty.
     * {@link LightRequestMessageLog.Builder#setDestination(String)} The destination of the message.
     */
    protected abstract void setMessageVector(LightRequestMessageLog.Builder messageLogBuilder, HttpServletRequest httpServletRequest);

    /**
     * @return LightToken that correlates with a LightRequest
     */
    protected abstract String getLightToken(HttpServletRequest httpServletRequest);

    @Nonnull
    protected String getBinaryLightTokenHash(HttpServletRequest httpServletRequest) {
        return LoggingUtil.createBltHash(getLightToken(httpServletRequest), loggingHashDigestAlgorithm, loggingHashDigestProvider);
    }

    protected ILightRequest getLightRequest(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        return getSpringManagedSpecificCommunicationLoggingService()
                .getRequest(getLightToken(httpServletRequest), getMessageLoggerUtils().retrieveAttributes());
    }

    protected String getRawLightRequest(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        return getSpringManagedSpecificCommunicationLoggingService().getRequest(getLightToken(httpServletRequest));
    }

    public MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    public String getLightTokenRequestNodeId() {
        return lightTokenRequestNodeId;
    }

    public void setLightTokenRequestNodeId(String lightTokenConnectorRequestNodeId) {
        this.lightTokenRequestNodeId = lightTokenConnectorRequestNodeId;
    }

    public void setLoggingHashDigestAlgorithm(String loggingHashDigestAlgorithm) {
        this.loggingHashDigestAlgorithm = loggingHashDigestAlgorithm;
    }

    public void setLoggingHashDigestProvider(String loggingHashDigestProvider) {
        this.loggingHashDigestProvider = loggingHashDigestProvider;
    }

    public SpecificCommunicationLoggingService getSpringManagedSpecificCommunicationLoggingService() {
        return springManagedSpecificCommunicationLoggingService;
    }

    public void setSpringManagedSpecificCommunicationLoggingService(SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService) {
        this.springManagedSpecificCommunicationLoggingService = springManagedSpecificCommunicationLoggingService;
    }
}
