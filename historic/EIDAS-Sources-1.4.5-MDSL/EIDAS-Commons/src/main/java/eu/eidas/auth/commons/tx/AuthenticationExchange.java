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
