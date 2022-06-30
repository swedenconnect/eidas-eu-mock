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

/**
 * 
 */

package org.opensaml.saml2.core.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml2.core.AuthnContextDecl;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml2.core.AuthnContextDecl}.
 */
public class AuthnContextDeclUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentXMLObject, XMLObject childXMLObject)
            throws UnmarshallingException {
        AuthnContextDecl authnCtcDecl = (AuthnContextDecl) parentXMLObject;

        authnCtcDecl.getUnknownXMLObjects().add(childXMLObject);
    }

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject xmlObject, Attr attribute) throws UnmarshallingException {
        AuthnContextDecl authnCtcDecl = (AuthnContextDecl) xmlObject;

        QName attribQName = XMLHelper.constructQName(attribute.getNamespaceURI(), attribute.getLocalName(), attribute
                .getPrefix());

        if (attribute.isId()) {
            authnCtcDecl.getUnknownAttributes().registerID(attribQName);
        }

        authnCtcDecl.getUnknownAttributes().put(attribQName, attribute.getValue());
    }

    /** {@inheritDoc} */
    protected void processElementContent(XMLObject xmlObject, String elementContent) {
        AuthnContextDecl authnCtcDecl = (AuthnContextDecl) xmlObject;

        authnCtcDecl.setTextContent(elementContent);
    }
}