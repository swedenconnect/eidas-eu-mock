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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

class HttpServerTestBuilder {
    //Port use by the server for HTTPS connection
    private int httpsPort;

    // The Test host DNS name is used by the tested URLs and it MUST be used in the CN name
    // of the generated certificate. This is needed to avoid a missing alias exception
    private final static String TESTHOST_DNS_NAME = "localhost";

    // url used for the test.
    private static String url;

    final static String EXPECTED_RESPONSE_MESSAGE = "Medata Fetched";

    // Thread pool used for starting the server.
    final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(1);

    // Password used for the key store
    private final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    // Constant LOG for logger
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerTestBuilder.class.getName());

    private HttpsServer httpsServer;
    private CipherSuiteTestData cipherSuiteTesttData;
    private KeyStore keyStore;

    HttpServerTestBuilder setCipherSuiteTesttData(CipherSuiteTestData cipherSuiteTesttData) {
        this.cipherSuiteTesttData = cipherSuiteTesttData;
        return this;
    }

    HttpServerTestBuilder setKeystore(Supplier<KeyStore> keystoreSupplier) {
        this.keyStore = keystoreSupplier.get();
        return this;
    }

    HttpServerTestBuilder build () {
        try {
            // Setup server key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            // Initialise server keystore
            kmf.init(keyStore, KEYSTORE_PASSWORD);

            // Setup server trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);

            // Setup server HTTPS context and parameters
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            // Creating server and binding it to an unused port
            httpsServer = HttpsServer.create();
            httpsServer.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            httpsPort = httpsServer.getAddress().getPort();
            url = "https://" + TESTHOST_DNS_NAME + ":" + httpsPort + "/test";

            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLEngine engine = sslContext.createSSLEngine();
                        if (CollectionUtils.isNotEmpty(cipherSuiteTesttData.getTlsVersions())) {
                            engine.setEnabledProtocols(TlsVersion.toStringArray(cipherSuiteTesttData.getTlsVersions()));
                        }
                        if (CollectionUtils.isNotEmpty(cipherSuiteTesttData.getCipherSuites())) {
                            engine.setEnabledCipherSuites(EIDASCipherSuite.toStringArray(cipherSuiteTesttData.getCipherSuites()));
                        }

                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        LOG.info("SERVER ENABLED CIPHER SUITES = " + Arrays.asList(engine.getEnabledCipherSuites()));
                        params.setProtocols(engine.getEnabledProtocols());
                        LOG.info("SERVER ENABLED PROTOCOLS = " + Arrays.asList(engine.getEnabledProtocols()));
                    } catch (Exception ex) {
                        LOG.error("Failed to create HTTPS port", ex);
                        throw new RuntimeException(ex);
                    }
                }
            });

            //Create context for test url
            httpsServer.createContext("/test", new MyHandler());

            //Start server with thread pool
            httpsServer.setExecutor(THREAD_POOL);
            httpsServer.start();

        } catch (Exception exception) {
            LOG.info("Failed to create HTTPS server on port " + httpsPort + " of " + TESTHOST_DNS_NAME, exception);
        }
        return this;
    }

    String getTestUrl() {
        return url;
    }

    void stopServer () {
        if (!THREAD_POOL.isShutdown()) {
            httpsServer.stop(0);
        }
    }

    /**
     * Default handler sending to the client the test expected response
     */
    private static final class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, EXPECTED_RESPONSE_MESSAGE.length());
            OutputStream os = t.getResponseBody();
            os.write(EXPECTED_RESPONSE_MESSAGE.getBytes());
            os.close();
        }
    }

}
