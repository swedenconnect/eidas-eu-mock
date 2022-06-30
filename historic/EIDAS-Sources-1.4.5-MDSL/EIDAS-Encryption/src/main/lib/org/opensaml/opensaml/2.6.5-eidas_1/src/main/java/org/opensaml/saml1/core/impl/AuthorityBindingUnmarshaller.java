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

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml1.core.AuthorityBinding;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;

/**
 * A thread-safe {@link org.opensaml.xml.io.Unmarshaller} for {@link org.opensaml.saml1.core.AuthorityBinding} objects.
 */
public class AuthorityBindingUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {

        AuthorityBinding authorityBinding = (AuthorityBinding) samlObject;

        if (AuthorityBinding.AUTHORITYKIND_ATTRIB_NAME.equals(attribute.getLocalName())) {
            authorityBinding.setAuthorityKind(XMLHelper.getAttributeValueAsQName(attribute));
        } else if (AuthorityBinding.LOCATION_ATTRIB_NAME.equals(attribute.getLocalName())) {
            authorityBinding.setLocation(attribute.getValue());
        } else if (AuthorityBinding.BINDING_ATTRIB_NAME.equals(attribute.getLocalName())) {
            authorityBinding.setBinding(attribute.getValue());
        } else {
            super.processAttribute(samlObject, attribute);
        }
    }
}