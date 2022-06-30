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

package org.opensaml.saml2.core;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;

/**
 * SAML 2.0 Core AssertionURIRef.
 */
public interface AssertionURIRef extends SAMLObject, Evidentiary {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "AssertionURIRef";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);

    /**
     * Gets the URI of the assertion this references.
     * 
     * @return the URI of the assertion this references
     */
    public String getAssertionURI();

    /**
     * Sets the URI of the assertion this references.
     * 
     * @param newURI the URI of the assertion this references
     */
    public void setAssertionURI(String newURI);
}