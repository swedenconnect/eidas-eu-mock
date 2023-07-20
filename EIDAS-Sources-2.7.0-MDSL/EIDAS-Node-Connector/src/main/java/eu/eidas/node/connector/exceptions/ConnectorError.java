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
package eu.eidas.node.connector.exceptions;

import eu.eidas.auth.commons.exceptions.AbstractEIDASException;

/**
 * ConnectorError
 * Error class to hold all exceptions thrown in Connector
 *
 * @see AbstractEIDASException
 */
public class ConnectorError extends AbstractEIDASException {

    public ConnectorError(final String errorCode, final String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ConnectorError(String errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public ConnectorError(String errorCode, Throwable cause) {
        super(errorCode, cause.getMessage());
    }

    public ConnectorError(String errorCode, String errorMessage, String additionalInformation) {
        super(errorCode, errorMessage, additionalInformation);
    }

    public ConnectorError(String errorCode, String errorMessage, String additionalInformation, Throwable cause) {
        super(errorCode, errorMessage, additionalInformation, cause);
    }

}
