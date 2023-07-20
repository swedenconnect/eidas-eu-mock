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
package eu.eidas.node.exceptions;

import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;

/**
 * This exception is thrown when SAML failure response must be generated.
 *
 * @see AbstractEIDASException
 */
public class SamlFailureResponseException extends AbstractEIDASException {

    private final String statusCode;
    private final String relayState;
    private final StoredAuthenticationRequest storedRequest;

    public SamlFailureResponseException(final String errorCode, final String errorMessage, String statusCode, String relayState, StoredAuthenticationRequest storedRequest) {
        super(errorCode, errorMessage);
        this.statusCode = statusCode;
        this.relayState = relayState;
        this.storedRequest = storedRequest;
    }

    public SamlFailureResponseException(String errorCode, String errorMessage, String statusCode, String relayState) {
        this(errorCode, errorMessage, statusCode, relayState, null);
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getRelayState() {
        return relayState;
    }

    public StoredAuthenticationRequest getStoredRequest() {
        return storedRequest;
    }

}
