/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.engine.exceptions.ValidationException;
import org.opensaml.saml.saml2.core.AuthnRequest;

/**
 * TODO This class was created as a temporary solution for the removal of AuthnRequestSchemaValidator from opensaml 3.
 */
public abstract class AuthnRequestSchemaValidator {

    /**
     * Constructor
     */
    public AuthnRequestSchemaValidator() {
        super();
    }

    /**
     * Validates the given authentication request.
     *
     * @param request the authentication request to be validated
     * @throws ValidationException if the validation fails
     */
    public void validate(AuthnRequest request) throws ValidationException {
    }
}