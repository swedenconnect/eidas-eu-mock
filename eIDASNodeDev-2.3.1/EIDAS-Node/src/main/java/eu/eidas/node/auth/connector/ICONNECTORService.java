/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.node.auth.connector;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;

/**
 * Interface for managing incoming requests.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.21 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ICONNECTORService {

    /**
     * Validates the origin of the request and of the Country Selected, and creates a SAML token to send to the
     * ProxyService.
     *
     * @param webRequest the current request.
     * @param lightRequest The lightRequest received from the specific.
     * @return An authentication request.
     * @see EidasAuthenticationRequest
     * @see CorrelationMap
     */
    IRequestMessage getAuthenticationRequest(@Nonnull WebRequest webRequest, ILightRequest lightRequest);

    /**
     * Receives an Authentication Response, validates the origin of the response, and generates a SAML token to be sent
     * to the SP.
     *
     * @param webRequest the current request.
     * @return An Authentication response.
     * @see EidasAuthenticationRequest
     * @see CorrelationMap
     */
    @Nonnull
    AuthenticationExchange getAuthenticationResponse(@Nonnull WebRequest webRequest);


    /**
     * Returns with encapsulated saml service bean
     * @return ICONNECTORSAMLService
     */
    ICONNECTORSAMLService getSamlService();
}
