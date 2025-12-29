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

import org.junit.Assert;
import org.junit.Test;

import javax.cache.Cache;

/**
 * Test class for {@link ConcurrentMapJcacheServiceDefaultImpl}
 */
public class ConcurrentMapJcacheServiceDefaultImplTest {

    @Test
    public void getConfiguredCache() {

        final ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        final Cache configuredCache = concurrentMapJcacheServiceDefault.getConfiguredCache();

        Assert.assertNotNull(configuredCache);
    }

    @Test
    public void getExpireAfterAccess() {
        final ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        final Long actualExpireAfterAccess = concurrentMapJcacheServiceDefault.getExpireAfterAccess();

        final Long expectedExpireAfterAccess = 1800L;
        Assert.assertEquals(expectedExpireAfterAccess, actualExpireAfterAccess);
    }

    @Test
    public void setExpireAfterAccess() {
        final ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        final Long expectedExpireAfterAccess = 2000L;
        concurrentMapJcacheServiceDefault.setExpireAfterAccess(expectedExpireAfterAccess);

        Long actualExpireAfterAccess = concurrentMapJcacheServiceDefault.getExpireAfterAccess();
        Assert.assertEquals(expectedExpireAfterAccess, actualExpireAfterAccess);
    }

    @Test
    public void getMaximumSize() {
        final ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        final Long actualMaximumSize = concurrentMapJcacheServiceDefault.getMaximumSize();

        final Long expectedMaximumSize = 1000000L;
        Assert.assertEquals(expectedMaximumSize, actualMaximumSize);
    }

    @Test
    public void setMaximumSize() {
        final ConcurrentMapJcacheServiceDefaultImpl concurrentMapJcacheServiceDefault = new ConcurrentMapJcacheServiceDefaultImpl();
        Long expectedMaximumSize = 5000L;
        concurrentMapJcacheServiceDefault.setMaximumSize(expectedMaximumSize);
        final Long actualMaximumSize = concurrentMapJcacheServiceDefault.getMaximumSize();

        Assert.assertEquals(expectedMaximumSize, actualMaximumSize);
    }
}