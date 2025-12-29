/*
 * Copyright (c) 2025 by European Commission
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

import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;

public class IgniteMarshallerWhitelistTest {

    private Ignite ignite;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        String whitelistPath = new File("src/test/resources/whitelist.txt").getAbsolutePath();
        System.setProperty("IGNITE_MARSHALLER_WHITELIST", whitelistPath);

        ignite = Ignition.start("src/test/resources/ignite-test-config.xml");
    }

    /**
     * Verifies that EidasMetadataParameters (present in the whitelist) can be stored and retrieved from the cache.
     */
    @Test
    public void testAllowedEidasMetadataParameters() {
        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", new EidasMetadataParameters());
        Object val = cache.get("key");
        Assert.assertNotNull(val);
        Assert.assertTrue(val instanceof EidasMetadataParametersI);
    }

    /**
     * Verifies that java.lang.String (present in the whitelist) can be stored and retrieved from the cache.
     */
    @Test
    public void testAllowedString() {
        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", "test-value");
        String val = (String) cache.get("key");
        Assert.assertEquals("test-value", val);
    }

    /**
     * Verifies that StoredAuthenticationRequest (when included in the whitelist) is accepted.
     */
    @Test
    public void testAllowedStoredAuthenticationRequest() {
        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", StoredAuthenticationRequest.builder());
        Object result = cache.get("key");
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof StoredAuthenticationRequest.Builder);
    }

    /**
     * Verifies that even if the top-level class is whitelisted, deserialization fails
     * if a nested class inside it is not whitelisted.
     */
    @Test
    public void testNestedObjectRequiresWhitelist() {
        expectedException.expect(Exception.class);

        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", new ClassWithNestedObject());

        cache.get("key");
    }

    /**
     * Verifies that a class not present in the whitelist (e.g. RandomClassToBeRejected) is rejected during deserialization.
     */
    @Test
    public void testBlockedCustomClass() {
        expectedException.expect(Exception.class);

        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", new RandomClassToBeRejected());
        cache.get("key");
    }

    /**
     * Verifies that java.lang.Object is accepted due to its presence in classnames-jdk.properties
     * and not explicitly rejected by Ignite.
     */
    @Test
    public void testBlockedObject() {
        IgniteCache<String, Object> cache = ignite.getOrCreateCache("testCache");
        cache.put("key", new Object());
        Object result = cache.get("key");
        Assert.assertNotNull(result);
    }

    @After
    public void tearDown() {
        if (ignite != null)
            ignite.close();
    }
}


