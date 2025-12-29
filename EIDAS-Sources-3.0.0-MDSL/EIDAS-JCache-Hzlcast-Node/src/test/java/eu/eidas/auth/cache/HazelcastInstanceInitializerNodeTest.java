/*
 * Copyright (c) 2025 by European Commission
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

import com.hazelcast.core.HazelcastInstance;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Test class for {@link HazelcastInstanceInitializerNode}.
 */
public class HazelcastInstanceInitializerNodeTest {

    /**
     * Test method for
     * {@link HazelcastInstanceInitializerNode#getInstance(String)}
     * when it is configured correctly.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException when the file from {@param hazelcastConfigFileName} is not found
     */
    @Test
    public void getInstance() throws FileNotFoundException {
        final String hazelcastConfigFileName = "src/test/resources/hazelcastConfigTest.xml";

        HazelcastInstanceInitializerNode initializer = HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(hazelcastConfigFileName);
        HazelcastInstance hazelcastInstance = initializer.getInstance();

        Assert.assertNotNull(hazelcastInstance);
        Assert.assertEquals("testHazelcastInstanceName", hazelcastInstance.getName());

        HazelcastInstanceInitializerTestUtil.shutdownHazelcastInstance(hazelcastInstance);
    }

    /**
     * Test method for
     * {@link HazelcastInstanceInitializerNode#initializeInstance()}}
     * when it is configured correctly.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException if the file used to initialize the {@link HazelcastInstanceInitializerNode} could not be found.
     */
    @Test
    public void initializeInstance() throws FileNotFoundException {
        final String hazelcastConfigFileName = "src/test/resources/hazelcastConfigTest.xml";

        HazelcastInstanceInitializerNode hazelcastInstanceInitializerNode =
                HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(hazelcastConfigFileName);
        HazelcastInstance hazelcastInstance = hazelcastInstanceInitializerNode.getInstance();

        Assert.assertNotNull(hazelcastInstance);
        Assert.assertEquals("testHazelcastInstanceName", hazelcastInstance.getName());

        HazelcastInstanceInitializerTestUtil.shutdownHazelcastInstance(hazelcastInstance);
    }
}