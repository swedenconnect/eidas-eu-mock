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

/**
 * A type safe enumeration of {@link org.opensaml.saml2.core.AuthzDecisionStatement} decision types.
 */
public final class DecisionTypeEnumeration {

    /** Permit decision type. */
    public static final DecisionTypeEnumeration PERMIT = new DecisionTypeEnumeration("Permit");

    /** Deny decision type. */
    public static final DecisionTypeEnumeration DENY = new DecisionTypeEnumeration("Deny");

    /** Indeterminate decision type. */
    public static final DecisionTypeEnumeration INDETERMINATE = new DecisionTypeEnumeration("Indeterminate");

    /** The decision type string. */
    private String decisionType;

    /**
     * Constructor.
     * 
     * @param newDecisionType the decision type string
     */
    protected DecisionTypeEnumeration(String newDecisionType) {
        this.decisionType = newDecisionType;
    }

    /** {@inheritDoc} */
    public String toString() {
        return decisionType;
    }
}