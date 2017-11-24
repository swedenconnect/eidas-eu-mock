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
package eu.eidas.auth.specific;

import java.util.Map;

import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;

/**
 * Interface for Specific Authentication methods on the Proxy Service side.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com
 */
public interface IAUService {

    /**
     * Prepares the citizen to be redirected to the IdP.
     *
     * @param lightRequest the request
     * @param parameters the parameters
     * @param requestAttributes the request attributes
     * @return byte[] containing a SAML Request.
     */
    byte[] prepareCitizenAuthentication(ILightRequest lightRequest,
                                        ImmutableAttributeMap modifiedRequestedAttributes,
                                        Map<String, Object> parameters,
                                        Map<String, Object> requestAttributes);

    /**
     * Validates a SAML Response.
     *
     * @param samlToken The SAML Token.
     * @return the AuthenticationExchange composed of the specific AuthenticationRequest and the AuthenticationResponse
     * associated with the validated response.
     */
    AuthenticationExchange processAuthenticationResponse(byte[] samlToken);

    /**
     * Generates a SAML Response in case of error.
     *
     * @param inResponseTo The SAML's identifier to response.
     * @param issuer The issuer value.
     * @param assertionURL The assertion URL.
     * @param code The error code.
     * @param subcode The sub error code.
     * @param message The error message.
     * @param ipUserAddress The user IP address.
     * @return byte[] containing the SAML Response.
     */
    byte[] generateErrorAuthenticationResponse(String inResponseTo,
                                               String issuer,
                                               String assertionURL,
                                               String code,
                                               String subcode,
                                               String message,
                                               String ipUserAddress);

    /**
     * Compares two given attribute lists.
     *
     * @param original The original Attribute List.
     * @param modified The modified Attribute List.
     * @return true if the original list contains the modified one. False otherwise.
     * @see ImmutableAttributeMap
     */
    boolean compareAttributeLists(ImmutableAttributeMap original, ImmutableAttributeMap modified);

    /**
     * Correlation Map between the specific request Id and the eIDAS Proxy Service request Object received from the
     * eIDAS Proxy Service.
     *
     * @return the Correlation Map between the specific request Id and the eIDAS Proxy Service request Object received
     * from the eIDAS Proxy Service.
     */
    CorrelationMap<StoredLightRequest> getProxyServiceRequestCorrelationMap();

    /**
     * Correlation Map between the specific request Id and the specific request Object sent to the IdentityProvider
     * (IdP).
     *
     * @return the Correlation Map between the specific request Id and the specific request Object sent to the
     * IdentityProvider (IdP).
     */
    CorrelationMap<StoredAuthenticationRequest> getSpecificIdpRequestCorrelationMap();

    /**
     * Returns the assertion consumer URL of the IdP in case of STORK specific implementation IdP-side.
     *
     * @return the assertion consumer URL of the IdP in case of STORK specific implementation IdP-side.
     */
    String getCallBackURL();
}
