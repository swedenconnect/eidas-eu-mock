package se.idsec.eidas.cef.trustconfig;

import eu.europa.eidas.metadata.servicelist.MetadataLocationType;
import eu.europa.eidas.metadata.servicelist.MetadataSchemeEndpointListType;
import eu.europa.eidas.metadata.servicelist.MetadataServiceListType;
import eu.europa.eidas.metadata.servicelist.MsEndpointType;
import org.w3.x2000.x09.xmldsig.KeyInfoType;
import se.idsec.eidas.cef.trustconfig.xml.XMLSignatureVerifier;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provider of certificates for eIDAS nodes trusted by a set of MDSL sources according to their MDSL configuration data
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 *
 */
public class EidasTrustedMDSLCertificates {

  private final static Logger LOG = Logger.getLogger(EidasTrustedMDSLCertificates.class.getName());
  private final static String CONNECTOR_TYPE = "http://eidas.europa.eu/metadata/ept/Connector";
  private final static String SERVICE_TYPE = "http://eidas.europa.eu/metadata/ept/ProxyService";

  private List<MDSLConfigData> mdslConfigDataList;

  /**
   * Constructor
   * @param mdslConfigFolder the configuration folder for MDSL support. This folder MUST contain a properties file named mdsl.properties
   *                         and MUST contain all certificates necessary to validate signatures on configured MDSL sources.
   */
  public EidasTrustedMDSLCertificates(String mdslConfigFolder) {

    mdslConfigDataList = new ArrayList<>();
    try {
      MDSLConfiguration mdslConfiguration = new MDSLConfiguration(mdslConfigFolder);
      mdslConfigDataList = mdslConfiguration.getConfigDataList();
    }
    catch (IOException e) {
      LOG.log(Level.SEVERE, "Unable to parse MDSL configuration", e);
    }
  }

  /**
   * Obtains a list of trusted metadata signing certificates obtained from MDSL sources according to MDSL configurations.
   * @return list of trusted certificates
   */
  public List<X509Certificate> getTrustedMdslCertificates() {

    List<X509Certificate> trustedCertList = new ArrayList<>();
    for (MDSLConfigData mdslConfig : mdslConfigDataList){
      getTrustedCertsFromMDSLConf(mdslConfig).stream()
      .forEach(certificate -> trustedCertList.add(certificate));
    }
    return trustedCertList;
  }

  /**
   * Obtains the list of trusted certificates from a signle MDSL according to available configuration data
   * @param mdslConfig
   * @return
   */
  private List<X509Certificate> getTrustedCertsFromMDSLConf(MDSLConfigData mdslConfig) {
    List<X509Certificate> trustedCerts = new ArrayList<>();
    MDSLSource mdslSource = mdslConfig.getMdslSource();
    MetadataServiceListType metadataServiceList = mdslSource.getMdsl().getMetadataServiceList();
    MetadataSchemeEndpointListType[] metadataListArray = metadataServiceList.getMetadataListArray();
    for (MetadataSchemeEndpointListType metadataList : metadataListArray){
      String territory = metadataList.getTerritory();
      getTrustedNodeCerts(metadataList, territory, mdslConfig.getConnectorCountries(), mdslConfig.isAllConnectorCountries(), trustedCerts, CONNECTOR_TYPE);
      getTrustedNodeCerts(metadataList, territory, mdslConfig.getServiceCountries(), mdslConfig.isAllProxyServiceCountries(), trustedCerts, SERVICE_TYPE);
    }
    return trustedCerts;
  }

  /**
   * Get all trusted certificates for eIDAS nodes within a specified territory and for a defined type
   * @param metadataList list of metadata locations for a specified territory
   * @param territory the territory
   * @param countryList list of countries trusted for this MDSL
   * @param allCountries true if all countries are trusted
   * @param trustedCerts the list of all trusted certificates
   * @param typeId the type of service
   */
  private void getTrustedNodeCerts(
    MetadataSchemeEndpointListType metadataList,
    String territory,
    List<String> countryList,
    boolean allCountries,
    List<X509Certificate> trustedCerts,
    String typeId
  ) {
    if (allCountries || countryList.contains(territory)){
      MetadataLocationType[] metadataLocationArray = metadataList.getMetadataLocationArray();
      for (MetadataLocationType mdLocation : metadataLocationArray){
        boolean trusted = false;
        MsEndpointType[] endpointArray = mdLocation.getEndpointArray();
        for (MsEndpointType eidasNode : endpointArray){
          if (!eidasNode.getSuspend() && eidasNode.getEndpointType().equalsIgnoreCase(typeId)){
            trusted = true;
          }
        }
        if (trusted) {
          addCertsFromKeyInfo(mdLocation.getKeyInfoArray(), trustedCerts);
        }
      }
    }
  }

  /**
   * Add certs from a trusted metadata source as trusted unless it is already on the list of trusted certs.
   * @param keyInfoArray Array of key info data
   * @param trustedCerts List of trusted certificates
   */
  private void addCertsFromKeyInfo(KeyInfoType[] keyInfoArray, List<X509Certificate> trustedCerts) {
    for (KeyInfoType ki : keyInfoArray){
      try {
        byte[] certBytes = ki.getX509DataArray(0).getX509CertificateArray(0);
        X509Certificate certificate = XMLSignatureVerifier.getCertificate(certBytes);
        if (!trustedCerts.contains(certificate)){
          trustedCerts.add(certificate);
        }
      } catch (Exception ex) {
        LOG.log(Level.WARNING, "Unable to read certificate from trusted MDSL location", ex);
      }
    }
  }

}
