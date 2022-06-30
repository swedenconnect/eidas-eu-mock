package eu.eidas.auth.commons.protocol;

/**
 * Message which contains the binary representation of the protocol.
 *
 * @since 1.1
 */
public interface IProtocolMessage {

    /**
     * Returns the byte array of this message accordingly to the protocol in vigor.
     *
     * @return the byte array of this message accordingly to the protocol in vigor.
     */
    byte[] getMessageBytes();
}
