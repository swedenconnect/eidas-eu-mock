package eu.eidas.auth.commons.protocol.impl;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.util.Preconditions;

/**
 * Request Message
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class BinaryRequestMessage extends AbstractProtocolMessage implements IRequestMessage {

    @Nonnull
    private final IAuthenticationRequest request;

    public BinaryRequestMessage(@Nonnull IAuthenticationRequest request, @Nonnull byte[] requestBytes) {
        super(requestBytes);
        Preconditions.checkNotNull(request, "request");
        this.request = request;
    }

    @Override
    @Nonnull
    public IAuthenticationRequest getRequest() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BinaryRequestMessage that = (BinaryRequestMessage) o;

        if (!request.equals(that.request)) {
            return false;
        }
        return Arrays.equals(getMessageBytes(), that.getMessageBytes());

    }

    @Override
    public int hashCode() {
        int result = request.hashCode();
        result = 31 * result + Arrays.hashCode(getMessageBytes());
        return result;
    }

    @Override
    public String toString() {
        return "BinaryRequestMessage{" +
                "request=" + request +
                ", requestBytes=" + EidasStringUtil.encodeToBase64(getMessageBytes()) +
                '}';
    }
}
