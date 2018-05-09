/*
 * Copyright (c) 2017 by European Commission
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
import org.opensaml.saml.saml2.core.Issuer;

public class EidasIssuerValidator extends IssuerSchemaValidator {

    private static final String FORMAT_ALLOWED_VALUE = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

    /**
     * Constructor
     */
    public EidasIssuerValidator() {

        super();
    }

    @Override
    public void validate(Issuer issuer) throws ValidationException {

        super.validate(issuer);

        // format is optional
        if (issuer.getFormat() != null && !issuer.getFormat().equals(FORMAT_ALLOWED_VALUE)) {
                throw new ValidationException("Format has an invalid value.");
        }

    }

}
