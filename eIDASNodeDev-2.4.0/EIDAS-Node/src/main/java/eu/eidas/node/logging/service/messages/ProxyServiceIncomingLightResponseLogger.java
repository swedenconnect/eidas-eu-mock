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
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
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
import static eu.eidas.node.logging.MessageLoggerTag.IN_RESPONSE_TO;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_HASH;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_ID;
import static eu.eidas.node.logging.MessageLoggerTag.NODE_ID;
import static eu.eidas.node.logging.MessageLoggerTag.OP_TYPE;
import static eu.eidas.node.logging.MessageLoggerTag.ORIGIN;
import static eu.eidas.node.logging.MessageLoggerTag.STATUS_CODE;
import static eu.eidas.node.logging.MessageLoggerTag.TIMESTAMP;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming {@link ILightResponse}
 * to eIDAS Proxy-Service from MS's Specific Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceIncomingLightResponseLogger extends AbstractLogger {

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    private SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension;
    private SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService;

    public ProxyServiceIncomingLightResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws SpecificCommunicationException {

        if (messageLoggerUtils.isLogMessages()) {
            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString(), messageStringBuilder);
            setTagToLog(NODE_ID, springManagedSpecificProxyserviceCommunicationServiceExtension.getLightTokenResponseNodeId(), messageStringBuilder);
            setTagToLog(ORIGIN, httpServletRequest.getHeader(EIDASValues.REFERER.toString()), messageStringBuilder);
            setTagToLog(DESTINATION, httpServletRequest.getRequestURL().toString(), messageStringBuilder);

            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            final String tokenBase64= webRequest.getEncodedLastParameterValue(EidasParameterKeys.TOKEN);

            final ILightResponse iLightResponse = springManagedSpecificProxyserviceCommunicationService
                    .getAndRemoveResponse(tokenBase64, messageLoggerUtils.retrieveConnectorAttributes());

            final String inResponseToId = iLightResponse.getInResponseToId();
            final String flowId = flowIdCache.get(inResponseToId);
            setTagToLog(FLOW_ID, flowId, messageStringBuilder);

            setTagToLog(MSG_ID, iLightResponse.getId(), messageStringBuilder);

            byte[] msgObj = iLightResponse.toString().getBytes();
            final String msgHashToLog = createMsgHashToLog(msgObj);
            setTagToLog(MSG_HASH, msgHashToLog, messageStringBuilder);

            final String bltHashToLog = createBltHashToLog(tokenBase64);
            setTagToLog(BLT_HASH, bltHashToLog, messageStringBuilder);

            setTagToLog(IN_RESPONSE_TO, inResponseToId, messageStringBuilder);
            setTagToLog(STATUS_CODE, iLightResponse.getStatus().getStatusCode(), messageStringBuilder);

            springManagedSpecificProxyserviceCommunicationServiceExtension.putResponse(tokenBase64, iLightResponse);

            String message = messageStringBuilder.toString();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, message);
        }
    }

    /**
     * The message logger utils etter.
     * @param messageLoggerUtils: The value to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The specific communication proxy service extension setter.
     * @param springManagedSpecificProxyserviceCommunicationServiceExtension: The value to be set.
     */
    public void setSpringManagedSpecificProxyserviceCommunicationServiceExtension(
            SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension) {

        this.springManagedSpecificProxyserviceCommunicationServiceExtension = springManagedSpecificProxyserviceCommunicationServiceExtension;
    }

    /**
     * The specific communication proxy service setter.
     * @param springManagedSpecificProxyserviceCommunicationService The value to be set.
     */
    public void setSpringManagedSpecificProxyserviceCommunicationService(SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService) {
        this.springManagedSpecificProxyserviceCommunicationService = springManagedSpecificProxyserviceCommunicationService;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache The value to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
