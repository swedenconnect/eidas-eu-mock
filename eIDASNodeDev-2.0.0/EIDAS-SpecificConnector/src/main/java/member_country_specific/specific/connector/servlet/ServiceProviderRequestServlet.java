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

package member_country_specific.specific.connector.servlet;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceImpl;
import member_country_specific.specific.connector.SpecificConnectorBeanNames;
import member_country_specific.specific.connector.SpecificConnectorParameterNames;
import member_country_specific.specific.connector.SpecificConnectorViewNames;
import member_country_specific.specific.connector.communication.SpecificConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Receives/processes the servlet request containing the simple protocol request coming from the SP,
 * transforms it into an {@link ILightRequest}, adds it as an attribute to the servlet request
 * and forwards it to the receiver at the eIDAS node.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns = {"/ServiceProvider"},
        name = "ServiceProviderRequestServlet",
        displayName = "ServiceProviderRequestServlet",
        description = "Specific Service Provider Request Servlet")
public class ServiceProviderRequestServlet extends AbstractSpecificConnectorServlet {

    private static final long serialVersionUID = 2037358134080320372L;

    private static final Logger LOG = LoggerFactory.getLogger(ServiceProviderRequestServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        execute(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        execute(httpServletRequest, httpServletResponse);
    }

    private void execute(@Nonnull final HttpServletRequest httpServletRequest,
                         @Nonnull final HttpServletResponse httpServletResponse) throws ServletException, IOException {

        SpecificConnector specificConnector = (SpecificConnector) getApplicationContext()
                .getBean(SpecificConnectorBeanNames.SPECIFIC_CONNECTOR_SERVICE.toString());

        final ILightRequest iLightRequest = prepareNodeRequest(httpServletRequest, specificConnector);

        final BinaryLightToken binaryLightToken = putRequestInCommunicationCache(iLightRequest);
        setTokenRedirectAttributes(httpServletRequest, binaryLightToken, specificConnector.getSpecificConnectorRequestUrl());

        final String destination = SpecificConnectorViewNames.TOKEN_REDIRECT.toString();
        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(destination);
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private BinaryLightToken putRequestInCommunicationCache(ILightRequest iLightRequest) throws ServletException {
        final BinaryLightToken binaryLightToken;
        try {
            final SpecificConnectorCommunicationServiceImpl springManagedSpecificConnectorCommunicationService =
                    (SpecificConnectorCommunicationServiceImpl) getApplicationContext().getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString());

            binaryLightToken = springManagedSpecificConnectorCommunicationService.putRequest(iLightRequest);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Unable to process specific request");
            throw new ServletException(e);
        }
        return binaryLightToken;
    }

    private void setTokenRedirectAttributes(@Nonnull HttpServletRequest httpServletRequest, BinaryLightToken binaryLightToken, String redirectUrl) {
        final String binaryLightTokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        httpServletRequest.setAttribute(EidasParameterKeys.TOKEN.toString(), binaryLightTokenBase64);
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
        httpServletRequest.setAttribute(SpecificConnectorParameterNames.REDIRECT_URL.toString(), redirectUrl);
    }

    private ILightRequest prepareNodeRequest(@Nonnull final HttpServletRequest httpServletRequest, SpecificConnector specificConnector) throws ServletException {
               final String specificRequest = httpServletRequest.getParameter(EidasParameterKeys.SMSSP_REQUEST.toString());

        final ILightRequest lightRequest;
        try {
            lightRequest = specificConnector.translateSpecificRequest(specificRequest);
        } catch (JAXBException e) {
            getLogger().error("Unable to process specific request");
            throw new ServletException(e);
        }

        return lightRequest;
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
