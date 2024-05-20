/*
 * Copyright (c) 2020 by European Commission
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

import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactory;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.HostnameVerifier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link BaseMetadataFetcher}
 */
public class BaseMetadataFetcherTest {

    /**
     * Test method for {@link BaseMetadataFetcher#newSslSocketFactory()}.
     * In this method, we check if newSslSocketFactory() method returns a correctly configured tlsSocketFactory object
     *
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
