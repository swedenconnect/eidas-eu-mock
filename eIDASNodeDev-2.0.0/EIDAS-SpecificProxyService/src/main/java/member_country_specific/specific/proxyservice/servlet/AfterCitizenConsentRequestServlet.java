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

import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightRequest;
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
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Map;

/**
 *
 * Servlet to process the user consent for the request. Based on that consent creates the specific request and sents it to the IdP.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns={"/AfterCitizenConsentRequest"},
        name="AfterCitizenConsentRequestServlet",
        displayName = "AfterCitizenConsentRequestServlet",
        description = "Member State's After Citizen Consent Request Attributes Servlet")
public final class AfterCitizenConsentRequestServlet extends AbstractSpecificProxyServiceServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = -4777824962588311613L;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AfterCitizenConsentRequestServlet.class.getName());

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

        final String destination;
        final String isCanceled = httpServletRequest.getParameter(SpecificProxyServiceParameterNames.CANCEL.toString());
        if (StringUtils.isNotEmpty(isCanceled)) {
            final ILightResponse iLightResponse = prepareILightResponseFailure(httpServletRequest);
            try {
                TokenRedirectHelper.setTokenRedirectAttributes(httpServletRequest, iLightResponse, getApplicationContext(), getSpecificProxyService().getSpecificProxyserviceResponseUrl());
            } catch (SpecificCommunicationException e) {
                getLogger().error("Error setting a binary light token");
                throw new ServletException(e);
            }
            destination = SpecificProxyServiceViewNames.TOKEN_REDIRECT.toString();
        } else {
            final String specificRequest = prepareSpecificRequest(httpServletRequest);
            httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.SMSSP_TOKEN.toString(), specificRequest);
            destination = SpecificProxyServiceViewNames.IDP_REDIRECT.toString();
        }

        //maintain the same binding of the initial request
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
        final RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher(destination);
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private ILightResponse prepareILightResponseFailure(@Nonnull final HttpServletRequest httpServletRequest) throws ServletException {
        final ILightRequest originalIlightRequest = getIlightRequest(httpServletRequest);
        return LightResponseHelper.createILightResponseFailure(originalIlightRequest.getId(),
                EIDASStatusCode.RESPONDER_URI, EIDASSubStatusCode.REQUEST_DENIED_URI, "user refused consent at request phase");
    }

    private String prepareSpecificRequest(@Nonnull final HttpServletRequest httpServletRequest) throws ServletException, IOException {
        final ILightRequest originalIlightRequest = getIlightRequest(httpServletRequest);

        final String specificRequest;
        final ILightRequest consentedIlightRequest;
        if (getSpecificProxyService().isAskConsentRequest()){
            final CitizenConsent citizenConsent = processCitizenConsent(httpServletRequest, originalIlightRequest);
            consentedIlightRequest = createConsentedIlightRequest(originalIlightRequest, citizenConsent);
            specificRequest = createSpecificRequest(originalIlightRequest, consentedIlightRequest);
        } else {
            specificRequest = createSpecificRequest(originalIlightRequest, originalIlightRequest);
        }

        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.IDP_URL.toString(), getSpecificProxyService().getIdpUrl());

        return specificRequest;
    }

    private ILightRequest getIlightRequest(@Nonnull HttpServletRequest httpServletRequest) throws ServletException {
        final ILightRequest originalIlightRequest;
        try {
            originalIlightRequest = getSpecificProxyService().getIlightRequest(httpServletRequest);
        } catch (SpecificCommunicationException e) {
            getLogger().error("Error in decoding the Binary Light Token");
            throw new ServletException("Error in decoding the Binary Light Token", e);
        }
        return originalIlightRequest;
    }

    private String createSpecificRequest(ILightRequest originalIlightRequest, ILightRequest consentedIlightRequest) throws ServletException {
        final String specificRequest;
        try {
            specificRequest = getSpecificProxyService().translateNodeRequest(originalIlightRequest, consentedIlightRequest);
        } catch (JAXBException e) {
            getLogger().error("Error converting the specific protocol instance to Json");
            throw new ServletException(e);
        }
        return specificRequest;
    }

    public CitizenConsent processCitizenConsent(HttpServletRequest httpServletRequest, ILightRequest iLightRequest) {
        WebRequest webRequest = new IncomingRequest(httpServletRequest);
        ImmutableAttributeMap consentedAttributes = iLightRequest.getRequestedAttributes();
        // construct citizen consent from the request
       return getCitizenConsent(webRequest, consentedAttributes);

    }

    private ILightRequest createConsentedIlightRequest(ILightRequest iLightRequest, CitizenConsent consent) {
        final ImmutableAttributeMap immutableAttributeMap = filterConsentedAttributes(consent, iLightRequest.getRequestedAttributes());
        LightRequest.Builder builder = LightRequest.builder()
                .requestedAttributes(immutableAttributeMap)
                .providerName(iLightRequest.getProviderName())
                .nameIdFormat(iLightRequest.getNameIdFormat())
                .relayState(iLightRequest.getRelayState())
                .levelOfAssurance(iLightRequest.getLevelOfAssurance())
                .citizenCountryCode(iLightRequest.getCitizenCountryCode())
                .id(iLightRequest.getId())
                .issuer(iLightRequest.getIssuer())
                .spType(iLightRequest.getSpType());

        return builder.build();
    }

    @Nonnull
    public ImmutableAttributeMap filterConsentedAttributes(@Nonnull CitizenConsent citizenConsent,
                                                           @Nonnull ImmutableAttributeMap attributes) {

        ImmutableAttributeMap.Builder consentedAttributes = ImmutableAttributeMap.builder();
        boolean modified = false;

        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attributes.getAttributeMap()
                .entrySet()) {

            AttributeDefinition<?> definition = entry.getKey();
            ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();

            String name = definition.getNameUri().toASCIIString();
            if (definition.isRequired() || citizenConsent.getOptionalList().contains(name)) {
                consentedAttributes.put(definition, (ImmutableSet) values);
            } else {
                LOG.trace("Removing " + name);
                modified = true;
            }
        }

        if (modified) {
            return consentedAttributes.build();
        } else {
            // no modification:
            return attributes;
        }
    }

    @Nonnull
    public CitizenConsent getCitizenConsent(@Nonnull WebRequest webRequest, @Nonnull ImmutableAttributeMap attributes) {
        CitizenConsent consent = new CitizenConsent();
        LOG.debug("[getCitizenConsent] Constructing consent...");
        for (final AttributeDefinition definition : attributes.getDefinitions()) {
            String name = definition.getNameUri().toASCIIString();
            LOG.debug("[getCitizenConsent] checking " + name);
            if (webRequest.getEncodedLastParameterValue(name) != null) {
                if (definition.isRequired()) {
                    LOG.trace("[getCitizenConsent] adding " + name + " to mandatory attributes");
                    consent.setMandatoryAttribute(name);
                } else {
                    LOG.trace("[getCitizenConsent] adding " + name + " to optional attributes");
                    consent.setOptionalAttribute(name);
                }
            }
        }
        return consent;
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
