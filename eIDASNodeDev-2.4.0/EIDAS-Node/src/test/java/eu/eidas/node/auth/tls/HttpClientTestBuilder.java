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
                .setHostnameVerifier(TLSSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
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
