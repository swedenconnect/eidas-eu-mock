package eu.eidas.auth.engine;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Engine providing the implementation of the actual protocol (e.g. SAML, eIDAS, etc).
 *
 * @since 1.1
 */
public interface ProtocolEngineI {

    /**
     * Generates the bytes of the request based on the given request data for the given target service.
     *
     * @param request the request data
     * @param serviceIssuer the target service to which the request is to be sent
     * @return the container of the request bytes
     * @throws EIDASSAMLEngineException
     */
    @Nonnull
    IRequestMessage generateRequestMessage(@Nonnull IAuthenticationRequest request, @Nonnull String serviceIssuer)
            throws EIDASSAMLEngineException;

    @Nonnull
    IResponseMessage generateResponseMessage(@Nonnull IAuthenticationRequest request,
                                             @Nonnull IAuthenticationResponse response,
                                             boolean signAssertion,
                                             @Nonnull String ipAddress) throws EIDASSAMLEngineException;

    @Nonnull
    IResponseMessage generateResponseErrorMessage(@Nonnull IAuthenticationRequest request,
                                                  @Nonnull IAuthenticationResponse response,
                                                  @Nonnull String ipAddress) throws EIDASSAMLEngineException;

    @Nullable
    ProtocolCipherI getCipher();

    @Nonnull
    SamlEngineClock getClock();

    @Nonnull
    SamlEngineCoreProperties getCoreProperties();

    @Nullable
    X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException;

    @Nonnull
    ProtocolProcessorI getProtocolProcessor();

    @Nonnull
    String getInstanceName();

    @Nonnull
    ProtocolSignerI getSigner();

    @Nonnull
    X509Certificate getSigningCertificate() throws EIDASSAMLEngineException;

    @Nonnull
    IAuthenticationRequest unmarshallRequestAndValidate(@Nonnull byte[] requestBytes,
                                                        @Nonnull String citizenCountryCode)
            throws EIDASSAMLEngineException;

    /**
     * Unmarshalls the given bytes into a {@link Correlated} response object.
     * <p>
     * The {@link Correlated} response object is used to retrieve the correlated request object in a {@link
     * CorrelationMap} before invoking {@link #validateUnmarshalledResponse(Correlated, String, long)} to obtain the
     * complete response.
     *
     * @param tokenSaml the SAML response bytes
     * @return the SAML response instance
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     */
    @Nonnull
    Correlated unmarshallResponse(@Nonnull byte[] responseBytes) throws EIDASSAMLEngineException;

    @Nonnull
    IAuthenticationResponse unmarshallResponseAndValidate(@Nonnull byte[] responseBytes,
                                                          @Nonnull String userIpAddress,
                                                          long beforeSkewTimeInMillis,
                                                          long afterSkewTimeInMillis,
                                                          @Nullable String audienceRestriction)
            throws EIDASSAMLEngineException;

    @Nonnull
    IAuthenticationResponse validateUnmarshalledResponse(@Nonnull Correlated unmarshalledResponse,
                                                         @Nonnull String userIpAddress,
                                                         long beforeSkewTimeInMillis,
                                                         long afterSkewTimeInMillis,
                                                         @Nullable String audienceRestriction)
            throws EIDASSAMLEngineException;
}
