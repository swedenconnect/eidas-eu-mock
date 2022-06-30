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

package org.opensaml.saml2.metadata.validator;

import org.opensaml.saml2.metadata.AffiliateMember;
import org.opensaml.xml.util.DatatypeHelper;
import org.opensaml.xml.validation.ValidationException;
import org.opensaml.xml.validation.Validator;

/**
 * Checks {@link org.opensaml.saml2.metadata.AffiliateMember} for Schema compliance.
 */
public class AffiliateMemberSchemaValidator implements Validator<AffiliateMember> {

    /** Constructor */
    public AffiliateMemberSchemaValidator() {

    }

    /** {@inheritDoc} */
    public void validate(AffiliateMember affiliateMember) throws ValidationException {
        validateID(affiliateMember);
    }

    /**
     * Checks that ID is valid and present.
     * 
     * @param affiliateMember
     * @throws ValidationException
     */
    protected void validateID(AffiliateMember affiliateMember) throws ValidationException {
        if (DatatypeHelper.isEmpty(affiliateMember.getID())) {
            throw new ValidationException("ID required");
        } else if (affiliateMember.getID().length() > 1024) {
            throw new ValidationException("Max ID length is 1024");
        }
    }
}