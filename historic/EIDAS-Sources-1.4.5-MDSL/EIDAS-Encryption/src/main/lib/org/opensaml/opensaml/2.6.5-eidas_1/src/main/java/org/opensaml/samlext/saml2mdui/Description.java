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

package org.opensaml.samlext.saml2mdui;

import javax.xml.namespace.QName;

import org.opensaml.saml2.metadata.LocalizedString;

/**
 * DisplayName.
 *
 * See IdP Discovery and Login UI Metadata Extension Profile.
 *
 * @author Rod Widdowson August 2010
 * 
 * Reflects the Description in the IdP Discovery and Login UI Metadata Extension Profile.
 * 
 */
public interface Description extends LocalizedName {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Description";
    
    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(UIInfo.MDUI_NS, 
            DEFAULT_ELEMENT_LOCAL_NAME, UIInfo.MDUI_PREFIX);
    
    /**
     * Gets the description of the EndPoint.
     * 
     * @return the description of the EndPoint
     */
    public LocalizedString getDescription();
    
    /**
     * Sets the description of the EndPoint.
     * 
     * @param newDescription description of the EndPoint
     */
    public void setDescription(LocalizedString newDescription);
}