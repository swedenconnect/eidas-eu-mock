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

package org.opensaml.saml2.metadata.provider;

import org.opensaml.common.BaseTestCase;

/**
 * Unit tests for {@link SchemaValidationFilter}.
 */
public class SchemaValidationFilterTest extends BaseTestCase {

    /** URL to InCommon metadata. */
    private String inCommonMDURL;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        inCommonMDURL = "https://svn.shibboleth.net/java-opensaml2"
                + "/branches/REL_2/src/test/resources/data/org/opensaml/saml2/metadata/InCommon-metadata.xml";
    }

    public void test() throws Exception {
        HTTPMetadataProvider metadataProvider = new HTTPMetadataProvider(inCommonMDURL, 1000 * 5);
        metadataProvider.setParserPool(parser);
        metadataProvider.setMetadataFilter(new SchemaValidationFilter(null));
        metadataProvider.initialize();

        metadataProvider.getMetadata();
    }
}