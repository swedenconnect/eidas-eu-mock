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

package org.opensaml.xacml.ctx;

import java.util.List;

import javax.xml.namespace.QName;

import org.opensaml.xacml.XACMLConstants;
import org.opensaml.xacml.XACMLObject;

/** XACML context Attribute schema type. */
public interface AttributeType extends XACMLObject {

    /** Local name of the Attribute element. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Attribute";

    /** Default element name XACML20. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(XACMLConstants.XACML20CTX_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, XACMLConstants.XACMLCONTEXT_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "AttributeType";

    /** QName of the XSI type XACML20. */
    public static final QName TYPE_NAME = new QName(XACMLConstants.XACML20CTX_NS, TYPE_LOCAL_NAME,
            XACMLConstants.XACMLCONTEXT_PREFIX);

    /** Name of the AttributeId attribute. */
    public static final String ATTRIBUTEID_ATTTRIB_NAME = "AttributeId";

    /** Name for the Datatype attribute. */
    public static final String DATATYPE_ATTRIB_NAME = "DataType";

    /** Name of the Issuer attribute. */
    public static final String ISSUER_ATTRIB_NAME = "Issuer";

    /**
     * gets the AttributeId.
     * 
     * @return the AttributeId
     */
    public String getAttributeID();

    /**
     * Gets the list of attribute values for this attribute.
     * 
     * @return the list of values for this attribute
     */
    public List<AttributeValueType> getAttributeValues();

    /**
     * Get the datatype of the attribute.
     * 
     * @return the datatype
     */
    public String getDataType();

    /**
     * Gets the issuer of the attribute.
     * 
     * @return the value of Issuer
     */
    public String getIssuer();

    /**
     * Sets the AttributeId.
     * 
     * @param attributeId is the wanted AttributeId
     */
    public void setAttributeID(String attributeId);

    /**
     * Sets the datatype of the attribute.
     * 
     * @param datatype is the wanted datatype
     */
    public void setDataType(String datatype);

    /**
     * Sets the issuer of the attribute.
     * 
     * @param issuer is the issuer of the attribute
     */
    public void setIssuer(String issuer);
}
