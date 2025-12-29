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

import eu.eidas.auth.commons.exceptions.AbstractEIDASException;

/**
 * The Class EIDASSAMLEngineRuntimeException.
 *
 * @see AbstractEIDASException
 */
public class EIDASSAMLEngineRuntimeException extends AbstractEIDASException {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 5829810358581493517L;

    /**
     * Instantiates a new EIDASSAMLEngineRuntimeException engine exception.
     *
     * @param wrappedException the wrapped exception
     */
    public EIDASSAMLEngineRuntimeException(final Exception wrappedException) {
        super(wrappedException);
    }

    /**
     * Instantiates a new EIDASSAMLEngineRuntimeException engine exception.
     *
     * @param errorCode             the error code
     * @param errorMessage          the error message
     * @param additionalInformation the error additional information
     */
    public EIDASSAMLEngineRuntimeException(final String errorCode, final String errorMessage, final String additionalInformation) {
        super(errorCode, errorMessage, additionalInformation);
    }

}