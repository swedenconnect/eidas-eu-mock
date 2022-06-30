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
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.node.logging.IMessageLogger;
import eu.eidas.node.logging.LoggingMarkerMDC;
import eu.eidas.node.logging.MessageLoggerBean;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceExtensionImpl;
import org.slf4j.Logger;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

/**
 * Implements {@link IMessageLogger} to be used
 * to log the incoming {@link ILightResponse}
 * to eIDAS Proxy-Service from MS's Specific Proxy-Service
 *
 * @since 2.3
 */
public class ProxyServiceIncomingLightResponseLogger implements IMessageLogger {

    private MessageLoggerUtils messageLoggerUtils;

    private Cache<String, String> flowIdCache;

    private SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension;

    @Override
    public void logMessage(final Logger logger, final HttpServletRequest httpServletRequest) throws SpecificCommunicationException {

        if (messageLoggerUtils.isLogMessages()) {
            final String origin = httpServletRequest.getHeader(EIDASValues.REFERER.toString());
            final String destinationUrl = httpServletRequest.getRequestURL().toString();
            final String nodeId = springManagedSpecificProxyserviceCommunicationServiceExtension.getLightTokenResponseNodeId();
            final String tokenBase64 = httpServletRequest.getParameter(EidasParameterKeys.TOKEN.toString());
            final ILightResponse iLightResponse = springManagedSpecificProxyserviceCommunicationService
                    .getAndRemoveResponse(tokenBase64, messageLoggerUtils.retrieveProxyServiceAttributes());

            final String msgId = iLightResponse.getId();
            final String inResponseToId = iLightResponse.getInResponseToId();
            final String flowId = flowIdCache.get(inResponseToId);
            final String opType = EIDASValues.EIDAS_SERVICE_SPECIFIC_RESPONSE.toString();

            final byte[] msgObj = iLightResponse.toString().getBytes();
            final String statusCode = iLightResponse.getStatus().getStatusCode();
            MessageLoggerBean loggerBean = new MessageLoggerBean(opType, msgObj, msgId, origin,
                    destinationUrl, flowId, nodeId, tokenBase64, inResponseToId, statusCode);
            final String messageToBeLogged = loggerBean.createLogMessage();

            springManagedSpecificProxyserviceCommunicationServiceExtension.putResponse(tokenBase64, iLightResponse);

            logger.info(LoggingMarkerMDC.SAML_EXCHANGE, messageToBeLogged);
        }
    }

    public void setMessageLoggerUtils(MessageLoggerUtils messageLoggerUtils) {
        this.messageLoggerUtils = messageLoggerUtils;
    }

    public void setSpringManagedSpecificProxyserviceCommunicationServiceExtension(
            SpecificProxyserviceCommunicationServiceExtensionImpl springManagedSpecificProxyserviceCommunicationServiceExtension) {

        this.springManagedSpecificProxyserviceCommunicationServiceExtension = springManagedSpecificProxyserviceCommunicationServiceExtension;
    }

    private SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService;

    public void setSpringManagedSpecificProxyserviceCommunicationService(SpecificCommunicationService springManagedSpecificProxyserviceCommunicationService) {
        this.springManagedSpecificProxyserviceCommunicationService = springManagedSpecificProxyserviceCommunicationService;
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
