/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.node.service;

import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.*;
import eu.eidas.node.service.validation.NodeParameterValidator;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static eu.eidas.node.BeanProvider.*;

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ColleagueRequestServlet extends AbstractNodeServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ColleagueRequestServlet.class.getName());

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
            LOG.warn("BUSINESS EXCEPTION : redirect binding is not allowed");//TODO: send back an error?
        }
    }

    /**
     * Post method
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        PropertiesUtil.checkProxyServiceActive();
        // Obtaining the assertion consumer url from SPRING context
        ServiceControllerService controllerService = getBean(ServiceControllerService.class,
                NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString());

        CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap = controllerService.getProxyServiceRequestCorrelationMap();

        // Prevent cookies from being accessed through client-side script WITHOUT renew of session.
        setHTTPOnlyHeaderToSession(false, httpServletRequest, httpServletResponse);
        SessionHolder.setId(httpServletRequest.getSession());
        httpServletRequest.getSession().setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_REQUEST);

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(httpServletRequest);

        // Validating the only HTTP parameter: SAMLRequest.
        String samlRequest = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);
        NodeParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST)
                .paramValue(samlRequest)
                .eidasError(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML)
                .validate();

        // Storing the Remote Address and Host for auditing proposes.
        String remoteIpAddress = webRequest.getRemoteIpAddress();

        // Validating the optional HTTP Parameter relayState.
        String relayState = webRequest.getEncodedLastParameterValue(NodeParameterNames.RELAY_STATE.toString());
        LOG.debug("Saving ProxyService relay state. " + relayState);

        // Obtaining the authData
        IAuthenticationRequest authData = controllerService.getProxyService()
                .processAuthenticationRequest(webRequest, relayState, requestCorrelationMap, remoteIpAddress);
        if (StringUtils.isNotBlank(relayState)) { // RelayState's HTTP Parameter is optional!
            NodeParameterValidator.paramName(NodeParameterNames.RELAY_STATE)
                    .paramValue(relayState)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_RELAY_STATE)
                    .validate();
        }

        String redirectUrl = authData.getAssertionConsumerServiceURL();
        LOG.debug("RedirectUrl: " + redirectUrl);
        LOG.debug("sessionId is on cookies () or fromURL ", httpServletRequest.isRequestedSessionIdFromCookie(),
                  httpServletRequest.isRequestedSessionIdFromURL());

        httpServletRequest.setAttribute(EidasParameterKeys.SP_ID.toString(), authData.getProviderName());

        LightRequest lightRequest = buildLightRequest(httpServletRequest, httpServletResponse, authData);

        final String tokenBase64 = putRequestInCommunicationCache(lightRequest);

        setTokenRedirectAttributes(httpServletRequest, tokenBase64);

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(NodeSpecificViewNames.TOKEN_REDIRECT_MS_PROXY_SERVICE.toString());
        dispatcher.forward(httpServletRequest, httpServletResponse);

    }

    private String putRequestInCommunicationCache(LightRequest lightRequest) throws ServletException {
        String beanName = SpecificCommunicationDefinitionBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString();
        final SpecificProxyserviceCommunicationServiceImpl specificProxyserviceCommunicationService = getBean(
                                            SpecificProxyserviceCommunicationServiceImpl.class,
                                            beanName);

        final BinaryLightToken binaryLightToken;
        try {
            binaryLightToken = specificProxyserviceCommunicationService.putRequest(lightRequest);
        } catch (SpecificCommunicationException e) {
            throw new ServletException(e);
        }

        return BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
    }

    private void setTokenRedirectAttributes(HttpServletRequest httpServletRequest, String tokenBase64) {
        httpServletRequest.setAttribute(EidasParameterKeys.TOKEN.toString(), tokenBase64);
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());

        httpServletRequest.setAttribute(NodeParameterNames.REDIRECT_URL.toString(), getRedirectUrl());
    }

    private String getRedirectUrl() {
        String beanName = NodeBeanNames.SPECIFIC_PROXYSERVICE_DEPLOYED_JAR.toString();
        final boolean isSpecificProxyServiceJar = getBean(Boolean.class, beanName);
        if (isSpecificProxyServiceJar){
            return NodeSpecificViewNames.IDP_REQUEST.toString();
        } else {
            return  PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER.toString());
        }
    }

    protected LightRequest buildLightRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, IAuthenticationRequest authenticationRequest)
            throws ServletException, IOException {
        try {
            // Prevent cookies from being accessed through client-side script.
            setHTTPOnlyHeaderToSession(false, httpServletRequest, httpServletResponse);
            return LightRequest.builder(authenticationRequest).build();
        } catch (AbstractEIDASException e) {
            LOG.info("BUSINESS EXCEPTION : " + e, e);
            throw e;
        }
    }
}
