/*
 * Copyright (c) 2024 by European Commission
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
 * limitations under the Licence.
 */
package eu.eidas.node.auth.service;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for communicating with the SAMLEngine.
 * @version $Revision: 1.29 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ISERVICESAMLService {

    /**
     * Process the token received the connector.
     * @param bindingFromHttp post or redirect
     * @param samlObj the byte[]
     * @param ipUserAddress  The citizen's IP address.
     * @param relayState the relay state
     * @return the transformed token in an authenticationRequest
     */
    @Nonnull
    IAuthenticationRequest processConnectorRequest(String bindingFromHttp,
                                                   byte[] samlObj,
                                                   String ipUserAddress,
                                                   String relayState);

    /**
     * Process the response received from the IDP.
     *
     * @param originalRequest The original authentication request.
     * @param response the instance of {@link AuthenticationResponse}
     * @param ipUserAddress The citizen's IP address.
     * @return A byte array containing the SAML Response Token.
     * @see EidasAuthenticationRequest
     */
    IResponseMessage processIdpSpecificResponse(IAuthenticationRequest originalRequest,
                                                AuthenticationResponse response,
                                                String ipUserAddress);

    /**
     * Checks whether all mandatory attributes for the person type are present
     *
     * @param attributes the instance of {@link ImmutableAttributeMap} holding the attributes
     * @return true if all mandatory attributes for the person type are present
     */
    boolean checkMandatoryAttributeSet(@Nullable ImmutableAttributeMap attributes);

    /**
     * Checks if it is a representation response, if at least a representative MDS is present in the
     * given attribute map.
     * If it is a representation response, verify that the maximum amount of MDS that should be
     * present is not exceeded.
     *
     * @param immutableAttributeMap the attribute map of attributes.
     * @return true if not a representation response or if valid representation response, false otherwise
     */
    boolean checkRepresentationResponse(@Nonnull ImmutableAttributeMap immutableAttributeMap);

    /**
     * Checks whether the attribute map satifisfies the rule of representation
     *
     * @param attributes the instance of {@link ImmutableAttributeMap} holding the attributes
     * @return true if the attribute map satifisfies the rule of representation and false otherwise
     */
    boolean checkRepresentativeAttributes(@Nullable ImmutableAttributeMap attributes);

    /**
     * Checks if all the requested mandatory attributes have values.
     *
     * @param requestedAttributes the instance of {@link ImmutableAttributeMap} holding the request attributes
     * @param responseAttributes the instance of {@link ImmutableAttributeMap} holding the response attributes
     *
     * @return true if all mandatory attributes have values, false if at least one
     * attribute doesn't have value.
     *
     */
    boolean checkMandatoryAttributes(@Nonnull ImmutableAttributeMap requestedAttributes, @Nonnull ImmutableAttributeMap responseAttributes);

    String getSamlEngineInstanceName();

    ProtocolEngineI getSamlEngine();

    @Nonnull
    IAuthenticationRequest updateRequest(@Nonnull IAuthenticationRequest authnRequest,
                                         @Nonnull ImmutableAttributeMap updatedAttributes);

    @Nonnull
    String getCountryCode();

}
