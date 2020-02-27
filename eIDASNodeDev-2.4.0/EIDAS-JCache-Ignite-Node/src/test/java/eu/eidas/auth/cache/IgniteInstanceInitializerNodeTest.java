/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.auth.cache;

import org.apache.ignite.Ignition;
import org.apache.ignite.internal.IgniteKernal;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Test class for {@link IgniteInstanceInitializerNode}.
 */
public class IgniteInstanceInitializerNodeTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentCacheServiceIgniteNodeImplTest.class.getName());

    private static IgniteInstanceInitializerNode igniteInstanceInitializerNode;

    private static final String CONFIG_FILE_NAME = "src/test/resources/ignite3.xml";

    /**
     * Initialization of the {@link IgniteInstanceInitializerNode} instance
     *
     * @throws FileNotFoundException if {@code CONFIG_FILE_NAME} is not found.
     */
    @BeforeClass
    public static void initializeIgniteInstance() throws FileNotFoundException {
        igniteInstanceInitializerNode = new IgniteInstanceInitializerNode();
        igniteInstanceInitializerNode.setConfigFileName(CONFIG_FILE_NAME);
        igniteInstanceInitializerNode.initializeInstance();
    }

    /**
     * Stops Ignite's instance created by {@link ConcurrentCacheServiceIgniteNodeImplTest#initializeIgniteInstance()}.
     */
    @AfterClass
    public static void stopIgnite() {
        String instanceName = ((IgniteKernal) igniteInstanceInitializerNode.getInstance()).getInstanceName();
        boolean wasIgniteStopped = Ignition.stop(instanceName, true);
        if (wasIgniteStopped) {
            LOGGER.debug("Ignite instance named: " + instanceName + " was stopped.");
        } else {
            LOGGER.error("Ignite instance named: " + instanceName + " failed to stop.");
        }

        igniteInstanceInitializerNode.destroyInstance();
    }

}