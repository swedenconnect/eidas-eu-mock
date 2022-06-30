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

package org.opensaml.saml1.core.impl;

import org.opensaml.common.impl.AbstractSAMLObjectUnmarshaller;
import org.opensaml.saml1.core.Status;
import org.opensaml.saml1.core.StatusCode;
import org.opensaml.saml1.core.StatusDetail;
import org.opensaml.saml1.core.StatusMessage;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.UnmarshallingException;

/**
 * A thread-safe Unmarshaller for {@link org.opensaml.saml1.core.Status} objects.
 */
public class StatusUnmarshaller extends AbstractSAMLObjectUnmarshaller {

    /** {@inheritDoc} */
    protected void processChildElement(XMLObject parentSAMLObject, XMLObject childSAMLObject)
            throws UnmarshallingException {

        Status status = (Status) parentSAMLObject;

        if (childSAMLObject instanceof StatusCode) {
            status.setStatusCode((StatusCode) childSAMLObject);
        } else if (childSAMLObject instanceof StatusMessage) {
            status.setStatusMessage((StatusMessage) childSAMLObject);
        } else if (childSAMLObject instanceof StatusDetail) {

            status.setStatusDetail((StatusDetail) childSAMLObject);

        } else {
            super.processChildElement(parentSAMLObject, childSAMLObject);
        }
    }
}