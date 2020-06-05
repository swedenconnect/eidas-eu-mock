package eu.eidas.node.auth.tls;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;

import static eu.eidas.node.auth.tls.HttpServerTestBuilder.EXPECTED_RESPONSE_MESSAGE;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * This class explores Client/Server configuration with various supported EIDAS Cipher suites.
 *
 * All the tests will run on java7 and Java8 JREs.
 * All the tests will also run whether or not the unlimited JCE policy is activated on the running JRE.
 */
public class HTTPSConnectionWithEIDASCipherSuiteTest {

    private final static String JAVA_VERSION = System.getProperty("java.version");
    private final static JdkVersion CURRENT_JDK = JdkVersion.lookup(JAVA_VERSION.substring(0,3));

    private HttpServerTestBuilder serverBuilder;
    private CipherSuiteTestData serverCipherSuite;

    private CipherSuiteTestData clientCipherSuite;

    private HttpClient httpClient;

    @After
    public void tearDown () {
        serverBuilder.stopServer();
    }

    @Test
    public void AllTlsCurrentJdkClientServerCS_RSA_1024() throws Exception {

            clientCipherSuite = new CipherSuiteTestData()
                    .setJdkVersions(CURRENT_JDK)
                    .buildLimitedCipherSuites();

            httpClient = new HttpClientTestBuilder()
                    .setCipherSuiteTestData(clientCipherSuite)
                    .build();

            serverCipherSuite = clientCipherSuite;

            serverBuilder = new HttpServerTestBuilder()
                    .setCipherSuiteTesttData(serverCipherSuite)
                    .setKeyAlg("RSA")
                    .setKeySize(1024)
                    .build();


            HttpResponse response = performHTTPS(httpClient);
            assertResponse(response);
    }

    @Test
    public void AllTlsCurrentJdkClientServerCS_EC_256() throws Exception {

        clientCipherSuite = new CipherSuiteTestData()
                .setJdkVersions(CURRENT_JDK)
                .buildLimitedCipherSuites();

        httpClient = new HttpClientTestBuilder()
                .setCipherSuiteTestData(clientCipherSuite)
                .build();

        serverCipherSuite = clientCipherSuite;

        serverBuilder = new HttpServerTestBuilder()
                .setCipherSuiteTesttData(serverCipherSuite)
                .setKeyAlg("EC")
                .setKeySize(256)
                .build();

        HttpResponse response = performHTTPS(httpClient);
        assertResponse(response);
    }

    @Test
    public void TLS_1ClientCS_TLS_1ServerCS_RSA_1024() throws Exception {
        clientServerConnection("RSA", 1024, TLS1_1);
    }

    @Test
    public void TLS_1ClientCS_TLS_1ServerCS_EC_256() throws Exception {
        clientServerConnection("EC", 256, TLS1_1);
    }

    @Test
    public void TLS_2ClientCS_TLS_2ServerCS_RSA_1024() throws Exception {
        clientServerConnection("RSA", 1024, TLS1_2);
    }

    @Test
    public void TLS_2ClientCS_TLS_2ServerCS_EC_256() throws Exception {
        clientServerConnection("EC", 256, TLS1_2);
    }

    @Test(expected = SSLHandshakeException.class)
    public void incompatibleClientServerCS  () throws Exception {
        // Test incompatible client and server cipher suites were client is configured with
        // EIDAS TLS1_1 cipher suites while server is configured with EIDAS TLS1_2 cipher suites
        clientCipherSuite = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1)
                .setJdkVersions(CURRENT_JDK)
                .buildLimitedCipherSuites();

        httpClient = new HttpClientTestBuilder()
                .setCipherSuiteTestData(clientCipherSuite)
                .build();

        serverCipherSuite = new CipherSuiteTestData()
                .setTlsVersions(TLS1_2)
                .setJdkVersions(CURRENT_JDK)
                .buildCipherSuites();

        serverBuilder = new HttpServerTestBuilder()
                .setCipherSuiteTesttData(serverCipherSuite)
                .setKeyAlg("EC")
                .setKeySize(256)
                .build();

        HttpResponse response = performHTTPS(httpClient);
        assertResponse(response);
    }

    private void clientServerConnection(String alg, int keySize, TlsVersion... tlsVersions) throws Exception {
        clientCipherSuite = new CipherSuiteTestData()
                .setTlsVersions(tlsVersions)
                .setJdkVersions(CURRENT_JDK)
                .buildLimitedCipherSuites();

        httpClient = new HttpClientTestBuilder()
                .setCipherSuiteTestData(clientCipherSuite)
                .build();

        serverCipherSuite = clientCipherSuite;

        serverBuilder = new HttpServerTestBuilder()
                .setCipherSuiteTesttData(serverCipherSuite)
                .setKeyAlg(alg)
                .setKeySize(keySize)
                .build();

        HttpResponse response = performHTTPS(httpClient);
        assertResponse(response);
    }

    private void assertResponse(HttpResponse response) throws java.io.IOException {
        assertThat(response.getStatusLine().toString(), containsString("200"));

        HttpEntity entity = response.getEntity();
        assertThat(EntityUtils.toString(entity), is(EXPECTED_RESPONSE_MESSAGE));
    }

    private HttpResponse performHTTPS(HttpClient httpClient) throws java.io.IOException {
        String url = serverBuilder.getTestUrl();
        HttpGet httpGet = new HttpGet(url);
        return httpClient.execute(httpGet);
    }
}
