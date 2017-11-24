package eu.eidas.auth.commons.protocol.impl;

import java.util.Arrays;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IProtocolMessage;
import eu.eidas.util.Preconditions;

/**
 * Abstract Protocol Message.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolMessage implements IProtocolMessage {

    @Nonnull
    private final byte[] messageBytes;

    protected AbstractProtocolMessage(@Nonnull byte[] messageBytes) {
        Preconditions.checkNotEmpty(messageBytes, "messageBytes");
        // defensive copy
        this.messageBytes = messageBytes.clone();
    }

    @Override
    @Nonnull
    public byte[] getMessageBytes() {
        // defensive copy
        return messageBytes.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractProtocolMessage that = (AbstractProtocolMessage) o;

        return Arrays.equals(messageBytes, that.messageBytes);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(messageBytes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "messageBytes=" + EidasStringUtil.encodeToBase64(messageBytes) +
                '}';
    }
}
