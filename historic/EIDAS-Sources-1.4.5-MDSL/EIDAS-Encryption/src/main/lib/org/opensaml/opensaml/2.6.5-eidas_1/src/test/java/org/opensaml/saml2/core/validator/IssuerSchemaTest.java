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

package org.opensaml.saml2.core.validator;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectValidatorTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.xml.validation.ValidationException;

/**
 * Test case for {@link org.opensaml.saml2.core.validator.IssuerSchemaValidator}.
 */
public class IssuerSchemaTest extends BaseSAMLObjectValidatorTestCase {

    /** Constructor */
    public IssuerSchemaTest() {
        targetQName = new QName(SAMLConstants.SAML20_NS, Issuer.DEFAULT_ELEMENT_LOCAL_NAME, SAMLConstants.SAML20_PREFIX);
        validator = new IssuerSchemaValidator();
    }

    protected void populateRequiredData() {
        super.populateRequiredData();
        Issuer issuer = (Issuer) target;
        issuer.setValue("name");
    }

    /**
     * Tests Name failure.
     * 
     * @throws ValidationException
     */
    public void testNameFailure() throws ValidationException {
        Issuer issuer = (Issuer) target;

        issuer.setValue(null);
        assertValidationFail("Name was null, should raise a Validation Exception");

        issuer.setValue("");
        assertValidationFail("Name was empty string, should raise a Validation Exception");
        
        issuer.setValue("    ");
        assertValidationFail("Name was white space, should raise a Validation Exception");
    }
}