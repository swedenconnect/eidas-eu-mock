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

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.xml.SAMLConstants;

/**
 * SAML 2.0 Core Subject.
 */
public interface Subject extends SAMLObject {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Subject";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "SubjectType";

    /** QName of the XSI type. */
    public static final QName TYPE_NAME = new QName(SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);

    /**
     * Gets the base identifier of the principal for this request.
     * 
     * @return the base identifier of the principal for this request
     */
    public BaseID getBaseID();

    /**
     * Sets the base identifier of the principal for this request.
     * 
     * @param newBaseID the base identifier of the principal for this request
     */
    public void setBaseID(BaseID newBaseID);

    /**
     * Gets the name identifier of the principal for this request.
     * 
     * @return the name identifier of the principal for this request
     */
    public NameID getNameID();

    /**
     * Sets the name identifier of the principal for this request.
     * 
     * @param newNameID the name identifier of the principal for this request
     */
    public void setNameID(NameID newNameID);

    /**
     * Gets the encrypted name identifier of the principal for this request.
     * 
     * @return the encrypted name identifier of the principal for this request
     */
    public EncryptedID getEncryptedID();

    /**
     * Sets the encrypted name identifier of the principal for this request.
     * 
     * @param newEncryptedID the new encrypted name identifier of the principal for this request
     */
    public void setEncryptedID(EncryptedID newEncryptedID);

    /**
     * Gets the confirmations made about this subject.
     * 
     * @return the confirmations made about this subject
     */
    public List<SubjectConfirmation> getSubjectConfirmations();
}