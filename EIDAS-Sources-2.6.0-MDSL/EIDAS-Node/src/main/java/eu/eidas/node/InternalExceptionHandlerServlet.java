/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.node;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.engine.exceptions.EIDASMetadataRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles the all the {@link Exception} and the
 * eu.eidas.node.exceptions.EidasInterceptorException.
 */
public final class InternalExceptionHandlerServlet extends AbstractNodeServlet {

    private static final long serialVersionUID = 7925862066060762369L;
    private static final Logger LOG = LoggerFactory.getLogger(InternalExceptionHandlerServlet.class.getName());

    final String SERVICE_EXCEPTION_HANDLER = "/ServiceExceptionHandler";
    final String CONNECTOR_EXCEPTION_HANDLER = "/EidasNodeExceptionHandler";

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleError(request, response);
    }

    /**
     * Prepares the exception to be displayed by the correct view.
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response) {
        setHTTPOnlyHeaderToSession(false, request, response);
        final Exception exception = (Exception) request.getAttribute("javax.servlet.error.exception");
        String redirectionErrorPage = NodeViewNames.INTERNAL_ERROR.toString();

        logException(exception);
        if (exception instanceof EIDASServiceException) {
            LOG.trace("Exception is instanceOf EIDASServiceException");
            redirectionErrorPage = SERVICE_EXCEPTION_HANDLER;
        } else if (exception instanceof AbstractEIDASException) {
            LOG.trace("Exception is instanceOf AbstractEIDASException");
            redirectionErrorPage = CONNECTOR_EXCEPTION_HANDLER;
            final Object samlPhase = request.getSession().getAttribute(EidasParameterKeys.SAML_PHASE.toString());
            if (EIDASValues.EIDAS_SERVICE_REQUEST.equals(samlPhase) || EIDASValues.EIDAS_SERVICE_RESPONSE.equals(samlPhase)) {
                redirectionErrorPage = SERVICE_EXCEPTION_HANDLER;
            }
        } else if (exception instanceof EIDASMetadataRuntimeException) {
            final String message = ((EIDASMetadataRuntimeException) exception).getMessage();
            if (StringUtils.contains(message, "metadata not in whitelist")) {
                final EidasNodeException incomingProxyServiceRequestInvalidSamlException = new EidasNodeException(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage())
                );
                redirectionErrorPage = SERVICE_EXCEPTION_HANDLER;
                request.setAttribute("javax.servlet.error.exception", incomingProxyServiceRequestInvalidSamlException);
            }
        } else {
            // finally: General error
            LOG.info("ERROR : Exception occurs (NOT instanceOf InterceptorException {})", exception.getMessage());
            LOG.debug("ERROR : Exception occurs (NOT instanceOf InterceptorException {})", exception);
        }

        forwardToExceptionHandler(request, response, redirectionErrorPage);
    }

    private void forwardToExceptionHandler(HttpServletRequest request, HttpServletResponse response, String redirectionErrorPage) {
        final RequestDispatcher dispatcher = getServletDispatcher(redirectionErrorPage);
        try {
            dispatcher.forward(request, response);
        } catch (IOException | ServletException e) {
            LOG.info("ERROR : Exception occurs {}", e.getMessage());
            LOG.debug("ERROR : Exception occurs {}", e);
        }
    }

    private void logException(Exception exception) {
        if (exception instanceof AbstractEIDASException) {
            LOG.info("ERROR : An error occurred on Eidas Node (no. {}) - {}",
                    ((AbstractEIDASException) exception).getErrorCode(),
                    ((AbstractEIDASException) exception).getErrorMessage());
            LOG.debug("ERROR : Exception Stacktrace", exception);
        } else {
            LOG.info("ERROR : An error occurred on Eidas Node", exception.getMessage());
            LOG.debug("ERROR : An error occurred on Eidas Node", exception);
        }
    }
}