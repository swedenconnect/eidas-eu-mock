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

package eu.eidas.auth.cache.metadata;

import eu.eidas.auth.cache.ConcurrentCacheServiceIgniteNodeImplTest;
import eu.eidas.auth.cache.IgniteInstanceInitializerNode;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
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
import java.io.FileNotFoundException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link DistributedMetadataCachingTest}
 */
public class DistributedMetadataCachingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DistributedMetadataCachingTest.class.getName());

    private static IgniteInstanceInitializerNode igniteInstanceInitializerNode;

    private static DistributedMetadataCaching distributedMetadataCaching;

    private static final String CONFIG_FILE_NAME = "src/test/resources/ignite3.xml";

    private static final String DEMO_CACHE_NAME = "stillAnotherDemoCacheName";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

        distributedMetadataCaching = new DistributedMetadataCaching();
        distributedMetadataCaching.setCacheName(DEMO_CACHE_NAME);
        distributedMetadataCaching.setIgniteInstanceInitializer(igniteInstanceInitializerNode);
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
     * Test method for {@link DistributedMetadataCaching#getCache()}
     * when {@link DistributedMetadataCaching} is correctly configured
     * <p>
     * Must succeed.
     */
    @Test
    public void getCache() {
        Cache<String, EidasMetadataParametersI> cache = distributedMetadataCaching.getCache();
        Assert.assertNotNull(cache);
    }

    /**
     * Test method for {@link DistributedMetadataCaching#getCache()}
     * when {@link DistributedMetadataCaching} cache name is null
     * <p>
     * Must fail and throw {@link InvalidParameterEIDASException}.
     */
    @Test
    public void getCacheWhenCacheNameIsNull() {
        thrown.expect(InvalidParameterEIDASException.class);

        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        distributedMetadataCaching.setIgniteInstanceInitializer(igniteInstanceInitializerNode);
        distributedMetadataCaching.getCache();
    }

    /**
     * Test method for {@link DistributedMetadataCaching#getCache()}
     * when {@link DistributedMetadataCaching} cache name is null
     * <p>
     * Must fail.
     */
    @Test
    public void getCacheWhenCacheIsNull() {
        thrown.expect(InvalidParameterEIDASException.class);

        IgniteInstanceInitializerNode mockedIgniteInstanceInitializerNode = mock(IgniteInstanceInitializerNode.class);
        Ignite mockedIgnite = mock(Ignite.class);
        when(mockedIgniteInstanceInitializerNode.getInstance()).thenReturn(mockedIgnite);
        when(mockedIgnite.cache(anyString())).thenReturn(null);

        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        distributedMetadataCaching.setIgniteInstanceInitializer(mockedIgniteInstanceInitializerNode);
        distributedMetadataCaching.getCache();
    }

}