package se.idsec.eidas.cef.trustconfig;

import com.google.common.collect.ImmutableSet;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.openssl.PEMParser;

import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used to extend the list of trusted certificates with a list of certificates provided in a separate
 * PEM certificate file or configured MetadataServiceList (MDSL).
 *
 * <p>This is primary used to extend certificates provided in a JKS trust store in the CEF eIDAS sample implementation code.
 * by amending the eu.eidas.auth.engine.configuration.dom.KeyStoreSignatureConfigurator class.</p>
 *
 * <ul>
 * <li>Add the field: <b>private static final EidasTrustedPEMCertificates trustedPemCerts = new EidasTrustedPEMCertificates();</b></li>
 * <li>Add row in method getSignatureConfiguration: <b>trustedCertificates = trustedPemCerts.addTrustedCertificates(trustedCertificates);</b></li>
 * </ul>
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class EidasTrustedCertificates {

  private static final Logger LOG = Logger.getLogger(EidasTrustedMDSLCertificates.class.getName());
  private static final String EIDAS_TRUSTED_CERTS_FILE = "EIDAS_TRUSTED_CERTS_FILE";
  private static final String EIDAS_TRUSTED_CERTS_CONSTRAINTS = "EIDAS_TRUSTED_CERTS_CONSTRAINTS";
  private static final String MDSL_CONFIG_FOLDER = "MDSL_CONFIG_FOLDER";
  private static final String KEY_STORE_PATH_PARAM = "keyStorePath";
  private String trustedCertsFile;
  private List<String> constraints;
  private EidasTrustedMDSLCertificates mdslCertificates;

  /**
   * Constructor
   * <p>
   * The constructor is setup using Environment variables
   *
   * <ul>
   * <li><b>EIDAS_TRUSTED_CERTS_FILE</b>: Path to the PEM certificates file holding one or more trusted PEM certificates</></li>
   * <li><b>EIDAS_TRUSTED_CERTS_CONSTRAINTS</b>: A comma separated list of either full KeyStore path, or the ending path of a keystore path.
   * If no constraint is defined, then the list of trusted certificates will always be amended with the PEM list. If the constraint is present,
   * Then the trusted list will only be amended when the keyStorePath properties matches one of the specified constraints.
   * </li>
   * </ul>*
   */
  public EidasTrustedCertificates() {
    Map<String, String> env = System.getenv();
    if (hasValue(EIDAS_TRUSTED_CERTS_FILE, env)) {
      this.trustedCertsFile = env.get(EIDAS_TRUSTED_CERTS_FILE);
    }
    if (hasValue(EIDAS_TRUSTED_CERTS_CONSTRAINTS, env)) {
      constraints = Arrays.asList(env.get(EIDAS_TRUSTED_CERTS_CONSTRAINTS).split(","));
    }
    else {
      constraints = new ArrayList<>();
    }
    if (hasValue(MDSL_CONFIG_FOLDER, env)) {
      mdslCertificates = new EidasTrustedMDSLCertificates(env.get(MDSL_CONFIG_FOLDER));
    }
  }

  private boolean hasValue(String key, Map<String, String> env) {
    if (env.containsKey(key)) {
      String val = env.get(key);
      return val != null && val.trim().length() > 0;
    }
    return false;
  }

  /**
   * Returns the ammended list of trusted certificates
   *
   * @param trustedCerts The list of certificates that are already trusted
   * @param properties   The eIDAS node parameters relevant to this request
   * @return The list of trusted certificates amended with PEM certificates matching the defined constraints
   */
  public ImmutableSet<X509Certificate> addTrustedCertificates(ImmutableSet<X509Certificate> trustedCerts, Map<String, String> properties) {
    List<X509Certificate> compiledCertList = new ArrayList<>(trustedCerts);

    if (!isConstraintsMatch(properties)) {
      //The key store in question is not allowed to be enriched by externally trusted certificates
      return trustedCerts;
    }

    // Add statically configured certificates from PEM file
    if (trustedCertsFile != null) {

      try {
        addCertsButNotDuplicates(getCertificatesFromPemFile(trustedCertsFile), compiledCertList);
      }
      catch (Exception ex) {
        LOG.log(Level.SEVERE, "Unable to parse provided trusted certificate PEM file", ex);
      }
    }

    if (mdslCertificates != null) {
      addCertsButNotDuplicates(mdslCertificates.getTrustedMdslCertificates(), compiledCertList);
    }

    return ImmutableSet.copyOf(compiledCertList);
  }

  private void addCertsButNotDuplicates(List<X509Certificate> certsToAdd, List<X509Certificate> compiledCertList) {
    for (X509Certificate certToAdd : certsToAdd) {
      if (!compiledCertList.contains(certToAdd)) {
        compiledCertList.add(certToAdd);
      }
    }
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
      if (keyStorePath.equals(constraint) || keyStorePath.endsWith(constraint)) {
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
   * @throws IOException          on error
   * @throws CertificateException on error
   */
  public static List<X509Certificate> getCertificatesFromPemFile(String fileName)
    throws IOException, CertificateException {
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
