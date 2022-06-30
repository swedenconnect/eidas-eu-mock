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
package org.opensaml.saml2.core.validator;

import org.opensaml.saml2.core.SubjectQuery;
import org.opensaml.xml.validation.ValidationException;

/**
 * Checks {@link org.opensaml.saml2.core.SubjectQuery} for Schema compliance.
 */
public abstract class SubjectQuerySchemaValidator<SubjectQueryType extends SubjectQuery> extends RequestAbstractTypeSchemaValidator<SubjectQueryType> {

    /**
     * Constructor
     *
     */
    public SubjectQuerySchemaValidator() {
    }

    /** {@inheritDoc} */
    public void validate(SubjectQueryType query) throws ValidationException {
        super.validate(query);
        validateSubject(query);
    }

    /**
     * Validates the Subject child element. 
     * 
     * @param query
     * @throws ValidationException 
     */
    protected void validateSubject(SubjectQuery query) throws ValidationException {
        if (query.getSubject() == null)
            throw new ValidationException("Subject is required");
    }

}
