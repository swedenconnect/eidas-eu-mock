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

package eu.eidas.node.connector;

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

import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceImpl;

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class SpecificConnectorRequestServlet extends AbstractNodeServlet {

    private static final Logger LOG = LoggerFactory.getLogger(SpecificConnectorRequestServlet.class.getName());

    private static final long serialVersionUID = 2037358134080320372L;

    private Collection<AttributeDefinition<?>> REGISTRY; 

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    public void init() throws ServletException {
    	REGISTRY = retrieveAttributes();
    }

	private Collection<AttributeDefinition<?>> retrieveAttributes() {
		ConnectorControllerService connectorController = (ConnectorControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
    	Collection<AttributeDefinition<?>> registry=ImmutableSortedSet.copyOf(connectorController
        		.getConnectorService()
        		.getSamlService()
        		.getSamlEngine()
        		.getProtocolProcessor()
        		.getAllSupportedAttributes());
		return registry;
	}
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (acceptsHttpRedirect()) {
            doPost(request, response);
        } else {
            LOG.warn("BUSINESS EXCEPTION : redirect binding is not allowed");
        }
    }

    /**
     * Post method
     *
     * @param httpServletRequest the http servlet request
     * @param httpServletResponse the http servlet response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        // Prevent cookies from being accessed through client-side script WITH renew of session.
        setHTTPOnlyHeaderToSession(true, httpServletRequest, httpServletResponse);
        HttpSession session = httpServletRequest.getSession();
        SessionHolder.setId(session);
        // request received from SP
        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.SP_REQUEST);
        // http session created on eidas connector
        session.setAttribute(EidasParameterKeys.EIDAS_CONNECTOR_SESSION.toString(), Boolean.TRUE);

        // Obtaining the assertion consumer url from SPRING context
        ConnectorControllerService connectorController = (ConnectorControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
        LOG.trace(connectorController.toString());

        //maintain the same binding of the initial request
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());

        final ILightRequest lightRequest = getiLightRequest(httpServletRequest,retrieveAttributes());

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(httpServletRequest);

        // Validating the optional HTTP Parameter relayState.
        String relayState = validateRelayState(lightRequest);

        // Validating injected parameter.
        NormalParameterValidator.paramName(EidasErrorKey.CONNECTOR_REDIRECT_URL.toString())
                .paramValue(connectorController.getAssertionConsUrl())
                .validate();

        webRequest.getRequestState().setServiceUrl(encodeURL(connectorController.getAssertionConsUrl(), httpServletResponse));

        // Validates the origin of the request, creates, sign and send an SAML.
        IRequestMessage requestMessage = connectorController.getConnectorService().getAuthenticationRequest(webRequest, lightRequest);
        IAuthenticationRequest authData = requestMessage.getRequest();
        session.setAttribute(EidasParameterKeys.SAML_IN_RESPONSE_TO.toString(), authData.getId());
        session.setAttribute(EidasParameterKeys.ISSUER.toString(), authData.getIssuer());

        //the request is valid, so normally for any error raised from here we have to send back a saml response

        PropertiesUtil.checkConnectorActive();

        // push the samlRequest in the distributed hashMap - Sets the internal ProxyService URL variable to redirect the Citizen to the ProxyService
        String serviceUrl = authData.getDestination();
        NormalParameterValidator.paramName(EidasErrorKey.SERVICE_REDIRECT_URL.toString())
                .paramValue(serviceUrl)
                .validate();

        LOG.debug("Redirecting to serviceUrl: " + serviceUrl);
        // Validates the SAML TOKEN
        String samlRequestTokenSaml = EidasStringUtil.toString(requestMessage.getMessageBytes());

        NormalParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST)
                .paramValue(serviceUrl)
                .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_ERROR_CREATE_SAML)
                .validate();

        LOG.debug("sessionId is on cookies () or fromURL ", httpServletRequest.isRequestedSessionIdFromCookie(),
                  httpServletRequest.isRequestedSessionIdFromURL());
        if (acceptsHttpRedirect() && EidasSamlBinding.REDIRECT == EidasSamlBinding.fromName(httpServletRequest.getMethod())) {
            httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), EidasSamlBinding.REDIRECT.getName());
        } else {
            httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), EidasSamlBinding.POST.getName());
        }
        httpServletRequest.setAttribute(NodeParameterNames.EIDAS_SERVICE_URL.toString(),
                             encodeURL(serviceUrl, httpServletResponse)); // // Correct URl redirect cookie implementation
        httpServletRequest.setAttribute(EidasParameterKeys.SAML_REQUEST.toString(), samlRequestTokenSaml);
        httpServletRequest.setAttribute(NodeParameterNames.RELAY_STATE.toString(), relayState);
        // Redirecting where it should be
        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(NodeViewNames.EIDAS_CONNECTOR_COLLEAGUE_REQUEST_REDIRECT.toString());

        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_CONNECTOR_REQUEST);
        SessionHolder.clear();

        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private ILightRequest getiLightRequest(HttpServletRequest httpServletRequest,
    		final Collection<AttributeDefinition<?>> registry) throws ServletException, IOException {
        final String tokenBase64 = httpServletRequest.getParameter(EidasParameterKeys.TOKEN.toString());

        final SpecificConnectorCommunicationServiceImpl springManagedSpecificConnectorCommunicationService = (SpecificConnectorCommunicationServiceImpl) getApplicationContext()
                .getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString());

        try {
            return springManagedSpecificConnectorCommunicationService.getAndRemoveRequest(tokenBase64,registry);
        } catch (SpecificCommunicationException e) {
            throw new ServletException(e);
        }
    }

    private String validateRelayState(ILightRequest request) {
        String relayState = request.getRelayState();
        if (StringUtils.isNotEmpty(relayState)) { // RelayState's HTTP Parameter is optional!
            NormalParameterValidator.paramName(NodeParameterNames.RELAY_STATE.toString())
                    .paramValue(relayState)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_RELAY_STATE)
                    .validate();
        }
        return relayState;
    }

}
