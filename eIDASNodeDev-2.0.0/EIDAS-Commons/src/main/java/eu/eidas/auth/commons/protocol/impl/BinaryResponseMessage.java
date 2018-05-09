package eu.eidas.auth.commons.protocol.impl;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.util.Preconditions;

/**
 * Response Message which contains the binary representation of the protocol response.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class BinaryResponseMessage extends AbstractProtocolMessage implements IResponseMessage {

    @Nonnull
    private final IAuthenticationResponse response;

    public BinaryResponseMessage(@Nonnull IAuthenticationResponse response, @Nonnull byte[] responseBytes) {
        super(responseBytes);
        Preconditions.checkNotNull(response, "response");
        this.response = response;
    }

    @Override
    @Nonnull
    public IAuthenticationResponse getResponse() {
        return response;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BinaryResponseMessage that = (BinaryResponseMessage) o;

        if (!response.equals(that.response)) {
            return false;
        }
        return Arrays.equals(getMessageBytes(), that.getMessageBytes());

    }

    @Override
    public int hashCode() {
        int result = response.hashCode();
        result = 31 * result + Arrays.hashCode(getMessageBytes());
        return result;
    }

    @Override
    public String toString() {
        return "BinaryResponseMessage{" +
                "response=" + response +
                ", responseBytes=" + EidasStringUtil.encodeToBase64(getMessageBytes()) +
                '}';
    }
}
