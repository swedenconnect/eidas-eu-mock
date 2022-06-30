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

import org.opensaml.common.xml.SAMLConstants;

/**
 * SAML 2.0 Core AuthzDecisionStatement.
 */
public interface AuthzDecisionStatement extends Statement {

    /** Element local name. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "AuthzDecisionStatement";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(SAMLConstants.SAML20_NS, DEFAULT_ELEMENT_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "AuthzDecisionStatementType";

    /** QName of the XSI type. */
    public static final QName TYPE_NAME = new QName(SAMLConstants.SAML20_NS, TYPE_LOCAL_NAME,
            SAMLConstants.SAML20_PREFIX);

    /** Resource attribute name. */
    public static final String RESOURCE_ATTRIB_NAME = "Resource";

    /** Decision attribute name. */
    public static final String DECISION_ATTRIB_NAME = "Decision";

    /**
     * Get URI of the resource to which authorization is saught.
     * 
     * @return URI of the resource to which authorization is saught
     */
    public String getResource();

    /**
     * Sets URI of the resource to which authorization is saught.
     * 
     * @param newResourceURI URI of the resource to which authorization is saught
     */
    public void setResource(String newResourceURI);

    /**
     * Gets the decision of the authorization request.
     * 
     * @return the decision of the authorization request
     */
    public DecisionTypeEnumeration getDecision();

    /**
     * Sets the decision of the authorization request.
     * 
     * @param newDecision the decision of the authorization request
     */
    public void setDecision(DecisionTypeEnumeration newDecision);

    /**
     * Gets the actions authorized to be performed.
     * 
     * @return the actions authorized to be performed
     */
    public List<Action> getActions();

    /**
     * Get the SAML assertion the authority relied on when making the authorization decision.
     * 
     * @return the SAML assertion the authority relied on when making the authorization decision
     */
    public Evidence getEvidence();

    /**
     * Sets the SAML assertion the authority relied on when making the authorization decision.
     * 
     * @param newEvidence the SAML assertion the authority relied on when making the authorization decision
     */
    public void setEvidence(Evidence newEvidence);
}