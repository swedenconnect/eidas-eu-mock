/*
 * Copyright (c) 2022 by European Commission
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

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Properties;

/**
 * Test class for the {@link PropertiesUtil} class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/propertiesUtilTestApplicationContext.xml")
public class PropertiesUtilTest {

    @Autowired
    @Qualifier("nodeProps")
    Properties nodeProps;

    @Autowired
    @Qualifier("serviceProps")
    Properties serviceProperties;

    @After
    public void clearProperties() {
        serviceProperties.clear();
    }

    /**
     * Test for the node properties content.
     * Verify that if value is not defined in the "external" config
     * The default value is returned.
     * <p>
     * Must succeed
     */
    @Test
    public void testNodePropertiesDefaultValueIsUsed() {
        String actualValue = nodeProps.getProperty("defaultKey");
        String expectedValue = "defaultValue";

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test for the node properties content
     * Verify that even though the value is defined in the default properties
     * The "external" properties value is overriding the default one.
     * <p>
     * Must succeed
     */
    @Test
    public void testNodePropertiesDefaultValueIsOverridden() {
        String actualValue = nodeProps.getProperty("overriddenKey");
        String expectedValue = "OverriddenValue";

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test for the {@link PropertiesUtil#getProperty(String)}  method.
     * Verify that if value is not defined in the "external" config
     * The default value is returned.
     * <p>
     * Must succeed
     */
    @Test
    public void testDefaultValueIsUsed() {
        String actualValue = PropertiesUtil.getProperty("defaultKey");
        String expectedValue = "defaultValue";

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test for the {@link PropertiesUtil#getProperty(String)} method.
     * Verify that eventhough the value is defined in the default properties
     * The "external" properties value is overriding the default one.
     * <p>
     * Must succeed
     */
    @Test
    public void testDefaultValueIsOverridden() {
        String actualValue = PropertiesUtil.getProperty("overriddenKey");
        String expectedValue = "OverriddenValue";

        Assert.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test for the {@link PropertiesUtil#isProxyServiceMetadataEnabled()} method.
     * Verify that the value of the flag metadata active is received from {@link AUSERVICEUtil} bean.
     * and that the value is true
     * <p>
     * Must succeed
     */
    @Test
    public void testIsProxyServiceMetadataEnabledTrue() {
        serviceProperties.setProperty(EidasParameterKeys.METADATA_ACTIVE.toString(), Boolean.TRUE.toString());
        boolean actualResult = PropertiesUtil.isProxyServiceMetadataEnabled();

        Assert.assertTrue(actualResult);
    }

    /**
     * Test for the {@link PropertiesUtil#isProxyServiceMetadataEnabled()} method.
     * Verify that the value of the flag metadata active is received from the {@link AUSERVICEUtil} bean.
     * and that the value is false
     * <p>
     * Must succeed
     */
    @Test
    public void testIsProxyServiceMetadataEnabledFalse() {
        boolean actualResult = PropertiesUtil.isProxyServiceMetadataEnabled();

        Assert.assertFalse(actualResult);
    }

}
