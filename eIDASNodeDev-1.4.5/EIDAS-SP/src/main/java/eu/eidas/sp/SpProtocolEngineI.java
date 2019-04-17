package eu.eidas.sp;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * SpProtocolEngineI
 *
 * @since 1.1
 */
public interface SpProtocolEngineI extends ProtocolEngineI {

    @Nonnull
    byte[] checkAndDecryptResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException;

    @Nonnull
    byte[] reSignRequest(@Nonnull byte[] requestBytes) throws EIDASSAMLEngineException;

    @Nonnull
    IRequestMessage resignEIDASAuthnRequest(@Nonnull IRequestMessage originalRequest, boolean changeDestination)
            throws EIDASSAMLEngineException;
}
