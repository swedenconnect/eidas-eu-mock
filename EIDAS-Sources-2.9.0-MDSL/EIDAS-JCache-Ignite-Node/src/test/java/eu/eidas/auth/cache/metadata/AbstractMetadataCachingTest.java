/*
 * Copyright (c) 2024 by European Commission
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

package eu.eidas.auth.cache.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import javax.cache.Cache;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class for {@link AbstractMetadataCaching}
 */
public class AbstractMetadataCachingTest {

    private final static String url = "url";

    /**
     * Test method for {@link AbstractMetadataCaching#getEidasMetadataParameters(String)}
     *
     * <p>
     * Must succeed.
     */
    @Test
    public void getEidasMetadataParameters() {
        final Cache<String, EidasMetadataParametersI> cache = mock(Cache.class);
        EidasMetadataParameters eidasMetadataParametersExpected = new EidasMetadataParameters();

        when(cache.get(url)).thenReturn(eidasMetadataParametersExpected);
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class, Mockito.CALLS_REAL_METHODS);

        when(abstractMetadataCaching.getCache()).thenReturn(cache);
        EidasMetadataParametersI eidasMetadataParametersActual = abstractMetadataCaching.getEidasMetadataParameters(url);

        Assert.assertEquals(eidasMetadataParametersExpected,eidasMetadataParametersActual);
    }

    /**
     * Test method for {@link AbstractMetadataCaching#getEidasMetadataParameters(String)}
     *
     * when {@link AbstractMetadataCaching#getCache()} returns null
     * <p>
     * Must succeed.
     */
    @Test
    public void getEidasMetadataParametersWhenGetChacheReturnNull() {
        AbstractMetadataCaching mockAbstractMetadataCaching = mock(AbstractMetadataCaching.class);
        when(mockAbstractMetadataCaching.getCache()).thenReturn(null);

        EidasMetadataParametersI eidasMetadataParametersActual = mockAbstractMetadataCaching.getEidasMetadataParameters(null);
        Assert.assertNull(eidasMetadataParametersActual);
    }

    /**
     * Test method for {@link AbstractMetadataCaching#putEidasMetadataParameters(String, EidasMetadataParametersI)}
     *
     * <p>
     * Must succeed.
     */
    @Test
    public void putEidasMetadataParameters() {
        final Cache<String, EidasMetadataParametersI> cache = mock(Cache.class);
        EidasMetadataParameters eidasMetadataParametersExpected = new EidasMetadataParameters();

        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class, Mockito.CALLS_REAL_METHODS);
        when(abstractMetadataCaching.getCache()).thenReturn(cache);
        abstractMetadataCaching.putEidasMetadataParameters(url, eidasMetadataParametersExpected);
    }

    /**
     * Test method for
     * {@link AbstractMetadataCaching#clearCachingService()}
     * when the cache is not empty
     * and the cache is cleared,
     * the cache should no longer contain elements
     */
    @Test
    public void clearCachingServiceTest() {
        final Cache<String, EidasMetadataParametersI> cache = mock(Cache.class);
        final AbstractMetadataCaching abstractMetadataCaching = new AbstractMetadataCaching() {
            @Override
            protected Cache<String, EidasMetadataParametersI> getCache() {
                return cache;
            }
        };
        abstractMetadataCaching.clearCachingService();

        verify(cache, atLeastOnce()).clear();
    }
}