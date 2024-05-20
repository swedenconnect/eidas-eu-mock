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
package eu.eidas.node.auth.service;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.cache.Cache;

import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;

/**
 * Interface for handling messages at the proxy service level.
 */
public interface ISERVICEService {

    /**
     * Process the authentication request sent from the connector.
     *
     * @param webRequest the webrequest containing the token
     * @param relayState the relay state if needed to be propagated
     * @param requestCorrelationCache the request correlation map used
     * @param remoteIpAddress the ipaddres
     * @return the processed request
     */
    IAuthenticationRequest processAuthenticationRequest(@Nonnull WebRequest webRequest,
                                                        @Nullable String relayState,
                                                        @Nonnull
                                                                Cache<String, StoredAuthenticationRequest> requestCorrelationCache,
                                                        @Nonnull String remoteIpAddress);


    /**
     * Normalizes the attributes to request format (eg eIDAS), generates the SAML Tokens to send to Connector.
     *
     * @param webRequest the instance of the {@link WebRequest}
     * @param proxyServiceRequest the instance of the {@link StoredAuthenticationRequest}
     * @param idpResponse the instance of the {@link ILightResponse}
     * @return The new authentication request.
     * @see EidasAuthenticationRequest
     * @see Map
     * <p>
     * TODO: rename as processIdpResponse
     */
    IResponseMessage processIdpResponse(@Nonnull WebRequest webRequest,
                                        @Nonnull StoredAuthenticationRequest proxyServiceRequest,
                                        @Nonnull ILightResponse idpResponse);

    ISERVICESAMLService getSamlService();
}
