/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence
 */
package eu.eidas.auth.engine;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.List;

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
     * @throws EIDASSAMLEngineException if the request could not be generated
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

    /**
     * Generates the Response error message.
     *
     * @param request the request data
     * @param response the response data
     * @param ipAddress the ip address
     * @param applicationIdentifiers the list of request protocol versioning's application identifiers
     * @return the response
     *
     * @since 2.2
     *
     * @throws EIDASSAMLEngineException if the response could not be generated
     */
    @Nonnull
    IResponseMessage generateResponseErrorMessage(@Nonnull IAuthenticationRequest request,
                                                  @Nonnull IAuthenticationResponse response,
                                                  @Nonnull String ipAddress,
                                                  List<String> applicationIdentifiers) throws EIDASSAMLEngineException;

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
     * CorrelationMap} before invoking {@link #validateUnmarshalledResponse(Correlated, String, long, long, String)} to obtain the
     * complete response.
     * @param responseBytes the SAML response bytes
     *
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
