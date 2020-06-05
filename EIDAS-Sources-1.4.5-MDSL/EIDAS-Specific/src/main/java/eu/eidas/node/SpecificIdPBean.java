package eu.eidas.node;

import eu.eidas.auth.specific.IAUService;

/**
 * Class for handling spring bean definition and use it on the servlets
 */
public class SpecificIdPBean {

  /**
   * Specific Node service.
   */
  private transient IAUService specificNode;

  /**
   * Setter for specificNode.
   *
   * @param specificNode The specificNode to set.
   */
  public void setSpecificNode(final IAUService specificNode) {
    this.specificNode = specificNode;
  }

  /**
   * Getter for specificNode.
   *
   * @return The specificNode value.
   */
  public IAUService getSpecificNode() {
    return specificNode;
  }

}
