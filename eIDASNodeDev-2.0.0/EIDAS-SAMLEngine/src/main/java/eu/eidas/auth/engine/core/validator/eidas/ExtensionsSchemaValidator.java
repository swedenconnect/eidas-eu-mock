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
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import org.opensaml.saml.saml2.core.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (element instanceof XSAnyImpl) {
                LOG.debug("ExtensionsSchemaValidator validation "+ ((XSAnyImpl) element).getElementQName());
                throw new ValidationException(
                        "Extensions element is not valid: " + ((XSAnyImpl) element).getElementQName());
            }
        }


    }
}
