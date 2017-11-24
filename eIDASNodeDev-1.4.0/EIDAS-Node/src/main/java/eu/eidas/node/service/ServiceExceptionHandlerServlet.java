/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.service;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.*;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.utils.EidasNodeErrorUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles the exceptions thrown by the ProxyService.
 *
 * @version $Revision: 1 $, $Date: 2014-10-21 $
 */

@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public final class ServiceExceptionHandlerServlet extends AbstractServiceServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = -8806380050113511720L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ServiceExceptionHandlerServlet.class.getName());

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleError(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleError(request, response);
    }

    /**
     * Prepares exception redirection, or if no information is available to redirect, prepares the exception to be
     * displayed. Also, clears the current session object, if not needed.
     *
     * @return {ERROR} if there is no URL to return to, {SUCCESS} otherwise.
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*
          Current exception.
         */
        AbstractEIDASException exception;
        /*
          URL to redirect the citizen to.
         */
        String errorRedirectUrl;

        String retVal = NodeViewNames.INTERNAL_ERROR.toString();
        try {
            // Prevent cookies from being accessed through client-side script.
            setHTTPOnlyHeaderToSession(false, request, response);

            //Set the Exception
            exception = (AbstractEIDASException) request.getAttribute("javax.servlet.error.exception");
            prepareErrorMessage(exception, request);
            errorRedirectUrl = prepareSession(exception, request);
            request.setAttribute(NodeParameterNames.EXCEPTION.toString(), exception);

            if (!StringUtils.isBlank(exception.getSamlTokenFail()) && null != errorRedirectUrl) {
                retVal = NodeViewNames.SUBMIT_ERROR.toString();
            } else if (exception instanceof EidasNodeException || exception instanceof EIDASServiceException) {
                retVal = NodeViewNames.PRESENT_ERROR.toString();
            } else if (exception instanceof InternalErrorEIDASException) {
                LOG.debug("BUSINESS EXCEPTION - null redirectUrl or SAML response");
                retVal = NodeViewNames.INTERNAL_ERROR.toString();
            } else if (exception instanceof SecurityEIDASException) {
                retVal = NodeViewNames.PRESENT_ERROR.toString();
            } else {
                LOG.warn("exception not recognized so internal error page shown", exception);
                retVal = NodeViewNames.INTERNAL_ERROR.toString();
            }

        } catch (Exception e) {
            LOG.info("BUSINESS EXCEPTION: in exception handler: " + e, e);
        }
        //Forward to error page
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(retVal);
        response.setStatus(HttpServletResponse.SC_OK);
        dispatcher.forward(request, response);
    }

    private void prepareErrorMessage(AbstractEIDASException exception, HttpServletRequest request) {
        if (exception.getMessage() == null) {
            LOG.info("BUSINESS EXCEPTION : An error occurred on EidasNode! Couldn't get Exception message.");
        } else {
            String errorMessage =
                    EidasNodeErrorUtil.resolveMessage(exception.getErrorMessage(), exception.getErrorCode(),
                                                      request.getLocale());
            if (errorMessage != null) {
                exception.setErrorMessage(errorMessage);
            }
            if (StringUtils.isBlank(exception.getSamlTokenFail())) {
                EidasNodeErrorUtil.prepareSamlResponseFail(request, exception,
                                                           EidasNodeErrorUtil.ErrorSource.PROXYSERVICE);
                LOG.info("BUSINESS EXCEPTION : ", errorMessage);
            } else {
                LOG.info("BUSINESS EXCEPTION : ", exception.getMessage());
            }
        }
    }

    private String prepareSession(AbstractEIDASException exception, HttpServletRequest request) {
        String errorRedirectUrl = null;
        if (exception instanceof ResponseCarryingServiceException) {

            ResponseCarryingServiceException responseCarryingServiceException =
                    (ResponseCarryingServiceException) exception;

            // Setting internal variables
            LOG.trace("Setting internal variables");

            errorRedirectUrl = responseCarryingServiceException.getErrorRedirectUrl();
            request.setAttribute(EidasParameterKeys.ERROR_REDIRECT_URL.toString(), errorRedirectUrl);

            String relayState = responseCarryingServiceException.getRelayState();

            // Validating the optional HTTP Parameter relayState.
            if (null != relayState) {
                request.setAttribute(NodeParameterNames.RELAY_STATE.toString(), relayState);
            }
        }
        return errorRedirectUrl;
    }
}
