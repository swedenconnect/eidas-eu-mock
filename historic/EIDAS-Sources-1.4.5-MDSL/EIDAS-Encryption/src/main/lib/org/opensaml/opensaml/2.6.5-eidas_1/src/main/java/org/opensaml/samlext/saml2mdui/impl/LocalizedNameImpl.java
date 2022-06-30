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

import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.samlext.saml2mdui.LocalizedName;
import org.opensaml.xml.LangBearing;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.util.DatatypeHelper;

/**
 * Concrete implementation of {@link org.opensaml.samlext.saml2mdui.LocalizedName}.
 */
public class LocalizedNameImpl extends AbstractSAMLObject implements LocalizedName {

    /** Display name. */
    private LocalizedString name;

    /**
     * Constructor.
     * 
     * @param namespaceURI the namespaceURI
     * @param elementLocalName the elementLocalName
     * @param namespacePrefix the namespacePrefix
     */
    protected LocalizedNameImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public LocalizedString getName() {
        return name;
    }

    /** {@inheritDoc} */
    public void setName(LocalizedString newName) {
        name = prepareForAssignment(name, newName);
        boolean hasXMLLang = false;
        if (name != null && !DatatypeHelper.isEmpty(name.getLanguage())) {
            hasXMLLang = true;
        }
        manageQualifiedAttributeNamespace(LangBearing.XML_LANG_ATTR_NAME, hasXMLLang);
    }

    /** {@inheritDoc} */
    public String getXMLLang() {
        return name.getLanguage();
    }

    /** {@inheritDoc} */
    public void setXMLLang(String newLang) {
        name.setLanguage(newLang);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        return null;
    }

}