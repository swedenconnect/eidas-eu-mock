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

import org.opensaml.common.SAMLObject;
import org.opensaml.saml2.metadata.LocalizedString;
import org.opensaml.xml.LangBearing;

/**
 * LocalizedName.
 */
public interface LocalizedName extends SAMLObject, LangBearing {

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "localizedNameType"; 
        
    /**
     * Gets the name.
     * 
     * @return the name
     */
    public LocalizedString getName();
    
    /**
     * Sets the  name.
     * 
     * @param newName new name
     */
    public void setName(LocalizedString newName);
}
