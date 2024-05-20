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

package eu.eidas.node.utils;

import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.specificcommunication.CommunicationCache;
import org.mockito.stubbing.Answer;

import javax.cache.Cache;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HelperUtil {

    /**
     * very basic file reader for testing where files can have a copyright header
     *
     * @param file location starting in the folder above src/
     * @param tag  line to start reading from
     * @return
     */
    public static String readXmlTextFileAfterTag(String file, String tag) {
        try {
            return Files.lines(Paths.get(file)).reduce("", (String buffer, String el) -> {
                if (el.equals(tag)) buffer = "";
                buffer += el + "\n";
                return buffer;
            }).trim();
        } catch (IOException ex) {
            fail("Bad test setup! Cannot read from file:  " + file);
            return null;
        }
    }

    public static CommunicationCache createHashMapCommunicationCacheMock() {
        Cache cache = createCacheMockUsingMap();
        ConcurrentCacheService concurrentCacheService = mock(ConcurrentCacheService.class);
        when(concurrentCacheService.getConfiguredCache()).thenReturn(cache);
        return new CommunicationCache(concurrentCacheService);
    }

    private static Cache<String, String> createCacheMockUsingMap() {
        Map<String, String> map = new HashMap();
        Cache cache = mock(Cache.class);
        when(cache.get(anyString())).thenAnswer((Answer<String>) invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return map.get(key);
        });

        when(cache.getAndRemove(anyString())).thenAnswer((Answer<String>) invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            String value = map.get(key);
            map.remove(key);
            return value;
        });

        doAnswer(invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            String value = invocationOnMock.getArgument(1);
            map.put(key, value);
            return null;
        }).when(cache).put(anyString(), anyString());

        return cache;
    }
}
