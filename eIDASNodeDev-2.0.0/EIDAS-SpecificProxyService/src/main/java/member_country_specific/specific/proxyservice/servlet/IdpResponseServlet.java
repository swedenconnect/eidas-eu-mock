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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.SimpleProtocol.utils.ContextClassTranslator;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import member_country_specific.specific.proxyservice.SpecificProxyServiceBeanNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceParameterNames;
import member_country_specific.specific.proxyservice.SpecificProxyServiceViewNames;
import member_country_specific.specific.proxyservice.communication.SpecificProxyService;
import member_country_specific.specific.proxyservice.utils.TokenRedirectHelper;
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
 * Receives/processes the servlet request containing the MS specific response coming from the IDP,
 * transforms it into an {@link ILightResponse}, and sends it to the receiver at the eIDAS Node.
 *
 * If a user consent is needed forwards to a consent page instead.
 *
 * @since 2.0
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
@WebServlet(urlPatterns={"/IdpResponse"},
        name="IdpResponseServlet",
        displayName = "IdpResponseServlet",
        description = "Specific IdP Response Servlet")
public final class IdpResponseServlet extends AbstractSpecificProxyServiceServlet {

    private static final long serialVersionUID = 3809925836547349408L;

    private static final Logger LOG = LoggerFactory.getLogger(IdpResponseServlet.class.getName());

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

        final ILightResponse lightResponse = prepareNodeResponse(httpServletRequest);

        final String destination;
        final boolean showConsent = isShowConsent(lightResponse);
        if (showConsent) {
            setConsentViewAttributes(httpServletRequest, lightResponse);
            destination = SpecificProxyServiceViewNames.CITIZEN_CONSENT_RESPONSE.toString();
        } else {
            try {
                TokenRedirectHelper.setTokenRedirectAttributes(httpServletRequest, lightResponse, getApplicationContext(), getSpecificProxyService().getSpecificProxyserviceResponseUrl());
            } catch (SpecificCommunicationException e) {
                getLogger().error("Error setting a binary light token");
                throw new ServletException(e);
            }

            destination = SpecificProxyServiceViewNames.TOKEN_REDIRECT.toString();
        }

        final RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(destination);
        dispatcher.forward(httpServletRequest, httpServletResponse);
    }

    private void setConsentViewAttributes(@Nonnull HttpServletRequest httpServletRequest, @Nonnull ILightResponse lightResponse) throws ServletException {
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attributes = prepareAttributesToAskConsent(lightResponse);
        if (attributes.size() == 0)
            throw new ServletException("Message with no Attributes list");
        httpServletRequest.setAttribute(EidasParameterKeys.ATTRIBUTE_LIST.toString(), attributes);

        String levelOfAssurance = lightResponse.getLevelOfAssurance();
        final String levelOfAssuranceAlias = ContextClassTranslator.getLevelOfAssuranceAlias(levelOfAssurance);
        if (levelOfAssuranceAlias != null)
            levelOfAssurance = levelOfAssuranceAlias;

        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.LOA_VALUE.toString(), levelOfAssurance);

        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.REDIRECT_URL.toString(), SpecificProxyServiceViewNames.AFTER_CITIZEN_CONSENT_RESPONSE_ATTRIBUTE.toString());
        httpServletRequest.setAttribute(EidasParameterKeys.BINDING.toString(), httpServletRequest.getMethod());
        setBinaryLightTokenHttpRequestAttribute(httpServletRequest, lightResponse);
    }

    private boolean isShowConsent(ILightResponse lightResponse) {
        return getSpecificProxyService().isAskConsentResponse() && !lightResponse.getStatus().isFailure();
    }

    private void setBinaryLightTokenHttpRequestAttribute(@Nonnull HttpServletRequest httpServletRequest, ILightResponse lightResponse) throws ServletException{
        final String binaryTokenResponseBase64;
        try {
            binaryTokenResponseBase64 = getSpecificProxyService().createStoreBinaryLightTokenResponseBase64(lightResponse);
        } catch (SpecificCommunicationException  e) {
            getLogger().error("Error encoding light token into a binary light token");
            throw new ServletException("Error encoding light token into a binary light token", e);
        }
        httpServletRequest.setAttribute(SpecificProxyServiceParameterNames.BINARY_LIGHT_TOKEN.toString(), binaryTokenResponseBase64);
    }

    private ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> prepareAttributesToAskConsent(ILightResponse lightResponse) {
        ImmutableAttributeMap responseImmutableAttributeMap = lightResponse.getAttributes();
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> responseImmutableMap = responseImmutableAttributeMap.getAttributeMap();

        ImmutableAttributeMap.Builder filteredAttrMapBuilder = ImmutableAttributeMap.builder();
        if (getSpecificProxyService().isAskConsentResponseShowOnlyEidasAttributes()) {
            for (AttributeDefinition attrDef : responseImmutableMap.keySet()) {
                final boolean isEidasCoreAttribute = getSpecificProxyService().getEidasAttributeRegistry().contains(attrDef);
                if (isEidasCoreAttribute) {
                    addAttributeDefinitionAndOrValue(responseImmutableAttributeMap, filteredAttrMapBuilder, attrDef);
                }
            }
        } else {
            for (AttributeDefinition attrDef : responseImmutableMap.keySet()) {
                addAttributeDefinitionAndOrValue(responseImmutableAttributeMap, filteredAttrMapBuilder, attrDef);
            }
        }
        return filteredAttrMapBuilder.build().getAttributeMap();
    }

    private void addAttributeDefinitionAndOrValue(@Nonnull ImmutableAttributeMap responseImmutableAttributeMap, ImmutableAttributeMap.Builder filteredAttrMapBuilder, AttributeDefinition attrDef) {
        if (getSpecificProxyService().isAskConsentResponseShowAttributeValues()) {
            filteredAttrMapBuilder.put(attrDef, responseImmutableAttributeMap.getAttributeValuesByNameUri(attrDef.getNameUri()));
        } else {
            filteredAttrMapBuilder.put(attrDef);
        }
    }

    private ILightResponse prepareNodeResponse(@Nonnull final HttpServletRequest httpServletRequest) throws ServletException {
        final String specificResponse = httpServletRequest.getParameter(EidasParameterKeys.SMSSP_RESPONSE.toString());

        final ILightResponse lightResponse;
        try {
            lightResponse = getSpecificProxyService().translateSpecificResponse(specificResponse);
        } catch (JAXBException e) {
            getLogger().error("Error unmarshalling MS Specific Request");
            throw new ServletException(e);
        }

        return lightResponse;
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
