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
package eu.eidas.node.auth.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public abstract class AbstractMetadataCaching implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetadataCaching.class);

    protected abstract Map<String, EidasMetadataParametersI> getMap();

    @Override
    public final EidasMetadataParametersI getEidasMetadataParameters(String url) throws EIDASMetadataProviderException {
        if(getMap()!=null){
            EidasMetadataParametersI content = getMap().get(url);
            if(content != null) {
                return content;
            }
        }
        return null;
    }

    @Override
    public final void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        if( getMap() != null){
            if(eidasMetadataParameters == null) {
                getMap().remove(url);
            } else {
                getMap().put(url, eidasMetadataParameters);
            }
        }
    }

}
