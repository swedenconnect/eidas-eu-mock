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
package eu.eidas.node.logging.connector.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.node.logging.AbstractLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceExtensionImpl;
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
 * to log the outgoing {@link ILightResponse}
 * to MS's Specific Connector from eIDAS Connector
 *
 * @since 2.3
 */
public final class ConnectorOutgoingLightResponseLogger extends AbstractLogger {

    private SpecificCommunicationService springManagedSpecificConnectorCommunicationService;

    private SpecificConnectorCommunicationServiceExtensionImpl springManagedSpecificConnectorCommunicationServiceExtension;

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    public ConnectorOutgoingLightResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws SpecificCommunicationException {
        if (messageLoggerUtils.isLogMessages()) {

            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.EIDAS_CONNECTOR_CONNECTOR_RESPONSE.toString(), messageStringBuilder);
            setTagToLog(NODE_ID, springManagedSpecificConnectorCommunicationServiceExtension.getLightTokenResponseNodeId(), messageStringBuilder);
            setTagToLog(ORIGIN, NOT_APPLICABLE, messageStringBuilder);
            setTagToLog(DESTINATION, messageLoggerUtils.getConnectorRedirectUrl(), messageStringBuilder);

            final String tokenBase64 = (String) httpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString());
            final ILightResponse iLightResponse = springManagedSpecificConnectorCommunicationService
                    .getAndRemoveResponse(tokenBase64, messageLoggerUtils.retrieveConnectorAttributes());

            final String inResponseToId = iLightResponse.getInResponseToId();
            final String flowId = flowIdCache.get(inResponseToId);
            setTagToLog(FLOW_ID, flowId, messageStringBuilder);

            setTagToLog(MSG_ID, iLightResponse.getId(), messageStringBuilder);

            byte[] msgObj = iLightResponse.toString().getBytes();
            final String msgHashToLog = createMsgHashToLog(msgObj);
            setTagToLog(MSG_HASH, msgHashToLog, messageStringBuilder);
            springManagedSpecificConnectorCommunicationServiceExtension.putResponse(tokenBase64, iLightResponse);

            final String bltHashToLog = createBltHashToLog(tokenBase64);
            setTagToLog(BLT_HASH, bltHashToLog, messageStringBuilder);

            setTagToLog(IN_RESPONSE_TO, inResponseToId, messageStringBuilder);
            setTagToLog(STATUS_CODE, iLightResponse.getStatus().getStatusCode(), messageStringBuilder);
            String message = messageStringBuilder.toString();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, message);
        }
    }


    /**
     * The messageLoggerUtils setter.
     * @param messageLoggerUtils: The value to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The specific connector communication service extension setter.
     * @param springManagedSpecificConnectorCommunicationServiceExtension: The value to be set.
     */
    public void setSpringManagedSpecificConnectorCommunicationServiceExtension(SpecificConnectorCommunicationServiceExtensionImpl springManagedSpecificConnectorCommunicationServiceExtension) {
        this.springManagedSpecificConnectorCommunicationServiceExtension = springManagedSpecificConnectorCommunicationServiceExtension;
    }

    /**
     * The specific connector communication service setter.
     * @param springManagedSpecificConnectorCommunicationService: The value to be set.
     */
    public void setSpringManagedSpecificConnectorCommunicationService(SpecificCommunicationService springManagedSpecificConnectorCommunicationService) {
        this.springManagedSpecificConnectorCommunicationService = springManagedSpecificConnectorCommunicationService;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache: The value to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
