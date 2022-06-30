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

package org.opensaml.saml2.ecp.impl;

import javax.xml.namespace.QName;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml2.ecp.Response;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.schema.XSBooleanValue;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Attr;

/**
 * Unmarshaller for instances of {@link Response}.
 */
public class ResponseUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processAttribute(XMLObject samlObject, Attr attribute) throws UnmarshallingException {
        Response response = (Response) samlObject;
        
        QName attrName = XMLHelper.getNodeQName(attribute);
        if (Response.SOAP11_MUST_UNDERSTAND_ATTR_NAME.equals(attrName)) {
            response.setSOAP11MustUnderstand(XSBooleanValue.valueOf(attribute.getValue()));
        } else if (Response.SOAP11_ACTOR_ATTR_NAME.equals(attrName)) {
            response.setSOAP11Actor(attribute.getValue()); 
        } else if (Response.ASSERTION_CONSUMER_SERVICE_URL_ATTRIB_NAME.equals(attribute.getLocalName())) {
            response.setAssertionConsumerServiceURL(attribute.getValue());
        } else { 
            super.processAttribute(samlObject, attribute);
        }
            
    }

}
