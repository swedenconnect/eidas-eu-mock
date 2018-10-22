/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.node.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.node.auth.connector.ICONNECTORService;
import eu.eidas.node.specificcommunication.ISpecificConnector;

public final class ConnectorControllerService {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectorControllerService.class.getName());

    private String assertionConsUrl;

    /**
     * Connector service.
     */
    private ICONNECTORService connectorService;

    private CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap;

    private CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap;

    private ISpecificConnector specificConnector;

    /**
     * URL of the Connector authentication service.
     */
    private String nodeAuth;

    public String getAssertionConsUrl() {
        return assertionConsUrl;
    }

    public void setAssertionConsUrl(String assertionConsUrl) {
        this.assertionConsUrl = assertionConsUrl;
    }

    /**
     * Setter for connectorService.
     *
     * @param connectorService The new connectorService value.
     * @see ICONNECTORService
     */
    public void setConnectorService(ICONNECTORService connectorService) {
        this.connectorService = connectorService;
    }

    /**
     * Getter for connectorService.
     *
     * @return The connectorService value.
     * @see ICONNECTORService
     */
    public ICONNECTORService getConnectorService() {
        return connectorService;
    }

    /**
     * Setter for nodeAuth.
     *
     * @param nodeAuth The new nodeAuth value.
     */
    public void setNodeAuth(final String nodeAuth) {
        this.nodeAuth = nodeAuth;
    }

    /**
     * Getter for nodeAuth.
     *
     * @return The nodeAuth value.
     */
    public String getNodeAuth() {
        return nodeAuth;
    }

    public CorrelationMap<StoredLightRequest> getSpecificSpRequestCorrelationMap() {
        return specificSpRequestCorrelationMap;
    }

    public void setSpecificSpRequestCorrelationMap(CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap) {
        this.specificSpRequestCorrelationMap = specificSpRequestCorrelationMap;
    }

    public CorrelationMap<StoredAuthenticationRequest> getConnectorRequestCorrelationMap() {
        return connectorRequestCorrelationMap;
    }

    public void setConnectorRequestCorrelationMap(CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap) {
        this.connectorRequestCorrelationMap = connectorRequestCorrelationMap;
    }

    public ISpecificConnector getSpecificConnector() {
        return specificConnector;
    }

    public void setSpecificConnector(ISpecificConnector specificConnector) {
        this.specificConnector = specificConnector;
    }

    @Override
    public String toString() {
        return "ConnectorControllerService{" +
                ", assertionConsUrl='" + assertionConsUrl + '\'' +
                ", connectorService=" + connectorService +
                ", specificSpRequestCorrelationMap=" + specificSpRequestCorrelationMap +
                ", connectorRequestCorrelationMap=" + connectorRequestCorrelationMap +
                '}';
    }
}
