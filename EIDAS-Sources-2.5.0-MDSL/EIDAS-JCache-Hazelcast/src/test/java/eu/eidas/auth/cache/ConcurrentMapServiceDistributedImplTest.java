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
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.cache.Cache;
import java.io.FileNotFoundException;

/**
 * Test class for testing {@link ConcurrentMapServiceDistributedImpl}
 */
public class ConcurrentMapServiceDistributedImplTest {

    private final static String TEST_CACHE_NAME = "TestCacheName";


    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#getConfiguredCache()}
     * when {@link ConcurrentMapServiceDistributedImpl} is correctly set with
     * {@link HazelcastInstanceInitializer} instance correctly configured and initialized.
     * <p/>
     * Must succeed.
     *
     * @throws FileNotFoundException when hazelcast configuration filename is not found
     */
    @Test
    public void getConfiguredCache() throws FileNotFoundException {
        final String testHazelcastInstanceName = "testHazelcastInstanceName";
        final String hazelcastConfigfileName = "src/test/resources/hazelcastConfigTest.xml";

        HazelcastInstanceInitializer hazelcastInstanceInitializer = HazelcastInstanceInitializerTestUtil.createHazelcastInstanceInitializer(testHazelcastInstanceName, hazelcastConfigfileName);

        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        concurrentMapServiceDistributedImpl.setHazelcastInstanceInitializer(hazelcastInstanceInitializer);
        concurrentMapServiceDistributedImpl.setCacheName("default");

        final Cache configuredCache = concurrentMapServiceDistributedImpl.getConfiguredCache();
        Assert.assertNotNull(configuredCache);

        HazelcastInstanceInitializer.getInstance(testHazelcastInstanceName).shutdown();
    }

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#getConfiguredCache()}
     * when {@link ConcurrentMapServiceDistributedImpl} is correctly set with
     * {@link HazelcastInstanceInitializer} instance correctly configured and initialized.
     * <p/>
     * Must fail and throw {@link InvalidParameterEIDASException}.
     *
     * @throws FileNotFoundException when hazelcast configuration filename is not found
     */
    @Test
    public void getConfiguredCacheWhenCacheNameIsNull() {
        thrown.expect(InvalidParameterEIDASException.class);
        thrown.expectMessage("Distributed Cache Configuration mismatch");

        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        concurrentMapServiceDistributedImpl.getConfiguredCache();
    }

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#getCacheName()}}
     * <p/>
     * Must succeed.
     */
    @Test
    public void getCacheName() {
        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        final String expectedCacheName = TEST_CACHE_NAME;
        concurrentMapServiceDistributedImpl.setCacheName(expectedCacheName);

        final String actualCacheName = concurrentMapServiceDistributedImpl.getCacheName();

        Assert.assertEquals(expectedCacheName, actualCacheName);
    }

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#setCacheName(String)}}
     * <p/>
     * Must succeed.
     */
    @Test
    public void setCacheName() {
        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        final String expectedCacheName = TEST_CACHE_NAME;
        concurrentMapServiceDistributedImpl.setCacheName(expectedCacheName);
        concurrentMapServiceDistributedImpl.setCacheName(null);

        final String actualCacheName = concurrentMapServiceDistributedImpl.getCacheName();

        Assert.assertNull(actualCacheName);

    }

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#getHazelcastInstanceInitializer()}
     * <p/>
     * Must succeed.
     */
    @Test
    public void getHazelcastInstanceInitializer() {
        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        final HazelcastInstanceInitializer expectedHazelcastInstanceInitializer = new HazelcastInstanceInitializer();

        concurrentMapServiceDistributedImpl.setHazelcastInstanceInitializer(expectedHazelcastInstanceInitializer);

        HazelcastInstanceInitializer actualHazelcastInstanceInitializer = concurrentMapServiceDistributedImpl.getHazelcastInstanceInitializer();
        Assert.assertEquals(expectedHazelcastInstanceInitializer, actualHazelcastInstanceInitializer);
    }

    /**
     * Test method for
     * {@link ConcurrentMapServiceDistributedImpl#setHazelcastInstanceInitializer(HazelcastInstanceInitializer)}
     * <p/>
     * Must succeed.
     */
    @Test
    public void setHazelcastInstanceInitializer() {
        final ConcurrentMapServiceDistributedImpl concurrentMapServiceDistributedImpl = new ConcurrentMapServiceDistributedImpl();
        final HazelcastInstanceInitializer firstHazelcastInstanceInitializer = new HazelcastInstanceInitializer();

        concurrentMapServiceDistributedImpl.setHazelcastInstanceInitializer(firstHazelcastInstanceInitializer);
        concurrentMapServiceDistributedImpl.setHazelcastInstanceInitializer(null);

        HazelcastInstanceInitializer actualHazelcastInstanceInitializer = concurrentMapServiceDistributedImpl.getHazelcastInstanceInitializer();
        Assert.assertNull(actualHazelcastInstanceInitializer);
    }
}