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
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.FileNotFoundException;

/**
 * Test class for  {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl}.
 */
public class ConcurrentCacheServiceIgniteSpecificCommunicationImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentCacheServiceIgniteSpecificCommunicationImplTest.class.getName());

    private static final String DEMO_CACHE_NAME = "stillAnotherDemoCacheName";

    private static final String CONFIG_FILE_NAME = "src/test/resources/ignite3.xml";

    private static IgniteInstanceInitializerSpecificCommunication igniteInstanceInitializerSpecificCommunication;

    private static ConcurrentCacheServiceIgniteSpecificCommunicationImpl concurrentCacheServiceIgniteSpecificCommunicationImpl;

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

        concurrentCacheServiceIgniteSpecificCommunicationImpl = new ConcurrentCacheServiceIgniteSpecificCommunicationImpl();
        concurrentCacheServiceIgniteSpecificCommunicationImpl.setIgniteInstanceInitializerSpecificCommunication(igniteInstanceInitializerSpecificCommunication);
        concurrentCacheServiceIgniteSpecificCommunicationImpl.setCacheName(DEMO_CACHE_NAME);
    }

    /**
     * Stops Ignite's instance created by {@link ConcurrentCacheServiceIgniteSpecificCommunicationImplTest#initializeIgniteInstance()}.
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
     * checking if the {@code igniteInstanceInitializerSpecificCommunication} was initialized.
     * <p>
     * Must succeed.
     */
    @Test
    public void testInitializeInstance() {
        Assert.assertNotNull(igniteInstanceInitializerSpecificCommunication);
        Ignite igniteInstance = igniteInstanceInitializerSpecificCommunication.getInstance();
        Assert.assertNotNull(igniteInstance);
    }

    /**
     * Test method for
     * {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl#getConfiguredCache()}}.
     * <p>
     * Must succeed.
     */
    @Test
    public void getConfiguredCache() {
        Assert.assertNotNull(concurrentCacheServiceIgniteSpecificCommunicationImpl);
        Cache<String, String> configuredCache = concurrentCacheServiceIgniteSpecificCommunicationImpl.getConfiguredCache();
        Assert.assertNotNull(configuredCache);
    }

    /**
     * Test method for
     * {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl#getCacheName()}.
     * <p>
     * Must succeed.
     */
    @Test
    public void getCacheName() {
        Assert.assertNotNull(concurrentCacheServiceIgniteSpecificCommunicationImpl);
        String cacheName = concurrentCacheServiceIgniteSpecificCommunicationImpl.getCacheName();
        Assert.assertEquals(cacheName, DEMO_CACHE_NAME);
    }

    /**
     * Test method for
     * {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl#setCacheName(String)}.
     * <p>
     * Must succeed.
     */
    @Test
    public void setCacheName() {
        String anotherCacheName = "anotherCacheName";
        concurrentCacheServiceIgniteSpecificCommunicationImpl.setCacheName(anotherCacheName);
    }

    /**
     * Test method for
     * {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl#getIgniteInstanceInitializerSpecificCommunication()}.
     * <p>
     * Must succeed.
     */
    @Test
    public void getIgniteInstanceInitializer() {
        concurrentCacheServiceIgniteSpecificCommunicationImpl.getIgniteInstanceInitializerSpecificCommunication();
        Assert.assertNotNull(igniteInstanceInitializerSpecificCommunication);
        Assert.assertEquals(this.igniteInstanceInitializerSpecificCommunication, igniteInstanceInitializerSpecificCommunication);
    }

    /**
     * Test method for
     * {@link ConcurrentCacheServiceIgniteSpecificCommunicationImpl#setIgniteInstanceInitializerSpecificCommunication(IgniteInstanceInitializerSpecificCommunication)}.
     * <p>
     * Must succeed.
     */
    @Test
    public void setIgniteInstanceInitializer() {
        concurrentCacheServiceIgniteSpecificCommunicationImpl.setIgniteInstanceInitializerSpecificCommunication(null);
        Assert.assertNull(concurrentCacheServiceIgniteSpecificCommunicationImpl.getIgniteInstanceInitializerSpecificCommunication());
    }
}