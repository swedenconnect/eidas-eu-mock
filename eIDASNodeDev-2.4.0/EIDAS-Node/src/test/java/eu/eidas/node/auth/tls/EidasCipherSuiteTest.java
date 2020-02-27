package eu.eidas.node.auth.tls;

import org.junit.Test;

import static eu.eidas.node.auth.tls.EIDASCipherSuite.*;
import static eu.eidas.node.auth.tls.JdkVersion.JDK_7;
import static eu.eidas.node.auth.tls.JdkVersion.JDK_8;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EidasCipherSuiteTest {

    @Test
    public void jdkSupport () {
        String javaVersion = System.getProperty("java.version");
        JdkVersion jdkVersion = JdkVersion.lookup(javaVersion.substring(0,3));
        assertThat("Unsupported java version " + javaVersion, jdkVersion, notNullValue());
    }

    @Test
    public void supportedCipherSuites () {
        assertThat(EIDASCipherSuite.values().length, is(EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES));
    }

    @Test
    public void selectCipherSuite_AllTls_And_AllJdk () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1, TLS1_2)
                .setJdkVersions(JDK_7, JDK_8)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(0));
    }

    @Test
    public void selectCipherSuite_Tls11_And_Jdk7 () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1)
                .setJdkVersions(JDK_7)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(4));
        assertThat(testData.getCipherSuites(), containsInAnyOrder(
                TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA
                ));
    }

    @Test
    public void selectCipherSuite_Tls12_And_Jdk7 () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_2)
                .setJdkVersions(JDK_7)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(4));
        assertThat(testData.getCipherSuites(), containsInAnyOrder(
                TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
                TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
        ));
    }

    @Test
    public void selectCipherSuite_Tls11_And_Jdk8 () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1)
                .setJdkVersions(JDK_8)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(6));
        assertThat(testData.getCipherSuites(), containsInAnyOrder(
                TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                TLS_DHE_RSA_WITH_AES_256_CBC_SHA
        ));
    }

    @Test
    public void selectCipherSuite_Tls12_And_Jdk8 () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_2)
                .setJdkVersions(JDK_8)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(14));
        assertThat(testData.getCipherSuites(), containsInAnyOrder(
                TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                TLS_DHE_RSA_WITH_AES_256_CBC_SHA,
                TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
                TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,
                TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
                TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
                TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,
                TLS_DHE_RSA_WITH_AES_256_GCM_SHA384
        ));
    }

    @Test
    public void selectCipherSuite_Jdk7() {
        String javaVersion = System.getProperty("java.version");
        JdkVersion currentJdk = JdkVersion.lookup(javaVersion.substring(0,3));
        if (currentJdk == JDK_7) {
            CipherSuiteTestData testData = new CipherSuiteTestData()
                    .setJdkVersions(JDK_7)
                    .buildLimitedCipherSuites();
            if (EIDASCipherSuite.limitedJCEActivated()) {
                assertThat(testData.getCipherSuites(), hasSize(4));
                assertThat(testData.getCipherSuites(), containsInAnyOrder(
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256
                ));
            } else {
                assertThat(testData.getCipherSuites(), hasSize(8));
                assertThat(testData.getCipherSuites(), containsInAnyOrder(
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                        TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                        TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384
                ));
            }
        }
    }

    @Test
    public void selectCipherSuite_Jdk8() {
        String javaVersion = System.getProperty("java.version");
        JdkVersion currentJdk = JdkVersion.lookup(javaVersion.substring(0,3));
        if (currentJdk == JDK_8) {
            CipherSuiteTestData testData = new CipherSuiteTestData()
                    .setJdkVersions(JDK_8)
                    .buildLimitedCipherSuites();
            for (EIDASCipherSuite cipherSuite :testData.getCipherSuites()) {
                System.out.println(cipherSuite.toString());
            }
            if (EIDASCipherSuite.limitedJCEActivated()) {
                assertThat(testData.getCipherSuites(), hasSize(9));
                assertThat(testData.getCipherSuites(), containsInAnyOrder(
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                        TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256,
                        TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
                        TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        TLS_DHE_RSA_WITH_AES_128_CBC_SHA256,
                        TLS_DHE_RSA_WITH_AES_128_GCM_SHA256
                ));
            } else {
                assertThat(testData.getCipherSuites(), hasSize(EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES));
            }
        }
    }
}
