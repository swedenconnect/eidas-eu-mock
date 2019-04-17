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

package eu.eidas.auth.engine.core.eidas.impl;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.w3c.dom.Attr;

import eu.eidas.auth.engine.core.eidas.SigningMethod;

/**
 * The Class SigningMethodUnmarshaller.
 */
public class SigningMethodUnmarshaller extends AbstractSAMLObjectUnmarshaller {


    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        if("Algorithm".equalsIgnoreCase(attribute.getName())) {
            final SigningMethod method = (SigningMethod) samlObject;
            method.setAlgorithm(attribute.getValue());
        }
    }
}