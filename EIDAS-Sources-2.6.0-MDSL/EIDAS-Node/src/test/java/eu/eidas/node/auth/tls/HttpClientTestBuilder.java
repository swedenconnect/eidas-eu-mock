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
package eu.eidas.node.auth.tls;

import net.shibboleth.utilities.java.support.httpclient.HttpClientBuilder;
import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactory;
import net.shibboleth.utilities.java.support.httpclient.TLSSocketFactoryBuilder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.HttpClient;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Arrays;

class HttpClientTestBuilder {
    private CipherSuiteTestData cipherSuiteTestData;

    HttpClientTestBuilder setCipherSuiteTestData(CipherSuiteTestData cipherSuiteTestData) {
        this.cipherSuiteTestData = cipherSuiteTestData;
        return this;
    }

    HttpClient build () throws Exception {
        HttpClientBuilder httpClientBuilder = new HttpClientBuilder();
        TLSSocketFactory factory = newSslSocketFactory();
        httpClientBuilder.setTLSSocketFactory(factory);

        return httpClientBuilder.buildClient();
    }

    private TLSSocketFactory newSslSocketFactory() {

        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        final TLSSocketFactoryBuilder tlsSocketFactoryBuilder = new TLSSocketFactoryBuilder()
                .setHostnameVerifier(TLSSocketFactory.STRICT_HOSTNAME_VERIFIER)
                .setTrustManagers(Arrays.asList(trustAllCerts));
        if (CollectionUtils.isNotEmpty(cipherSuiteTestData.getTlsVersions())) {
            tlsSocketFactoryBuilder.setEnabledProtocols(TlsVersion.toStringList(cipherSuiteTestData.getTlsVersions()));
        }
        if (CollectionUtils.isNotEmpty(cipherSuiteTestData.getCipherSuites())) {
            tlsSocketFactoryBuilder.setEnabledCipherSuites(EIDASCipherSuite.toStringList(cipherSuiteTestData.getCipherSuites()));
        }

        System.out.println("CLIENT ENABLED PROTOCOLS: " + tlsSocketFactoryBuilder.getEnabledProtocols());
        System.out.println("CLIENT ENABLED CIPHER SUITES: " + tlsSocketFactoryBuilder.getEnabledCipherSuites());
        return tlsSocketFactoryBuilder.build();
    }
}
