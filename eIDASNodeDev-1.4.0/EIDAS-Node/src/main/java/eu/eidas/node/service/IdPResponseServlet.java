/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.service;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.stork.IStorkAuthenticationRequest;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.eidas.spec.EidasSpec;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.NodeParameterNames;
import eu.eidas.node.NodeViewNames;
import eu.eidas.node.specificcommunication.ISpecificProxyService;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.utils.EidasAttributesUtil;
import eu.eidas.node.utils.SessionHolder;

/**
 * Action that handles the incoming response from the ID Provider.
 *
 * @see eu.eidas.node.service.AbstractServiceServlet
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public final class IdPResponseServlet extends AbstractServiceServlet {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 4539991356226362922L;

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(IdPResponseServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        execute(request, response);
    }

    /**
     * Executes the method {@link eu.eidas.node.auth.service.AUSERVICE#processIdpResponse} (of the ProxyService) and
     * then sets the internal variables used by the redirection JSP or the consent-value jsp, accordingly to {@link
     * EidasParameterKeys#NO_CONSENT_VALUE} or {@link EidasParameterKeys#CONSENT_VALUE} respectively.
     *
     * @param request
     * @param response
     * @return {@link EidasParameterKeys#CONSENT_VALUE} if the consent-value form is to be displayed, {@link
     * EidasParameterKeys#NO_CONSENT_VALUE} otherwise.
     * @see EidasParameterKeys#NO_CONSENT_VALUE
     * @see EidasParameterKeys#CONSENT_VALUE
     */

    private void execute(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        try {

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(handleExecute(request, response));
            dispatcher.forward(request, response);
            HttpSession session = request.getSession(false);
            if (null != session
                    && session.getAttribute(EidasParameterKeys.EIDAS_CONNECTOR_SESSION.toString()) == null) {
                session.invalidate();
            }
        } catch (ServletException e) {
            getLogger().info("ERROR : ServletException {}", e.getMessage());
            getLogger().debug("ERROR : ServletException {}", e);
            throw e;
        } catch (IOException e) {
            getLogger().info("IOException {}", e.getMessage());
            getLogger().debug("IOException {}", e);
            throw e;
        }

    }

    private String handleExecute(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        IdPResponseBean controllerService =
                (IdPResponseBean) getApplicationContext().getBean(NodeBeanNames.IdP_RESPONSE.toString());

        ISpecificProxyService specificProxyService = controllerService.getSpecificProxyService();
        ILightResponse lightResponse;
        try {
            lightResponse = specificProxyService.processResponse(request, response);
        } catch (SpecificException e) {
            getLogger().error("SpecificException" + e, e);
            // Illegal state: no error AND no success response received from the specific
            throw new ServletException("Unable to process specific response: " + e, e);
        }
        if (null == lightResponse) {
            getLogger().error("SpecificException: Missing specific response");
            // Illegal state: no error AND no success response received from the specific
            throw new ServletException("Missing specific response: no error and no success");
        }

        HttpSession session = request.getSession();
        SessionHolder.setId(session);
        session.setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_SERVICE_RESPONSE);

        // This is not the specific Map
        CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap =
                controllerService.getProxyServiceRequestCorrelationMap();
        StoredAuthenticationRequest storedAuthenticationRequest =
                requestCorrelationMap.remove(lightResponse.getInResponseToId());

        if (null == storedAuthenticationRequest) {
            // send the error back:
            if (LOG.isErrorEnabled()) {
                LOG.error("Could not correlate any eIDAS request to the received specific IdP response: "
                                  + lightResponse);
            }
            throw new ServletException(
                    "Could not correlate specific response ID: " + lightResponse.getId() + " to any eIDAS request");
        }

        IAuthenticationRequest originalRequest = storedAuthenticationRequest.getRequest();

        WebRequest webRequest = new IncomingRequest(request);

        String retVal;

        IResponseStatus responseStatus = lightResponse.getStatus();
        if (responseStatus.isFailure()) {

            String statusCode = responseStatus.getStatusCode();
            String errorMessage = responseStatus.getStatusMessage();
            String errorSubCode = responseStatus.getSubStatusCode();

            // send the error back:
            if (LOG.isErrorEnabled()) {
                LOG.error("Received failed authentication from Specific Idp: errorMessage=\"" + errorMessage
                                  + "\", statusCode=\"" + statusCode + "\", subCode=\"" + errorSubCode + "\"");
            }
            retVal = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();

            getLogger().trace("Generate SAMLTokenFail because of authentication failure received from specific IdP");
            String samlTokenFail = controllerService.getProxyService()
                    .generateSamlTokenFail(originalRequest, statusCode, null, errorSubCode, errorMessage,
                                           webRequest.getRemoteIpAddress(), false);

            request.setAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString(), samlTokenFail);
        } else {

            // generate the eIDAS response with the ProxyService and send it back to the Connector:
            if (controllerService.isAskConsentValue()) {
                getLogger().trace("consent-value");
                retVal = NodeViewNames.EIDAS_SERVICE_CITIZEN_CONSENT.toString();

                // TODO: DO NOT generate this in advance!
                getLogger().trace(
                        "Generate SAMLTokenFail proactively in case of future consent refusal by the citizen");
                String samlTokenFail = controllerService.getProxyService()
                        .generateSamlTokenFail(originalRequest, EIDASStatusCode.REQUESTER_URI.toString(),
                                               EidasErrorKey.CITIZEN_NO_CONSENT_MANDATORY,
                                               webRequest.getRemoteIpAddress());

                request.setAttribute(NodeParameterNames.SAML_TOKEN_FAIL.toString(), samlTokenFail);

            } else {
                getLogger().trace("no-consent-value");
                retVal = NodeViewNames.EIDAS_CONNECTOR_REDIRECT.toString();
            }

            IResponseMessage responseMessage = controllerService.getProxyService()
                    .processIdpResponse(webRequest, storedAuthenticationRequest, lightResponse);

            String samlToken = EidasStringUtil.encodeToBase64(responseMessage.getMessageBytes());

            request.setAttribute(NodeParameterNames.SAML_TOKEN.toString(), samlToken);

            IAuthenticationResponse authnResponse = responseMessage.getResponse();

            ImmutableAttributeMap responseImmutableAttributeMap = authnResponse.getAttributes();

            ProtocolProcessorI extProx =
                    controllerService.getProxyService().getSamlService().getSamlEngine().getProtocolProcessor();

            ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> responseImmutableMap =
                    responseImmutableAttributeMap.getAttributeMap();

            ImmutableAttributeMap.Builder filteredAttrMapBuilder = ImmutableAttributeMap.builder();
            if (controllerService.isAskConsentAllAttributes()) {
                for (AttributeDefinition attrDef : responseImmutableMap.keySet()) {
                    if (controllerService.isAskConsentAttributeNamesOnly()) {
                        filteredAttrMapBuilder.put(attrDef);
                    } else {
                        filteredAttrMapBuilder.put(attrDef, responseImmutableAttributeMap.getAttributeValuesByNameUri(attrDef.getNameUri()));
                    }
                }

            } else {
                for (AttributeDefinition attrDef : responseImmutableMap.keySet()) {
                    // filter out non file-registry attributes = dynamic, additional ones, where value must not be displayed
                    if (extProx.getAttributeDefinitionNullable(attrDef.getNameUri().toString()) != null
                            && extProx.getAdditionalAttributes().getByName(attrDef.getNameUri().toString()) == null) {
                        if (controllerService.isAskConsentAttributeNamesOnly()) {
                            filteredAttrMapBuilder.put(attrDef);
                        } else {
                            filteredAttrMapBuilder.put(attrDef, responseImmutableAttributeMap.getAttributeValuesByNameUri(attrDef.getNameUri()));
                        }
                    }
                }
            }
            request.setAttribute(NodeParameterNames.PAL.toString(), filteredAttrMapBuilder.build().getAttributeMap());

            boolean hasEidasAttributes = false;
            for (final AttributeDefinition attributeDefinition : responseImmutableMap.keySet()) {
                if (EidasSpec.REGISTRY.contains(attributeDefinition)) {
                    hasEidasAttributes = true;
                    break;
                }
            }
            request.setAttribute(NodeParameterNames.EIDAS_ATTRIBUTES_PARAM.toString(),
                                 Boolean.valueOf(hasEidasAttributes));

            if (StringUtils.isNotBlank(authnResponse.getLevelOfAssurance())) {
                request.setAttribute(NodeParameterNames.LOA_VALUE.toString(),
                                     EidasAttributesUtil.getUserFriendlyLoa(authnResponse.getLevelOfAssurance()));
            }
        }

        // Prevent cookies from being accessed through client-side script.
        setHTTPOnlyHeaderToSession(false, request, response);

        // Gets the attributes from Attribute Providers and validates mandatory
        // attributes.

        // Setting internal variables, to be included by the Struts on the JSP
        getLogger().trace("setting internal variables");

        String redirectUrl = originalRequest.getAssertionConsumerServiceURL();
        getLogger().debug("redirectUrl: " + redirectUrl);

        // These attributes are used by citizenConsent.jsp
        request.setAttribute(NodeParameterNames.REDIRECT_URL.toString(),
                             response.encodeRedirectURL(redirectUrl)); // Correct URl redirect cookie implementation
        request.setAttribute(EidasParameterKeys.SP_ID.toString(), originalRequest.getProviderName());
        if (originalRequest instanceof IStorkAuthenticationRequest) {
            request.setAttribute(NodeParameterNames.QAA_LEVEL.toString(),
                                 ((IStorkAuthenticationRequest) originalRequest).getQaa());
        }

        String relayState = storedAuthenticationRequest.getRelayState();
        if (StringUtils.isNotBlank(relayState)) {
            getLogger().debug("Relay State ProxyService " + relayState);
            request.setAttribute(NodeParameterNames.RELAY_STATE.toString(), relayState);
        }

        return retVal;
    }
}
