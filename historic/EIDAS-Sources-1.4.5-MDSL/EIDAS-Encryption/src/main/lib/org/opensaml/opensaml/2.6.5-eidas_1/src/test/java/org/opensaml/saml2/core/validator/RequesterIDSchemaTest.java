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
package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.RequesterID;

/**
 *
 */
public class RequesterIDSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /**
     * Constructor
     *
     */
    public RequesterIDSchemaTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML20P_NS, RequesterID.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20P_PREFIX);
        validator = new RequesterIDSchemaValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        RequesterID reqid = (RequesterID) target;
        reqid.setRequesterID("urn:string:requester");
    }
    
    /**
     *  Tests invalid element content
     */
    public void testRequesterIDFailure() {
        RequesterID reqid = (RequesterID) target;
        
        reqid.setRequesterID(null);
        assertValidationFail("RequesterID element content was null");
        
        reqid.setRequesterID("");
        assertValidationFail("RequesterID element content was empty");
        
        reqid.setRequesterID("            ");
        assertValidationFail("RequesterID element content was all whitespace");
    }
    
}
