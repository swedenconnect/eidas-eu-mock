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

package eu.eidas.auth.engine.core.validator.stork;

import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.validator.AssertionSchemaValidator;
import org.opensaml.xml.validation.ValidationException;

/**
 * The Class ExtensionsSchemaValidator.
 *
 * @author cph
 */
public class StorkAssertionValidator extends AssertionSchemaValidator {
    /**
     * Constructor
     */
    public StorkAssertionValidator() {

        super();
    }

    @Override
    public void validate(Assertion assertion) throws ValidationException {

        super.validate(assertion);

        if (assertion.getID() == null) {

            throw new ValidationException("ID is required.");
        }

        if (assertion.getVersion() == null || !assertion.getVersion().equals(SAMLVersion.VERSION_20)) {

            throw new ValidationException("Version of assertion not present or invalid.");
        }

        if (assertion.getIssueInstant() == null) {

            throw new ValidationException("IssueInstant is required.");
        }

        if (assertion.getSubject() == null) {

            throw new ValidationException("Subject is required.");
        }

        if (assertion.getConditions() == null) {

            throw new ValidationException("Conditions is required.");
        }

        if (assertion.getAuthnStatements() == null ||
                assertion.getAuthnStatements().size() != 1) {

            throw new ValidationException("Incorrect number of AuthnStatements.");
        }

        if (assertion.getAttributeStatements() != null &&
                !assertion.getAttributeStatements().isEmpty() &&
                assertion.getAttributeStatements().size() != 1) {
                throw new ValidationException("Incorrect number of AttributeStatements.");
        }

    }


}
