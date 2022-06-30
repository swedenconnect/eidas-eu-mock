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
 * Invalid session Exception class.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com, hugo.magalhaes@multicert.com,
 *         paulo.ribeiro@multicert.com
 * @version $Revision: 1.14 $, $Date: 2010-11-17 05:15:28 $
 * @see eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException
 */
public class InvalidSAMLResponseException extends AbstractEIDASException {

    /**
     * Unique identifier.
     */
    private static final long serialVersionUID = 1328100834655923156L;

    /**
     * Exception Constructor with two Strings representing the errorCode and
     * errorMessage as parameters.
     *
     * @param errorCode    The error code value.
     * @param errorMessage The error message value.
     */
    public InvalidSAMLResponseException(final String errorCode,
                                        final String errorMessage) {

        super(errorCode, errorMessage);
    }

    /**
     * {@inheritDoc}
     */
    public final String getMessage() {

        return "InvalidSAMLResponseException Error (" + this.getErrorCode() + ") processing request : "
                + this.getErrorMessage();
    }
}
