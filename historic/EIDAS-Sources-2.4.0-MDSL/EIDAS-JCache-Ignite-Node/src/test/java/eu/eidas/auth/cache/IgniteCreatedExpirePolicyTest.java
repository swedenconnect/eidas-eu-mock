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
 * limitations under the Licence.
 */

package eu.eidas.auth.cache;

import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.internal.IgniteKernal;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.ExpiryPolicy;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

/**
 * Test class for {@link ConcurrentCacheServiceIgniteNodeImpl}.
 */
public class IgniteCreatedExpirePolicyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteCreatedExpirePolicyTest.class.getName());

    private static IgniteInstanceInitializerNode igniteInstanceInitializerNode;

    private static ConcurrentCacheServiceIgniteNodeImpl concurrentCacheServiceIgniteNode;

    private static final String CONFIG_FILE_NAME = "src/test/resources/igniteCreatedExpiryPolicy.xml";

    private static final String DEMO_CACHE_NAME = "stillAnotherDemoCacheName";

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /**
     * Initialization of the {@link IgniteInstanceInitializerNode} instance
     * and creates and configures a {@link ConcurrentCacheServiceIgniteNodeImpl} instance.
     *
     * @throws FileNotFoundException if {@code CONFIG_FILE_NAME} is not found.
     */
    @BeforeClass
    public static void initializeIgniteInstance() throws FileNotFoundException {
        igniteInstanceInitializerNode = new IgniteInstanceInitializerNode();
        igniteInstanceInitializerNode.setConfigFileName(CONFIG_FILE_NAME);
        igniteInstanceInitializerNode.initializeInstance();

        concurrentCacheServiceIgniteNode = new ConcurrentCacheServiceIgniteNodeImpl();
        concurrentCacheServiceIgniteNode.setIgniteInstanceInitializer(igniteInstanceInitializerNode);
        concurrentCacheServiceIgniteNode.setCacheName(DEMO_CACHE_NAME);
    }

    /**
     * Stops Ignite's instance created by {@link IgniteCreatedExpirePolicyTest#initializeIgniteInstance()}.
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

    /**
     * Test for Ignite CreatedExpiryPolicy config
     * Controls if the spring bean configuration is loaded
     */
    @Test
    public void getConfiguredCacheWithExpiryPolicy() {
        Assert.assertNotNull(concurrentCacheServiceIgniteNode);
        Cache<String, String> configuredCache = concurrentCacheServiceIgniteNode.getConfiguredCache();
        ExpiryPolicy expiryPolicy = (ExpiryPolicy) configuredCache.getConfiguration(CacheConfiguration.class).getExpiryPolicyFactory().create();

        Assert.assertEquals(1, expiryPolicy.getExpiryForCreation().getDurationAmount());
        Assert.assertEquals(TimeUnit.SECONDS, expiryPolicy.getExpiryForCreation().getTimeUnit());
        Assert.assertTrue(expiryPolicy.getExpiryForCreation().equals(new Duration(TimeUnit.MILLISECONDS, 1000)));
    }

    /**
     * Test for Ignite CreatedExpiryPolicy config
     * A simple case for testing if cache expires
     */
    @Test
    synchronized public void testConfiguredExpiryPolicy() throws InterruptedException {
        Assert.assertNotNull(concurrentCacheServiceIgniteNode);
        Cache<String, String> configuredCache = concurrentCacheServiceIgniteNode.getConfiguredCache();
        final String referenceKey = "referenceKey";
        final String referenceValue = "referenceValue";

        configuredCache.put(referenceKey, referenceValue);

        Assert.assertEquals("at case zero", referenceValue, configuredCache.get(referenceKey));
        wait(900);
        Assert.assertEquals("at case 1", referenceValue, configuredCache.get(referenceKey));
        wait(1000);
        Assert.assertEquals("after create has passed", null, configuredCache.get(referenceKey));
    }

    /**
     * Test for Ignite CreatedExpiryPolicy config
     * A stepwise expiration of values on the same cache
     * to test cross influence on expiry time
     */
    @Test
    synchronized public void testConfiguredCacheMultipleExpiringKeys() throws InterruptedException {
        Assert.assertNotNull(concurrentCacheServiceIgniteNode);
        Cache<String, String> configuredCache = concurrentCacheServiceIgniteNode.getConfiguredCache();

        for(int incr = 0; incr < 5 ; incr++) {
            configuredCache.put("key" + incr, "value" + incr);
            wait(100);
        }
        wait(450);
        for(int incr = 0; incr < 5; incr++) {
            Assert.assertEquals("value" + incr, configuredCache.get("key" + incr));
            Assert.assertEquals(null, configuredCache.get("key" + (incr - 1)));
            wait(100);
        }
        Assert.assertEquals(null, configuredCache.get("key" + 5));
    }
}