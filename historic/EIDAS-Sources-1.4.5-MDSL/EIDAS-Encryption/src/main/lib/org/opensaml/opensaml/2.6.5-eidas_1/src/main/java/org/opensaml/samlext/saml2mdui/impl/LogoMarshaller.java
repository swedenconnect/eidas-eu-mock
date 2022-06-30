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

package org.opensaml.samlext.saml2mdui.impl;

import org.opensaml.common.impl.AbstractSAMLObjectMarshaller;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.samlext.saml2mdui.Logo;
import org.opensaml.xml.LangBearing;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * A thread safe Marshaller for {@link org.opensaml.samlext.saml2mdui.Logo} objects.
 */
public class LogoMarshaller extends AbstractSAMLObjectMarshaller {

    /**
     * {@inheritDoc}
     */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        Logo logo = (Logo) samlObject;

        if (logo.getXMLLang() != null) {
            Attr attribute = XMLHelper.constructAttribute(domElement.getOwnerDocument(), SAMLConstants.XML_NS,
                    LangBearing.XML_LANG_ATTR_LOCAL_NAME, SAMLConstants.XML_PREFIX);
            attribute.setValue(logo.getXMLLang());
            domElement.setAttributeNodeNS(attribute);
        }
        if (logo.getHeight() != null) {
            domElement.setAttributeNS(null, Logo.HEIGHT_ATTR_NAME, logo.getHeight().toString());
        }
        if (logo.getWidth() != null) {
            domElement.setAttributeNS(null, Logo.WIDTH_ATTR_NAME, logo.getWidth().toString());
        }
    }

    /** {@inheritDoc} */
    protected void marshallElementContent(XMLObject samlObject, Element domElement) throws MarshallingException {
        Logo logo = (Logo) samlObject;

        if (logo.getURL() != null) {
            XMLHelper.appendTextContent(domElement, logo.getURL());
        }
    }
}