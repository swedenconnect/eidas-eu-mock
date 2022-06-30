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

import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.internal.IgniteKernal;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.io.FileNotFoundException;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link ConcurrentCacheServiceIgniteNodeImpl}.
 */
public class ConcurrentCacheServiceIgniteNodeImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcurrentCacheServiceIgniteNodeImplTest.class.getName());

    private static IgniteInstanceInitializerNode igniteInstanceInitializerNode;

    private static ConcurrentCacheServiceIgniteNodeImpl concurrentCacheServiceIgniteNode;

    private static final String CONFIG_FILE_NAME = "src/test/resources/ignite3.xml";

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

    /**
     * Test method for {@link ConcurrentCacheServiceIgniteNodeImpl#getConfiguredCache()}
     * when the instance of {@link ConcurrentCacheServiceIgniteNodeImpl} is well configured.
     * <p>
     * Must succeed.
     */
    @Test
    public void getConfiguredCache() {
        Assert.assertNotNull(concurrentCacheServiceIgniteNode);
        Cache<String, String> configuredCache = concurrentCacheServiceIgniteNode.getConfiguredCache();
        Assert.assertNotNull(configuredCache);
    }

    /**
     * Test method for {@link ConcurrentCacheServiceIgniteNodeImpl#getConfiguredCache()}
     * when cache is null.
     * <p>
     * Must fail and throw {@link InvalidParameterEIDASException}.
     */
    @Test
    public void getConfiguredCacheWheCacheIsNull() {
        thrown.expect(InvalidParameterEIDASException.class);

        IgniteInstanceInitializerNode mockIgniteInstanceInitializerNode = mock(IgniteInstanceInitializerNode.class);
        Ignite mockIgnite = mock(Ignite.class);
        when(mockIgniteInstanceInitializerNode.getInstance()).thenReturn(mockIgnite);
        when(mockIgnite.cache(anyString())).thenReturn(null);

        ConcurrentCacheServiceIgniteNodeImpl concurrentCacheServiceIgniteNode = new ConcurrentCacheServiceIgniteNodeImpl();
        concurrentCacheServiceIgniteNode.setIgniteInstanceInitializer(mockIgniteInstanceInitializerNode);
        concurrentCacheServiceIgniteNode.setCacheName(DEMO_CACHE_NAME);

        concurrentCacheServiceIgniteNode.getConfiguredCache();
    }

    /**
     * Test method for {@link ConcurrentCacheServiceIgniteNodeImpl#getCacheName()}
     * when the instance of {@link ConcurrentCacheServiceIgniteNodeImpl} is well configured.
     * <p>
     * Must succeed.
     */
    @Test
    public void getCacheName() {
        final String actualCacheName = concurrentCacheServiceIgniteNode.getCacheName();

        Assert.assertEquals(DEMO_CACHE_NAME, actualCacheName);
    }

    /**
     * Test method for {@link ConcurrentCacheServiceIgniteNodeImpl#getIgniteInstanceInitializer()}
     * when the instance of {@link ConcurrentCacheServiceIgniteNodeImpl} is well configured.
     * <p>
     * Must succeed.
     */
    @Test
    public void getIgniteInstanceInitializer() {
        IgniteInstanceInitializerNode igniteInstanceInitializer = concurrentCacheServiceIgniteNode.getIgniteInstanceInitializer();

        Assert.assertNotNull(igniteInstanceInitializer);
    }
}