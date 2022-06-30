package eu.eidas.auth.commons.protocol;

import javax.annotation.Nonnull;

/**
 * Response Message which contains the binary representation of the protocol response.
 *
 * @since 1.1
 */
public interface IResponseMessage extends IProtocolMessage {

    @Nonnull
    IAuthenticationResponse getResponse();
}
