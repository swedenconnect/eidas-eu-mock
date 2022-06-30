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

package org.opensaml.saml1.core.impl;

import java.util.List;

import org.opensaml.common.impl.AbstractSAMLObject;
import org.opensaml.saml1.core.AttributeDesignator;
import org.opensaml.xml.XMLObject;

/**
 * Concrete Implementation of the {@link org.opensaml.saml1.core.AttributeDesignator} interface.
 */
public class AttributeDesignatorImpl extends AbstractSAMLObject implements AttributeDesignator {

    /** Contains the AttributeName */
    private String attributeName;

    /** Contains the AttributeNamespace */
    private String attributeNamespace;

    /**
     * Constructor
     * 
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected AttributeDesignatorImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public String getAttributeName() {
        return attributeName;
    }

    /** {@inheritDoc} */
    public void setAttributeName(String attributeName) {
        this.attributeName = prepareForAssignment(this.attributeName, attributeName);
    }

    /** {@inheritDoc} */
    public String getAttributeNamespace() {
        return attributeNamespace;
    }

    /** {@inheritDoc} */
    public void setAttributeNamespace(String attributeNamespace) {
        this.attributeNamespace = prepareForAssignment(this.attributeNamespace, attributeNamespace);
    }

    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}