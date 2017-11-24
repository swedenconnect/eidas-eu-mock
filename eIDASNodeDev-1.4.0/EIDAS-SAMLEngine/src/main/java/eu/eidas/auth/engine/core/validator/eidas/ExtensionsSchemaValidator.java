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

import org.opensaml.saml2.common.Extensions;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.core.stork.impl.QAAAttributeImpl;

/**
 * The Class ExtensionsSchemaValidator for eIDAS format.
 */
public class ExtensionsSchemaValidator implements Validator<Extensions> {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(ExtensionsSchemaValidator.class.getName());
    /**
     * validate the extensions.
     *
     * @param extensions the extensions
     * @throws ValidationException the validation exception
     */
    public final void validate(final Extensions extensions) throws ValidationException {
        LOG.debug("ExtensionsSchemaValidator validation");
        if (extensions.getUnknownXMLObjects() == null
                || extensions.getUnknownXMLObjects().isEmpty()) {
            throw new ValidationException("Extension element is empty or not exist.");
        }
        //Unknown elements are unmarshalled as XSAnyImpl type objects
        for (Object element : extensions.getUnknownXMLObjects()) {
            LOG.debug("element ClassName " + element.getClass().toString());
            if (element instanceof QAAAttributeImpl){
                throw new ValidationException("QAA Level attribute is the STORK 1 attribute");
            }
            if (element instanceof XSAnyImpl) {
                LOG.debug("ExtensionsSchemaValidator validation "+ ((XSAnyImpl) element).getElementQName());
                throw new ValidationException(
                        "Extensions element is not valid: " + ((XSAnyImpl) element).getElementQName());
            }
        }


    }
}
