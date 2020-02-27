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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.logging.AbstractLogger;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

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
 * to log the outgoing eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public final class ProxyServiceOutgoingEidasResponseLogger extends AbstractLogger {

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    public ProxyServiceOutgoingEidasResponseLogger() {
    }

    @Override
    public final void logMessage(final Logger logger, final HttpServletRequest httpServletRequest)
            throws EIDASSAMLEngineException {

        if (messageLoggerUtils.isLogMessages()) {
            final StringBuilder messageStringBuilder = new StringBuilder();
            setTagToLog(TIMESTAMP, DateTime.now(DateTimeZone.UTC).toString(),messageStringBuilder);

            setTagToLog(OP_TYPE, EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString(), messageStringBuilder);

            final String samlResponseToken = getSamlResponseToken(httpServletRequest);
            final byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlResponseToken);

            final IAuthenticationResponse authenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);
            final String inResponseToId = authenticationResponse.getInResponseToId();
            final String issuer = messageLoggerUtils.getIssuer(inResponseToId, httpServletRequest);
            final String nodeId = messageLoggerUtils.getProxyServiceEntityId(issuer);
            setTagToLog(NODE_ID, nodeId, messageStringBuilder);

            setTagToLog(ORIGIN, NOT_APPLICABLE, messageStringBuilder);
            setTagToLog(DESTINATION, getDestinationUrl(httpServletRequest), messageStringBuilder);

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

    private String getDestinationUrl(HttpServletRequest httpServletRequest) {
        String destinationUrl = (String) httpServletRequest.getAttribute(NodeParameterNames.REDIRECT_URL.toString());

        if (StringUtils.isEmpty(destinationUrl)) {
            destinationUrl = (String) httpServletRequest.getAttribute(EidasParameterKeys.ERROR_REDIRECT_URL.toString());
        }

        destinationUrl = removeSessionIdFromUrl(destinationUrl);

        return destinationUrl;
    }

    private String removeSessionIdFromUrl(String destinationUrl) {
        return destinationUrl.replaceAll(";.*", "");
    }

    private String getSamlResponseToken(HttpServletRequest httpServletRequest) {
        String samlResponseTokenSaml = (String) httpServletRequest.getAttribute(NodeParameterNames.SAML_TOKEN.toString());

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            samlResponseTokenSaml = (String) httpServletRequest.getAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString());
        }

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            final WebRequest webRequest = new IncomingRequest(httpServletRequest);
            samlResponseTokenSaml = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);
        }

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            samlResponseTokenSaml = ((ResponseCarryingServiceException) httpServletRequest.getAttribute("exception")).getSamlTokenFail();
        }

        return samlResponseTokenSaml;
    }


    /**
     * The messageLoggerUtils setter.
     * @param messageLoggerUtils: The messageLoggerUils to be set.
     */
    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * The flowIdCache setter.
     * @param flowIdCache The flowIdCache to be set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }

}
