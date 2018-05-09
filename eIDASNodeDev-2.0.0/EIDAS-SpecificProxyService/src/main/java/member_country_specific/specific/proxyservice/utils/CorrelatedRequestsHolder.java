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

package member_country_specific.specific.proxyservice.utils;

import eu.eidas.SimpleProtocol.AuthenticationRequest;
import eu.eidas.auth.commons.light.ILightRequest;

import java.io.Serializable;

/**
 * Holds the light request and the correlated specific request {@link AuthenticationRequest}.
 *
 * @since 2.0
 */
public class CorrelatedRequestsHolder implements Serializable{

    private static final long serialVersionUID = 8942548697342198159L;

    private ILightRequest iLightRequest;

    private AuthenticationRequest authenticationRequest;

    public CorrelatedRequestsHolder(ILightRequest iLightRequest, AuthenticationRequest authenticationRequest) {
        this.iLightRequest = iLightRequest;
        this.authenticationRequest = authenticationRequest;
    }

    public ILightRequest getiLightRequest() {
        return iLightRequest;
    }

    public AuthenticationRequest getAuthenticationRequest() {
        return authenticationRequest;
    }
}
