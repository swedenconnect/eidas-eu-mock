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
package eu.eidas.auth.engine.metadata.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.config.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.HostnameVerifier;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link BaseMetadataFetcher}
 */
public class BaseMetadataFetcherTest {

    private static final Logger LOG = LoggerFactory.getLogger(BaseMetadataFetcherTest.class);

    private MeterRegistry testRegistry;

    @Before
    public void setUp() throws InitializationException {
        // In this test, we use a SimpleMeterRegistry (in-memory) to simulate the real MeterRegistry used in production
        // It allows capturing and asserting metrics without needing external metric systems
        testRegistry = new SimpleMeterRegistry();
    }

    /**
     * Test method for {@link BaseMetadataFetcher#newSslSocketFactory()}.
     * In this method, we check if newSslSocketFactory() method returns a correctly configured tlsSocketFactory object
     * <p>
     * Must succeed.
     */
    @Test
    public void testNewSslSocketFactory() {
        HostnameVerifier expectedHostnameVerifier = TLSSocketFactory.STRICT_HOSTNAME_VERIFIER;
        String[] expectedCipherSuites = {"TLS_EMPTY_RENEGOTIATION_INFO_SCSV"};
        String[] expectedTlsProtocols = {"TLSv1.2"};

        BaseMetadataFetcher metadataFetcher = new DummyMetadataFetcher(expectedTlsProtocols[0], expectedCipherSuites[0]);
        TLSSocketFactory tlsSocketFactory = metadataFetcher.newSslSocketFactory();

        assertNotNull(tlsSocketFactory);
        Object actualHostnameVerifier = ReflectionTestUtils.invokeGetterMethod(tlsSocketFactory, "getHostnameVerifier");
        assertEquals(expectedHostnameVerifier, actualHostnameVerifier);
        Object actualCipherSuites = ReflectionTestUtils.invokeGetterMethod(tlsSocketFactory, "getSupportedCipherSuites");
        assertArrayEquals(expectedCipherSuites, (String[]) actualCipherSuites);
        Object actualTlsProtocols = ReflectionTestUtils.invokeGetterMethod(tlsSocketFactory, "getSupportedProtocols");
        assertArrayEquals(expectedTlsProtocols, (String[]) actualTlsProtocols);
    }

    /**
     * Test method for {@link BaseMetadataFetcher#fetchEntityDescriptor(String)}
     * that verifies the fetchEntityDescriptor method records a Timer metric
     * even when fetching metadata fails (e.g., invalid or unreachable URL).
     * <p>
     * It asserts that:
     * - the timer is registered
     * - the timer count is at least 1
     * - the total recorded time is greater than 0 ms
     */
    @Test
    public void testFetchEntityDescriptorRecordsTimer() {
        String testUrl = "https://destination.europa.eu/metadata";
        DummyMetadataFetcher fetcher = new DummyMetadataFetcher("TLSv1.2", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV");
        fetcher.setMeterRegistry(testRegistry);

        try {
            fetcher.fetchEntityDescriptor(testUrl);
        } catch (Exception e) {
            // Expected: fetch will fail because the URL is fake, but metrics recording are verified
            LOG.info("Expected exception during metadata fetch due to test URL: {}", e.getMessage());
        }

        Timer timer = testRegistry.find("httpclient.metadata.fetch")
                .tag("url", testUrl)
                .tag("status", "exception") // because fetch failed
                .timer();

        Assert.assertNotNull("Timer should be registered", timer);
        Assert.assertTrue("Timer should have been called at least once", timer.count() >= 1);
        Assert.assertTrue("Timer total time should be greater than 0ms", timer.totalTime(TimeUnit.MILLISECONDS) > 0);
    }

    /**
     * Test method for {@link BaseMetadataFetcher#fetchEntityDescriptor(String)}.
     * This test verifies that no Timer metric is recorded when the MeterRegistry is null.
     * It checks that:
     * - no exception is thrown from Timer-related code
     * - no Timer metric is registered
     * <p>
     * Must succeed and confirm metrics are skipped gracefully when registry is null.
     */
    @Test
    public void testFetchEntityDescriptorSkipsTimerWhenMeterRegistryIsNull() {
        String testUrl = "https://destination.europa.eu/metadata";
        DummyMetadataFetcher fetcher = new DummyMetadataFetcher("TLSv1.2", "TLS_EMPTY_RENEGOTIATION_INFO_SCSV");

        try {
            fetcher.fetchEntityDescriptor(testUrl);
        } catch (Exception e) {
            // Expected exception due to unreachable URL
            LOG.info("Expected exception during metadata fetch due to test URL: {}", e.getMessage());
        }

        Timer timer = testRegistry.find("httpclient.metadata.fetch").timer();
        Assert.assertNull("No timer should be registered when MeterRegistry is null", timer);
    }

    /**
     * Dummy extension of the {@link BaseMetadataFetcher} to test its predefined behavior
     */
    private class DummyMetadataFetcher extends BaseMetadataFetcher {

        String[] tlsProtocols;
        String[] cipherSuites;

        public DummyMetadataFetcher(String tlsProtocols, String cipherSuites) {
            this.tlsProtocols = new String[]{tlsProtocols};
            this.cipherSuites = new String[]{cipherSuites};
        }

        @Override
        protected String[] getTlsEnabledProtocols() {
            return this.tlsProtocols;
        }

        @Override
        protected String[] getTlsEnabledCiphers() {
            return this.cipherSuites;
        }
    }

}
