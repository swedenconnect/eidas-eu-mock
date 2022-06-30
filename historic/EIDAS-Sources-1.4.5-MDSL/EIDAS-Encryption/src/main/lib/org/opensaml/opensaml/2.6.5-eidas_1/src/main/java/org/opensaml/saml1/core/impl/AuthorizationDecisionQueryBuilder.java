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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectBuilder;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.AuthorizationDecisionQuery;

/**
 * Builder of {@link org.opensaml.saml1.core.impl.AuthorizationDecisionQueryImpl} objects.
 */
public class AuthorizationDecisionQueryBuilder extends AbstractSAMLObjectBuilder<AuthorizationDecisionQuery> {

    /**
     * Constructor
     */
    public AuthorizationDecisionQueryBuilder() {

    }

    /** {@inheritDoc} */
    public AuthorizationDecisionQuery buildObject() {
        return buildObject(SAMLConstants.SAML10P_NS, AuthorizationDecisionQuery.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1P_PREFIX);
    }

    /** {@inheritDoc} */
    public AuthorizationDecisionQuery buildObject(String namespaceURI, String localName, String namespacePrefix) {
        return new AuthorizationDecisionQueryImpl(namespaceURI, localName, namespacePrefix);
    }
}