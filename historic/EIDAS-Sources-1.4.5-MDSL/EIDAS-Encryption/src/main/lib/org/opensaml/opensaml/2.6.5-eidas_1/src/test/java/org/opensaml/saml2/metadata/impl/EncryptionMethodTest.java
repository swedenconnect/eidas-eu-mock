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

package org.opensaml.saml2.metadata.impl;


import org.opensaml.common.BaseSAMLObjectProviderTestCase;
import org.opensaml.saml2.metadata.EncryptionMethod;
import org.opensaml.xml.encryption.KeySize;
import org.opensaml.xml.encryption.OAEPparams;
import org.opensaml.xml.signature.DigestMethod;

/**
 *
 */
public class EncryptionMethodTest extends BaseSAMLObjectProviderTestCase {
    
    private String expectedAlgorithm;
    
    private int expectedNumUnknownChildren;
    
    /**
     * Constructor
     *
     */
    public EncryptionMethodTest() {
        singleElementFile = "/data/org/opensaml/saml2/metadata/impl/EncryptionMethod.xml";
        childElementsFile = "/data/org/opensaml/saml2/metadata/impl/EncryptionMethodChildElements.xml";
    }

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();
        
        expectedAlgorithm = "urn:string:foo";
        expectedNumUnknownChildren = 2;
    }

    /** {@inheritDoc} */
    public void testSingleElementUnmarshall() {
        EncryptionMethod em = (EncryptionMethod) unmarshallElement(singleElementFile);
        
        assertNotNull("EncryptionMethod", em);
        assertEquals("Algorithm attribute", expectedAlgorithm, em.getAlgorithm());
        assertNull("KeySize child", em.getKeySize());
        assertNull("OAEPparams child", em.getOAEPparams());
        assertEquals("Unknown children", 0, em.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testChildElementsUnmarshall() {
        EncryptionMethod em = (EncryptionMethod) unmarshallElement(childElementsFile);
        
        assertNotNull("EncryptionMethod", em);
        assertEquals("Algorithm attribute", expectedAlgorithm, em.getAlgorithm());
        assertNotNull("KeySize child", em.getKeySize());
        assertNotNull("OAEPparams child", em.getOAEPparams());
        assertEquals("Unknown children", expectedNumUnknownChildren, em.getUnknownXMLObjects().size());
    }

    /** {@inheritDoc} */
    public void testSingleElementMarshall() {
        EncryptionMethod em = (EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
        
        em.setAlgorithm(expectedAlgorithm);
        
        assertEquals(expectedDOM, em);
    }

    /** {@inheritDoc} */
    public void testChildElementsMarshall() {
        EncryptionMethod em = (EncryptionMethod) buildXMLObject(EncryptionMethod.DEFAULT_ELEMENT_NAME);
        
        em.setAlgorithm(expectedAlgorithm);
        em.setKeySize((KeySize) buildXMLObject(KeySize.DEFAULT_ELEMENT_NAME));
        em.setOAEPparams((OAEPparams) buildXMLObject(OAEPparams.DEFAULT_ELEMENT_NAME));
        em.getUnknownXMLObjects().add( buildXMLObject(DigestMethod.DEFAULT_ELEMENT_NAME));
        em.getUnknownXMLObjects().add( buildXMLObject(DigestMethod.DEFAULT_ELEMENT_NAME));
        
        assertEquals(expectedChildElementsDOM, em);
    }

}
