/*
 * Copyright (c) 2018 by European Commission
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
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.configuration.dom.SignatureKey;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.auth.LoggingUtil;
import eu.eidas.node.auth.service.AUSERVICE;
import eu.eidas.node.logging.ILogSamlCachingService;
import eu.eidas.node.logging.LogSamlHolder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet that logs the saml message and send the saml token to the destination.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class LogSamlServlet extends AbstractServiceServlet {

    private static final Logger LOG = LoggerFactory.getLogger(LogSamlServlet.class.getName());

    /**
     * Response logging.
     */
//    TODO quick fix check this
    private static final Logger LOGGER_COM_RESP = LoggerFactory.getLogger(EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "_" + AUSERVICE.class.getSimpleName());

    private static final int SIZE_FORTY_EIGHT = 48;

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (acceptsHttpRedirect()) {
            doPost(request, response);
        } else {
            LOG.info("DoGet called but redirect binding is not allowed");
        }
    }

    /**
     * Post method
     *
     * @param httpServletRequest the request
     * @param httpServletResponse the response
     * @throws ServletException if the request cannot be forwarded
     * @throws IOException if the request cannot be forwarded
     */
    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
            final String logSamlToken = httpServletRequest.getParameter(NodeParameterNames.LOG_SAML_TOKEN.toString());
            if (!StringUtils.isEmpty(logSamlToken)) {
                executeRetrieveDataFromCache(httpServletRequest, httpServletResponse, logSamlToken);
            } else {
                executeRetrieveDataFromHttpAttributes(httpServletRequest, httpServletResponse);
            }

            final String redirectJspUrl = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(redirectJspUrl);
            dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private void executeRetrieveDataFromHttpAttributes(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        final String redirectConnectorUrl = getAttributeValueFromHttpRequest(httpServletRequest, NodeParameterNames.REDIRECT_CONNECTOR_URL.toString());
        final String samlToken = getAttributeValueFromHttpRequest(httpServletRequest, NodeParameterNames.SAML_TOKEN.toString());
        final String issuer = getAttributeValueFromHttpRequest(httpServletRequest, SignatureKey.ISSUER.toString());

        if (StringUtils.isEmpty(redirectConnectorUrl) || StringUtils.isEmpty(samlToken) || StringUtils.isEmpty(issuer)) {
            getLogger().error("Attempt to log saml message without attribute values in http request");
            throw new ServletException("Attempt to log saml message without attribute values in http request");
        }

        httpServletRequest.setAttribute(NodeParameterNames.SAML_TOKEN.toString(), samlToken);
        httpServletRequest.setAttribute(NodeParameterNames.REDIRECT_URL.toString(), encodeURL(redirectConnectorUrl, httpServletResponse));
        logSamlTokenSends(samlToken, issuer);
    }

    private void executeRetrieveDataFromCache(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String logSamlToken) throws ServletException {
        IdPResponseBean controllerService = (IdPResponseBean) getApplicationContext().getBean(NodeBeanNames.IdP_RESPONSE.toString());
        ILogSamlCachingService iLogSamlCachingService = controllerService.getLogSamlCache();

        validateLogSamlToken(logSamlToken);

        final LogSamlHolder logSamlHolder = iLogSamlCachingService.getAndRemove(logSamlToken);

        if (null == logSamlHolder) {
            getLogger().error("Received logSamlToken not present in the cache");
            throw new ServletException("Received logSamlToken not present in the cache");
        }

        httpServletRequest.setAttribute(NodeParameterNames.REDIRECT_URL.toString(), encodeURL(logSamlHolder.getRedirectUrl(), httpServletResponse));
        httpServletRequest.setAttribute(NodeParameterNames.RELAY_STATE.toString(), logSamlHolder.getRelayState());

        final String samlToken;
        final String consentOkParameter = httpServletRequest.getParameter(NodeParameterNames.CONSENT_OK.toString());
        if (null != consentOkParameter) {
            samlToken = logSamlHolder.getSamlTokenSuccess();
        } else {
            final String correlationId = logSamlHolder.getSamlResponseOriginatingMsgId();
            samlToken = logSamlHolder.getSamlTokenFail();
            if (!StringUtils.isEmpty(correlationId)) {
                logSamlTokenGenerates(samlToken, correlationId);
            }
        }

        logSamlTokenSends(samlToken, logSamlHolder.getIssuer());
        httpServletRequest.setAttribute(NodeParameterNames.SAML_TOKEN.toString(), samlToken);
    }

    private void validateLogSamlToken(String logSamlToken) throws ServletException {
        if (StringUtils.isEmpty(logSamlToken) || SIZE_FORTY_EIGHT != logSamlToken.length()) {
            getLogger().error("Received logSamlToken does not have expected size");
            throw new ServletException("Received logSamlToken does not have expected size");
        }

        EidasStringUtil.decodeStringFromBase64(logSamlToken);//check if logSamlToken is Base64 encoded will throw DecoderException if could not perform Base64 decoding
    }

    /**
     * Gets value from the @param httpServletRequest from attribute or empty if not possible to retrieve a value.
     *
     * @param httpServletRequest the http servlet request
     * @param attributeKey the attribute's key
     * @return the value related to either attribute key or parameter key.
     */
    private String getAttributeValueFromHttpRequest(final HttpServletRequest httpServletRequest, final String attributeKey) {
        final Object attribute = httpServletRequest.getAttribute(attributeKey);
        if (attribute instanceof  String) {
            return (String) attribute;
        }

        return StringUtils.EMPTY;
    }

    private void logSamlTokenSends(String samlToken, String issuer) {
        final IdPResponseBean controllerService =
                (IdPResponseBean) getApplicationContext().getBean(NodeBeanNames.IdP_RESPONSE.toString());

        final String origin = "N/A";
        final String correlationId = "N/A";

        byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlToken);
        final LoggingUtil serviceLoggingUtil  = controllerService.getConnectorLoggingUtil();

        serviceLoggingUtil.prepareAndSaveResponseToLog(LogSamlServlet.LOGGER_COM_RESP, EIDASValues.EIDAS_SERVICE_CONNECTOR_RESPONSE.toString(),
                issuer, samlObj, correlationId, origin, null, LoggingUtil.OperationTypes.SENDS);

    }

    private void logSamlTokenGenerates(String samlToken, String correlationId) {
        final IdPResponseBean controllerService =
                (IdPResponseBean) getApplicationContext().getBean(NodeBeanNames.IdP_RESPONSE.toString());

        final String origin = "N/A";
        final String issuer = "N/A";

        byte[] samlObj = EidasStringUtil.decodeBytesFromBase64(samlToken);
        final LoggingUtil serviceLoggingUtil  = controllerService.getConnectorLoggingUtil();

        serviceLoggingUtil.prepareAndSaveResponseToLog(LogSamlServlet.LOGGER_COM_RESP, EIDASValues.EIDAS_SERVICE_GENERATES_RESPONSE.toString(),
                issuer, samlObj, correlationId, origin, null, LoggingUtil.OperationTypes.GENERATES);

    }
}
