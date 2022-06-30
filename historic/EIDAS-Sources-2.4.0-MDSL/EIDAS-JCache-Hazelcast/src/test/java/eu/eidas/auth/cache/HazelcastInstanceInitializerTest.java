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

import com.hazelcast.core.HazelcastInstance;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileNotFoundException;

/**
 * Test class for {@link HazelcastInstanceInitializer}.
 */
public class HazelcastInstanceInitializerTest {

    /**
     * Test method for
     * {@link HazelcastInstanceInitializer#getInstance(String)}
     * when it is configured correctly.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException when the file from {@param hazelcastConfigfileName} is not found
     */
    @Test
    public void getInstance() throws FileNotFoundException {
        final String testHazelcastInstanceName = "testHazelcastInstanceName";
        final String hazelcastConfigfileName = "src/test/resources/hazelcastConfigTest.xml";

        HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(testHazelcastInstanceName, hazelcastConfigfileName);
        HazelcastInstance actualHazelcastInstance = HazelcastInstanceInitializer.getInstance(testHazelcastInstanceName);

        Assert.assertNotNull(actualHazelcastInstance);

        HazelcastInstanceInitializerTestUtil.shutdownHazelcastInstance(actualHazelcastInstance);
    }

    /**
     * Test method for
     * {@link HazelcastInstanceInitializer#getInstance(String)}
     * when {@link HazelcastInstanceInitializer#hazelcastConfigfileName} field is null.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException when the file from {@param hazelcastConfigfileName} is not found
     */
    @Test
    public void getInstanceWhenHazelcastConfigfileNameIsNull() throws FileNotFoundException {
        final String testHazelcastInstanceName = "testHazelcastInstanceName";
        final String hazelcastConfigfileName = null;

        HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(testHazelcastInstanceName, hazelcastConfigfileName);
        HazelcastInstance actualHazelcastInstance = HazelcastInstanceInitializer.getInstance(testHazelcastInstanceName);

        Assert.assertNotNull(actualHazelcastInstance);

        HazelcastInstanceInitializerTestUtil.shutdownHazelcastInstance(actualHazelcastInstance);
    }

    /**
     * Test method for
     * {@link HazelcastInstanceInitializer#initializeInstance()}}
     * when it is configured correctly.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException if the file used to initialize the {@link HazelcastInstanceInitializer} could not be found.
     */
    @Test
    public void initializeInstance() throws FileNotFoundException {

        final String testHazelcastInstanceName = "testHazelcastInstanceName";
        final String hazelcastConfigfileName = "src/test/resources/hazelcastConfigTest.xml";

        HazelcastInstanceInitializer hazelcastInstanceInitializer = HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(testHazelcastInstanceName, hazelcastConfigfileName);
        String hazelcastInstanceName = hazelcastInstanceInitializer.getHazelcastInstanceName();

        Assert.assertNotNull(hazelcastInstanceName);

        HazelcastInstanceInitializerTestUtil.shutdownHazelcastInstance(testHazelcastInstanceName);
    }
}