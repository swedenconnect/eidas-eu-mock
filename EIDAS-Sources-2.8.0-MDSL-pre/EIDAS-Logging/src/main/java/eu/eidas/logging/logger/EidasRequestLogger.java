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
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.auth.engine.xml.opensaml.XmlSchemaUtil;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.logging.IFullMessageLogger;
import eu.eidas.logging.IMessageLogger;
import eu.eidas.logging.LoggingMarkerMDC;
import eu.eidas.logging.LoggingUtil;
import eu.eidas.logging.MessageLoggerUtils;
import eu.eidas.logging.messages.EidasRequestMessageLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

/**
 * Logs {@link IAuthenticationRequest}
 * <p>
 * Implements {@link IMessageLogger} to log brief highlights under {@link EIDASValues#EIDAS_PACKAGE_REQUEST_LOGGER_VALUE}
 * Implements {@link IFullMessageLogger} log the raw message under {@link EIDASValues#EIDAS_PACKAGE_LOGGING_FULL}
 */
public abstract class EidasRequestLogger implements IMessageLogger, IFullMessageLogger {

    private final Logger fullLogger = LoggerFactory.getLogger(IFullMessageLogger.getLoggerName(this.getClass()));
    private final Logger commsLogger = LoggerFactory.getLogger(
            IMessageLogger.getLoggerName(EIDASValues.EIDAS_PACKAGE_REQUEST_LOGGER_VALUE, this.getClass())
    );

    protected MessageLoggerUtils messageLoggerUtils;

    @Override
    public final void logMessage(final HttpServletRequest httpServletRequest) {
        logMessage(commsLogger, httpServletRequest);
    }

    /**
     * @deprecated use {@link #logMessage(HttpServletRequest)} instead
     */
    @Deprecated
    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) {
        if (messageLoggerUtils.isLogMessages()) {
            final EidasRequestMessageLog.Builder messageLogBuilder = new EidasRequestMessageLog.Builder();
            try {
                final byte[] msgObj = getSamlRequestDecodedBytes(httpServletRequest);
                final IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(msgObj);
                setMessageVector(messageLogBuilder, httpServletRequest, authenticationRequest);

                final String msgId = authenticationRequest.getId();
                final String flowId = messageLoggerUtils.trackMessageFlow(msgId);
                final String msgHashToLog = LoggingUtil.createMsgHash(msgObj);
                messageLogBuilder.setFlowId(flowId);
                messageLogBuilder.setMsgId(msgId);
                messageLogBuilder.setMsgHash(msgHashToLog);
            } catch (RuntimeException | EIDASSAMLEngineException e) {
                setMessageVector(messageLogBuilder, httpServletRequest, null);
                logger.error(LoggingMarkerMDC.SAML_EXCHANGE, "Incomplete log of the eIDAS SAML request because of ", e);
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

                boolean valid = NormalParameterValidator
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


    /**
     * Creates a message header for logging
     * (subject, from, who, to)
     * {@link EidasRequestMessageLog.Builder#setOpType(String)} Human Readable description of the use-case.
     * {@link EidasRequestMessageLog.Builder#setOrigin(String)} The origin of the incoming message.
     * {@link EidasRequestMessageLog.Builder#setNodeId(String)} Identifier for the counterparty.
     * {@link EidasRequestMessageLog.Builder#setDestination(String)} The destination of the message.
     */
    protected abstract void setMessageVector(@Nonnull EidasRequestMessageLog.Builder messageLogBuilder,
                                             @Nonnull HttpServletRequest httpServletRequest,
                                             @Nullable IAuthenticationRequest authenticationRequest);

    /**
     * @return EidasRequest in bytes
     */
    protected abstract byte[] getSamlRequestDecodedBytes(HttpServletRequest httpServletRequest);

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

}
