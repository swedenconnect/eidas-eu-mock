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

package org.opensaml.samlext.saml2mdquery.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opensaml.saml2.metadata.NameIDFormat;
import org.opensaml.saml2.metadata.impl.RoleDescriptorImpl;
import org.opensaml.samlext.saml2mdquery.QueryDescriptorType;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSBooleanValue;
import org.opensaml.xml.util.XMLObjectChildrenList;

/**
 * Concrete implementation of {@link QueryDescriptorType}.
 */
public abstract class QueryDescriptorTypeImpl extends RoleDescriptorImpl implements QueryDescriptorType {

    /** WantAssertionSigned attribute value. */
    private XSBooleanValue wantAssertionsSigned;
    
    /** Supported NameID formats. */
    private XMLObjectChildrenList<NameIDFormat> nameIDFormats;
    
    /**
     * Constructor.
     * 
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected QueryDescriptorTypeImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
        
        nameIDFormats = new XMLObjectChildrenList<NameIDFormat>(this);
    }
    
    /** {@inheritDoc} */
    public Boolean getWantAssertionsSigned() {
        if (wantAssertionsSigned != null) {
            return wantAssertionsSigned.getValue();
        }
        return Boolean.FALSE;
    }

    /** {@inheritDoc} */
    public void setWantAssertionsSigned(Boolean newWantAssertionsSigned) {
        if (newWantAssertionsSigned != null) {
            wantAssertionsSigned = prepareForAssignment(wantAssertionsSigned, 
                    new XSBooleanValue(newWantAssertionsSigned, false));
        } else {
            wantAssertionsSigned = prepareForAssignment(wantAssertionsSigned, null);
        }
    }

    /** {@inheritDoc} */
    public XSBooleanValue getWantAssertionsSignedXSBoolean(){
        return wantAssertionsSigned;
    }
    
    /** {@inheritDoc} */
    public void setWantAssertionsSigned(XSBooleanValue wantAssertionSigned){
        this.wantAssertionsSigned = prepareForAssignment(this.wantAssertionsSigned, wantAssertionSigned);
    }
    
    /** {@inheritDoc} */
    public List<NameIDFormat> getNameIDFormat(){
        return nameIDFormats;
    }
    
    /** {@inheritDoc} */
    public List<XMLObject> getOrderedChildren() {
        ArrayList<XMLObject> children = new ArrayList<XMLObject>();
        
        children.addAll(super.getOrderedChildren());
        children.addAll(nameIDFormats);
        
        return Collections.unmodifiableList(children);
    }
}