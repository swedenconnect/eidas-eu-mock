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

import java.io.Serializable;

/**
 * Abstract class to represent the various eIDAS Node exceptions.
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AbstractEIDASException extends RuntimeException implements Serializable {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = -1884417567740138022L;

    /**
     * Error code.
     */
    private String errorCode;//NOSONAR

    /**
     * Error message.
     */
    private String errorMessage;//NOSONAR

    /**
     * SAML token.
     */
    private String samlTokenFail;//NOSONAR

    private final String userErrorCode;

    private final String userErrorMessage;

    /**
     * Exception Constructor with two Strings representing the errorCode and errorMessage as parameters.
     *
     * @param code The error code value.
     * @param message The error message value.
     */
    protected AbstractEIDASException(String code, String message) {

        super(message);
        this.errorCode = code;
        this.errorMessage = message;
        userErrorCode = null;
        userErrorMessage = null;
    }

    /**
     * Exception Constructor with the errorMessage as parameters and the Throwable cause.
     *
     * @param message The error message value.
     * @param cause The throwable object.
     */
    protected AbstractEIDASException(String message, Throwable cause) {

        super(message, cause);
        this.errorMessage = message;
        userErrorCode = null;
        userErrorMessage = null;
    }

    /**
     * Exception Constructor with two Strings representing the errorCode and errorMessage as parameters and the
     * Throwable cause.
     *
     * @param code The error code value.
     * @param message The error message value.
     * @param cause The throwable object.
     */
    protected AbstractEIDASException(String code, String message, Throwable cause) {

        this(code, message, cause, null, null);
    }

    protected AbstractEIDASException(String code, String message, Throwable cause,
                                     String userErrorCodeArg,
                                     String userErrorMessageArg) {

        super(message, cause);
        this.errorCode = code;
        this.errorMessage = message;
        this.userErrorCode = userErrorCodeArg;
        this.userErrorMessage = userErrorMessageArg;
    }

    /**
     * Exception Constructor with three Strings representing the errorCode, errorMessage and encoded samlToken as
     * parameters.
     *
     * @param code The error code value.
     * @param message The error message value.
     * @param samlToken The error SAML Token.
     */
    protected AbstractEIDASException(String code, String message, String samlToken) {

        super(message);
        this.errorCode = code;
        this.errorMessage = message;
        this.samlTokenFail = samlToken;
        userErrorCode = null;
        userErrorMessage = null;
    }

    /**
     * Constructor with SAML Token as argument. Error message and error code are embedded in the SAML.
     *
     * @param samlToken The error SAML Token.
     */
    protected AbstractEIDASException(String samlToken) {
        super();
        this.samlTokenFail = samlToken;
        userErrorCode = null;
        userErrorMessage = null;
    }

    /**
     * Getter for errorCode.
     *
     * @return The errorCode value.
     */
    public final String getErrorCode() {
        return errorCode;
    }

    /**
     * Setter for errorCode.
     *
     * @param code The error code value.
     */
    public final void setErrorCode(final String code) {
        this.errorCode = code;
    }

    /**
     * Getter for errorMessage.
     *
     * @return The error Message value.
     */
    public final String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Setter for errorMessage.
     *
     * @param message The error message value.
     */
    public final void setErrorMessage(final String message) {
        this.errorMessage = message;
    }

    /**
     * Getter for SAMLTokenFail.
     *
     * @return The error SAML Token.
     */
    public final String getSamlTokenFail() {
        return samlTokenFail;
    }

    /**
     * Setter for SAMLTokenFail.
     *
     * @param samlToken The error SAML token.
     */
    public final void setSamlTokenFail(final String samlToken) {
        this.samlTokenFail = samlToken;
    }

    public final String getUserErrorCode() {
        return userErrorCode;
    }

    public final String getUserErrorMessage() {
        return userErrorMessage;
    }
}
