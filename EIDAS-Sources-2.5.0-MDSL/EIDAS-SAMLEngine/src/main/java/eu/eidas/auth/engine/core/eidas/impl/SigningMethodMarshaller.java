/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.auth.engine.core.eidas.impl;

import eu.eidas.auth.engine.core.eidas.SigningMethod;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.AbstractSAMLObjectMarshaller;
import org.w3c.dom.Element;

/**
 * The Class SigningMethodMarshaller.
 *
 */
public class SigningMethodMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * Marshall element content.
     *
     * @param samlObject the SAML object
     * @param domElement the DOM element
     * @throws MarshallingException the marshalling exception
     */
    protected final void marshallElementContent(final XMLObject samlObject,
	    final Element domElement) throws MarshallingException {
	    final SigningMethod method = (SigningMethod) samlObject;
        domElement.setAttribute("Algorithm", method.getAlgorithm());
    }
}