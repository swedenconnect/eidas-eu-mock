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
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.FlowIdCache;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.xml.opensaml.SAMLEngineUtils;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.servlet.binding.ProxyLightRequestViewMapping;
import eu.eidas.node.service.servlet.view.NodeSpecificViewNames;
import eu.eidas.node.service.validation.ProxyParameterValidator;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static eu.eidas.node.BeanProvider.getBean;

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ColleagueRequestServlet extends AbstractNodeServlet {

    private static final Logger LOG = LoggerFactory.getLogger(ColleagueRequestServlet.class.getName());

    private FlowIdCache getFlowIdCache() {
        String beanName = ProxyBeanNames.EIDAS_PROXYSERVICE_FLOWID_CACHE.toString();
        FlowIdCache flowIdCache = getBean(FlowIdCache.class, beanName);
        return flowIdCache;
    }

    private ServiceControllerService getServiceControllerService() {
        // Obtaining the assertion consumer url from SPRING context
        ServiceControllerService controllerService = getBean(ServiceControllerService.class,
                ProxyBeanNames.EIDAS_SERVICE_CONTROLLER.toString());
        return controllerService;
    }

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
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.INVALID_PROTOCOL_BINDING.errorCode()),
                    EidasErrors.get(EidasErrorKey.INVALID_PROTOCOL_BINDING.errorMessage()),
                    "redirect binding is not allowed");
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        // Prevent cookies from being accessed through client-side script WITHOUT renew of session.
        setHTTPOnlyHeaderToSession(false, httpServletRequest, httpServletResponse);
        SessionHolder.setId(httpServletRequest.getSession());
        httpServletRequest.getSession().setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_REQUEST);

        // Obtains the parameters from httpRequest
        WebRequest webRequest = new IncomingRequest(httpServletRequest);

        // Validating the only HTTP parameter: SAMLRequest.
        String samlRequest = webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_REQUEST);
        ProxyParameterValidator.paramName(EidasParameterKeys.SAML_REQUEST)
                .paramValue(samlRequest)
                .eidasError(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML)
                .validate();

        // Storing the Remote Address and Host for auditing proposes.
        String remoteIpAddress = webRequest.getRemoteIpAddress();

        // Validating the optional HTTP Parameter relayState.
        String relayState = webRequest.getEncodedLastParameterValue(EidasParameterKeys.RELAY_STATE.toString());
        LOG.debug("Saving ProxyService relay state. " + relayState);

        //TODO: remove requestCorrelationMap parameter from processAuthenticationRequest
        Cache<String, StoredAuthenticationRequest> requestCorrelationMap = getServiceControllerService().getProxyServiceRequestCorrelationCache();
        // Obtaining the authData
        IAuthenticationRequest iAuthenticationRequest = getServiceControllerService().getProxyService()
                .processAuthenticationRequest(webRequest, relayState, requestCorrelationMap, remoteIpAddress);
        if (StringUtils.isNotBlank(relayState)) { // RelayState's HTTP Parameter is optional!
            ProxyParameterValidator.paramName(EidasParameterKeys.RELAY_STATE)
                    .paramValue(relayState)
                    .eidasError(EidasErrorKey.SPROVIDER_SELECTOR_INVALID_RELAY_STATE)
                    .validate();
        }

        String redirectUrl = iAuthenticationRequest.getAssertionConsumerServiceURL();
        LOG.debug("RedirectUrl: " + redirectUrl);
        LOG.debug("sessionId is on cookies () or fromURL ", httpServletRequest.isRequestedSessionIdFromCookie(),
                httpServletRequest.isRequestedSessionIdFromURL());

        httpServletRequest.setAttribute(EidasParameterKeys.SP_ID.toString(), iAuthenticationRequest.getProviderName());

        LightRequest lightRequest = buildLightRequest(httpServletRequest, httpServletResponse, iAuthenticationRequest);

        updateRequestCorrelationCache(iAuthenticationRequest, lightRequest.getId(), webRequest.getRemoteIpAddress());

        String iAuthenticationRequestId = iAuthenticationRequest.getId();
        final String flowId = getFlowIdCache().get(iAuthenticationRequestId);
        if (StringUtils.isNotEmpty(flowId)) {
            String lightRequestId = lightRequest.getId();
            getFlowIdCache().put(lightRequestId, flowId);
        }

        final String tokenBase64 = putRequestInCommunicationCache(lightRequest);

        setTokenRedirectAttributes(httpServletRequest, tokenBase64);

        String dispatchURL = NodeSpecificViewNames.REDIRECT_SPECIFIC_PROXYSERVICE_REQUEST.toString();
        forwardRequest(dispatchURL, httpServletRequest, httpServletResponse);

    }

    private void updateRequestCorrelationCache(IAuthenticationRequest authData, String newRequestId, String remoteIpAddress) {

        StoredAuthenticationRequest updatedStoredRequest =
                new StoredAuthenticationRequest.Builder()
                        .remoteIpAddress(remoteIpAddress)
                        .request(authData)
                        .build();

        //put into the Map the new Id generated with the previous authRequest
        Cache<String, StoredAuthenticationRequest> requestCorrelationMap = getServiceControllerService().getProxyServiceRequestCorrelationCache();
        requestCorrelationMap.put(newRequestId, updatedStoredRequest);
    }

    private String putRequestInCommunicationCache(LightRequest lightRequest) {
        String beanName = ProxyBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString();
        final SpecificCommunicationService specificProxyserviceCommunicationService = getBean(
                SpecificCommunicationService.class,
                beanName);

        BinaryLightToken binaryLightToken = null;
        try {
            binaryLightToken = specificProxyserviceCommunicationService.putRequest(lightRequest);
        } catch (SpecificCommunicationException e) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()), e);
        }

        return BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
    }

    private void setTokenRedirectAttributes(HttpServletRequest httpServletRequest, String tokenBase64) {
        httpServletRequest.setAttribute(ProxyLightRequestViewMapping.LIGHT_TOKEN, tokenBase64);
        httpServletRequest.setAttribute(ProxyLightRequestViewMapping.BINDING, httpServletRequest.getMethod());

        httpServletRequest.setAttribute(ProxyLightRequestViewMapping.REDIRECT_URL, getRedirectUrl());
    }

    private String getRedirectUrl() {
        String beanName = ProxyBeanNames.SPECIFIC_PROXYSERVICE_DEPLOYED_JAR.toString();
        final boolean isSpecificProxyServiceJar = getBean(Boolean.class, beanName);
        if (isSpecificProxyServiceJar) {
            return NodeSpecificViewNames.MONOLITH_SPECIFIC_PROXYSERVICE_REQUEST.toString();
        } else {
            return PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER.toString());
        }
    }

    private LightRequest buildLightRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, IAuthenticationRequest authenticationRequest) {
        try {
            // Prevent cookies from being accessed through client-side script.
            setHTTPOnlyHeaderToSession(false, httpServletRequest, httpServletResponse);

            LightRequest.Builder builder = LightRequest.builder(authenticationRequest);
            builder.id(SAMLEngineUtils.generateNCName());
            builder.spCountryCode(authenticationRequest.getServiceProviderCountryCode());

            return builder.build();
        } catch (AbstractEIDASException e) {
            LOG.info("BUSINESS EXCEPTION : " + e, e);
            throw e;
        } catch (IllegalArgumentException e) {
            throw new ProxyServiceError(
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                    EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()),
                    "Illegal argument while constructing Light Request", e);
        }
    }
}
