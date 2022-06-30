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

package org.opensaml.saml1.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml1.core.Action;

/**
 * Test case for {@link org.opensaml.saml1.core.validator.ActionSpecValidator}.
 */
public class ActionSpecTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public ActionSpecTest() {
        super();
        targetQName = new QName(SAMLConstants.SAML1_NS, Action.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML1_PREFIX);
        validator = new ActionSpecValidator();
    }

    /** {@inheritDoc} */
    protected void populateRequiredData() {
        super.populateRequiredData();
        
        Action action = (Action) target;
        action.setContents("data and other cool stuff");
    }
    
    public void testMissingContents(){
        Action action = (Action) target;
        action.setContents(null);
        assertValidationFail("Contents null, should raise a Validation Exception");

        action.setContents("");
        assertValidationFail("Contents empty, should raise a Validation Exception");

        action.setContents("  ");
        assertValidationFail("Contents whitespace, should raise a Validation Exception");
    }
    
    // No children so noi version mismatch to test
}