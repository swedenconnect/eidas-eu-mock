/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.engine.exceptions;

/**
 * The main SAMLEngine Exception.
 */
public class EIDASSAMLEngineException extends Exception {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -8319723167019122930L;

    /**
     * Error code.
     */
    private String errorCode;//NOSONAR

    /**
     * Error message.
     */
    private String errorMessage;//NOSONAR

    /**
     * Error additional information.
     */
    private String additionalInformation;

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code null}
     *              value is permitted, and indicates that the cause is nonexistent or unknown.)
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
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code null}
     *                value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASSAMLEngineException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     */
    public EIDASSAMLEngineException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param errorCode    the error code
     * @param errorMessage the error message
     * @param cause        the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code null}
     *                     value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public EIDASSAMLEngineException(String errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Instantiates a new EIDASSAML engine exception.
     *
     * @param errorCode             the error code
     * @param errorMessage          the error message
     * @param additionalInformation the error additional information
     */
    public EIDASSAMLEngineException(String errorCode, String errorMessage, String additionalInformation) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.additionalInformation = additionalInformation;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public String getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

}
