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
package org.opensaml.saml2.metadata.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.metadata.EmailAddress;

/**
 * Test case for creating, marshalling, and unmarshalling
 * {@link org.opensaml.saml2.metadata.impl.EmailAddressImpl}.
 */
public class EmailAddressTest extends BaseSAMLObjectProviderTestCase {
    
    /** Expected email address */
    protected String expectedAddress;
    
    /**
     * Constructor
     */
    public EmailAddressTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/EmailAddress.xml";
    }
    
    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedAddress = "foo@example.org";
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EmailAddress address = (EmailAddress) unmarshallElement(singleElementFile);
        
        assertEquals("Email address was not expected value", expectedAddress, address.getAddress());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        QName qname = new QName(SAMLConstants.SAML20MD_NS, EmailAddress.DEFAULT_ELEMENT_LOCAL_NAME);
        EmailAddress address = (EmailAddress) buildXMLObject(qname);
        
        address.setAddress(expectedAddress);

        assertEquals(expectedDOM, address);
    }
}