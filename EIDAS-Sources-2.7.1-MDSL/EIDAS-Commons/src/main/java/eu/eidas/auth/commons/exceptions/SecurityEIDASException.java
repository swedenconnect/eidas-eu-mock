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

/**
 * Security eIDAS Node Exception class.
 *
 * @see AbstractEIDASException
 */
public final class SecurityEIDASException extends AbstractEIDASException {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 5605743302478554967L;

    /**
     * Exception Constructor with two Strings representing the errorCode and
     * errorMessage as parameters.
     *
     * @param errorCode    The error code value.
     * @param errorMessage The error message value.
     */
    public SecurityEIDASException(final String errorCode, final String errorMessage) {
        super(errorCode, errorMessage);
    }

    /**
     * Exception Constructor with three Strings representing the errorCode, errorMessage and additionalInformation as
     * parameters.
     *
     * @param errorCode             The error code value.
     * @param errorMessage          The error code message value.
     * @param additionalInformation The error additional information.
     */
    public SecurityEIDASException(final String errorCode, final String errorMessage, final String additionalInformation) {
        super(errorCode, errorMessage, additionalInformation);
    }

    /**
     * Exception Constructor with two Strings representing the errorCode and
     * errorMessage as parameters and the Throwable cause.
     *
     * @param errorCode    The error code value.
     * @param errorMessage The error message value.
     * @param cause        The throwable object.
     */
    public SecurityEIDASException(final String errorCode, final String errorMessage, final Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

}
