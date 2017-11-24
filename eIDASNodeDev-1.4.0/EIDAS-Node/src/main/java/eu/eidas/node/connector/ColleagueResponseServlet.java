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

package eu.eidas.node.connector;

import java.io.IOException;
import java.security.InvalidParameterException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.UnmodifiableIterator;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.engine.core.eidas.spec.LegalPersonSpec;
import eu.eidas.auth.engine.core.eidas.spec.RepresentativeLegalPersonSpec;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.validation.NormalParameterValidator;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.node.specificcommunication.ISpecificConnector;
import eu.eidas.node.specificcommunication.exception.SpecificException;
import eu.eidas.node.utils.SessionHolder;

/**
 * Is invoked when ProxyService wants to pass control to the Connector.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public final class ColleagueResponseServlet extends AbstractConnectorServlet {

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
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            // Prevent cookies from being accessed through client-side script with renew of session.
            setHTTPOnlyHeaderToSession(false, request, response);
            SessionHolder.setId(request.getSession());
            request.getSession()
                    .setAttribute(EidasParameterKeys.SAML_PHASE.toString(), EIDASValues.EIDAS_CONNECTOR_RESPONSE);

            // Obtaining the assertion consumer url from SPRING context
            ConnectorControllerService controllerService = (ConnectorControllerService) getApplicationContext().getBean(
                    NodeBeanNames.EIDAS_CONNECTOR_CONTROLLER.toString());
            LOG.trace("ConnectorControllerService {}", controllerService);

            // Obtains the parameters from httpRequest
            WebRequest webRequest = new IncomingRequest(request);

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

            //TODO START remove correction of erroneous attributes after transition period of EID-423
            ImmutableAttributeMap respAttributes = authenticationExchange.getConnectorResponse().getAttributes();
            ImmutableAttributeMap.Builder correctedAttrs = ImmutableAttributeMap.builder();
            if (respAttributes.getDefinitions().contains(LegalPersonSpec.Definitions.LEGAL_ADDRESS) ||
                    respAttributes.getDefinitions().contains(LegalPersonSpec.Definitions.VAT_REGISTRATION) ||
                    respAttributes.getDefinitions().contains(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION) ||
                    respAttributes.getDefinitions().contains(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION)) {
                UnmodifiableIterator<ImmutableAttributeMap.ImmutableAttributeEntry<?>> attrIterator = respAttributes.entrySet().iterator();
                while (attrIterator.hasNext()) {
                    ImmutableAttributeMap.ImmutableAttributeEntry<?> attr = attrIterator.next();
                    if (attr.getKey().equals(LegalPersonSpec.Definitions.VAT_REGISTRATION)) {
                        LOG.warn("Replacing VATRegistration with VATRegistrationNumber from " + authenticationExchange.getConnectorResponse().getIssuer());
                        ImmutableSet values = attr.getValues();
                        correctedAttrs.put(LegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER, values);
                    } else if (attr.getKey().equals(LegalPersonSpec.Definitions.LEGAL_ADDRESS)) {
                        LOG.warn("Replacing LegalAddress to LegalPersonAddress from " + authenticationExchange.getConnectorResponse().getIssuer());
                        ImmutableSet values = attr.getValues();
                        correctedAttrs.put(LegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS, values);
                    } else if (attr.getKey().equals(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION)) {
                        LOG.warn("Replacing RepresentativeVATRegistration with RepresentativeVATRegistrationNumber from " + authenticationExchange.getConnectorResponse().getIssuer());
                        ImmutableSet values = attr.getValues();
                        correctedAttrs.put(RepresentativeLegalPersonSpec.Definitions.VAT_REGISTRATION_NUMBER, values);
                    } else if (attr.getKey().equals(RepresentativeLegalPersonSpec.Definitions.LEGAL_ADDRESS)) {
                        LOG.warn("Replacing RepresentativeLegalAddress to RepresentativeLegalPersonAddress from " + authenticationExchange.getConnectorResponse().getIssuer());
                        ImmutableSet values = attr.getValues();
                        correctedAttrs.put(RepresentativeLegalPersonSpec.Definitions.LEGAL_PERSON_ADDRESS, values);
                    } else {
                        ImmutableSet values = attr.getValues();
                        correctedAttrs.put(attr.getKey(), values);
                    }
                }
                respAttributes = correctedAttrs.build();
            }
            //TODO END remove correction of erroneous attributes after transition period of EID-423

            // Build the LightResponse
            LightResponse lightResponse =
                    LightResponse.builder(authenticationExchange.getConnectorResponse()).attributes(respAttributes).build();

            // Call the specific module
            sendResponse(lightResponse, request, response, controllerService);

        } catch (ServletException se) {
            LOG.info("BUSINESS EXCEPTION : ServletException", se.getMessage());
            LOG.debug("BUSINESS EXCEPTION : ServletException", se);
            throw se;
        }
    }

    private void sendResponse(ILightResponse lightResponse, HttpServletRequest request, HttpServletResponse response, ConnectorControllerService connectorController) throws ServletException {
        try {
            ISpecificConnector specificConnector = connectorController.getSpecificConnector();
            specificConnector.sendResponse(lightResponse, request, response);

        } catch (SpecificException e) {
            getLogger().error("SpecificException" + e, e);
            // Illegal state: exception received from the specific module
            throw new ServletException("Unable to send specific response: " + e, e);
        }
    }

}
