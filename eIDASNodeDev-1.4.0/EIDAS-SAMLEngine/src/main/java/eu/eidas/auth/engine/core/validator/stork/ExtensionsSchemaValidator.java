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

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.saml2.common.Extensions;
import org.opensaml.xml.ElementExtensibleXMLObject;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

import eu.eidas.auth.engine.core.stork.QAAAttribute;

/**
 * The Class ExtensionsSchemaValidator.
 *
 * @author fjquevedo
 */
public class ExtensionsSchemaValidator implements Validator<Extensions> {


    /**
     * validate the extensions.
     *
     * @param extensions the extensions
     *
     * @throws ValidationException the validation exception
     */
    public final void validate(final Extensions extensions)
    throws ValidationException {
	if (extensions.getUnknownXMLObjects() == null
		|| extensions.getUnknownXMLObjects().isEmpty()) {
	    throw new ValidationException("Extension element is empty or not exist.");
	}
    //Unknown elements are unmarshalled as XSAnyImpl type objects
	for(Object element: extensions.getUnknownXMLObjects()){
        if(element instanceof XSAnyImpl){
            throw new ValidationException(
                    "Extensions element is not valid: "+((XSAnyImpl) element).getElementQName());
        }
    }
	List<XMLObject> qaa = extensions.getUnknownXMLObjects(QAAAttribute.DEF_ELEMENT_NAME);

	if (qaa.size() == 1 && qaa.get(0) instanceof QAAAttribute) {
		final Validator<QAAAttribute> validatorQaa = new QAAAttributeSchemaValidator();
		validatorQaa.validate((QAAAttribute) qaa.get(0));
	} else {
		throw new ValidationException(
	    "Extensions must contain only one element QAALevel.");
	}
    checkChildren(extensions, QAAAttribute.DEF_ELEMENT_NAME);


    }
    private void checkChildren(ElementExtensibleXMLObject parent, QName childQName) throws ValidationException {
        if (parent.getUnknownXMLObjects(childQName).isEmpty()) {
            throw new ValidationException(childQName + " must be present");
        }

    }
}
