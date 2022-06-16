/*
 * Copyright (c) 2021 by European Commission
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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.*;
import org.junit.rules.ExpectedException;

import javax.net.ssl.SSLHandshakeException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import static eu.eidas.node.auth.tls.HttpServerTestBuilder.EXPECTED_RESPONSE_MESSAGE;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

/**
 * This class explores Client/Server configuration with various supported EIDAS Cipher suites.
 * <p>
 * All the tests will run on java7 and Java8 JREs.
 * All the tests will also run whether or not the unlimited JCE policy is activated on the running JRE.
 */
public class HTTPSConnectionWithEIDASCipherSuiteTest {

    private final static String JAVA_VERSION = System.getProperty("java.version");
    private static int JAVA_VERSION_MAJOR;
    private static int JAVA_VERSION_MINOR;
    private static int JAVA_VERSION_SECURITY;
    private final static JdkVersion CURRENT_JDK = JdkVersion.lookup(JAVA_VERSION.substring(0, 3));

    private HttpServerTestBuilder serverBuilder;
    private CipherSuiteTestData serverCipherSuite;

    private CipherSuiteTestData clientCipherSuite;

    private HttpClient httpClient;

    private static Map<String, KeyStoreHolder> keyStoresMap = new HashMap<>();

    public KeyStoreHolder getKeyStore(String keyAlg, int keySize) throws Exception {
        String keystoreMapKey = keyAlg + "_" + keySize;
        KeyStoreHolder keystoreHolder = keyStoresMap.get(keystoreMapKey);
        if (keystoreHolder == null) {
            keystoreHolder = KeyStoreHolder.build(keyAlg, keySize);
            keyStoresMap.put(keystoreMapKey, keystoreHolder);
        }
        return keystoreHolder;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUpClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
        getJavaVersions();
    }

    @After
    public void tearDown() {
        if (serverBuilder != null) {
            serverBuilder.stopServer();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        for (KeyStoreHolder keystoreHolder : keyStoresMap.values()) {
            try {
                Files.deleteIfExists(Paths.get(keystoreHolder.getKeyStorePath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
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
                .setKeystore(getKeyStore("RSA", 1024))
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
                .setKeystore(getKeyStore("EC", 256))
                .build();

        HttpResponse response = performHTTPS(httpClient);
        assertResponse(response);
    }

    @Test
    public void testHttpClientWithTLS1_1IfSupportedByJdkAndAlgorithmRSA1024() throws Exception {
        if (jdkSupportsTls1_1()) {
            clientServerConnection("RSA", 1024, TLS1_1);
        }
    }

    @Test
    public void testHttpClientWithTLS1_1IfSupportedByJdkAndAlgorithmEC256() throws Exception {
        if (jdkSupportsTls1_1()) {
            clientServerConnection("EC", 256, TLS1_1);
        }
    }

    @Test
    public void testHttpClientWithTLS1_1IfNotSupportedByJdkRSA1024() throws Exception {
        if (!jdkSupportsTls1_1()) {
            expectedException.expect(SSLHandshakeException.class);
            clientServerConnection("RSA", 1024, TLS1_1);
        }
    }

    @Test
    public void testHttpClientWithTLS1_1IfNotSupportedByJdkEC256() throws Exception {
        if (!jdkSupportsTls1_1()) {
            expectedException.expect(SSLHandshakeException.class);
            clientServerConnection("EC", 256, TLS1_1);
        }
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
    public void incompatibleClientServerCS() throws Exception {
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
                .setKeystore(getKeyStore("EC", 256))
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
                .setKeystore(getKeyStore(alg, keySize))
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

    private boolean jdkSupportsTls1_1() {
        if (JAVA_VERSION_MAJOR == 7
                && JAVA_VERSION_MINOR >= 301) {
            return false;
        }

        else if (JAVA_VERSION_MAJOR == 8
                && JAVA_VERSION_MINOR >= 291) {
            return false;
        }

        else if (JAVA_VERSION_MAJOR == 11) {
            if (JAVA_VERSION_MINOR == 0
                    && JAVA_VERSION_SECURITY >= 11) {
                return false;
            } else if (JAVA_VERSION_MINOR > 0) {
                return false;
            }
        }

        else if (JAVA_VERSION_MAJOR == 13) {
            if (JAVA_VERSION_MINOR == 0
                    && JAVA_VERSION_SECURITY >= 8) {
                return false;
            } else if (JAVA_VERSION_MINOR > 0) {
                return false;
            }
        }

        else if (JAVA_VERSION_MAJOR == 15) {
            if (JAVA_VERSION_MINOR == 0
                    && JAVA_VERSION_SECURITY >= 3) {
                return false;
            } else if (JAVA_VERSION_MINOR > 0) {
                return false;
            }
        }

        else if (JAVA_VERSION_MAJOR == 16) {
            if (JAVA_VERSION_MINOR == 0
                    && JAVA_VERSION_SECURITY >= 1) {
                return false;
            } else if (JAVA_VERSION_MINOR > 0) {
                return false;
            }
        }

        else if (JAVA_VERSION_MAJOR > 16) {
            return false;
        }
        return true;
    }

    private static void getJavaVersions() {
        if (JAVA_VERSION.startsWith("1.")) {
            JAVA_VERSION_MAJOR = Integer.parseInt(JAVA_VERSION.substring(2, 3));
            JAVA_VERSION_MINOR = Integer.parseInt(JAVA_VERSION.substring(JAVA_VERSION.lastIndexOf("_") + 1));
        } else {
            String[] versions = JAVA_VERSION.split("\\.");
            JAVA_VERSION_MAJOR = Integer.parseInt(versions[0]);
            JAVA_VERSION_MINOR = Integer.parseInt(versions[1]);
            JAVA_VERSION_SECURITY = Integer.parseInt(versions[2]);
        }
    }
}
