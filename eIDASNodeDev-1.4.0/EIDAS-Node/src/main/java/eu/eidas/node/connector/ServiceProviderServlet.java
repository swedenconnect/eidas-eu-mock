package eu.eidas.node.connector;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.specificcommunication.ISpecificConnector;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ServiceProviderServlet extends AbstractConnectorServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProviderServlet.class.getName());

    private static final long serialVersionUID = 2037358134080320372L;

    @Override
    protected Logger getLogger() {
        return LOG;
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
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Prevent cookies from being accessed through client-side script WITH renew of session.
        setHTTPOnlyHeaderToSession(true, request, response);
        HttpSession session = request.getSession();
        SessionHolder.setId(session);
        // request received from SP
        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.SP_REQUEST);
        // http session created on eidas connector
        session.setAttribute(EidasParameterKeys.EIDAS_CONNECTOR_SESSION.toString(), Boolean.TRUE);

        // Obtaining the assertion consumer url from SPRING context
        ConnectorControllerService connectorController = (ConnectorControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
        LOG.trace(connectorController.toString());

        // Call the specific module
        ILightRequest lightRequest = processSpecificRequest(request, response, connectorController);

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(request);

        // Validating the optional HTTP Parameter relayState.
        String relayState = validateRelayState(webRequest);

        // Validating injected parameter.
        NormalParameterValidator.paramName(EidasErrorKey.CONNECTOR_REDIRECT_URL.toString())
                .paramValue(connectorController.getAssertionConsUrl())
                .validate();

        webRequest.getRequestState().setServiceUrl(encodeURL(connectorController.getAssertionConsUrl(), response));

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

        LOG.debug("sessionId is on cookies () or fromURL ", request.isRequestedSessionIdFromCookie(),
                  request.isRequestedSessionIdFromURL());
        if (acceptsHttpRedirect() && EidasSamlBinding.REDIRECT == EidasSamlBinding.fromName(request.getMethod())) {
            request.setAttribute(EidasParameterKeys.BINDING.toString(), EidasSamlBinding.REDIRECT.getName());
        } else {
            request.setAttribute(EidasParameterKeys.BINDING.toString(), EidasSamlBinding.POST.getName());
        }
        request.setAttribute(NodeParameterNames.EIDAS_SERVICE_URL.toString(),
                             encodeURL(serviceUrl, response)); // // Correct URl redirect cookie implementation
        request.setAttribute(EidasParameterKeys.SAML_REQUEST.toString(), samlRequestTokenSaml);
        request.setAttribute(NodeParameterNames.RELAY_STATE.toString(), relayState);
        // Redirecting where it should be
        RequestDispatcher dispatcher = request.getRequestDispatcher(NodeViewNames.EIDAS_CONNECTOR_COLLEAGUE_REQUEST_REDIRECT.toString());

        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_CONNECTOR_REQUEST);
        SessionHolder.clear();

        dispatcher.forward(request, response);
    }

    private String validateRelayState(WebRequest webRequest) {
        String relayState = webRequest.getEncodedLastParameterValue(NodeParameterNames.RELAY_STATE.toString());
        if (StringUtils.isNotEmpty(relayState)) { // RelayState's HTTP Parameter is optional!
            NormalParameterValidator.paramName(NodeParameterNames.RELAY_STATE.toString())
                    .paramValue(relayState)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_RELAY_STATE)
                    .validate();
        }
        return relayState;
    }

    private ILightRequest processSpecificRequest(HttpServletRequest request,
                                        HttpServletResponse response,
                                        ConnectorControllerService connectorController) throws ServletException {
        ILightRequest lightRequest;
        try {
            ISpecificConnector specificConnector = connectorController.getSpecificConnector();
            lightRequest = specificConnector.processRequest(request, response);
        } catch (SpecificException e) {
            getLogger().error("SpecificException" + e, e);
            // Illegal state: no request received from the specific
            throw new ServletException("Unable to process specific request: " + e, e);
        }
        if (lightRequest == null)  {
            getLogger().error("SpecificException: Missing specific request");
            // Illegal state: no exception and no request received from the specific
            throw new ServletException("Missing specific response: no error and no success");
        }
        return lightRequest;
    }

}
