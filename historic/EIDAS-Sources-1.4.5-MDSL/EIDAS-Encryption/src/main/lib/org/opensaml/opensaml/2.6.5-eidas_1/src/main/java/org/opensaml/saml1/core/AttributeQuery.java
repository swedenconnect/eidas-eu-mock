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
package org.opensaml.saml1.core;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.common.xml.SAMLConstants;

/**
 * Description of the behaviour of the <code> AttributeQuery </code> element
 */
public interface AttributeQuery extends SubjectQuery {

    /** Element name, no namespace. */
    public final static String DEFAULT_ELEMENT_LOCAL_NAME = "AttributeQuery";
    
    /** Default element name */
    public final static QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML10P_NS, DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    
    /** Local name of the XSI type */
    public final static String TYPE_LOCAL_NAME = "AttributeQueryType"; 
        
    /** QName of the XSI type */
    public final static QName TYPE_NAME = new QName(SAMLConstants.SAML10P_NS, TYPE_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    
    /** AuthenticationMethod attribute name */
    public final static String RESOURCE_ATTRIB_NAME = "Resource"; 

    /** Get list of AttributeDesignators */
    public List<AttributeDesignator> getAttributeDesignators();
    
    /** Get Resource attribute */
    public String getResource();
    
    /** Set Resource attribute */
    public void setResource(String resource);

}
