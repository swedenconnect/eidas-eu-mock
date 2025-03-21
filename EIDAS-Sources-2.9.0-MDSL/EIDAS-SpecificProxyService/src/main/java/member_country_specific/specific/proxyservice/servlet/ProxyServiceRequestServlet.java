/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.SimpleProtocol.utils.ContextClassTranslator;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import member_country_specific.specific.proxyservice.SpecificProxyServiceBeanNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceParameterNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceViewNames;
import member_country_specific.specific.proxyservice.communication.SpecificProxyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import javax.annotation.Nonnull;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Receives/processes the servlet request that contains the token
 * used to retrieve the {@link ILightRequest} coming from the eIDAS-Node,
 * transforms it into an MS specific request and sends it to the receiver at the IdP.
 *
 * If a user consent is needed forwards to a consent page instead.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns={"/ProxyServiceRequest"},
        name="ProxyServiceRequestServlet",
        displayName = "ProxyServiceRequestServlet",
        description = "Proxy Service Request Servlet")
public final class ProxyServiceRequestServlet extends AbstractSpecificProxyServiceServlet {

    private static final long serialVersionUID = 6077723139090497981L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ProxyServiceRequestServlet.class.getName());

    private Collection<AttributeDefinition<?>> REGISTRY; 

    @Override
    public void init() {
    	REGISTRY=retrieveAttributes();
    }

    private Collection<AttributeDefinition<?>> retrieveAttributes() {
        Set<AttributeDefinition<?>> registry = new HashSet<>();
        registry.addAll(getSpecificProxyService().getEidasAttributeRegistry().getAttributes());
        registry.addAll(getSpecificProxyService().getAdditionalAttributeRegistry().getAttributes());
        return Collections.unmodifiableSet(new TreeSet<>(registry));
    }

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

        final ILightRequest lightRequest = getIncomingiLightRequest(httpServletRequest, REGISTRY);

        setBinaryLightTokenToHttpRequest(httpServletRequest, lightRequest);

        final String destination;
        if (isShowConsent()) {
            httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
            setConsentAttributes(httpServletRequest, lightRequest);
            destination = SpecificProxyServiceViewNames.CITIZEN_CONSENT_REQUEST_ATTRIBUTES.toString();
        } else {
            destination = SpecificProxyServiceViewNames.AFTER_CITIZEN_CONSENT_REQUEST.toStringPrefixSlash();
        }

        final RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(destination);
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private boolean isShowConsent() {
        return getSpecificProxyService().isAskConsentRequest();
    }

    private void setConsentAttributes(@Nonnull HttpServletRequest httpServletRequest, @Nonnull ILightRequest lightRequest) {
        final List<String> levelsOfAssuranceAlias = getLevelsOfAssuranceAliases(lightRequest.getLevelsOfAssurance());
        if (!levelsOfAssuranceAlias.isEmpty()) {
            httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.LOA_VALUE.toString(), levelsOfAssuranceAlias);
        }
        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.ATTR_LIST.toString(), new ArrayList<>(lightRequest.getRequestedAttributes().entrySet()));
        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.CITIZEN_CONSENT_URL.toString(), SpecificProxyServiceViewNames.AFTER_CITIZEN_CONSENT_REQUEST.toString());
    }

    private List<String> getLevelsOfAssuranceAliases(List<ILevelOfAssurance> levelsOfAssurance) {
        List<String> levelsOfAssuranceAliases = new ArrayList<>();
        for (ILevelOfAssurance loa : levelsOfAssurance) {
            LevelOfAssuranceType loaType = LevelOfAssuranceType.fromString(loa.getType());
            if (LevelOfAssuranceType.NOTIFIED.equals(loaType)) {
                final String alias = ContextClassTranslator.getLevelOfAssuranceAlias(loa.getValue());
                levelsOfAssuranceAliases.add(alias);
            } else {
                levelsOfAssuranceAliases.add(loa.getValue());
            }
        }
        return levelsOfAssuranceAliases;
    }

    private ILightRequest getIncomingiLightRequest(@Nonnull HttpServletRequest httpServletRequest,final Collection<AttributeDefinition<?>> registry) throws ServletException {
        final SpecificCommunicationService specificProxyserviceCommunicationService
                = (SpecificCommunicationService) ContextLoader.getCurrentWebApplicationContext()
                .getBean(SpecificProxyServiceBeanNames.SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE.toString());

        final String tokenBase64 = httpServletRequest.getParameter(EidasParameterKeys.TOKEN.toString());
        try {
            return specificProxyserviceCommunicationService.getAndRemoveRequest(tokenBase64,registry);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error unmarshalling MS Specific Request"+e);
            throw new ServletException(e);
        }
    }

    private void setBinaryLightTokenToHttpRequest(@Nonnull HttpServletRequest httpServletRequest, ILightRequest lightRequest) throws ServletException {
        final String binaryLightTokenBase64 = getBinaryLightToken(lightRequest);
        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.BINARY_LIGHT_TOKEN.toString(), binaryLightTokenBase64);
    }

    private String getBinaryLightToken(ILightRequest lightRequest) throws ServletException {
        final String binaryLightTokenBase64;
        try {
            binaryLightTokenBase64 = getSpecificProxyService().createStoreBinaryLightTokenRequestBase64(lightRequest);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error encoding light token into a binary light token"+e);
            throw new ServletException("Error encoding light token into a binary light token", e);
        }
        return binaryLightTokenBase64;
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
