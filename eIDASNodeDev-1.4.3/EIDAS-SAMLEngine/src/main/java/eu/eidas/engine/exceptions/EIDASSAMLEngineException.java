/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.engine.exceptions;

/**
 * The main SAMLEngine Exception.
 *
 * @author fjquevedo
 */
public class EIDASSAMLEngineException extends Exception {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -8319723167019122930L;

    /**
     * The error code.
     */
    private String errorCode;

    /**
     * The error detail.
     */
    private String errorDetail;

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     * value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASSAMLEngineException(Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param errorMessage the error message
     */
    public EIDASSAMLEngineException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     * value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASSAMLEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param newErrorCode the error code
     * @param errorMessage the error message
     */

    public EIDASSAMLEngineException(String newErrorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = newErrorCode;
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param newErrorCode the error code
     * @param errorMessage the error message
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A <tt>null</tt>
     * value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASSAMLEngineException(String newErrorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = newErrorCode;
        this.errorDetail = cause.getMessage();
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param newErrorCode the error code
     * @param errorMessage the error message
     * @param newErrorDetail the error detail
     */
    public EIDASSAMLEngineException(String newErrorCode, String errorMessage, String newErrorDetail) {
        super(errorMessage);
        this.errorCode = newErrorCode;
        this.errorDetail = newErrorDetail;
    }

    /**
     * Gets the error code.
     *
     * @return the error code
     */
    public final String getErrorCode() {
        return this.errorCode;
    }

    /**
     * Gets the error detail.
     *
     * @return the error detail
     */
    public final String getErrorDetail() {
        return errorDetail;
    }

    /**
     * Gets the error message.
     *
     * @return the error message
     */
    public final String getErrorMessage() {
        return super.getMessage();
    }

    /**
     * Gets the message.
     *
     * @return the message of the exception.
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public final String getMessage() {
        return "Error (no. " + errorCode + ") processing request : " + super.getMessage() + " - " + getErrorDetail();
    }

    /**
     * Sets the error code.
     *
     * @param newErrorCode the new error code
     */
    public final void setErrorCode(String newErrorCode) {
        this.errorCode = newErrorCode;
    }

    /**
     * Sets the error detail.
     *
     * @param newErrorDetail the new error detail
     */
    public final void setErrorDetail(String newErrorDetail) {
        this.errorDetail = newErrorDetail;
    }

}
