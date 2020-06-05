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

import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.core.eidas.RequestedAttribute;


public class EidasRequestedAttributeValidator implements Validator<RequestedAttribute> {

    /**
     * The Constant LOG.
     */
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EidasRequestedAttributeValidator.class.getName());

    public EidasRequestedAttributeValidator() {
        super();
    }

    public void validate(org.opensaml.saml2.metadata.RequestedAttribute attribute) throws ValidationException {
        if (attribute instanceof RequestedAttribute) {
            validate((RequestedAttribute) attribute);
        }
    }

    public void validate(RequestedAttribute attr) throws ValidationException {
        LOG.info("Validating the attribute " + attr.getName());

        if (attr.getName() == null) {

            throw new ValidationException("Name is required.");
        }

        if (attr.getNameFormat() == null) {

            throw new ValidationException("NameFormat is required.");
        }

    }

}
