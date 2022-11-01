package se.idsec.eidas.cef.trustconfig;

import java.util.List;

/**
 * Provides the configuration data for an MDSL source
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class MDSLConfigData {

  /** Description of the MDSL source */
  private String desc;
  /** Boolean indicator if all eIDAS connectors from all countries in this MDSL are trusted */
  private boolean allConnectorCountries;
  /** Boolean indicator if all eIDAS proxy services from all countries in this MDSL are trusted */
  private boolean allProxyServiceCountries;
  /**
   * A white space separated list of 2 letter country codes, indicating that eIDAS connectors from these countries are trusted.
   * This list is ignored/empty if allConnectorCountries is true
   */
  List<String> connectorCountries;
  /**
   * A white space separated list of 2 letter country codes, indicating that eIDAS proxy services from these countries are trusted.
   * This list is ignored/empty if allProxyServiceCountries is true
   */
  List<String> serviceCountries;
  /** MDSL aggregator for this MDSL */
  MDSLSource mdslSource;

  /**
   * Constructor
   */
  public MDSLConfigData() {
  }

  /**
   * Getter for description
   * @return description
   */
  public String getDesc() {
    return desc;
  }

  /**
   * Setter for description
   * @param desc description
   */
  public void setDesc(String desc) {
    this.desc = desc;
  }

  /**
   * Gets all connector countries trusted indication
   * @return true if all connector countries are trusted
   */
  public boolean isAllConnectorCountries() {
    return allConnectorCountries;
  }

  /**
   * Setter for all connector countries trusted indication
   * @param allConnectorCountries true if all connector countries are trusted
   */
  public void setAllConnectorCountries(boolean allConnectorCountries) {
    this.allConnectorCountries = allConnectorCountries;
  }

  /**
   * Gets all proxy service countries trusted indication
   * @return true if all proxy service countries are trusted
   */
  public boolean isAllProxyServiceCountries() {
    return allProxyServiceCountries;
  }

  /**
   * Setter for all proxy service countries trusted indication
   * @param allProxyServiceCountries true if all proxy service countries are trusted
   */
  public void setAllProxyServiceCountries(boolean allProxyServiceCountries) {
    this.allProxyServiceCountries = allProxyServiceCountries;
  }

  /**
   * Gets trusted connector countries
   * @return trusted connector countries
   */
  public List<String> getConnectorCountries() {
    return connectorCountries;
  }

  /**
   * Setter for trusted connector countries
   * @param connectorCountries trusted connector countries
   */
  public void setConnectorCountries(List<String> connectorCountries) {
    this.connectorCountries = connectorCountries;
  }

  /**
   * Getter for trusted proxy service countries
   * @return trusted proxy service countries
   */
  public List<String> getServiceCountries() {
    return serviceCountries;
  }

  /**
   * Setter for proxy service countries
   * @param serviceCountries trusted proxy service countries
   */
  public void setServiceCountries(List<String> serviceCountries) {
    this.serviceCountries = serviceCountries;
  }

  /**
   * Getter for MDSL aggregator
   * @return MDSL aggregator
   */
  public MDSLSource getMdslSource() {
    return mdslSource;
  }

  /**
   * Setter for MDSL aggregator
   * @param mdslSource MDSL aggregator
   */
  public void setMdslSource(MDSLSource mdslSource) {
    this.mdslSource = mdslSource;
  }
}
