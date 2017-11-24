package se.elegnamnden.eidas.cef.trustconfig;

import com.google.common.collect.ImmutableSet;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCSException;

import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * This class is used to extend the list of trusted certificates with a list of certificates provided in a separate
 * PEM certificate file.
 *
 * <p>This is primary used to extend certificates provided in a JKS trust store in the CEF eIDAS sample implementation code.
 * by ammending the eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator class.</p>
 *
 * <ul>
 *     <li>Add the field: <b>private static final EidasTrustedPEMCertificates trustedPemCerts = new EidasTrustedPEMCertificates();</b></li>
 *     <li>Add row in method getSignatureConfiguration: <b>trustedCertificates = trustedPemCerts.addTrustedCertificates(trustedCertificates);</b></li>
 * </ul>
 *
 * @author Stefan Santesson, 3xA Secruity AB
 * @author Martin Lindström, Litsec AB
 */
public class EidasTrustedPEMCertificates {

    private static final String EIDAS_TRUSTED_CERTS_FILE = "EIDAS_TRUSTED_CERTS_FILE";
    private static final String EIDAS_TRUSTED_CERTS_CONSTRAINTS = "EIDAS_TRUSTED_CERTS_CONSTRAINTS";
    private static final String KEY_STORE_PATH_PARAM = "keyStorePath";
    private String trustedCertsFile;
    private List<String> constraints;

    /**
     * Constructor
     *
     * The constructor is setup using Environment variables
     *
     * <ul>
     *     <li><b>EIDAS_TRUSTED_CERTS_FILE</b>: Path to the PEM certificates file holding one or more trusted PEM certificates</></li>
     *     <li><b>EIDAS_TRUSTED_CERTS_CONSTRAINTS</b>: A comma separated list of either full KeyStore path, or the ending path of a keystore path.
     *     If no constraint is defined, then the list of trusted certificates will always be amended with the PEM list. If the constraint is present,
     *     Then the trusted list will only be amended when the keyStorePath properties matches one of the specified constraints.
     *     </li>
     * </ul>*
     */
    public EidasTrustedPEMCertificates() {
        Map<String, String> env = System.getenv();
        if (env.containsKey(EIDAS_TRUSTED_CERTS_FILE)) {
            this.trustedCertsFile = env.get(EIDAS_TRUSTED_CERTS_FILE);
        }
        if (env.containsKey(EIDAS_TRUSTED_CERTS_CONSTRAINTS)) {
            constraints = Arrays.asList(env.get(EIDAS_TRUSTED_CERTS_CONSTRAINTS).split(","));
        } else {
            constraints = new ArrayList<>();
        }
    }

    /**
     * Returns the ammended list of trusted certificates
     * @param trustedCerts The list of certificates that are already trusted
     * @param properties The eIDAS node parameters relevant to this request
     * @return The list of trusted certificates amended with PEM certificates matching the defined constraints
     */
    public ImmutableSet<X509Certificate> addTrustedCertificates(ImmutableSet<X509Certificate> trustedCerts, Map<String, String> properties) {
        if (trustedCertsFile == null || !isConstraintsMatch(properties)) {
            return trustedCerts;
        }
        List<X509Certificate> compiledCertList = new ArrayList<>();
        for (X509Certificate cert : trustedCerts) {
            compiledCertList.add(cert);
        }

        try {
            List<X509Certificate> certificates = getCertificatesFromPemFile(trustedCertsFile);
            for (X509Certificate cert : certificates) {
                compiledCertList.add(cert);
            }
        } catch (Exception ex) {
        }

        return ImmutableSet.copyOf(compiledCertList);
    }

    private boolean isConstraintsMatch(Map<String, String> params) {
        if (constraints.isEmpty()) {
            return true;
        }
        if (!params.containsKey(KEY_STORE_PATH_PARAM)) {
            return false;
        }
        String keyStorePath = params.get(KEY_STORE_PATH_PARAM);
        for (String constraint : constraints) {
            constraint = constraint.trim();
            if (keyStorePath.equals(constraint) || keyStorePath.endsWith("/" + constraint)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Retrieve a list of PEM objects found in the provided input stream that are of the types PrivateKey (Encrypted or Plaintext), KeyPair or certificate
     *
     * @param fileName name of the PEM resources file
     * @return A list of Certificates
     * @throws IOException
     * @throws OperatorCreationException
     * @throws PKCSException
     */
    public static List<X509Certificate> getCertificatesFromPemFile(String fileName) throws IOException, OperatorCreationException, PKCSException, CertificateException {
        List<X509Certificate> pemObjList = new ArrayList<>();
        Reader rdr = new BufferedReader(new FileReader(fileName));
        PEMParser parser = new PEMParser(rdr);
        Object o;
        while ((o = parser.readObject()) != null) {
            if (o instanceof X509CertificateHolder) {
                pemObjList.add(getCert((X509CertificateHolder) o));
            }
        }
        return pemObjList;
    }

    private static X509Certificate getCert(X509CertificateHolder certificateHolder) throws CertificateException, IOException {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certificateHolder.getEncoded()));
    }

}
