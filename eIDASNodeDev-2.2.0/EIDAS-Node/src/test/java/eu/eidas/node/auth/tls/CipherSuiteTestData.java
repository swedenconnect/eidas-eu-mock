package eu.eidas.node.auth.tls;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

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
     *
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
        Preconditions.checkArgument(isNotEmpty(tlsVersions) || isNotEmpty(jdkVersions),
                "tlsVersions and jdkVersions can not be both empty");
        cipherSuites = EIDASCipherSuite.selectCipherSuites(tlsVersions, jdkVersions);
        return this;
    }

    /**
     *
     * Filter the EIDAS Cipher suites taking in account the JCE Policy and stores it internally
     */
    CipherSuiteTestData buildLimitedCipherSuites() {
        Preconditions.checkArgument(isNotEmpty(tlsVersions) || isNotEmpty(jdkVersions),
                "tlsVersions and jdkVersions can not be both empty");
        cipherSuites = EIDASCipherSuite.jcePolicyCipherSuites(tlsVersions, jdkVersions);
        return this;
    }
}
