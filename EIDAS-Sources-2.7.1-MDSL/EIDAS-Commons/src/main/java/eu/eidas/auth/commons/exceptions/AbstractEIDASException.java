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
package eu.eidas.auth.commons.exceptions;

import java.io.Serializable;
import java.util.UUID;

/**
 * Abstract class to represent the various eIDAS Node exceptions.
 */
@SuppressWarnings("AbstractClassExtendsConcreteClass")
public abstract class AbstractEIDASException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = -1884417567740138022L;

    private String errorCode;
    private String errorMessage;
    private String additionalInformation;
    private final String errorId;
    @Deprecated
    private String userErrorCode;
    @Deprecated
    private String userErrorMessage;

    /**
     * Exception Constructor with three Strings representing the errorCode, errorMessage, additionalInformation
     * and the Throwable cause.
     *
     * @param errorCode             The error errorCode value.
     * @param errorMessage          The error errorMessage value.
     * @param additionalInformation The error additional information value.
     * @param cause                 The throwable object.
     */
    protected AbstractEIDASException(String errorCode, String errorMessage, String additionalInformation, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.additionalInformation = additionalInformation;
        errorId = UUID.randomUUID().toString();
    }

    protected AbstractEIDASException(String errorCode, String errorMessage) {
        this(errorCode, errorMessage, null, null);
    }

    protected AbstractEIDASException(String errorCode, String errorMessage, Throwable cause) {
        this(errorCode, errorMessage, null, cause);
    }

    protected AbstractEIDASException(String errorCode, String errorMessage, String additionalInformation) {
        this(errorCode, errorMessage, additionalInformation, null);
    }

    protected AbstractEIDASException(String additionalInformation) {
        this(null, null, additionalInformation, null);
    }

    public AbstractEIDASException(Throwable cause) {
        this(null, null, null, cause);
    }

    @Deprecated
    protected AbstractEIDASException(String errorCode, String errorMessage, Throwable cause,
                                     String userErrorCodeArg,
                                     String userErrorMessageArg) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.userErrorCode = userErrorCodeArg;
        this.userErrorMessage = userErrorMessageArg;
        errorId = UUID.randomUUID().toString();
    }

    public final String getErrorCode() {
        return errorCode;
    }

    public final void setErrorCode(final String code) {
        this.errorCode = code;
    }

    public final String getErrorMessage() {
        return errorMessage;
    }

    public final void setErrorMessage(final String message) {
        this.errorMessage = message;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public String getErrorId() {
        return errorId;
    }

    @Deprecated
    public final String getUserErrorCode() {
        return userErrorCode;
    }

    @Deprecated
    public final String getUserErrorMessage() {
        return userErrorMessage;
    }

}
