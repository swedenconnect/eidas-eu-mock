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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.AbstractLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

import static eu.eidas.auth.commons.EIDASValues.REFERER;
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
 * to log the incoming eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ConnectorIncomingEidasResponseLogger extends AbstractLogger {

    private MessageLoggerUtils messageLoggerUtils;
    private Cache<String, String> flowIdCache;

    public ConnectorIncomingEidasResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws EIDASSAMLEngineException {
        if (messageLoggerUtils.isLogMessages()) {

            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.EIDAS_CONNECTOR_RESPONSE.toString(), messageStringBuilder);

            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            final String samlResponseTokenSaml = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);

            final byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlResponseTokenSaml);
            final IAuthenticationResponse authenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);
            final String issuer = authenticationResponse.getIssuer();
            final String nodeId = messageLoggerUtils.getConnectorEntityId(issuer);
            setTagToLog(NODE_ID, nodeId, messageStringBuilder);

            setTagToLog(ORIGIN, httpServletRequest.getHeader(REFERER.toString()), messageStringBuilder);
            setTagToLog(DESTINATION, httpServletRequest.getRequestURL().toString(), messageStringBuilder);

            final String inResponseToId = authenticationResponse.getInResponseToId();

            setTagToLog(FLOW_ID, flowIdCache.get(inResponseToId), messageStringBuilder);
            setTagToLog(MSG_ID, authenticationResponse.getId(), messageStringBuilder);

            final String msgHashToLog = createMsgHashToLog(samlObj);
            setTagToLog(MSG_HASH, msgHashToLog, messageStringBuilder);

            setTagToLog(IN_RESPONSE_TO, inResponseToId, messageStringBuilder);
            setTagToLog(STATUS_CODE, authenticationResponse.getStatusCode(), messageStringBuilder);

            String message = messageStringBuilder.toString();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, message);
        }
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
