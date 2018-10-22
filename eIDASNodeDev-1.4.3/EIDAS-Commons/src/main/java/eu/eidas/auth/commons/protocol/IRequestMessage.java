package eu.eidas.auth.commons.protocol;

import javax.annotation.Nonnull;

/**
 * Request Message which contains the binary representation of the protocol request.
 *
 * @since 1.1
 */
public interface IRequestMessage extends IProtocolMessage {

    @Nonnull
    IAuthenticationRequest getRequest();
}
