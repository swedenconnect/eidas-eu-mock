/*
 * Copyright (c) 2018 by European Commission
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

package eu.eidas.auth.cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.internal.IgniteKernal;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Test class for  {@link IgniteInstanceInitializerSpecificCommunication}.
 */
public class IgniteInstanceInitializerSpecificCommunicationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteInstanceInitializerSpecificCommunicationTest.class.getName());

    private static final String CONFIG_FILE_NAME = "src/test/resources/ignite1.xml";

    private static final String ANOTHER_CONFIG_FILE_NAME = "src/test/resources/ignite2.xml";

    private static IgniteInstanceInitializerSpecificCommunication igniteInstanceInitializerSpecificCommunication;

    /**
     * Initialization of the {@link IgniteInstanceInitializerSpecificCommunication} instance.
     *
     * @throws FileNotFoundException if {@code CONFIG_FILE_NAME} is not found.
     */
    @BeforeClass
    public static void initializeIgniteInstance() throws FileNotFoundException {
        igniteInstanceInitializerSpecificCommunication = new IgniteInstanceInitializerSpecificCommunication();
        igniteInstanceInitializerSpecificCommunication.setConfigFileName(CONFIG_FILE_NAME);
        igniteInstanceInitializerSpecificCommunication.initializeInstance();
    }

    /**
     * Stops Ignite's instance created by {@link IgniteInstanceInitializerSpecificCommunicationTest#initializeIgniteInstance()}.
     */
    @AfterClass
    public static void stopIgnite() {
        String instanceName = ((IgniteKernal) igniteInstanceInitializerSpecificCommunication.getInstance()).getInstanceName();
        boolean wasIgniteStopped = Ignition.stop(instanceName, true);
        if (wasIgniteStopped) {
            LOGGER.debug("Ignite instance named: " + instanceName + " was stopped.");
        } else {
            LOGGER.error("Ignite instance named: " + instanceName + " failed to stop.");
        }

        igniteInstanceInitializerSpecificCommunication.destroyInstance();
    }

    /**
     * Test method for
     * {@link IgniteInstanceInitializerSpecificCommunication#getInstance()}
     * when the {@link IgniteInstanceInitializerSpecificCommunication} has been initialized before.
     * <p>
     * Must succeed.
     *
     */
    @Test
    public void getInstance() {
        final Ignite instance = igniteInstanceInitializerSpecificCommunication.getInstance();
        Assert.assertNotNull(instance);
    }

    /**
     * Test method for
     * {@link IgniteInstanceInitializerSpecificCommunication#initializeInstance()}
     * <p>
     * Must succeed.
     *
     */
    @Test
    public void initializeInstance() {
        Assert.assertNotNull(igniteInstanceInitializerSpecificCommunication.getInstance());
    }
    
}