/*
 * Licensed to the University Corporation for Advanced Internet Development, 
 * Inc. (UCAID) under one or more contributor license agreements.  See the 
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache 
 * License, Version 2.0 (the "License"); you may not use this file except in 
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.w3c.dom.Element;

/** Marshaller of {@link AttributeDesignator} objects. */
public class AttributeDesignatorMarshaller extends AbstractSAMLObjectMarshaller {

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlElement, Element domElement) throws MarshallingException {
        AttributeDesignator designator = (AttributeDesignator) samlElement;

        if (designator.getAttributeName() != null) {
            domElement.setAttributeNS(null, AttributeDesignator.ATTRIBUTENAME_ATTRIB_NAME, designator
                    .getAttributeName());
        }

        if (designator.getAttributeNamespace() != null) {
            domElement.setAttributeNS(null, AttributeDesignator.ATTRIBUTENAMESPACE_ATTRIB_NAME, designator
                    .getAttributeNamespace());
        }
    }

}
