package eu.eidas.auth.commons.tx;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.util.Preconditions;

/**
 * The Authentication Exchange is composed of the original request from the ServiceProvider and the final response from
 * the Connector.
 *
 * @since 1.1
 */
public final class BinaryAuthenticationExchange {

    @Nonnull
    private final StoredAuthenticationRequest storedRequest;

    @Nonnull
    private final IResponseMessage connectorResponseMessage;

    public BinaryAuthenticationExchange(@Nonnull StoredAuthenticationRequest storedRequest,
                                        @Nonnull IResponseMessage connectorResponseMessage) {
        Preconditions.checkNotNull(storedRequest, "storedRequest");
        Preconditions.checkNotNull(connectorResponseMessage, "connectorResponseMessage");
        this.storedRequest = storedRequest;
        this.connectorResponseMessage = connectorResponseMessage;
    }

    @Nonnull
    public StoredAuthenticationRequest getStoredRequest() {
        return storedRequest;
    }

    @Nonnull
    public IResponseMessage getConnectorResponseMessage() {
        return connectorResponseMessage;
    }
}
