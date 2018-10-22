/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.auth.service;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;

/**
 * Interface for handling messages at the proxy service level.
 */
public interface ISERVICEService {

    /**
     * Decodes the SAML Token, normalizes data from the request format to specific format, and presents a consent-type
     * form for the citizen to choose the optional attributes to be requested from the IdP/AP. Alternatively, the user
     * can cancel the process.
     *
     * @param parameters A map of attributes.
     * @param session    The session to store the incoming authentication request.
     * @return The newly created authentication request.
     * @see EidasAuthenticationRequest
     * @see Map
     */
    /**
     * Process the authentication request sent from the connector.
     *
     * @param webRequest the webrequest containing the token
     * @param relayState the relay state if needed to be propagated
     * @param requestCorrelationMap the request correlation map used
     * @param remoteIpAddress the ipaddres
     * @return the processed request
     */
    IAuthenticationRequest processAuthenticationRequest(@Nonnull WebRequest webRequest,
                                                        @Nullable String relayState,
                                                        @Nonnull
                                                                CorrelationMap<StoredAuthenticationRequest> requestCorrelationMap,
                                                        @Nonnull String remoteIpAddress);

    /**
     * Validates the consent sent by the citizen, then redirects the citizen to the IdP for the login process.
     *
     * @param parameters A map of attributes.
     * @param session The current session.
     * @param askConsentType Whether The consent-type form was present or not.
     * @return The Personal Attribute List updated with user consent.
     * @see Map
     * @see EidasAuthenticationRequest
     */
    IAuthenticationRequest processCitizenConsent(WebRequest webRequest,
                                                 @Nonnull StoredAuthenticationRequest storedRequest,
                                                 boolean askConsentType);

    /**
     * Normalizes the attributes to request format (eg eIDAS), generates the SAML Tokens to send to Connector, and if
     * required displays the consent-value form.
     *
     * @param parameters A map of attributes.
     * @param session The current session.
     * @return The new authentication request.
     * @see EidasAuthenticationRequest
     * @see Map
     * <p/>
     * TODO: rename as processIdpResponse
     */
    IResponseMessage processIdpResponse(@Nonnull WebRequest webRequest,
                                        @Nonnull StoredAuthenticationRequest proxyServiceRequest,
                                        @Nonnull ILightResponse idpResponse);

    /**
     * Generates an error SAML token.
     *
     * @param authData The authentication request.
     * @param statusCode The status code.
     * @param errorId The status code to set.
     * @param ipUserAddress The citizen's IP address.
     * @return A {@link Base64} encoded SAML token.
     * @see EidasAuthenticationRequest
     * @see EidasErrorKey
     * @see Base64
     */
    String generateSamlTokenFail(IAuthenticationRequest authData,
                                 String statusCode,
                                 EidasErrorKey errorId,
                                 String ipUserAddress);

    /**
     * Generates an error SAML token.
     *
     * @param originalRequest The authentication request.
     * @param statusCode The status code to set.
     * @param errorCode The error code to set.
     * @param subCode The sub status code to set.
     * @param errorMessage Error message to set.
     * @param ipUserAddress The citizen's IP address.
     * @return A {@link Base64} encoded SAML token.
     * @see EidasAuthenticationRequest
     * @see EidasErrorKey
     * @see Base64
     */
    String generateSamlTokenFail(IAuthenticationRequest originalRequest,
                                 String statusCode,
                                 String errorCode,
                                 String subCode,
                                 String errorMessage,
                                 String ipUserAddress,
                                 boolean isAuditable);

    ISERVICESAMLService getSamlService();
}
