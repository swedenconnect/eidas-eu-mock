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
package eu.eidas.node.service.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.MessageLoggerBean;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming eIDAS Request
 * to eIDAS Proxy-Service from eIDAS Connector
 *
 * @since 2.3
 */
public class ProxyServiceIncomingEidasRequestLogger implements IMessageLogger {

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    @Override
    public void logMessage(final Logger logger, final HttpServletRequest httpServletRequest)
            throws EIDASSAMLEngineException {

        if (messageLoggerUtils.isLogMessages()) {
            final String origin = httpServletRequest.getHeader(EIDASValues.REFERER.toString());
            final String samlRequestTokenSaml = httpServletRequest.getParameter(EidasParameterKeys.SAML_REQUEST.toString());
            final byte[] msgObj = EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);
            IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(msgObj);

            final String issuer = authenticationRequest.getIssuer();
            final String msgId = authenticationRequest.getId();
            final String flowId = SAMLEngineUtils.generateNCName();
            flowIdCache.put(msgId, flowId);

            final String nodeId = messageLoggerUtils.getProxyServiceEntityId(issuer);
            final String destinationUrl = authenticationRequest.getDestination();
            final String opType = EIDASValues.EIDAS_SERVICE_REQUEST.toString();

            MessageLoggerBean loggerBean = new MessageLoggerBean(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId);
            final String messageToBeLogged = loggerBean.createLogMessage();
            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageToBeLogged);
        }
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    /**
     * Setter for messageLogged.
     *
     * @param flowIdCache The flowIdCache to set.
     */
    public void setFlowIdCache(Cache<String, String> flowIdCache) {
        this.flowIdCache = flowIdCache;
    }
}
