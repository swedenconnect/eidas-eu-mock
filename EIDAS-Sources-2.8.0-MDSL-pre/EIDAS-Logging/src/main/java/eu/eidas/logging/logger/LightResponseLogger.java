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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.LoggingUtil;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.LightResponseMessageLog;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationLoggingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

/**
 * Logs {@link ILightResponse}
 * <p>
 * Implements {@link IMessageLogger} to log brief highlights under {@link EIDASValues#EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE}
 * Implements {@link IFullMessageLogger} log the raw message under {@link EIDASValues#EIDAS_PACKAGE_LOGGING_FULL}
 */
public abstract class LightResponseLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));
    private final Logger commsLogger = LoggerFactory.getLogger(IMessageLogger.getLoggerName(
            EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE, this.getClass()));

    private SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService;
    private MessageLoggerUtils messageLoggerUtils;
    private String lightTokenResponseNodeId;
    private String loggingHashDigestAlgorithm;
    private String loggingHashDigestProvider;

    @Override
    public void logMessage(HttpServletRequest httpServletRequest) {
        logMessage(commsLogger, httpServletRequest);
    }

    /**
     * @deprecated use {@link #logMessage(HttpServletRequest)} instead
     */
    @Deprecated
    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogMessages()) {

            final LightResponseMessageLog.Builder messageLogBuilder = new LightResponseMessageLog.Builder();
            try {
                setMessageVector(messageLogBuilder, httpServletRequest);

                final String bltHashToLog = getBinaryLightTokenHash(httpServletRequest);
                final ILightResponse iLightResponse = getLightResponse(httpServletRequest);
                final String msgHashToLog = LoggingUtil.createMsgHash(iLightResponse.toString().getBytes());

                messageLogBuilder.setFlowId(messageLoggerUtils.trackMessageFlow(iLightResponse.getInResponseToId()));
                messageLogBuilder.setMsgId(iLightResponse.getId());
                messageLogBuilder.setBltHash(bltHashToLog);
                messageLogBuilder.setInResponseTo(iLightResponse.getInResponseToId());
                messageLogBuilder.setMsgHash(msgHashToLog);
                messageLogBuilder.setStatusCode(iLightResponse.getStatus().getStatusCode());
            } catch (InvalidParameterEIDASException | SpecificCommunicationException e) {
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the light response because of ", e);
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
                final String lightResponse = getRawLightResponse(httpServletRequest);
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
     * Creates a message header for logging
     * (subject, from, who, to)
     * {@link LightResponseMessageLog.Builder#setOpType(String)} Human Readable description of the use-case.
     * {@link LightResponseMessageLog.Builder#setOrigin(String)} The origin of the incoming message.
     * {@link LightResponseMessageLog.Builder#setNodeId(String)} Identifier for the counterparty.
     * {@link LightResponseMessageLog.Builder#setDestination(String)} The destination of the message.
     */
    protected abstract void setMessageVector(LightResponseMessageLog.Builder messageLogBuilder, HttpServletRequest httpServletRequest);

    /**
     * @return LightToken that correlates with a LightResponse
     */
    protected abstract String getLightToken(HttpServletRequest httpServletRequest);

    @Nonnull
    protected String getBinaryLightTokenHash(HttpServletRequest httpServletRequest) {
        return LoggingUtil.createBltHash(getLightToken(httpServletRequest), loggingHashDigestAlgorithm, loggingHashDigestProvider);
    }

    protected ILightResponse getLightResponse(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        return getSpringManagedSpecificCommunicationLoggingService()
                .getResponse(getLightToken(httpServletRequest), getMessageLoggerUtils().retrieveAttributes());
    }

    protected String getRawLightResponse(HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        return getSpringManagedSpecificCommunicationLoggingService()
                .getResponse(getLightToken(httpServletRequest));
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    protected MessageLoggerUtils getMessageLoggerUtils() {
        return messageLoggerUtils;
    }

    public SpecificCommunicationLoggingService getSpringManagedSpecificCommunicationLoggingService() {
        return springManagedSpecificCommunicationLoggingService;
    }

    public void setSpringManagedSpecificCommunicationLoggingService(SpecificCommunicationLoggingService springManagedSpecificCommunicationLoggingService) {
        this.springManagedSpecificCommunicationLoggingService = springManagedSpecificCommunicationLoggingService;
    }

    public String getLightTokenResponseNodeId() {
        return lightTokenResponseNodeId;
    }

    public void setLightTokenResponseNodeId(String lightTokenProxyserviceResponseNodeId) {
        this.lightTokenResponseNodeId = lightTokenProxyserviceResponseNodeId;
    }

    public void setLoggingHashDigestAlgorithm(String loggingHashDigestAlgorithm) {
        this.loggingHashDigestAlgorithm = loggingHashDigestAlgorithm;
    }

    public void setLoggingHashDigestProvider(String loggingHashDigestProvider) {
        this.loggingHashDigestProvider = loggingHashDigestProvider;
    }
}
