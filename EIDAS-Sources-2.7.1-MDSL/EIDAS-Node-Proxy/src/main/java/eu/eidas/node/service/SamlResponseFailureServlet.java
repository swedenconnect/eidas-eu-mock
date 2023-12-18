/*
 * Copyright (c) 2022 by European Commission
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

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.exceptions.SamlFailureResponseException;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.utils.ProxyServiceErrorUtil;
import eu.eidas.node.service.utils.ProxyServiceSamlFailureUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

public class SamlResponseFailureServlet extends AbstractNodeServlet {

    private static final String JAVAX_SERVLET_ERROR_EXCEPTION = "javax.servlet.error.exception";

    private static final Logger LOG = LoggerFactory.getLogger(SamlResponseFailureServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        SamlFailureResponseException exception = (SamlFailureResponseException) httpServletRequest.getAttribute(JAVAX_SERVLET_ERROR_EXCEPTION);
        final StoredAuthenticationRequest storedRequest = exception.getStoredRequest();
        IAuthenticationRequest authenticationRequest = null;
        try {
            if (null == storedRequest) {
                authenticationRequest = ProxyServiceSamlFailureUtil.getAuthenticationRequestFromHttpServletRequest(httpServletRequest);
            } else if (null != storedRequest) {
                authenticationRequest = storedRequest.getRequest();
                httpServletRequest.setAttribute(NodeParameterNames.RELAY_STATE.toString(), exception.getRelayState());
            }

            final String errorRedirectUrl = authenticationRequest.getAssertionConsumerServiceURL();
            final String samlTokenFail = ProxyServiceSamlFailureUtil.generateSamlFailure(httpServletRequest, authenticationRequest,
                    exception);

            localizeErrorMessage(exception, httpServletRequest.getLocale());
            httpServletRequest.setAttribute(NodeParameterNames.SAML_TOKEN.toString(), samlTokenFail);
            httpServletRequest.setAttribute(NodeParameterNames.REDIRECT_URL.toString(), errorRedirectUrl);;
            httpServletRequest.setAttribute(NodeParameterNames.EXCEPTION.toString(), exception);
            httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), BindingMethod.POST.toString());

            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            forwardRequest(NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString(), httpServletRequest, httpServletResponse);

        } catch (EIDASSAMLEngineException e) {
            ProxyServiceError newException = new ProxyServiceError(e.getErrorCode(), e.getErrorMessage());
            httpServletRequest.setAttribute(JAVAX_SERVLET_ERROR_EXCEPTION, newException);
            forwardRequest("/ProxyServiceError",httpServletRequest,httpServletResponse);
        }
    }

    private void localizeErrorMessage(AbstractEIDASException abstractEIDASException, Locale locale) {
        if (abstractEIDASException.getMessage() == null) {
            LOG.info("BUSINESS EXCEPTION : An error occurred on EidasNode! Couldn't get Exception message.");
        } else {
            String errorMessage = ProxyServiceErrorUtil.getLocalizedErrorMessage(abstractEIDASException, locale);
            if (errorMessage != null) {
                abstractEIDASException.setErrorMessage(errorMessage);
            }
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
