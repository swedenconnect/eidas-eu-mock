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

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link AbstractMetadataCaching}
 */
public class AbstractMetadataCachingTest {

    /**
     * Constant for a URL e.g. CONNECTOR
     */
    private static String URL_CONNECTOR_METADATA = "https://localhost/EidasNode/ConnectorMetadata";

    /**
     * Constant for a URL e.g. PROXY-SERVICE
     */
    private static String URL_PROXY_SERVICE_METADATA = "https://localhost/EidasNode/ConnectorMetadata";

    /**
     * Test method for
     * {@link AbstractMetadataCaching#getEidasMetadataParameters(String)}
     * when the map has one url, eidasMetadataParameters pair
     * and that url is used as key to retrieve the value from the map.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getEidasMetadataParameters() {
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class);

        final HashMap<String, EidasMetadataParametersI> map = new HashMap<>();
        when(abstractMetadataCaching.getMap()).thenReturn(map);
        final String url = URL_PROXY_SERVICE_METADATA;
        final EidasMetadataParametersI eidasMetadataParametersActual = abstractMetadataCaching.getEidasMetadataParameters(url);

        EidasMetadataParametersI eidasMetadataParametersExpected = null;
        Assert.assertEquals(eidasMetadataParametersExpected, eidasMetadataParametersActual);
    }

    /**
     * Test method for
     * {@link AbstractMetadataCaching#getEidasMetadataParameters(String)}
     * when the map has one url, eidasMetadataParameters pair
     * and that url is used as key to retrieve the value from the map.
     * <p/>
     * Must succeed.
     */
    @Test
    public void getEidasMetadataParametersNullMap() {
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class);

        when(abstractMetadataCaching.getMap()).thenReturn(null);
        final String url = URL_PROXY_SERVICE_METADATA;
        final EidasMetadataParametersI eidasMetadataParametersActual = abstractMetadataCaching.getEidasMetadataParameters(url);

        EidasMetadataParametersI eidasMetadataParametersExpected = null;
        Assert.assertEquals(eidasMetadataParametersExpected, eidasMetadataParametersActual);
    }

    /**
     * Test method for
     * {@link AbstractMetadataCaching#putEidasMetadataParameters(String, EidasMetadataParametersI)}
     * when the map is empty
     * and a url,eidasMetadataParameters pair is put in the map.
     * <p/>
     * Must succeed.
     */
    @Test
    public void putEidasMetadataParameters() {
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class);

        final HashMap<String, EidasMetadataParametersI> map = new HashMap<>();
        final String url = URL_CONNECTOR_METADATA;
        final EidasMetadataParametersI eidasMetadataParametersExpected = new EidasMetadataParameters();
        abstractMetadataCaching.putEidasMetadataParameters(url, eidasMetadataParametersExpected);
        map.put(url, eidasMetadataParametersExpected);
        when(abstractMetadataCaching.getMap()).thenReturn(map);
        EidasMetadataParametersI eidasMetadataParametersActual = abstractMetadataCaching.getEidasMetadataParameters(url);

        Assert.assertEquals(eidasMetadataParametersExpected, eidasMetadataParametersActual);
    }

    /**
     * Test method for
     * {@link AbstractMetadataCaching#putEidasMetadataParameters(String, EidasMetadataParametersI)}
     * when the map is empty
     * and a url,eidasMetadataParameters pair is put in the map.
     * and when afterwards {@link AbstractMetadataCaching#putEidasMetadataParameters(String, EidasMetadataParametersI)}
     * is called again with the save url but with {@link EidasMetadataParametersI} instance null
     * <p/>
     * Must succeed.
     */
    @Test
    public void putEidasMetadataParametersNullEidasMetadataParameters() {
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class);

        final HashMap<String, EidasMetadataParametersI> map = new HashMap<>();
        when(abstractMetadataCaching.getMap()).thenReturn(map);
        final String url = URL_CONNECTOR_METADATA;
        final EidasMetadataParametersI eidasMetadataParametersOriginal = new EidasMetadataParameters();

        abstractMetadataCaching.putEidasMetadataParameters(url, eidasMetadataParametersOriginal);
        map.put(url, eidasMetadataParametersOriginal);

        abstractMetadataCaching.putEidasMetadataParameters(url, null);
        map.put(url, null);

        final EidasMetadataParametersI eidasMetadataParametersActual = abstractMetadataCaching.getEidasMetadataParameters(url);

        Assert.assertNull(eidasMetadataParametersActual);
    }

    /**
     * Test method for
     * {@link AbstractMetadataCaching#putEidasMetadataParameters(String, EidasMetadataParametersI)}
     * when the {@link AbstractMetadataCaching#getMap()} returns null
     * <p/>
     * Must succeed.
     */
    @Test
    public void putEidasMetadataParametersNullEidasMetadataParametersWhenGetMapReturnsNull() {
        final AbstractMetadataCaching abstractMetadataCaching = mock(AbstractMetadataCaching.class);
        when(abstractMetadataCaching.getMap()).thenReturn(null);
        final String url = URL_CONNECTOR_METADATA;
        abstractMetadataCaching.putEidasMetadataParameters(url, null);
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
        final HashMap<String, EidasMetadataParametersI> map = new HashMap<>();
        map.put("warm", new EidasMetadataParameters());
        final AbstractMetadataCaching abstractMetadataCaching = new AbstractMetadataCaching() {
            @Override
            protected Map<String, EidasMetadataParametersI> getMap() {
                return map;
            }
        };

        abstractMetadataCaching.clearCachingService();

        Assert.assertEquals(0, map.size());
    }
}