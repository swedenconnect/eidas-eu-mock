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

package org.opensaml.common.binding.security;

import org.opensaml.common.binding.SAMLMessageContext;
import org.opensaml.security.MetadataCriteria;
import org.opensaml.ws.message.MessageContext;
import org.opensaml.ws.security.SecurityPolicyException;
import org.opensaml.ws.security.provider.CertificateNameOptions;
import org.opensaml.ws.security.provider.ClientCertAuthRule;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.trust.TrustEngine;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAML specialization of {@link ClientCertAuthRule} which provides support for X509Credential trust engine validation
 * based on SAML metadta.
 */
public class SAMLMDClientCertAuthRule extends ClientCertAuthRule {

    /** Logger. */
    private final Logger log = LoggerFactory.getLogger(SAMLMDClientCertAuthRule.class);

    /**
     * Constructor.
     * 
     * @param engine Trust engine used to verify the request X509Credential
     * @param nameOptions options for deriving issuer names from an X.509 certificate
     */
    public SAMLMDClientCertAuthRule(TrustEngine<X509Credential> engine, CertificateNameOptions nameOptions) {
        super(engine, nameOptions);
    }

    /** {@inheritDoc} */
    protected CriteriaSet buildCriteriaSet(String entityID, MessageContext messageContext) 
        throws SecurityPolicyException {
        
        if (!(messageContext instanceof SAMLMessageContext)) {
            log.error("Supplied message context was not an instance of SAMLMessageContext, can not build criteria set from SAML metadata parameters");
            throw new SecurityPolicyException("Supplied message context was not an instance of SAMLMessageContext");
        }
        
        SAMLMessageContext samlContext = (SAMLMessageContext) messageContext;

        CriteriaSet criteriaSet = super.buildCriteriaSet(entityID, messageContext);
        MetadataCriteria mdCriteria = 
            new MetadataCriteria(samlContext.getPeerEntityRole(), samlContext.getInboundSAMLProtocol());
        criteriaSet.add(mdCriteria);

        return criteriaSet;
    }
}