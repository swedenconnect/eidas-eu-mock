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
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerBean;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the outgoing eIDAS Response
 * to eIDAS Connector from eIDAS Proxy-Service
 *
 * @since 2.3
 */
public class ProxyServiceOutgoingEidasResponseLogger implements IMessageLogger {

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    @Override
    public void logMessage(final Logger logger, final HttpServletRequest httpServletRequest)
            throws EIDASSAMLEngineException {

        if (messageLoggerUtils.isLogMessages()) {

            final String origin = "N/A";
            final String destinationUrl = getDestinationUrl(httpServletRequest);

            final String samlResponseToken = getSamlResponseToken(httpServletRequest);

            final byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlResponseToken);

            final IAuthenticationResponse authenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);

            final String inResponseToId = authenticationResponse.getInResponseToId();
            final String flowId = flowIdCache.get(inResponseToId);
            final String issuer = messageLoggerUtils.getIssuer(inResponseToId, httpServletRequest);
            final String nodeId = messageLoggerUtils.getProxyServiceEntityId(issuer);

            final String opType = EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString();
            String msgId = authenticationResponse.getId();
            String statusCode = authenticationResponse.getStatusCode();
            MessageLoggerBean loggerBean = new MessageLoggerBean(opType, samlObj, msgId, origin,
                    destinationUrl, flowId, nodeId, inResponseToId, statusCode);

            final String messageToBeLogged = loggerBean.createLogMessage();

            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageToBeLogged);
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
            samlResponseTokenSaml = httpServletRequest.getParameter("SAMLResponse");
        }

        if (StringUtils.isEmpty(samlResponseTokenSaml)) {
            samlResponseTokenSaml = ((ResponseCarryingServiceException) httpServletRequest.getAttribute("exception")).getSamlTokenFail();
        }

        return samlResponseTokenSaml;
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
