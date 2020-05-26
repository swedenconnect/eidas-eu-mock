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
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.logging.AbstractLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

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
 * to log the outgoing eIDAS Request
 * to eIDAS Proxy-Service from eIDAS Connector
 *
 * @since 2.3
 */
public final class ConnectorOutgoingEidasRequestLogger extends AbstractLogger {

    private AUCONNECTORUtil springManagedAUCONNECTORUtil;
    private MessageLoggerUtils messageLoggerUtils;
    private Cache<String, String> flowIdCache;

    public ConnectorOutgoingEidasRequestLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws EIDASSAMLEngineException {
        if (messageLoggerUtils.isLogMessages()) {

            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.CONNECTOR_SERVICE_REQUEST.toString(), messageStringBuilder);
            createNodeIdToLog(httpServletRequest, messageStringBuilder);
            setTagToLog(ORIGIN, NOT_APPLICABLE, messageStringBuilder);

            final String samlRequestTokenSaml = (String) httpServletRequest.getAttribute(EidasParameterKeys.SAML_REQUEST.toString());
            final byte[] msgObj = EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
            IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(msgObj);
            setTagToLog(DESTINATION, authenticationRequest.getDestination(), messageStringBuilder);

            final String msgId = authenticationRequest.getId();
            final String flowId = flowIdCache.get(msgId);
            setTagToLog(FLOW_ID, flowId, messageStringBuilder);
            setTagToLog(MSG_ID, msgId, messageStringBuilder);

            final String msgHashToLog = createMsgHashToLog(msgObj);
            setTagToLog(MSG_HASH, msgHashToLog, messageStringBuilder);

            String message = messageStringBuilder.toString();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, message);
        }
    }

    private void createNodeIdToLog(HttpServletRequest httpServletRequest, StringBuilder messageStringBuilder) {
        final String citizenCountryCode = (String) httpServletRequest.getAttribute(NodeParameterNames.CITIZEN_COUNTRY_CODE.toString());
        String issuer = springManagedAUCONNECTORUtil.loadConfigServiceMetadataURL(citizenCountryCode);
        setTagToLog(NODE_ID, messageLoggerUtils.getConnectorEntityId(issuer), messageStringBuilder);
    }

    /**
     * The MessageLoggerUtils setter.
     * @param messageLoggerUtils: The value to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The {@link AUCONNECTORUtil} setter.
     * @param springManagedAUCONNECTORUtil: The value to be set.
     */
    public void setSpringManagedAUCONNECTORUtil(AUCONNECTORUtil springManagedAUCONNECTORUtil) {
        this.springManagedAUCONNECTORUtil = springManagedAUCONNECTORUtil;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache: The value to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
