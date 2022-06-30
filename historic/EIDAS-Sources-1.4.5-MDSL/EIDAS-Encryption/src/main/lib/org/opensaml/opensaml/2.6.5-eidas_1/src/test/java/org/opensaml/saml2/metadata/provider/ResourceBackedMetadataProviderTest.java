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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Timer;

import org.opensaml.common.BaseTestCase;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml2.metadata.RoleDescriptor;
import org.opensaml.util.resource.FilesystemResource;

/** Unit test for {@link ResourceBackedMetadataProvider}. */
public class ResourceBackedMetadataProviderTest extends BaseTestCase {

    private ResourceBackedMetadataProvider metadataProvider;

    private String entityID;

    private String supportedProtocol;

    /** {@inheritDoc} */
    protected void setUp() throws Exception {
        super.setUp();

        entityID = "urn:mace:incommon:washington.edu";
        supportedProtocol = "urn:oasis:names:tc:SAML:1.1:protocol";

        URL mdURL = ResourceBackedMetadataProviderTest.class
                .getResource("/data/org/opensaml/saml2/metadata/InCommon-metadata.xml");
        FilesystemResource mdResource = new FilesystemResource(new File(mdURL.toURI()).getAbsolutePath());

        metadataProvider = new ResourceBackedMetadataProvider(new Timer(), mdResource);
        metadataProvider.setParserPool(parser);
        metadataProvider.setMaxRefreshDelay(500000);
        metadataProvider.initialize();
    }

    /**
     * Tests the {@link ResourceBackedMetadataProvider#getEntityDescriptor(String)} method.
     */
    public void testGetEntityDescriptor() throws MetadataProviderException {
        EntityDescriptor descriptor = metadataProvider.getEntityDescriptor(entityID);
        assertNotNull("Retrieved entity descriptor was null", descriptor);
        assertEquals("Entity's ID does not match requested ID", entityID, descriptor.getEntityID());
    }

    /**
     * Tests the {@link ResourceBackedMetadataProvider#getRole(String, javax.xml.namespace.QName)} method.
     */
    public void testGetRole() throws MetadataProviderException {
        List<RoleDescriptor> roles = metadataProvider.getRole(entityID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        assertNotNull("Roles for entity descriptor was null", roles);
        assertEquals("Unexpected number of roles", 1, roles.size());
    }

    /**
     * Test the {@link ResourceBackedMetadataProvider#getRole(String, javax.xml.namespace.QName, String)} method.
     */
    public void testGetRoleWithSupportedProtocol() throws MetadataProviderException {
        RoleDescriptor role = metadataProvider.getRole(entityID, IDPSSODescriptor.DEFAULT_ELEMENT_NAME,
                supportedProtocol);
        assertNotNull("Roles for entity descriptor was null", role);
    }
}