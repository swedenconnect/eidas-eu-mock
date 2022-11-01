package se.idsec.eidas.cef.trustconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class provides the functionality that builds MDSL configuration data and MDSL aggregator for available MDSL sources
 * based on data provided in the MDSL configuration properties file in the MDSL configuration folder
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class MDSLConfiguration {

  private static final Logger LOG = Logger.getLogger(MDSLConfiguration.class.getName());
  /** Property name for the description property */
  private static final String DESCRIPTION_PROP = "description";
  /** Property name for the connector countries property */
  private static final String CONNECTOR_COUNTRIES_PROP = "connector-countries";
  /** Property name for the proxy service countries property */
  private static final String SERVICE_COUNTRIES_PROP = "service-countries";
  /** Property name for the certificate file name property */
  private static final String CERTIFICATE_PROP = "cerificate";
  /** Property name for the location url property */
  private static final String URL_PROP = "url";
  /** List of MDSL configurations extracted from the property settings */
  private List<MDSLConfigData> configDataList;

  /**
   * Constructor that builds the MDSL configuration data based on property settings.
   * @param mdslConfigFolderPath the absolute path to the folder holding MDSL configuration data
   * @throws IOException on error to build MDSL configuration data
   * @throws RuntimeException on error to build MDSL configuration data based in invalid input
   */
  public MDSLConfiguration(String mdslConfigFolderPath) throws IOException, RuntimeException {
    configDataList = new ArrayList<>();
    if (mdslConfigFolderPath == null || mdslConfigFolderPath.trim().length() == 0) {
      // No configuration folder is set. Just set an empty config list
      return;
    }

    // A MDSL config folder is specified. Parse data.
    File mdslConfigFolder = new File(mdslConfigFolderPath);
    File mdslPropFile = new File(mdslConfigFolder, "mdsl.properties");
    if (!mdslPropFile.exists()) {
      throw new IllegalArgumentException("No MDSL config file is found at the specified location: " + mdslConfigFolderPath);
    }
    Properties properties = new Properties();
    properties.load(new FileInputStream(mdslPropFile));

    int idx = 0;
    while (true) {
      if (!properties.containsKey(getKey(URL_PROP, idx))) {
        //No more properties. exit loop
        break;
      }
      MDSLConfigData data = new MDSLConfigData();
      try {

        // Get Connector countries
        String connectorCountries = properties.getProperty(getKey(CONNECTOR_COUNTRIES_PROP, idx));
        if (connectorCountries == null || connectorCountries.trim().length() == 0) {
          // No parameter. Empty country list and not all
          data.setAllConnectorCountries(false);
          data.setConnectorCountries(new ArrayList<>());
        }
        else {
          // Some parameter value
          if (connectorCountries.trim().equalsIgnoreCase("all")) {
            // Set to all
            data.setAllConnectorCountries(true);
            data.setConnectorCountries(new ArrayList<>());
          }
          else {
            // Look for specific countries
            List<String> connectorCountryList = Arrays.stream(connectorCountries.trim().split("\\s+"))
              .filter(s -> s.length() == 2)
              .map(s -> s.toUpperCase())
              .collect(Collectors.toList());
            data.setAllConnectorCountries(false);
            data.setConnectorCountries(connectorCountryList);
          }
        }

        // Get Service countries
        String serviceCountries = properties.getProperty(getKey(SERVICE_COUNTRIES_PROP, idx));
        if (serviceCountries == null || serviceCountries.trim().length() == 0) {
          // No parameter. Empty country list and not all
          data.setAllProxyServiceCountries(false);
          data.setServiceCountries(new ArrayList<>());
        }
        else {
          // Some parameter value
          if (serviceCountries.trim().equalsIgnoreCase("all")) {
            // Set to all
            data.setAllProxyServiceCountries(true);
            data.setServiceCountries(new ArrayList<>());
          }
          else {
            // Look for specific countries
            List<String> serviceCountryList = Arrays.stream(serviceCountries.trim().split("\\s+"))
              .filter(s -> s.length() == 2)
              .map(s -> s.toUpperCase())
              .collect(Collectors.toList());
            data.setAllProxyServiceCountries(false);
            data.setServiceCountries(serviceCountryList);
          }
        }

        // Get description
        data.setDesc(properties.getProperty(getKey(DESCRIPTION_PROP, idx)));
        // Get MDSL source
        String url = properties.getProperty(getKey(URL_PROP, idx));
        File certFile = new File(mdslConfigFolder, properties.getProperty(getKey(CERTIFICATE_PROP, idx)));
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        InputStream is = new FileInputStream(certFile);
        X509Certificate mdslCert = (X509Certificate) fact.generateCertificate(is);
        is.close();
        data.setMdslSource(new MDSLSource(url, mdslCert));

        // Strore data
        configDataList.add(data);

        // Increase counter
        idx++;
      }
      catch (Exception ex) {
        LOG.log(Level.SEVERE, "Unable to parse MDSL configuration data with index " + idx, ex);
        idx++;
      }
    }
  }

  /**
   * Getter for the configuraiton data list
   * @return list of MDSL configuration data
   */
  public List<MDSLConfigData> getConfigDataList() {
    return configDataList;
  }

  /**
   * Constructs the property name
   * @param paramName the name of the parameter
   * @param idx the index of the MDSL source
   * @return actual property name
   */
  private String getKey(String paramName, int idx) {
    return "mdsl." + idx + "." + paramName;
  }
}
