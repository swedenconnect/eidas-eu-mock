/*
 * Copyright (c) 2017 by European Commission
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

import java.io.IOException;
import java.util.Collection;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.auth.commons.EIDASValues;
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
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.utils.EidasAttributesUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceImpl;

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

    private Collection<AttributeDefinition<?>> REGISTRY; 

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SpecificProxyServiceResponse.class.getName());

    @Override
    public void init() throws ServletException {
        REGISTRY=retrieveAttributes();
    }

	private Collection<AttributeDefinition<?>> retrieveAttributes() {
		ServiceControllerService controllerService = (ServiceControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString());

    	return ImmutableSortedSet.copyOf(controllerService.getProxyService()
        		.getSamlService()
        		.getSamlEngine()
        		.getProtocolProcessor()
        		.getAllSupportedAttributes());
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
     * @param request the servlet request
     * @param response the servlet response
     * @throws IOException
     * @throws ServletException
     */

    private void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final String token = request.getParameter(EidasParameterKeys.TOKEN.toString());
            final SpecificProxyserviceCommunicationServiceImpl specificProxyserviceCommunicationService
                    = (SpecificProxyserviceCommunicationServiceImpl) getApplicationContext()
                    .getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString());
            final ILightResponse iLightResponse = specificProxyserviceCommunicationService.getAndRemoveResponse(token, retrieveAttributes());

            final String url = handleExecute(request, response, iLightResponse);
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(url);
            dispatcher.forward(request, response);
            HttpSession session = request.getSession(false);
            if (null != session
                    && session.getAttribute(EidasParameterKeys.EIDAS_CONNECTOR_SESSION.toString()) == null) {
                session.invalidate();
            }
        } catch (ServletException e) {
            getLogger().info("ERROR : ServletException {}", e.getMessage());
            getLogger().debug("ERROR : ServletException {}", e);
            throw e;
        } catch (IOException e) {
            getLogger().info("IOException {}", e.getMessage());
            getLogger().debug("IOException {}", e);
            throw e;
        } catch (SpecificCommunicationException e) {
            throw new ServletException(e);
        }

    }


    private String handleExecute(HttpServletRequest request, HttpServletResponse response, final ILightResponse lightResponse) throws ServletException {

        ServiceControllerService controllerService = (ServiceControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString());

        HttpSession session = request.getSession();
        SessionHolder.setId(session);
        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_RESPONSE);

        // This is not the specific Map
        CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap =
                controllerService.getProxyServiceRequestCorrelationMap();
        StoredAuthenticationRequest storedAuthenticationRequest =
                requestCorrelationMap.remove(lightResponse.getInResponseToId());

        if (null == storedAuthenticationRequest) {
            // send the error back:
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not correlate any eIDAS request to the received specific IdP response: "
                                  + lightResponse);
            }
            throw new ServletException(
                    "Could not correlate specific response ID: " + lightResponse.getId() + " to any eIDAS request");
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
            String samlTokenFail = controllerService.getProxyService()
                    .generateSamlTokenFail(originalRequest, statusCode, null, errorSubCode, errorMessage,
                                           webRequest.getRemoteIpAddress(), false);

            request.setAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString(), samlTokenFail);
        } else {

            retVal = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();

            IResponseMessage responseMessage = controllerService.getProxyService()
                    .processIdpResponse(webRequest, storedAuthenticationRequest, lightResponse);

            String samlToken = EidasStringUtil.encodeToBase64(responseMessage.getMessageBytes());

            request.setAttribute(NodeParameterNames.SAML_TOKEN.toString(), samlToken);

            IAuthenticationResponse authnResponse = responseMessage.getResponse();

            ImmutableAttributeMap responseImmutableAttributeMap = authnResponse.getAttributes();

            ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> responseImmutableMap =
                    responseImmutableAttributeMap.getAttributeMap();

            boolean hasEidasAttributes = false;
            for (final AttributeDefinition attributeDefinition : responseImmutableMap.keySet()) {
                if (EidasSpec.REGISTRY.contains(attributeDefinition)) {
                    hasEidasAttributes = true;
                    break;
                }
            }
            request.setAttribute(NodeParameterNames.EIDAS_ATTRIBUTES_PARAM.toString(),
                                 Boolean.valueOf(hasEidasAttributes));

            if (StringUtils.isNotBlank(authnResponse.getLevelOfAssurance())) {
                request.setAttribute(NodeParameterNames.LOA_VALUE.toString(),
                                     EidasAttributesUtil.getUserFriendlyLoa(authnResponse.getLevelOfAssurance()));
            }
        }

        // Prevent cookies from being accessed through client-side script.
        setHTTPOnlyHeaderToSession(false, request, response);

        // Gets the attributes from Attribute Providers and validates mandatory
        // attributes.

        // Setting internal variables, to be included by the Struts on the JSP
        getLogger().trace("setting internal variables");

        String redirectUrl = originalRequest.getAssertionConsumerServiceURL();
        getLogger().debug("redirectUrl: " + redirectUrl);

        request.setAttribute(NodeParameterNames.REDIRECT_URL.toString(),
                             response.encodeRedirectURL(redirectUrl)); // Correct URl redirect cookie implementation

        request.setAttribute(EidasParameterKeys.SP_ID.toString(), originalRequest.getProviderName());

        String relayState = lightResponse.getRelayState();
        if (StringUtils.isNotBlank(relayState)) {
            getLogger().debug("Relay State ProxyService " + relayState);
            request.setAttribute(NodeParameterNames.RELAY_STATE.toString(), relayState);
        }

        return retVal;
    }
}
