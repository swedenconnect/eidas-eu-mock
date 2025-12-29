/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.node.utils;

import eu.eidas.auth.engine.metadata.ContactData;
import eu.eidas.auth.engine.metadata.OrganizationData;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test class for {@link NodeMetadataUtil}.
 */
public class NodeMetadataUtilTest {

    /**
     * Properties instance with all properties set.
     */
    public static Properties configs;

    /**
     * Empty Properties instance.
     */
    public static Properties configsEmpty;

    /**
     * Initialization of properties instances used in the tests.
     *
     * @throws IOException if properties file cannot be read
     */
    @BeforeClass
    public static void init() throws IOException {
        configs = new Properties();
        final String name = "src/test/resources/eidasContactOrganizationOnly.xml";
        InputStream fileProperties = new FileInputStream(name);
        configs.loadFromXML(fileProperties);

        configsEmpty = new Properties();
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorTechnicalContact(Properties)}
     * when all technical contact properties are filled in.
     *
     * Method must return {@link ContactData} with all fields neither empty or null.
     */
    @Test
    public void testCreateConnectorTechnicalContact() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorTechnicalContact(configs);
        Assert.assertNotNull(contactData);
        assertAllFieldsNotEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorTechnicalContact(Properties)}
     * when all technical contact properties are not existent.
     *
     * Method must return {@link ContactData} with all fields empty.
     */
    @Test
    public void testCreateConnectorTechnicalContactEmptyConfigs() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorTechnicalContact(configsEmpty);
        Assert.assertNotNull(contactData);
        assertAllFieldsEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorSupportContact(Properties)}
     * when all connector support contact properties are filled in.
     *
     * Method must return {@link ContactData} with all fields neither empty or null.
     */
    @Test
    public void testCreateConnectorSupportContact() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorSupportContact(configs);
        Assert.assertNotNull(contactData);
        assertAllFieldsNotEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorSupportContact(Properties)}
     * when all connector support contact properties are not existent.
     *
     * Method must return {@link ContactData} with all fields are empty.
     */
    @Test
    public void testCreateConnectorSupportContactEmptyConfigs() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorSupportContact(configsEmpty);
        Assert.assertNotNull(contactData);
        assertAllFieldsEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorTechnicalContact(Properties)}
     * when all connector technical contact properties are filled in.
     *
     * Method must return {@link ContactData} with all fields neither empty or null.
     */
    @Test
    public void testCreateServiceTechnicalContact() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorTechnicalContact(configs);
        Assert.assertNotNull(contactData);
        assertAllFieldsNotEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorTechnicalContact(Properties)}
     * when all connector technical contact properties are not existent.
     *
     * Method must return {@link ContactData} with all fields empty.
     */
    @Test
    public void testCreateServiceTechnicalContactEmptyConfigs() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createConnectorTechnicalContact(configsEmpty);
        Assert.assertNotNull(contactData);
        assertAllFieldsEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createServiceSupportContact(Properties)}
     * when all service support contact properties are filled in.
     *
     * Method must return {@link ContactData} with all fields neither empty or null.
     */
    @Test
    public void testCreateServiceSupportContact() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createServiceSupportContact(configs);
        Assert.assertNotNull(contactData);
        assertAllFieldsNotEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createServiceSupportContact(Properties)}
     * when all service support contact properties are not existent.
     *
     * Method must return {@link ContactData} with all fields are empty.
     */
    @Test
    public void testCreateServiceSupportContactEmptyConfigs() throws Exception {
        final ContactData contactData = NodeMetadataUtil.createServiceSupportContact(configsEmpty);
        Assert.assertNotNull(contactData);
        assertAllFieldsEmpty(contactData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createServiceOrganization(Properties)}
     * when all service organization properties are filled in.
     *
     * Method must return {@link OrganizationData} with all fields neither empty or null.
     */
    @Test
    public void testCreateServiceOrganization() throws Exception {
        final OrganizationData organizationData = NodeMetadataUtil.createServiceOrganization(configs);
        Assert.assertNotNull(organizationData);
        assertAllFieldsNotEmpty(organizationData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createServiceOrganization(Properties)}
     * when all service organization properties are not existent.
     *
     * Method must return {@link OrganizationData} with all fields empty.
     */
    @Test
    public void testCreateServiceOrganizationEmptyConfigs() throws Exception {
        final OrganizationData organizationData = NodeMetadataUtil.createServiceOrganization(configsEmpty);
        Assert.assertNotNull(organizationData);
        assertAllFieldsEmpty(organizationData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorOrganizationData(Properties)}
     * when all connector organization data properties are filled in.
     *
     * Method must return {@link OrganizationData} with all fields neither empty or null.
     */
    @Test
    public void testCreateConnectorOrganizationData() throws Exception {
        final OrganizationData organizationData = NodeMetadataUtil.createConnectorOrganizationData(configs);
        Assert.assertNotNull(organizationData);
        assertAllFieldsNotEmpty(organizationData);
    }

    /**
     * Test for {@link NodeMetadataUtil#createConnectorOrganizationData(Properties)}
     * when all connector organization data properties are not filled in.
     *
     * Method must return {@link OrganizationData} with all fields empty.
     */
    @Test
    public void testCreateConnectorOrganizationDataEmptyConfigs() throws Exception {
        final OrganizationData organizationData = NodeMetadataUtil.createConnectorOrganizationData(configsEmpty);
        Assert.assertNotNull(organizationData);
        assertAllFieldsEmpty(organizationData);
    }

    /**
     * Method to assert that all fields of {@link ContactData} are not empty.
     * @param contactData the instance that contains the fields to be asserted.
     */
    private void assertAllFieldsNotEmpty(ContactData contactData) {
        Assert.assertTrue(StringUtils.isNotEmpty(contactData.getCompany()));
        Assert.assertTrue(StringUtils.isNotEmpty(contactData.getEmail()));
        Assert.assertTrue(StringUtils.isNotEmpty(contactData.getGivenName()));
        Assert.assertTrue(StringUtils.isNotEmpty(contactData.getSurName()));
        Assert.assertTrue(StringUtils.isNotEmpty(contactData.getPhone()));
    }

    /**
     * Method to assert that all fields of {@link OrganizationData} are not empty.
     * @param organizationData the instance that contains the fields to be asserted.
     */
    private void assertAllFieldsNotEmpty(OrganizationData organizationData) {
        Assert.assertTrue(StringUtils.isNotEmpty(organizationData.getDisplayName()));
        Assert.assertTrue(StringUtils.isNotEmpty(organizationData.getName()));
        Assert.assertTrue(StringUtils.isNotEmpty(organizationData.getUrl()));
    }

    /**
     * Method to assert that all fields of {@link ContactData} are empty.
     * @param contactData the instance that contains the fields to be asserted.
     */
    private void assertAllFieldsEmpty(ContactData contactData) {
        Assert.assertTrue(contactData.getCompany().isEmpty());
        Assert.assertTrue(contactData.getEmail().isEmpty());
        Assert.assertTrue(contactData.getGivenName().isEmpty());
        Assert.assertTrue(contactData.getSurName().isEmpty());
        Assert.assertTrue(contactData.getPhone().isEmpty());
    }

    /**
     * Method to assert that all fields of {@link OrganizationData} are empty.
     * @param organizationData the instance that contains the fields to be asserted.
     */
    private void assertAllFieldsEmpty(OrganizationData organizationData) {
        Assert.assertTrue(organizationData.getDisplayName().isEmpty());
        Assert.assertTrue(organizationData.getName().isEmpty());
        Assert.assertTrue(organizationData.getUrl().isEmpty());
    }

}