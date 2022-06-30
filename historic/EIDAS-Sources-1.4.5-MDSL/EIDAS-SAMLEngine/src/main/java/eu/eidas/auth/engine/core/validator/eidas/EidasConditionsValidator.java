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

import org.joda.time.DateTime;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.validator.ConditionsSpecValidator;
import org.opensaml.xml.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EidasConditionsValidator extends ConditionsSpecValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EidasConditionsValidator.class.getName());

    /**
     * Constructor
     */
    public EidasConditionsValidator() {

        super();
    }

    @Override
    public void validate(Conditions conditions) throws ValidationException {
        LOG.debug("conditions.getNotBefore() "+ conditions.getNotBefore());
        LOG.debug("conditions.getNotOnOrAfter() "+ conditions.getNotOnOrAfter());

        super.validate(conditions);

        if (conditions.getNotBefore() == null) {

            throw new ValidationException("NotBefore is required.");
        }

        if (conditions.getNotOnOrAfter() == null) {

            throw new ValidationException("NotOnOrAfter is required.");
        }

        if (conditions.getAudienceRestrictions() == null || conditions.getAudienceRestrictions().isEmpty()) {

            throw new ValidationException("AudienceRestriction is required.");
        }

    }

}

