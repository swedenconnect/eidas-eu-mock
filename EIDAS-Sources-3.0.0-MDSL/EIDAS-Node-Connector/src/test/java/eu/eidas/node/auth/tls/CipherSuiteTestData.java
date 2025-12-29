/*
 * Copyright (c) 2024 by European Commission
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class CipherSuiteTestData {

    private List<TlsVersion> tlsVersions = new ArrayList<>();
    private List<JdkVersion> jdkVersions = new ArrayList<>();
    private Collection<EIDASCipherSuite> cipherSuites = new ArrayList<>();

    /**
     * @return The Tls filter used for filtering the EIDAS Cipher Suites
     */
    List<TlsVersion> getTlsVersions() {
        return tlsVersions;
    }

    /**
     * @return The internally stored filtered list of EIDAS Cipher suites
     */
    Collection<EIDASCipherSuite> getCipherSuites() {
        return cipherSuites;
    }

    /**
     * Sets the TLS filtering parameter
     * @param tlsVersions:
     */
    CipherSuiteTestData setTlsVersions(TlsVersion... tlsVersions) {
        this.tlsVersions = Arrays.asList(tlsVersions);
        return this;
    }

    /**
     * Sets the Jdk filtering parameter
     * @param jdkVersions:
     */
    CipherSuiteTestData setJdkVersions(JdkVersion... jdkVersions) {
        this.jdkVersions = Arrays.asList(jdkVersions);
        return this;
    }

    /**
     * Filter the EIDAS Cipher suites WITHOOUT taking in account the JCE Policy and stores it internally
     */
    CipherSuiteTestData buildCipherSuites() {
        Objects.requireNonNull(tlsVersions);
        Objects.requireNonNull(jdkVersions);
        if (tlsVersions.isEmpty() && jdkVersions.isEmpty()) {
            throw new IllegalArgumentException("tlsVersions and jdkVersions can not be both empty");
        }

        cipherSuites = EIDASCipherSuite.selectCipherSuites(tlsVersions, jdkVersions);
        return this;
    }

    /**
     *
     * Filter the EIDAS Cipher suites taking in account the JCE Policy and stores it internally
     */
    CipherSuiteTestData buildLimitedCipherSuites() {
        Objects.requireNonNull(tlsVersions);
        Objects.requireNonNull(jdkVersions);
        if (tlsVersions.isEmpty() && jdkVersions.isEmpty()) {
            throw new IllegalArgumentException("tlsVersions and jdkVersions can not be both empty");
        }
        cipherSuites = EIDASCipherSuite.jcePolicyCipherSuites(tlsVersions, jdkVersions);
        return this;
    }
}
