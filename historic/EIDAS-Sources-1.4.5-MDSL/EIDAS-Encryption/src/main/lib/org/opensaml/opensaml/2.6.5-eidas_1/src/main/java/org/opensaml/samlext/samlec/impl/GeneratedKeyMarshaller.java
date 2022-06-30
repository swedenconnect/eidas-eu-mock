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

package org.opensaml.samlext.samlec.impl;

import org.opensaml.samlext.samlec.GeneratedKey;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.MarshallingException;
import org.opensaml.xml.schema.impl.XSBase64BinaryMarshaller;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Element;

/**
 * A thread-safe Marshaller for {@link GeneratedKey} objects.
 */
public class GeneratedKeyMarshaller extends XSBase64BinaryMarshaller {

    /** {@inheritDoc} */
    protected void marshallAttributes(XMLObject samlObject, Element domElement) throws MarshallingException {
        GeneratedKey key = (GeneratedKey) samlObject;

        if (key.isSOAP11MustUnderstandXSBoolean() != null) {
            XMLHelper.marshallAttribute(GeneratedKey.SOAP11_MUST_UNDERSTAND_ATTR_NAME, 
                    key.isSOAP11MustUnderstandXSBoolean().toString(), domElement, false);
        }
        
        if (key.getSOAP11Actor() != null) {
            XMLHelper.marshallAttribute(GeneratedKey.SOAP11_ACTOR_ATTR_NAME, 
                    key.getSOAP11Actor(), domElement, false);
        }
    }
}