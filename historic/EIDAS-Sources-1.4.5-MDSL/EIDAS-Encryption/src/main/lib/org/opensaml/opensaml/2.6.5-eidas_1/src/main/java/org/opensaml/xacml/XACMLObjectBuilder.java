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

package org.opensaml.xacml;

import org.opensaml.xml.XMLObjectBuilder;

/**
 * Builder of XACML objects.
 * 
 * @param <XACMLObjectType> type of XACML object built
 */
public interface XACMLObjectBuilder<XACMLObjectType extends XACMLObject> extends XMLObjectBuilder<XACMLObjectType> {

    /**
     * Builds a XACML object, using its default attribute name.
     * 
     * @return constructed XACML object
     */
    public XACMLObjectType buildObject();
}