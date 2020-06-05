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
package eu.eidas.node.auth.connector;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.tx.AuthenticationExchange;

/**
 * Interface for managing incoming requests.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.21 $, $Date: 2010-11-18 23:17:50 $
 */
public interface ICONNECTORService {

    /**
     * Validates the origin of the request and of the Country Selected, and creates a SAML token to send to the
     * ProxyService.
     *
     * @param webRequest the current request.
     * @param lightRequest The lightRequest received from the specific.
     * @return An authentication request.
     * @see EidasAuthenticationRequest
     * @see RequestCorrelationMap
     */
    IRequestMessage getAuthenticationRequest(@Nonnull WebRequest webRequest, ILightRequest lightRequest);

    /**
     * Receives an Authentication Response, validates the origin of the response, and generates a SAML token to be sent
     * to the SP.
     *
     * @param webRequest the current request.
     * @return An Authentication response.
     * @see EidasAuthenticationRequest
     * @see RequestCorrelationMap
     */
    @Nonnull
    AuthenticationExchange getAuthenticationResponse(@Nonnull WebRequest webRequest);
}
