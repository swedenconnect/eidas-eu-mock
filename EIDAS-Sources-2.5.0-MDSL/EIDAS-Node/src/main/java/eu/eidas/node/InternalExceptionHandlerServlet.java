/*
 * Copyright (c) 2019 by European Commission
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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.engine.exceptions.EIDASMetadataRuntimeException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;

/**
 * Handles the all the {@link Exception} and the
 * eu.eidas.node.exceptions.EidasInterceptorException.
 */
public final class InternalExceptionHandlerServlet extends AbstractNodeServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 7925862066060762369L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InternalExceptionHandlerServlet.class.getName());

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
     *
     * @param request
     * @param response
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response) {
      /**
       * Current exception.
       */
      Exception exception;

      //Default error page
      String retVal = NodeViewNames.INTERNAL_ERROR.toString();
        try {
            // Prevent cookies from being accessed through client-side script.
            setHTTPOnlyHeaderToSession(false, request, response);

            // Analyze the servlet exception
            exception = (Exception) request.getAttribute("javax.servlet.error.exception");

            //Websphere wraps another layer onto the thrown exception
            if(exception instanceof ServletException && exception.getCause() instanceof ServletException) {
                exception = (Exception) exception.getCause();
            }
            if (exception.getCause() instanceof AbstractEIDASException) {
                LOG.info("ERROR : An error occurred on Eidas Node (no. {}) - {}", ((AbstractEIDASException) exception.getCause()).getErrorCode(), ((AbstractEIDASException) exception.getCause()).getErrorMessage());
                LOG.debug("ERROR : Exception Stacktrace", exception);
            } else {
                LOG.info("ERROR : An error occurred on Eidas Node", exception.getMessage());
                LOG.debug("ERROR : An error occurred on Eidas Node", exception);
            }

            if (exception.getCause() instanceof EIDASServiceException) {
                LOG.trace("Exception is instanceOf EIDASServiceException");
                retVal = "/ServiceExceptionHandler";
                //Restore the exception with the original because filters
                //may have thrown it as ServletException
                exception = (Exception) exception.getCause();
                request.setAttribute("javax.servlet.error.exception", exception);
            }else if (exception.getCause() instanceof AbstractEIDASException) {
                LOG.trace("Exception is instanceOf AbstractEIDASException");
                retVal = "/EidasNodeExceptionHandler";
                Object phase=request.getSession().getAttribute(EidasParameterKeys.SAML_PHASE.toString());
                if(EIDASValues.EIDAS_SERVICE_REQUEST.equals(phase) || EIDASValues.EIDAS_SERVICE_RESPONSE.equals(phase)){
                    retVal="/ServiceExceptionHandler";
                }
                //Restore the exception with the original because filters
                //may have thrown it as ServletException
                exception = (Exception) exception.getCause();
                request.setAttribute("javax.servlet.error.exception", exception);
            } else if (exception.getCause() instanceof EIDASMetadataRuntimeException) {
                // Quick Hack for fixing EDINT-3249. A better fix would have implied a deep refactoring of
                // the Node Exception Handling.mechanism.
                EIDASMetadataRuntimeException metadataRuntimeException = (EIDASMetadataRuntimeException)exception.getCause();
                String message = metadataRuntimeException.getMessage();
                if (StringUtils.contains(message, "metadata not in whitelist"))  {
                    exception = new EidasNodeException(EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                            EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
                    retVal="/ServiceExceptionHandler";
                    request.setAttribute("javax.servlet.error.exception", exception);
                }
            } else {
                // Default General error
                LOG.info("ERROR : Exception occurs (NOT instanceOf InterceptorException {})", exception.getMessage());
                LOG.debug("ERROR : Exception occurs (NOT instanceOf InterceptorException {})", exception);
            }

            //Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code"); NEVER used
            request.setAttribute(NodeParameterNames.EXCEPTION.toString(), exception);

            //Forward to error page
            RequestDispatcher dispatcher = getServletDispatcher(retVal);
            dispatcher.forward(request, response);
        } catch (Exception e) {
            LOG.info("ERROR : Exception occurs {}", e.getMessage());
            LOG.debug("ERROR : Exception occurs {}", e);
        }
    }
}