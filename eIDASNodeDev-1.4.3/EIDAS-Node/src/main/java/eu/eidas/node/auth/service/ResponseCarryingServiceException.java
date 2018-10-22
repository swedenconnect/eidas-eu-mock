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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.util.Preconditions;

/**
 * Copied from {@link eu.eidas.auth.commons.exceptions.EIDASServiceException} which is Anti-Pattern of using an
 * exception to carry a failure response to a Servlet.
 * <p>
 * This is really bad and James Gosling dies a little every time this exception is thrown.
 *
 * @see eu.eidas.auth.commons.exceptions.EIDASServiceException
 * @deprecated Anti-Pattern of using an exception to carry a failure response to a Servlet
 */
@Deprecated
public final class ResponseCarryingServiceException extends EIDASServiceException {

    private static final long serialVersionUID = 2265301301918985175L;

    @Nonnull
    private final String errorRedirectUrl;

    @Nullable
    private final String relayState;

    /**
     * Exception Constructor with three strings representing the errorCode, errorMessage and encoded samlToken as
     * parameters.
     *
     * @param errorCode The error code value.
     * @param errorMessage The error message value.
     * @param samlTokenFail The error SAML Token.
     */
    public ResponseCarryingServiceException(@Nonnull String errorCode,
                                            @Nonnull String errorMessage,
                                            @Nonnull String samlTokenFail,
                                            @Nonnull String errorRedirectUrl,
                                            @Nullable String relayState) {
        super(errorCode, errorMessage, samlTokenFail);
        Preconditions.checkNotNull(samlTokenFail, "samlTokenFail");
        Preconditions.checkNotNull(errorRedirectUrl, "errorRedirectUrl");
        this.errorRedirectUrl = errorRedirectUrl;
        this.relayState = relayState;
    }

    @Nonnull
    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    @Nullable
    public String getRelayState() {
        return relayState;
    }
}
