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

package member_country_specific.specific.proxyservice.servlet;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import member_country_specific.specific.proxyservice.SpecificProxyServiceBeanNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceParameterNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceViewNames;
import member_country_specific.specific.proxyservice.communication.SpecificProxyService;
import member_country_specific.specific.proxyservice.utils.LightResponseHelper;
import member_country_specific.specific.proxyservice.utils.TokenRedirectHelper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet to process the user consent for the response. Based on that consent creates the node response and sents it to the node proxy-service.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns={"/AfterCitizenConsentResponse"},
        name="AfterCitizenConsentResponseServlet",
        displayName = "AfterCitizenConsentResponseServlet",
        description = "Member State's After Citizen Consent Response Servlet")
public final class AfterCitizenConsentResponseServlet extends AbstractSpecificProxyServiceServlet {

    private static final long serialVersionUID = 4263032231488021275L;

    private static final Logger LOG = LoggerFactory.getLogger(AfterCitizenConsentResponseServlet.class.getName());

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

        final ILightResponse iLightResponse;
        final String isCanceled = httpServletRequest.getParameter(SpecificProxyServiceParameterNames.CANCEL.toString());
        if (StringUtils.isNotEmpty(isCanceled)) {
            iLightResponse = prepareILightResponseFailure(httpServletRequest);
        } else {
            iLightResponse = prepareILightResponse(httpServletRequest);
        }

        try {
            TokenRedirectHelper.setTokenRedirectAttributes(httpServletRequest, iLightResponse, getApplicationContext(), getSpecificProxyService().getSpecificProxyserviceResponseUrl());
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error setting a binary light token");
            throw new ServletException(e);
        }

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(SpecificProxyServiceViewNames.TOKEN_REDIRECT.toString());
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private ILightResponse prepareILightResponseFailure(@Nonnull final HttpServletRequest httpServletRequest) throws ServletException {
        final ILightResponse iLightResponse = prepareILightResponse(httpServletRequest);
        return LightResponseHelper.createILightResponseFailure(iLightResponse.getInResponseToId(),
                EIDASStatusCode.RESPONDER_URI, EIDASSubStatusCode.REQUEST_DENIED_URI, "user refused consent at response phase");
    }

    private ILightResponse prepareILightResponse(@Nonnull HttpServletRequest httpServletRequest) throws ServletException {
        final ILightResponse iLightResponse;
        try {
            iLightResponse = getSpecificProxyService().getIlightResponse(httpServletRequest);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error in decoding the Binary Light Token");
            throw new ServletException("Error in decoding the Binary Light Token", e);
        }

        return iLightResponse;
    }

    public SpecificProxyService getSpecificProxyService() {
        return  (SpecificProxyService) getApplicationContext()
                .getBean(SpecificProxyServiceBeanNames.SPECIFIC_PROXY_SERVICE.toString());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
