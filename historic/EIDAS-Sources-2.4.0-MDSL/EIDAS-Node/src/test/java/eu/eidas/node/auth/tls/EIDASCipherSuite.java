package eu.eidas.node.auth.tls;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.Collections2;
import org.apache.commons.collections.CollectionUtils;

import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static eu.eidas.node.auth.tls.JdkVersion.JDK_7;
import static eu.eidas.node.auth.tls.JdkVersion.JDK_8;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_1;
import static eu.eidas.node.auth.tls.TlsVersion.TLS1_2;
import static java.util.Collections.singletonList;

/**
 * This enum holds all the TLS cipher suites supported by EIDAS.
 *
 * The list of supported cipher suites is based on the following document:
 * eIDAS - Crypto Requirements for the eIDAS Interoperability Framework_v1.
 *
 * The TLS version associated with each cipher suite was based on the following Internet
 * Engineering Task force: rfc4346 (TLS1.1),  rfc4492 (TLS_1.1 elliptic curve),
 * rfc5246 (TLS1.2), rfc5288 (TLS1.2 Galois Counter Mode) and rfc5289 (TLS 1.2 elliptic curve)
 *
 * The JDK version associated with each cipher suite is based on the following Oracle web information page
 * https://docs.oracle.com/javase/8/docs/technotes/guides/security/SunProviders.html#SunJSSEProvider
 *
 */
public enum EIDASCipherSuite {

    // TLS 1.1 Cipher suites
    TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_7, JDK_8),
            false
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_7, JDK_8),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_7, JDK_8),
            false
    ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA(
            singletonList(TLS1_1),
            Arrays.asList(JDK_7, JDK_8),
            true //Unlimited JCE required
    ),

    // TLS1.1 AND TLS1.2 Cipher suites
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA(
            Arrays.asList(TLS1_1, TLS1_2),
            Arrays.asList(JDK_8), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            false
    ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA(
            Arrays.asList(TLS1_1, TLS1_2),
            Arrays.asList(JDK_8), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            true //Unlimited JCE required
    ),

   // TLS1.2 Cipher suites
   TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256(
           singletonList(TLS1_2),
           Arrays.asList(JDK_7, JDK_8),
           false
   ),
   TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256(
           singletonList(TLS1_2),
           singletonList(JDK_8),
           false
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384(
           singletonList(TLS1_2),
           Arrays.asList(JDK_7,JDK_8),
           true //Unlimited JCE required
    ),
    TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            singletonList(JDK_8),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256(
           singletonList(TLS1_2),
           Arrays.asList(JDK_7, JDK_8),
           false
    ),
    TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256(
            singletonList(TLS1_2),
            singletonList(JDK_8),
            false
    ),
    TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384(
            singletonList(TLS1_2),
            Arrays.asList(JDK_7, JDK_8),
            true //Unlimited JCE required
    ),
    TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            singletonList(JDK_8),
            true //Unlimited JCE required
    ),
    TLS_DHE_RSA_WITH_AES_128_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_8), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            false
    ),
    TLS_DHE_RSA_WITH_AES_128_GCM_SHA256(
            singletonList(TLS1_2),
            singletonList(JDK_8),
            false
    ),
    TLS_DHE_RSA_WITH_AES_256_CBC_SHA256(
            singletonList(TLS1_2),
            Arrays.asList(JDK_8), // Ephemeral key length of 2048 is required by eIDAS specs but not supported by Java7
            true //Unlimited JCE required
    ),
    TLS_DHE_RSA_WITH_AES_256_GCM_SHA384(
            singletonList(TLS1_2),
            singletonList(JDK_8),
            true //Unlimited JCE required
    );

    private final List<TlsVersion> tlsVersions;
    private final List<JdkVersion> jdkVersions;
    private final boolean unlimitedJCE;
    private final String strValue;

    public final static int EIDAS_NB_OF_SUPPORTED_CIPHER_SUITES = 18;

    EIDASCipherSuite (List<TlsVersion> tlsVersions, List<JdkVersion> jdkVersions, boolean unlimitedJCE) {
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(tlsVersions), "Cipher Suite supported TLS version(s) must be provided");
        Preconditions.checkArgument(CollectionUtils.isNotEmpty(tlsVersions), "Cipher Suite supported JDK version(s) must be provided");
        this.tlsVersions = tlsVersions;
        this.jdkVersions = jdkVersions;
        this.unlimitedJCE = unlimitedJCE;
        strValue = " for " + tlsVersions + " and " + jdkVersions +" Unlimited JCE" +
                             (unlimitedJCE ? " required" : "not required");
    }

    /**
     *
     * @param filteredTls: Filter the EIDAS cipher supported according to the selected TLS versins
     * @param filteredJdk: Filter the EIDAS cipher supported according to the selected JDK versions
     * @return All the EIDAS cipher suites matching the TLS selection AND the JDK selection.
     */
    public static Collection<EIDASCipherSuite> selectCipherSuites(final List<TlsVersion> filteredTls,
                                                                  final List<JdkVersion> filteredJdk) {
        Predicate<EIDASCipherSuite> tlsPredicate = new Predicate<EIDASCipherSuite>() {
            @Override
            public boolean apply(EIDASCipherSuite input) {
                return input.getTlsVersions().containsAll(filteredTls);
            }
        };
        Predicate<EIDASCipherSuite> jdkPredicate = new Predicate<EIDASCipherSuite>() {
            @Override
            public boolean apply(EIDASCipherSuite input) {
                return input.getJdkVersions().containsAll(filteredJdk);
            }
        };
        Collection<EIDASCipherSuite> result;
        if (CollectionUtils.isNotEmpty(filteredTls) && CollectionUtils.isNotEmpty(filteredJdk)) {
            result = Collections2.filter(Arrays.asList(EIDASCipherSuite.values()),
                    Predicates.and(tlsPredicate,jdkPredicate));

        } else if (CollectionUtils.isNotEmpty(filteredTls)) {
            result = Collections2.filter(Arrays.asList(EIDASCipherSuite.values()),tlsPredicate);
        } else {
            result = Collections2.filter(Arrays.asList(EIDASCipherSuite.values()),jdkPredicate);
        }

        return result;
    }

    /**
     *
     * @param filteredTls: Filter the EIDAS cipher supported according to the selected TLS versins
     * @param filteredJdk: Filter the EIDAS cipher supported according to the selected JDK versions
     * @return All the EIDAS cipher suites matching the TLS selection AND the JDK selection taking in account the
     * currently activated JCE Policy.
     */
    public static Collection<EIDASCipherSuite> jcePolicyCipherSuites(final List<TlsVersion> filteredTls,
                                                                     final List<JdkVersion> filteredJdk) {
        Collection<EIDASCipherSuite> result = selectCipherSuites(filteredTls, filteredJdk);

        if (limitedJCEActivated()) {
            Predicate<EIDASCipherSuite> jcePredicate = new Predicate<EIDASCipherSuite>() {
                @Override
                public boolean apply(EIDASCipherSuite input) {
                    return input.needsLimitedJCE();
                }
            };
            result = Collections2.filter(result,jcePredicate);
        }
        return result;
    }

    /**
     * true if limited JCE Policy is activated, false otherwise
     */
    public static boolean limitedJCEActivated () {
        boolean limitedJCE = false;
        try {
            limitedJCE = Cipher.getMaxAllowedKeyLength("RC5") < 256;
        } catch (NoSuchAlgorithmException e) {
            Throwables.propagate(e);
        }
        return limitedJCE;
    }

    /**
     * @param cipherSuites: EIDAS Cipher suite collection to be converted
     */
    public static List<String> toStringList(Collection<EIDASCipherSuite> cipherSuites) {
        List<String>  result = new ArrayList<>();
        for (EIDASCipherSuite cipherSuite : cipherSuites) {
            result.add(cipherSuite.toString());
        }
        return result;
    }

    /**
     * @param cipherSuites: EIDAS Cipher suite collection to be converted
     */
    public static String[] toStringArray(Collection<EIDASCipherSuite> cipherSuites) {
        String[]  result = new String[cipherSuites.size()];
        int ndx = 0;
        for (EIDASCipherSuite cipherSuite : cipherSuites) {
            result[ndx++] = cipherSuite.toString();
        }
        return result;
    }

    /** TLS Version filters**/
    private List<TlsVersion> getTlsVersions() {
        return tlsVersions;
    }

    /** JDK Version filters**/
    private List<JdkVersion> getJdkVersions() {
        return jdkVersions;
    }

    /**
     *
     * @return true if unlimited JCE is required for this Cipher Suite. False otherwise
     */
    private boolean needsUnlimitedJCE() {
        return unlimitedJCE;
    }

    /**
     *
     * @return true if limited JCE is required for this Cipher Suite. False otherwise
     */
    private boolean needsLimitedJCE() {
        return !needsUnlimitedJCE();
    }


    /**
     * We don't want to override the default toString method. This method is used for more detailed logging
     * purposes
     * @return The String to be printed out
     */
    public String print () {
        return strValue;
    }
}
