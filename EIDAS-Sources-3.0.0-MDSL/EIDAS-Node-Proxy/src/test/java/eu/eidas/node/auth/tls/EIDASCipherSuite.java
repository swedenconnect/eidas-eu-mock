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

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static eu.eidas.node.auth.tls.JdkVersion.JDK_11;
import static eu.eidas.node.auth.tls.JdkVersion.JDK_17;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_3;
import static java.util.Collections.singletonList;

/**
 * This enum holds all the TLS cipher suites supported by EIDAS.
 * <p>
 * The list of supported cipher suites is based on the following document:
 * eIDAS - Crypto Requirements for the eIDAS Interoperability Framework_v1.
 * <p>
 * The TLS version associated with each cipher suite was based on the following Internet
 * Engineering Task force: rfc4346 (TLS1.1),  rfc4492 (TLS_1.1 elliptic curve),
 * rfc5246 (TLS1.2), rfc5288 (TLS1.2 Galois Counter Mode) and rfc5289 (TLS 1.2 elliptic curve)
 * <p>
 * The JDK version associated with each cipher suite is based on the following Oracle web information page
 * https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider
 */
public enum EIDASCipherSuite {

    // TLS 1.1 Cipher suites
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),

    // TLS1.1 AND TLS1.2 Cipher suites
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA(
            Arrays.asList(TLS1_1, TLS1_2),
            Arrays.asList(JDK_11, JDK_17), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            false
    ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA(
            Arrays.asList(TLS1_1, TLS1_2),
            Arrays.asList(JDK_11, JDK_17), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            true //Unlimited JCE required
    ),

    // TLS1.2 Cipher suites
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            false
    ),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            false
    ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            true //Unlimited JCE required
    ),
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_11, JDK_17),
            true
    ),
    //    TLS_AES_128_CCM_SHA256( // Disabled by Default in JDK "Unsupported CipherSuite: TLS_AES_128_CCM_SHA256"
//            singletonList(TLS1_3),
//            Arrays.asList(JDK_11, JDK_17),
//            true
//    ),
    TLS_AES_128_GCM_SHA256(
            singletonList(TLS1_3),
            Arrays.asList(JDK_11, JDK_17),
            true
    ),
    TLS_AES_256_GCM_SHA384(
            singletonList(TLS1_3),
            Arrays.asList(JDK_11, JDK_17),
            true
    ),
    TLS_CHACHA20_POLY1305_SHA256(
            singletonList(TLS1_3),
            Arrays.asList(JDK_11, JDK_17),
            true
    ),
    ;


    private final List<TlsVersion> tlsVersions;
    private final List<JdkVersion> jdkVersions;
    private final boolean unlimitedJCE;
    private final String strValue;

    public final static int EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES = 23;

    EIDASCipherSuite(List<TlsVersion> tlsVersions, List<JdkVersion> jdkVersions, boolean unlimitedJCE) {
        if (tlsVersions == null || tlsVersions.isEmpty()) {
            throw new IllegalArgumentException("Cipher Suite supported TLS version(s) must be provided");
        }
        if (jdkVersions == null || jdkVersions.isEmpty()) {
            throw new IllegalArgumentException("Cipher Suite supported JDK version(s) must be provided");
        }
        this.tlsVersions = tlsVersions;
        this.jdkVersions = jdkVersions;
        this.unlimitedJCE = unlimitedJCE;
        strValue = " for " + tlsVersions + " and " + jdkVersions + " Unlimited JCE" +
                (unlimitedJCE ? " required" : "not required");
    }

    /**
     * @param filteredTls: Filter the EIDAS cipher supported according to the selected TLS versins
     * @param filteredJdk: Filter the EIDAS cipher supported according to the selected JDK versions
     * @return All the EIDAS cipher suites matching the TLS selection AND the JDK selection.
     */
    public static Collection<EIDASCipherSuite> selectCipherSuites(final List<TlsVersion> filteredTls,
                                                                  final List<JdkVersion> filteredJdk) {
        Collection<EIDASCipherSuite> result = new ArrayList<>();
        for (EIDASCipherSuite suite : EIDASCipherSuite.values()) {
            boolean matchesTls = filteredTls.isEmpty() || suite.getTlsVersions().containsAll(filteredTls);
            boolean matchesJdk = filteredJdk.isEmpty() || suite.getJdkVersions().containsAll(filteredJdk);
            if (matchesTls && matchesJdk) {
                result.add(suite);
            }
        }
        return result;
    }

    /**
     * @param filteredTls: Filter the EIDAS cipher supported according to the selected TLS versins
     * @param filteredJdk: Filter the EIDAS cipher supported according to the selected JDK versions
     * @return All the EIDAS cipher suites matching the TLS selection AND the JDK selection taking in account the
     * currently activated JCE Policy.
     */
    public static Collection<EIDASCipherSuite> jcePolicyCipherSuites(final List<TlsVersion> filteredTls,
                                                                     final List<JdkVersion> filteredJdk) {
        Collection<EIDASCipherSuite> result = selectCipherSuites(filteredTls, filteredJdk);

        if (limitedJCEActivated()) {
            result = filterLimitedJCECiphers(result);
        }
        return result;
    }

    /**
     * @return true if limited JCE Policy is activated, false otherwise
     */
    public static boolean limitedJCEActivated() {
        int maxKeyLength;
        try {
            maxKeyLength = Cipher.getMaxAllowedKeyLength("RC5");
        } catch (NoSuchAlgorithmException e) {
            return true;
        }
        return maxKeyLength < 256;
    }

    /**
     * @param cipherSuites: EIDAS Cipher suite collection to be converted
     */
    public static List<String> toStringList(Collection<EIDASCipherSuite> cipherSuites) {
        List<String> result = new ArrayList<>();
        for (EIDASCipherSuite cipherSuite : cipherSuites) {
            result.add(cipherSuite.toString());
        }
        return result;
    }

    /**
     * @param cipherSuites: EIDAS Cipher suite collection to be converted
     */
    public static String[] toStringArray(Collection<EIDASCipherSuite> cipherSuites) {
        String[] result = new String[cipherSuites.size()];
        int ndx = 0;
        for (EIDASCipherSuite cipherSuite : cipherSuites) {
            result[ndx++] = cipherSuite.toString();
        }
        return result;
    }

    private static Collection<EIDASCipherSuite> filterLimitedJCECiphers(Collection<EIDASCipherSuite> suites) {
        Collection<EIDASCipherSuite> limitedJceSuites = new ArrayList<>();
        for (EIDASCipherSuite suite : suites) {
            if (suite.needsLimitedJCE()) {
                limitedJceSuites.add(suite);
            }
        }
        return limitedJceSuites;
    }

    /**
     * TLS Version filters
     **/
    private List<TlsVersion> getTlsVersions() {
        return tlsVersions;
    }

    /**
     * JDK Version filters
     **/
    private List<JdkVersion> getJdkVersions() {
        return jdkVersions;
    }

    /**
     * @return true if unlimited JCE is required for this Cipher Suite. False otherwise
     */
    private boolean needsUnlimitedJCE() {
        return unlimitedJCE;
    }

    /**
     * @return true if limited JCE is required for this Cipher Suite. False otherwise
     */
    private boolean needsLimitedJCE() {
        return !needsUnlimitedJCE();
    }


    /**
     * We don't want to override the default toString method. This method is used for more detailed logging
     * purposes
     *
     * @return The String to be printed out
     */
    public String print() {
        return strValue;
    }
}
