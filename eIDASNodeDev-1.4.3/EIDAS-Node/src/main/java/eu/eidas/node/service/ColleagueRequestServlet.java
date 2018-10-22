/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.service;

import com.google.common.collect.Sets;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.service.validation.NodeParameterValidator;
import eu.eidas.node.utils.EidasAttributesUtil;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ColleagueRequestServlet extends AbstractServiceServlet {

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
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PropertiesUtil.checkProxyServiceActive();
        // Obtaining the assertion consumer url from SPRING context
        ServiceControllerService controllerService = (ServiceControllerService) getApplicationContext().getBean(
                NodeBeanNames.EIDAS_SERVICE_CONTROLLER.toString());

        CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap = controllerService.getProxyServiceRequestCorrelationMap();

        // Prevent cookies from being accessed through client-side script WITHOUT renew of session.
        setHTTPOnlyHeaderToSession(false, request, response);
        SessionHolder.setId(request.getSession());
        request.getSession().setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_REQUEST);

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(request);

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
        // Validating the personal attribute list
       // IPersonalAttributeList persAttrList = PersonalAttributeList.copyOf(authData.getRequestedAttributes());
        //List<PersonalAttribute> attrList = new ArrayList<PersonalAttribute>();

        boolean hasEidasAttributes = !Sets.intersection(EidasSpec.REGISTRY.getAttributes(),
                                                        authData.getRequestedAttributes().getDefinitions()).isEmpty();
        //ImmutablePersonalAttributeSet
/*        for (PersonalAttribute pa : persAttrList) {
            attrList.add(pa);
        }*/
        String redirectUrl = authData.getAssertionConsumerServiceURL();
        LOG.debug("RedirectUrl: " + redirectUrl);
        // Validating the citizenConsentUrl
        NodeParameterValidator.paramName(EidasParameterKeys.EIDAS_SERVICE_REDIRECT_URL)
                .paramValue(controllerService.getCitizenConsentUrl())
                .eidasError(EidasErrorKey.COLLEAGUE_REQ_INVALID_DEST_URL)
                .validate();
        LOG.debug("sessionId is on cookies () or fromURL ", request.isRequestedSessionIdFromCookie(),
                  request.isRequestedSessionIdFromURL());

        request.setAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString(), controllerService.getProxyService()
                .generateSamlTokenFail(authData, EIDASStatusCode.REQUESTER_URI.toString(), EidasErrorKey.CITIZEN_RESPONSE_MANDATORY, remoteIpAddress));

        request.setAttribute(EidasParameterKeys.SP_ID.toString(), authData.getProviderName());
        if (authData instanceof IStorkAuthenticationRequest) {
            request.setAttribute(NodeParameterNames.QAA_LEVEL.toString(),
                                 ((IStorkAuthenticationRequest) authData).getQaa());
        }

        request.setAttribute(NodeParameterNames.LOA_VALUE.toString(),
                             EidasAttributesUtil.getUserFriendlyLoa(authData.getLevelOfAssurance()));
        request.setAttribute(NodeParameterNames.CITIZEN_CONSENT_URL.toString(),
                             encodeURL(controllerService.getCitizenConsentUrl(),
                                       response)); // Correct URl redirect cookie implementation

        request.setAttribute(NodeParameterNames.ATTR_LIST.toString(), authData.getRequestedAttributes().entrySet().asList());
        request.setAttribute(NodeParameterNames.REDIRECT_URL.toString(),
                             encodeURL(redirectUrl, response));// Correct URl redirect cookie implementation
        request.setAttribute(NodeParameterNames.EIDAS_ATTRIBUTES_PARAM.toString(), Boolean.valueOf(hasEidasAttributes));

        request.setAttribute(NodeParameterNames.REQUEST_ID.toString(), authData.getId());
        request.setAttribute(NodeParameterNames.COLLEAGUE_REQUEST.toString(), authData);

        NodeViewNames forwardUrl;
        if (controllerService.isAskConsentType()) {
            forwardUrl = NodeViewNames.EIDAS_SERVICE_PRESENT_CONSENT;
        } else {
            forwardUrl = NodeViewNames.EIDAS_SERVICE_NO_CONSENT;
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher(forwardUrl.toString());
        dispatcher.forward(request, response);
    }
}
