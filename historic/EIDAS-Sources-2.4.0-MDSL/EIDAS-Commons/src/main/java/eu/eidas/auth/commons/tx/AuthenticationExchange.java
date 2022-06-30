/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.tx;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.util.Preconditions;

/**
 * The Authentication Exchange is composed of the node's authentication request and the final response from
 * the node.
 *
 * @since 1.1
 */
public final class AuthenticationExchange {

    @Nonnull
    private final StoredAuthenticationRequest storedRequest;

    @Nonnull
    private final IAuthenticationResponse connectorResponse;

    public AuthenticationExchange(@Nonnull StoredAuthenticationRequest storedRequest,
                                  @Nonnull IAuthenticationResponse connectorResponse) {
        Preconditions.checkNotNull(storedRequest, "storedRequest");
        Preconditions.checkNotNull(connectorResponse, "connectorResponse");
        this.storedRequest = storedRequest;
        this.connectorResponse = connectorResponse;
    }

    @Nonnull
    public StoredAuthenticationRequest getStoredRequest() {
        return storedRequest;
    }

    @Nonnull
    public IAuthenticationResponse getConnectorResponse() {
        return connectorResponse;
    }
}
