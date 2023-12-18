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

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.security.ExtendedServletResponseWrapper;
import eu.eidas.node.security.SecurityResponseHeaderHelper;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.service.utils.ProxyServiceErrorUtil;
import eu.eidas.node.utils.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to handle {@link ProxyServiceError} when caught by web.xml
 */
public class ProxyServiceErrorServlet extends AbstractNodeServlet {

    private static final String JAVAX_SERVLET_ERROR_EXCEPTION = "javax.servlet.error.exception";

    private static final Logger LOG = LoggerFactory.getLogger(ProxyServiceErrorServlet.class.getName());

    private final SecurityResponseHeaderHelper securityResponseHeaderHelper = new SecurityResponseHeaderHelper();

    @Override
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        populateResponseHeader(httpServletRequest, httpServletResponse, securityResponseHeaderHelper);
        final AbstractEIDASException abstractEIDASException = (AbstractEIDASException) httpServletRequest.getAttribute(JAVAX_SERVLET_ERROR_EXCEPTION);

        final String errorMessage = ProxyServiceErrorUtil.getLocalizedErrorMessage(abstractEIDASException, httpServletRequest.getLocale());
        logExceptionAndErrorMessage(abstractEIDASException, errorMessage);

        final String contactSupportEmail = retrieveContactSupportEmail();
        final String errorId = abstractEIDASException.getErrorId();

        httpServletRequest.setAttribute(NodeParameterNames.ERROR_MESSAGE.toString(), errorMessage);
        httpServletRequest.setAttribute(NodeParameterNames.CONTACT_SUPPORT_EMAIL.toString(), contactSupportEmail);
        httpServletRequest.setAttribute(NodeParameterNames.ERROR_ID.toString(), errorId);

        httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        forwardRequest(NodeViewNames.PROXY_SERVICE_ERROR_PAGE.toString(), httpServletRequest, httpServletResponse);
    }

    private void logExceptionAndErrorMessage(AbstractEIDASException abstractEIDASException, String errorMessage) {
        final Logger logger = LoggerFactory.getLogger(abstractEIDASException.getStackTrace()[0].getClassName());
        final String additionalInformation = abstractEIDASException.getAdditionalInformation();
        final String additionalInformationMessage = additionalInformation != null ? ", Additional information: " + additionalInformation : "";
        final String errorId = abstractEIDASException.getErrorId();
        logger.error("Error ID:" + errorId + ", Error code and error message: " + errorMessage
                + additionalInformationMessage, abstractEIDASException);
    }

    private String retrieveContactSupportEmail() {
        return PropertiesUtil.getProperty(EidasParameterKeys.PROXYSERVICE_CONTACT_SUPPORT.toString());
    }

    /**
     * Detects if response is wrapped in ExtendedServletResponseWrapper from ContentSecurityPolicyFilter
     * If WLS rewrites the response object (as it does in 10.x), re-add CSP headers.
     */
    private void populateResponseHeader(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                        SecurityResponseHeaderHelper securityResponseHeaderHelper) {
        try {
            /*  Check if the servlet has removed CSP headers added by filter before */
            if (!(httpServletResponse instanceof ExtendedServletResponseWrapper)
                    || !((ExtendedServletResponseWrapper) httpServletResponse).hasCSPHeaders()) {
                /* if the response was rewritten or no CSP flags, place them back if needed */
               securityResponseHeaderHelper.populateResponseHeader(httpServletRequest, httpServletResponse);
            }
        } catch (ServletException exception) {
            httpServletRequest.setAttribute(JAVAX_SERVLET_ERROR_EXCEPTION, new ProxyServiceError(
                    EidasErrorKey.INVALID_HASH_ALGORITHM.errorCode(),
                    EidasErrorKey.INVALID_HASH_ALGORITHM.errorMessage()));
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
