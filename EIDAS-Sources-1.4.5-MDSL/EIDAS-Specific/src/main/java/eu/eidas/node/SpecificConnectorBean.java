package eu.eidas.node;

import eu.eidas.auth.specific.IAUConnector;

/**
 *
 * Class for handling spring bean definition
 *
 * @since 1.1
 */
public class SpecificConnectorBean {

    /**
     * Specific node service.
     */
    private transient IAUConnector specificConnectorNode;

    public IAUConnector getSpecificConnectorNode() {
        return specificConnectorNode;
    }

    public void setSpecificConnectorNode(IAUConnector specificConnectorNode) {
        this.specificConnectorNode = specificConnectorNode;
    }
}
