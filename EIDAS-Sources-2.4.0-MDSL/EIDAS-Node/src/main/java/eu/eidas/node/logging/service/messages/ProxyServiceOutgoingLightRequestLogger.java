/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.node.logging.service.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.logging.AbstractLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceExtensionImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

import static eu.eidas.node.logging.MessageLoggerTag.BLT_HASH;
import static eu.eidas.node.logging.MessageLoggerTag.DESTINATION;
import static eu.eidas.node.logging.MessageLoggerTag.FLOW_ID;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_HASH;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_ID;
import static eu.eidas.node.logging.MessageLoggerTag.NODE_ID;
import static eu.eidas.node.logging.MessageLoggerTag.OP_TYPE;
import static eu.eidas.node.logging.MessageLoggerTag.ORIGIN;
import static eu.eidas.node.logging.MessageLoggerTag.TIMESTAMP;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing {@link ILightRequest}
 * to MS's Specific Proxy-Service from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceOutgoingLightRequestLogger extends AbstractLogger {

    private MessageLoggerUtils messageLoggerUtils;
    private SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService;
    private SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension;
    private Cache<String, String> flowIdCache;

    public ProxyServiceOutgoingLightRequestLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest)
            throws SpecificCommunicationException {

        if (messageLoggerUtils.isLogMessages()) {
            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString(), messageStringBuilder);
            setTagToLog(NODE_ID, springManagedSpecificProxyserviceCommunicationServiceExtension
                    .getLightTokenRequestNodeId(), messageStringBuilder);
            setTagToLog(ORIGIN, NOT_APPLICABLE, messageStringBuilder);
            setTagToLog(DESTINATION, messageLoggerUtils.getProxyServiceRedirectUrl(), messageStringBuilder);

            final String tokenBase64 = (String) httpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString());
            final ILightRequest iLightRequest = springManagedSpecificProxyserviceCommunicationService.
                    getAndRemoveRequest(tokenBase64, messageLoggerUtils.retrieveConnectorAttributes());

            byte[] msgObj = iLightRequest.toString().getBytes();

            final String msgId = iLightRequest.getId();
            final String flowId = flowIdCache.get(msgId);
            setTagToLog(FLOW_ID, flowId, messageStringBuilder);
            setTagToLog(MSG_ID, msgId, messageStringBuilder);

            final String msgHashToLog = createMsgHashToLog(msgObj);
            setTagToLog(MSG_HASH, msgHashToLog, messageStringBuilder);

            springManagedSpecificProxyserviceCommunicationServiceExtension.putRequest(tokenBase64, iLightRequest);

            final String bltHashToLog = createBltHashToLog(tokenBase64);
            setTagToLog(BLT_HASH, bltHashToLog, messageStringBuilder);

            String message = messageStringBuilder.toString();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, message);
        }
    }


    /**
     * MessageLoggerUtils setter
     * @param messageLoggerUtils: the messageLoggerUtils to be set
     */
    public final void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * Communication service extension setter
     * @param springManagedSpecificProxyserviceCommunicationServiceExtension: the communication service extension to be set
     */
    public final void setSpringManagedSpecificProxyserviceCommunicationServiceExtension(
            SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension) {

        this.springManagedSpecificProxyserviceCommunicationServiceExtension = springManagedSpecificProxyserviceCommunicationServiceExtension;
    }

    /**
     * Communication service setter
     * @param springManagedSpecificProxyserviceCommunicationService: the Communication service to be set
     */
    public final void setSpringManagedSpecificProxyserviceCommunicationService(
            SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService) {

        this.springManagedSpecificProxyserviceCommunicationService = springManagedSpecificProxyserviceCommunicationService;
    }

    /**
     * flowIdCache setter.
     *
     * @param flowIdCache The flowIdCache to set.
     */
    public final void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
