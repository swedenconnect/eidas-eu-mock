/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.commons.exceptions;

/**
 * Security eIDAS Exception class.
 *
 */
public final class EidasNodeException extends AbstractEIDASException {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 8048033129798427574L;

    /**
     * Exception Constructor with two Strings representing the errorCode and
     * errorMessage as parameters.
     *
     * @param errorCode The error code value.
     * @param errorMsg  The error message value.
     */
    public EidasNodeException(final String errorCode, final String errorMsg) {
        super(errorCode, errorMsg);
    }


    public EidasNodeException(final String errorCode, final String errorMsg, Throwable cause) {
        super(errorCode, errorMsg, cause);
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage() {

        return "Security Error (" + this.getErrorCode() + ") processing request : "
                + this.getErrorMessage();
    }

}
