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
package eu.eidas.auth.commons.exceptions;

/**
 * This exception is thrown by the eIDAS Service service and holds the relative information to present to the citizen.
 */
public class EIDASServiceException extends AbstractEIDASException {

    /**
     * Serial id.
     */
    private static final long serialVersionUID = -4012295047127999362L;

    public EIDASServiceException(String code, String message) {
        super(code, message);
    }

    /**
     * Exception Constructor with two Strings representing the errorCode and errorMessage as parameters.
     *
     * @param code The error code value.
     * @param message The error message value.
     * @param samlToken The SAML Token.
     */
    public EIDASServiceException(String code, String message, String samlToken) {
        super(code, message, samlToken);
    }

    /**
     * Exception Constructor with two Strings representing the errorCode and errorMessage as parameters.
     *
     * @param code The error code value.
     * @param message The error message value.
     * @param cause The original exception;
     * @param samlToken The SAML Token.
     */
    public EIDASServiceException(String code, String message, Throwable cause, String samlToken) {
        super(code, message, cause);
        this.setSamlTokenFail(samlToken);
    }

    public EIDASServiceException(String code,
                                 String message,
                                 Throwable cause,
                                 String userErrorCode,
                                 String userErrorMessage,
                                 String samlToken) {

        super(code, message, cause, userErrorCode, userErrorMessage);
        this.setSamlTokenFail(samlToken);
    }

    /**
     * Constructor with SAML Token as argument. Error message and error code are embedded in the SAML.
     *
     * @param samlToken The error SAML Token.
     */
    public EIDASServiceException(String samlToken) {
        super(samlToken);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getMessage() {
        return this.getErrorMessage() + " (" + this.getErrorCode() + ")";
    }
}
