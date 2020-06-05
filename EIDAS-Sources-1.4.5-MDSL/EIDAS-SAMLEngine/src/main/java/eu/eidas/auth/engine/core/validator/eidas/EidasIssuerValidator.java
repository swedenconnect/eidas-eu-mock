/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.auth.engine.core.validator.eidas;

import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.validator.IssuerSchemaValidator;
import org.opensaml.xml.validation.ValidationException;



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
