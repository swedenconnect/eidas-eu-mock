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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.utils.EidasNodeMetadataGenerator;
import eu.eidas.node.utils.PropertiesUtil;

/**
 * generates metadata used to communicate with the Connector.
 */
@SuppressWarnings("squid:S1989") // due to the code uses correlation maps, not http sessions
public class ConnectorMetadataGeneratorServlet extends AbstractConnectorServlet{
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConnectorMetadataGeneratorServlet.class.getName());
    //TODO: Connector responder metadata generator belongs in fact to a Connector Specific module
    private static final String IDP_METADATA_URL="/ConnectorResponderMetadata";

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    //the Connector presents itself as either an IdP or a SP
    //IdP: will use SP-Specific SAMLEngine (since it is an IdP for a SP)
    //SP: will use Connector-Service SAMLEngine

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String generatorName=request.getServletPath().startsWith(IDP_METADATA_URL)?NodeBeanNames.CONNECTOR_AS_IDP_METADATA_GENERATOR.toString():NodeBeanNames.CONNECTOR_METADATA_GENERATOR.toString();
        EidasNodeMetadataGenerator generator = (EidasNodeMetadataGenerator)getApplicationContext().getBean(generatorName);
        PropertiesUtil.checkConnectorActive();
        if(PropertiesUtil.isMetadataEnabled()) {
            response.getOutputStream().print(generator.generateConnectorMetadata());
        }else{
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
