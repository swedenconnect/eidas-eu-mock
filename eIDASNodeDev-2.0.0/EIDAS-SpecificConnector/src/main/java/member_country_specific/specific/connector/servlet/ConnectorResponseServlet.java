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

import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightResponse;
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
import java.util.Collection;

/**
 * Receives/processes the servlet request that contains the token
 * used to retrieve the {@link ILightResponse} coming from the eIDAS-Node,
 * transforms it into an MS specific response, adds it as an attribute to the servlet request
 * and forwards it to the receiver at the SP.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns={"/ConnectorResponse"},
        name="ConnectorResponseServlet",
        displayName = "ConnectorResponseServlet",
        description = "Connector Response Servlet")
public final class ConnectorResponseServlet extends AbstractSpecificConnectorServlet {

    private Collection<AttributeDefinition<?>> REGISTRY; 

    @Override
    public void init() throws ServletException {
    	REGISTRY=retrieveAttributes();
    }

	private Collection<AttributeDefinition<?>> retrieveAttributes() {
		SpecificConnector specificConnector = (SpecificConnector) getApplicationContext()
                .getBean(SpecificConnectorBeanNames.SPECIFIC_CONNECTOR_SERVICE.toString());
    	return ImmutableSortedSet.copyOf(specificConnector.getCoreAttributeRegistry().getAttributes());
	}
    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 4263032231488021275L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorResponseServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        execute(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        execute(httpServletRequest, httpServletResponse);
    }

    private void execute(@Nonnull final HttpServletRequest httpServletRequest,
                         @Nonnull final HttpServletResponse httpServletResponse) throws IOException, ServletException {

        final String specificResponse = prepareSpecificResponse(httpServletRequest);
        httpServletRequest.setAttribute(SpecificConnectorParameterNames.SMSSP_TOKEN.toString(), specificResponse);
        RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(SpecificConnectorViewNames.COLLEAGUE_RESPONSE_REDIRECT.toString());
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private String prepareSpecificResponse(@Nonnull final HttpServletRequest httpServletRequest) throws ServletException, IOException {
        SpecificConnector specificConnector = (SpecificConnector) getApplicationContext()
                .getBean(SpecificConnectorBeanNames.SPECIFIC_CONNECTOR_SERVICE.toString());

        final ILightResponse lightResponse = getResponseFromCommunicationCache(httpServletRequest,REGISTRY);

        final String specificResponse;
        try {
            specificResponse = specificConnector.translateNodeResponse(lightResponse, httpServletRequest);
        } catch (JAXBException e) {
            getLogger().error("Error converting the simple protocol response to Json.");
            throw new ServletException(e);
        }

        return specificResponse;
    }

    private ILightResponse getResponseFromCommunicationCache(@Nonnull HttpServletRequest httpServletRequest,final Collection<AttributeDefinition<?>> registry) throws IOException, ServletException {
        final String tokenBase64 = httpServletRequest.getParameter(EidasParameterKeys.TOKEN.toString());

        final SpecificConnectorCommunicationServiceImpl specificConnectorCommunicationService =
                (SpecificConnectorCommunicationServiceImpl) getApplicationContext().getBean(SpecificCommunicationDefinitionBeanNames.SPECIFIC_CONNECTOR_COMMUNICATION_SERVICE.toString());

        try {
            return specificConnectorCommunicationService.getAndRemoveResponse(tokenBase64,registry);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error converting the simple protocol response to Json.");
            throw new ServletException(e);
        }
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
