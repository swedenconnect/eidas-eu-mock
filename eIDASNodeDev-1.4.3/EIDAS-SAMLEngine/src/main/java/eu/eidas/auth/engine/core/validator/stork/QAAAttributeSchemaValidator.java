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

import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.core.stork.QAAAttribute;

/**
 * The Class QAAAttributeSchemaValidator.
 *
 * @author
 */
public class QAAAttributeSchemaValidator implements Validator<QAAAttribute> {
    private static final Logger LOG = LoggerFactory.getLogger(QAAAttributeSchemaValidator.class);


    /**
     * Validate action.
     *
     * @param qaa the quality authentication assurance level attribute
     * @throws ValidationException the validation exception
     */
    public final void validate(final QAAAttribute qaa) throws ValidationException {
        validateAction(qaa);
    }


    /**
     * Validate action.
     *
     * @param qaaAttribute the quality authentication assurance level attribute.
     * @throws ValidationException the validation exception
     */
    protected final void validateAction(final QAAAttribute qaaAttribute)
            throws ValidationException {
        LOG.debug("validateAction");
        if (DatatypeHelper.isEmpty(qaaAttribute.getQaaLevel())) {
            throw new ValidationException("QAALevel label must be specified.");
        }

        final int qaa = Integer.parseInt(qaaAttribute.getQaaLevel());

        if (qaa < QAAAttribute.MIN_VALUE || qaa > QAAAttribute.MAX_VALUE) {
            throw new ValidationException(
                    "QAALevel label must be greater than 0.");
        }
    }

}
