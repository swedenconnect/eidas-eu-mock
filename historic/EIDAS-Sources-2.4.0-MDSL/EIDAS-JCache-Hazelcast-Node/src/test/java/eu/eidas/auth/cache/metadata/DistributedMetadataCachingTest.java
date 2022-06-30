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

import eu.eidas.auth.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Test class for {@link DistributedMetadataCaching}
 */
public class DistributedMetadataCachingTest {

    /**
     * The expected exception.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#getMap()}
     * when the map field references an instance of a {@link ConcurrentMap}
     * <p/>
     * Must succeed.
     */
    @Test
    public void getMap() {
        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        ConcurrentHashMap<String, EidasMetadataParametersI> expectedMap = new ConcurrentHashMap<>();
        distributedMetadataCaching.map = expectedMap;
        Map<String, EidasMetadataParametersI> actualMap = distributedMetadataCaching.getMap();

        Assert.assertEquals(expectedMap, actualMap);
    }

    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#getMap()}
     * when the map field references an instance of a {@link ConcurrentMap}
     * <p/>
     * Must fail and throw {@link InvalidParameterEIDASException}.
     */
    @Test
    public void getMapWhenMapIsNotSetBefore() {
        thrown.expect(InvalidParameterEIDASException.class);

        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        Map<String, EidasMetadataParametersI> actualMap = distributedMetadataCaching.getMap();

        Assert.assertNull(actualMap);
    }

    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#getCacheName()}
     * when the map field references an instance of a {@link ConcurrentMap}
     * when the cacheName field is not set before.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getCacheName() {
        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        distributedMetadataCaching.map = new ConcurrentHashMap<>();
        String actualCacheName = distributedMetadataCaching.getCacheName();

        Assert.assertNull(actualCacheName);
    }

    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#setCacheName()}
     * when the cacheName field is not set before.
     * <p/>
     * Must succeed.
     */
    @Test
    public void setCacheName() {
        final DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        final String expectedCacheName = "expectedCacheName";
        distributedMetadataCaching.setCacheName(expectedCacheName);

        final String actualCacheName = distributedMetadataCaching.getCacheName();
        Assert.assertEquals(expectedCacheName, actualCacheName);
    }

    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#getHazelcastInstanceInitializer()}
     * when the hazelcastInstanceInitializer field is not set before.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getHazelcastInstanceInitializer() {
        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        HazelcastInstanceInitializer hazelcastInstanceInitializer = distributedMetadataCaching.getHazelcastInstanceInitializer();
        Assert.assertNull(hazelcastInstanceInitializer);
    }

    /**
     * Test method for
     * {@link DistributedMetadataCachingTest#setHazelcastInstanceInitializer()}
     * when the hazelcastInstanceInitializer field is not set before.
     * <p/>
     * Must succeed.
     */
    @Test
    public void setHazelcastInstanceInitializer() {
        DistributedMetadataCaching distributedMetadataCaching = new DistributedMetadataCaching();
        HazelcastInstanceInitializer expectedHazelcastInstanceInitializer = new HazelcastInstanceInitializer();
        distributedMetadataCaching.setHazelcastInstanceInitializer(expectedHazelcastInstanceInitializer);

        HazelcastInstanceInitializer actualHazelcastInstanceInitializer = distributedMetadataCaching.getHazelcastInstanceInitializer();

        Assert.assertEquals(expectedHazelcastInstanceInitializer, actualHazelcastInstanceInitializer);
    }
}