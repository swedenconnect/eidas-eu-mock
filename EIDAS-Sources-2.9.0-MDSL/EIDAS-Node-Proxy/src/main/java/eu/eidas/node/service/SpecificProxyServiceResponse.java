/*
 * Copyright (c) 2024 by European Commission
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
 * limitations under the Licence.
 */
package eu.eidas.node.service;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.servlet.binding.ProxySamlResponseViewMapping;
import eu.eidas.node.service.servlet.view.NodeViewNames;
import eu.eidas.node.service.utils.ProxyServiceSamlFailureUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * Handles the incoming response from the MS Specific Proxy Service.
 *
 * @see eu.eidas.node.AbstractNodeServlet
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public final class SpecificProxyServiceResponse extends AbstractNodeServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 4539991356226362922L;

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SpecificProxyServiceResponse.class.getName());

    private Collection<AttributeDefinition<?>> retrieveAttributes() {
        String beanName = ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
        ServiceControllerService controllerService = getBean(ServiceControllerService.class, beanName);

        return controllerService.getProxyService()
                .getSamlService()
                .getSamlEngine()
                .getProtocolProcessor()
                .getAllSupportedAttributes()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    /**
     * Receives/processes the {@link ILightResponse} from the MS Specific Proxy Service
     * then sets the internal variables used by the redirection JSP.
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @throws IOException
     * @throws ServletException
     */

    private void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            String beanName = ProxyBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString();
            final SpecificCommunicationService specificProxyserviceCommunicationService = getBean(
                    SpecificCommunicationService.class, beanName);

            final WebRequest webRequest = new IncomingRequest(request);
            final String token = webRequest.getEncodedLastParameterValue(EidasParameterKeys.TOKEN.toString());
            final ILightResponse iLightResponse = specificProxyserviceCommunicationService.getAndRemoveResponse(token, retrieveAttributes());

            final String url = handleExecute(request, response, iLightResponse);
            forwardRequest(url, request, response);
        } catch (ServletException e) {
            getLogger().info("ERROR : ServletException {}", e.getMessage());
            getLogger().debug("ERROR : ServletException {}", e);
            throw e;
        } catch (IOException e) {
            getLogger().info("IOException {}", e.getMessage());
            getLogger().debug("IOException {}", e);
            throw e;
        } catch (SpecificCommunicationException e) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorMessage()), e);
        }
    }

    private String handleExecute(HttpServletRequest request, HttpServletResponse response, final ILightResponse lightResponse) throws ServletException {

        checkLightResponseAntiReplay(lightResponse);

        String beanName = ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString();
        ServiceControllerService controllerService = getBean(ServiceControllerService.class, beanName);

        HttpSession session = request.getSession();
        SessionHolder.setId(session);
        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_RESPONSE);

        // This is not the specific Map
        Cache<String, StoredAuthenticationRequest> requestCorrelationMap =
                controllerService.getProxyServiceRequestCorrelationCache();
        StoredAuthenticationRequest storedAuthenticationRequest =
                requestCorrelationMap.getAndRemove(lightResponse.getInResponseToId());

        if (null == storedAuthenticationRequest) {
            // send the error back:
            String additionalInformation = "Could not correlate any eIDAS request to the received specific IdP response: "
                    + lightResponse.getId();
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorCode()),
                    EidasErrors.get(EidasErrorKey.MESSAGE_FORMAT_UNSUPPORTED.errorMessage()), additionalInformation);
        }

        IAuthenticationRequest originalRequest = storedAuthenticationRequest.getRequest();

        WebRequest webRequest = new IncomingRequest(request);

        String retVal;

        IResponseStatus responseStatus = lightResponse.getStatus();
        if (responseStatus.isFailure()) {

            String statusCode = responseStatus.getStatusCode();
            String errorMessage = responseStatus.getStatusMessage();
            String errorSubCode = responseStatus.getSubStatusCode();

            // send the error back:
            if (LOG.isErrorEnabled()) {
                LOG.error("Received failed authentication from Specific Idp: errorMessage=\"" + errorMessage
                        + "\", statusCode=\"" + statusCode + "\", subCode=\"" + errorSubCode + "\"");
            }
            retVal = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();

            getLogger().trace("Generate SAMLTokenFail because of authentication failure received from specific IdP");
            String samlTokenFail = ProxyServiceSamlFailureUtil
                    .generateErrorAuthenticationResponse(originalRequest, statusCode, null, errorSubCode, errorMessage,
                            webRequest.getRemoteIpAddress(), false);

            request.setAttribute(ProxySamlResponseViewMapping.SAML_TOKEN, samlTokenFail);
        } else {

            retVal = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();

            IResponseMessage responseMessage = controllerService.getProxyService()
                    .processIdpResponse(webRequest, storedAuthenticationRequest, lightResponse);

            String samlToken = EidasStringUtil.encodeToBase64(responseMessage.getMessageBytes());

            request.setAttribute(ProxySamlResponseViewMapping.SAML_TOKEN, samlToken);

            IAuthenticationResponse authnResponse = responseMessage.getResponse();

            ImmutableAttributeMap responseImmutableAttributeMap = authnResponse.getAttributes();
            Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> responseMap =
                    responseImmutableAttributeMap.getAttributeMap();

            boolean hasEidasAttributes = false;
            for (final AttributeDefinition attributeDefinition : responseMap.keySet()) {
                if (EidasSpec.REGISTRY.contains(attributeDefinition)) {
                    hasEidasAttributes = true;
                    break;
                }
            }
            request.setAttribute(ProxySamlResponseViewMapping.EIDAS_ATTRIBUTES_PARAM,
                    Boolean.valueOf(hasEidasAttributes));
        }

        // Prevent cookies from being accessed through client-side script.
        setHTTPOnlyHeaderToSession(false, request, response);

        // Gets the attributes from Attribute Providers and validates mandatory
        // attributes.

        // Setting internal variables, to be included by the Struts on the JSP
        getLogger().trace("setting internal variables");

        String redirectUrl = originalRequest.getAssertionConsumerServiceURL();
        getLogger().debug("redirectUrl: " + redirectUrl);

        request.setAttribute(ProxySamlResponseViewMapping.REDIRECT_URL,
                response.encodeRedirectURL(redirectUrl)); // Correct URl redirect cookie implementation

        request.setAttribute(EidasParameterKeys.SP_ID.toString(), originalRequest.getProviderName());

        request.setAttribute(EidasParameterKeys.ISSUER.toString(),
                response.encodeRedirectURL(storedAuthenticationRequest.getRequest().getIssuer()));

        String relayState = lightResponse.getRelayState();
        if (StringUtils.isNotBlank(relayState)) {
            getLogger().debug("Relay State ProxyService " + relayState);
            request.setAttribute(ProxySamlResponseViewMapping.RELAY_STATE, relayState);
        }

        return retVal;
    }

    /**
     * Prevents multiple submission of the same {@link eu.eidas.auth.commons.light.impl.LightResponse}
     *
     * @param lightResponse the {@link eu.eidas.auth.commons.light.impl.LightResponse} under anti replay check
     */
    private void checkLightResponseAntiReplay(ILightResponse lightResponse) {
        AUSERVICEUtil serviceUtil = getBean(AUSERVICEUtil.class);
        String messageId = lightResponse.getId() + lightResponse.getInResponseToId();
        final boolean isNotPresentInCache = serviceUtil.checkNotPresentInCache(messageId).booleanValue();
        if (!isNotPresentInCache) {
            // There is no (code, message) defined for Light Response error handling in the Proxy Service
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorMessage()));
        }
    }
}
