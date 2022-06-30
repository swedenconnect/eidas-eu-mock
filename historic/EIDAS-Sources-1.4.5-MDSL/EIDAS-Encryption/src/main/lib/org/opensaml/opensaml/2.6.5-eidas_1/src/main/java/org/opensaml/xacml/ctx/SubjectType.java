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

/** XACML context Subject schema type. */
public interface SubjectType extends XACMLObject {

    /** Local name of the element. */
    public static final String DEFAULT_ELEMENT_LOCAL_NAME = "Subject";

    /** Default element name. */
    public static final QName DEFAULT_ELEMENT_NAME = new QName(XACMLConstants.XACML20CTX_NS,
            DEFAULT_ELEMENT_LOCAL_NAME, XACMLConstants.XACMLCONTEXT_PREFIX);

    /** Local name of the XSI type. */
    public static final String TYPE_LOCAL_NAME = "SubjectType";

    /** QName of the XSI type. */
    public static final QName TYPE_NAME = new QName(XACMLConstants.XACML20CTX_NS, TYPE_LOCAL_NAME,
            XACMLConstants.XACMLCONTEXT_PREFIX);

    /** Name of the SubjectCategory attribute. */
    public static final String SUBJECT_CATEGORY_ATTTRIB_NAME = "SubjectCategory";

    /**
     * Returns the list of attributes in the subject.
     * 
     * @return the list of attributes in the subject
     */
    public List<AttributeType> getAttributes();

    /**
     * Gets the subjectcategory of the subject.
     * 
     * @return The subjectcategory of the subject
     */
    public String getSubjectCategory();

    /**
     * Sets the subjectcategory.
     * 
     * @param subjectCategory Sets the subjectcategory
     */
    public void setSubjectCategory(String subjectCategory);
}