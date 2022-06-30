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
package eu.eidas.node.connector.messages;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.MessageLoggerBean;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.utils.PropertiesUtil;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing eIDAS Request
 * to eIDAS Proxy-Service from eIDAS Connector
 *
 * @since 2.3
 */
public class ConnectorOutgoingEidasRequestLogger implements IMessageLogger {

    private AUCONNECTORUtil springManagedAUCONNECTORUtil;

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    @Override
    public void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws EIDASSAMLEngineException {
        if (messageLoggerUtils.isLogMessages()) {
            final String origin = "N/A";

            final String nodeId = getNodeId(httpServletRequest);
            final String samlRequestTokenSaml = (String) httpServletRequest.getAttribute(EidasParameterKeys.SAML_REQUEST.toString());
            final byte[] msgObj = EidasStringUtil.decodeBytesFromBase64(samlRequestTokenSaml);

            IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(msgObj);

            final String msgId = authenticationRequest.getId();
            final String flowId = flowIdCache.get(msgId);
            final String destinationUrl = authenticationRequest.getDestination();

            final String opType = EIDASValues.CONNECTOR_SERVICE_REQUEST.toString();
            MessageLoggerBean loggerBean = new MessageLoggerBean(opType, msgObj, msgId, origin, destinationUrl, flowId, nodeId);
            final String messageToBeLogged = loggerBean.createLogMessage();

            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageToBeLogged);
        }
    }

    private String getNodeId(HttpServletRequest httpServletRequest) {
        final String citizenCountryCode = (String) httpServletRequest.getAttribute(NodeParameterNames.CITIZEN_COUNTRY_CODE.toString());
        String issuer = springManagedAUCONNECTORUtil.loadConfigServiceMetadataURL(citizenCountryCode);
        return messageLoggerUtils.getConnectorEntityId(issuer);
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    public void setSpringManagedAUCONNECTORUtil(AUCONNECTORUtil springManagedAUCONNECTORUtil) {
        this.springManagedAUCONNECTORUtil = springManagedAUCONNECTORUtil;
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
