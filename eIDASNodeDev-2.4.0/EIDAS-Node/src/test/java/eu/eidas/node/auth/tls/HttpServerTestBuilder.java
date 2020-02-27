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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    private final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(1);

    // Password used for the key store
    private final char[] KEYSTORE_PASSWORD = "changeit".toCharArray();

    // Constant LOG for logger
    private static final Logger LOG = LoggerFactory.getLogger(HttpServerTestBuilder.class.getName());


    private HttpsServer httpsServer;
    private CipherSuiteTestData cipherSuiteTesttData;
    private String keyAlg;
    private int keySize;

    HttpServerTestBuilder setCipherSuiteTesttData(CipherSuiteTestData cipherSuiteTesttData) {
        this.cipherSuiteTesttData = cipherSuiteTesttData;
        return this;
    }

    HttpServerTestBuilder setKeyAlg(String keyAlg) {
        this.keyAlg = keyAlg;
        return this;
    }

    HttpServerTestBuilder setKeySize(int keySize) {
        this.keySize = keySize;
        return this;
    }

    HttpServerTestBuilder build () {
        try {
            // Builds the keystore that will be used by the server.
            KeyStore ks = new KeyStoreBuilder()
                    .setKeyStoreType(KeyStore.getDefaultType())
                    .setKeyAlg(keyAlg)
                    .setKeysize(keySize)
                    .build();

            // Setup server key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

            // Initialise server keystore
            kmf.init(ks, KEYSTORE_PASSWORD);

            // Setup server trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

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
            try {
                THREAD_POOL.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // Can be blank
            }
        }
        try {
            Files.deleteIfExists(Paths.get(KeyStoreBuilder.keyStoreFile.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
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

    /**
     * Builds a keystore using keytool command.
     * The keystore will hold one single entry with a private key and a corresponding certificate.
     */
    private static final class KeyStoreBuilder {

        // Password used for the key store
        private final static String KEYSTORE_PASSWORD = "changeit";

        // The Test host DNS name is used by the tested URLs and it MUST be used in the CN name
        // of the generated certificate. This is needed to avoid a missing alias exception
        private final static String TESTHOST_DNS_NAME = "localhost";

        //        String keytoolCmd = "keytool -genkeypair -keystore apikeystore_ECDSA.jks -deststoretype jks -dname \"CN=localhost\" -keypass changeit -storepass changeit -keyalg EC -keysize 256 -validity 250000 -alias localhost";

        /* Generated keystore file */
        private static File keyStoreFile;

        /* Key algorithm used while generating the keystore */
        private String keyAlg;

        /* Key size used while generating the keystore */
        private int keysize;

        /*Used keystore type*/
        private String keyStoreType;

        /* Path of the generated Keystore */
        private String keyStorePath;


        private KeyStoreBuilder setKeyAlg(String keyAlg) {
            this.keyAlg = keyAlg;
            return this;
        }

        private KeyStoreBuilder setKeysize(int keysize) {
            this.keysize = keysize;
            return this;
        }

        private KeyStoreBuilder setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
            return this;
        }

        private KeyStore build () throws Exception {
            String separator = FileSystems.getDefault().getSeparator();

            String keyStoreDir = System.getProperty("user.dir") + separator + "target";
            String keyStoreFileName = "apikeystore-" + keyAlg + "_" + keysize + '.' + keyStoreType;
            keyStorePath = keyStoreDir + separator + keyStoreFileName;

//            keytool -genkeypair -keystore apikeystore-RSA_1024.jks -deststoretype jks -dname "CN=localhost" -keypass changeit -storepass changeit -keyalg RSA -keysize 1024 -validity 1 -alias localhost
            List<String> processArgs = Arrays.asList("keytool",
                                                     "-genkeypair",
                                                     "-keystore", keyStorePath,
                                                     "-deststoretype", keyStoreType,
                                                     "-dname", "CN=" + TESTHOST_DNS_NAME,
                                                     "-keypass", KEYSTORE_PASSWORD,
                                                     "-storepass", KEYSTORE_PASSWORD,
                                                     "-keyalg", keyAlg,
                                                     "-keysize", "" + keysize,
                                                     "-validity", "1",
                                                     "-alias", TESTHOST_DNS_NAME
                    );

            // Launch keystore generation command and wait for its completion
            ProcessBuilder keytool = new ProcessBuilder(processArgs);
            keytool.directory(new File(keyStoreDir));
            keytool.start().waitFor();

            // Loading Keystore
            keyStoreFile = new File(keyStorePath);
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            try (FileInputStream fis = new FileInputStream(keyStoreFile.getPath())) {
                ks.load(fis, KEYSTORE_PASSWORD.toCharArray());
            }

            return ks;
        }
    }
}
