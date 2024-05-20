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

import org.junit.Test;

import static eu.eidas.node.auth.tls.EIDASCipherSuite.EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384;
import static eu.eidas.node.auth.tls.EIDASCipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384;
import static eu.eidas.node.auth.tls.JdkVersion.JDK_11;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.is;


public class EidasCipherSuiteTest {

    @Test
    public void jdkSupport () {
        String javaVersion = System.getProperty("java.version");
        String javaMajorVersion = javaVersion.substring(0,3);
        if (javaMajorVersion.endsWith(".")) {
            javaMajorVersion = javaMajorVersion.substring(0, javaMajorVersion.length() - 1);
        }
        JdkVersion jdkVersion = JdkVersion.lookup(javaMajorVersion);
        assertThat("Unsupported java version " + javaVersion, jdkVersion, notNullValue());
    }

    @Test
    public void supportedCipherSuites () {
        assertThat(EIDASCipherSuite.values().length, is(EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES));
    }

    @Test
    public void selectCipherSuiteAllTlsAndAllJdk () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1, TLS1_2)
                .setJdkVersions(JDK_11)
                .buildCipherSuites();
        assertThat(testData.getCipherSuites(), hasSize(2));
    }

    @Test
    public void selectCipherSuiteTls11AndCurrentJdk () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_1)
                .setJdkVersions(JDK_11)
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
    public void selectCipherSuiteTls12AndCurrentJdk () {
        CipherSuiteTestData testData = new CipherSuiteTestData()
                .setTlsVersions(TLS1_2)
                .setJdkVersions(JDK_11)
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
    public void selectCipherSuiteWithCurrentJdk() {
        String javaVersion = System.getProperty("java.version");
        JdkVersion currentJdk = JdkVersion.lookup(javaVersion.substring(0,3));
        if (currentJdk == JDK_11) {
            CipherSuiteTestData testData = new CipherSuiteTestData()
                    .setJdkVersions(JDK_11)
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
