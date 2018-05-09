/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.node.connector;

import java.io.IOException;
import java.security.InvalidParameterException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.AbstractNodeServlet;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeSpecificViewNames;
import eu.eidas.node.utils.PropertiesUtil;
import eu.eidas.node.utils.SessionHolder;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceImpl;

/**
 * Is invoked when ProxyService wants to pass control to the Connector.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public final class ColleagueResponseServlet extends AbstractNodeServlet {

    private static final long serialVersionUID = -2511363089207242981L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ColleagueResponseServlet.class.getName());

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private boolean validateParameterAndIsNormalSAMLResponse(String sAMLResponse) {
        // Validating the only HTTP parameter: sAMLResponse.

        LOG.trace("Validating Parameter SAMLResponse");

        if (!NormalParameterValidator.paramName(EidasParameterKeys.SAML_RESPONSE).paramValue(sAMLResponse).isValid()) {
            LOG.info("ERROR : SAMLResponse parameter is invalid or missing");
            throw new InvalidParameterException("SAMLResponse parameter is invalid or missing");
        }
        return true;
    }

    /**
     * This call is used for the moa/mocca get
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * Executes {@link eu.eidas.node.auth.connector.AUCONNECTOR#getAuthenticationResponse} and prepares the citizen to
     * be redirected back to the SP.
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @return
     * @throws Exception
     */
    @Override
    public void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        try {
            // Prevent cookies from being accessed through client-side script with renew of session.
            setHTTPOnlyHeaderToSession(false, httpServletRequest, httpServletResponse);
            SessionHolder.setId(httpServletRequest.getSession());
            httpServletRequest.getSession()
                    .setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_CONNECTOR_RESPONSE);

            // Obtaining the assertion consumer url from SPRING context
            ConnectorControllerService controllerService = (ConnectorControllerService) getApplicationContext().getBean(
                    NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
            LOG.trace("ConnectorControllerService {}", controllerService);
            LOG.debug("doPost to SP");
            // Obtains the parameters from httpRequest
            WebRequest webRequest = new IncomingRequest(httpServletRequest);

            String samlResponseFromProxyService =
                    webRequest.getEncodedLastParameterValue(EidasParameterKeys.SAML_RESPONSE);
            if (null == samlResponseFromProxyService) {
                samlResponseFromProxyService = StringUtils.EMPTY;
            }

            // Validating the only HTTP parameter: SAMLResponse or samlArtifact.
            if (!validateParameterAndIsNormalSAMLResponse(samlResponseFromProxyService)) {
                LOG.info("ERROR : Cannot validate parameter or abnormal SAML response");
            }
            LOG.trace("Normal SAML response decoding");
            AuthenticationExchange
                    authenticationExchange = controllerService.getConnectorService().getAuthenticationResponse(webRequest);

			ImmutableAttributeMap respAttributes = authenticationExchange.getConnectorResponse().getAttributes();

			// Build the LightResponse
            LightResponse lightResponse =
                    LightResponse.builder(authenticationExchange.getConnectorResponse()).attributes(respAttributes).build();

            // Call the specific module
            sendResponse(lightResponse, httpServletRequest, httpServletResponse);

        } catch (ServletException se) {
            LOG.info("BUSINESS EXCEPTION : ServletException", se.getMessage());
            LOG.debug("BUSINESS EXCEPTION : ServletException", se);
            throw se;
        }
    }

    private void sendResponse(ILightResponse lightResponse, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        try {
            final BinaryLightToken binaryLightToken = putResponseInCommunicationCache(lightResponse);
            setTokenRedirectAttributes(httpServletRequest, binaryLightToken);

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(NodeSpecificViewNames.TOKEN_REDIRECT_MS_CONNECTOR.toString());
            dispatcher.forward(httpServletRequest, httpServletResponse);
        } catch (ServletException | IOException e) {
            getLogger().error("SpecificException" + e, e);
            // Illegal state: exception received from the specific module
            throw new ServletException("Unable to send specific response: " + e, e);
        }
    }

    private BinaryLightToken putResponseInCommunicationCache(final ILightResponse lightResponse) throws ServletException {
        final SpecificConnectorCommunicationServiceImpl specificConnectorCommunicationService =
                (SpecificConnectorCommunicationServiceImpl) getApplicationContext().getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString());
        try {
            return specificConnectorCommunicationService.putResponse(lightResponse);
        } catch (SpecificCommunicationException e) {
            throw new ServletException(e);
        }
    }

    private void setTokenRedirectAttributes(HttpServletRequest httpServletRequest, BinaryLightToken binaryLightToken) {
        final String tokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        httpServletRequest.setAttribute(EidasParameterKeys.TOKEN.toString(), tokenBase64);
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
        httpServletRequest.setAttribute(NodeParameterNames.REDIRECT_URL.toString(), getRedirectUrl());
    }

    private String getRedirectUrl() {
        final boolean isSpecificConnectorJar = (Boolean) getApplicationContext().getBean(NodeBeanNames.SPECIFIC_CONNECTOR_DEPLOYED_JAR.toString());
        if (isSpecificConnectorJar) {
            return NodeSpecificViewNames.SPECIFIC_SP_RESPONSE.toString();
        } else {
            return PropertiesUtil.getProperty(EidasParameterKeys.SPECIFIC_CONNECTOR_RESPONSE_RECEIVER.toString());
        }
    }

}
