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

package eu.eidas.auth.engine.core.eidas.impl;


import eu.eidas.auth.engine.core.eidas.RequestedAttribute;
import eu.eidas.auth.engine.core.eidas.RequestedAttributes;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectUnmarshaller;

/**
 * The Class RequestedAttributesUnmarshaller.
 *
 */
public class RequestedAttributesUnmarshaller extends
		AbstractSAMLObjectUnmarshaller {

    /**
     * Process child element.
     *
     * @param parentObject the parent object
     * @param childObject the child object
     *
     * @throws UnmarshallingException error in unmarshall
     */
    protected final void processChildElement(final XMLObject parentObject,
	    final XMLObject childObject) throws UnmarshallingException {
	final RequestedAttributes attrStatement = (RequestedAttributes) parentObject;

	if (childObject instanceof RequestedAttribute) {
	    attrStatement.getAttributes().add((RequestedAttribute) childObject);
	} else {
	    super.processChildElement(parentObject, childObject);
	}
    }

}
