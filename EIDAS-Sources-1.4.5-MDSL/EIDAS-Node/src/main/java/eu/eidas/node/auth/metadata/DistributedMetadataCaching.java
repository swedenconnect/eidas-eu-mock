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
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.commons.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.node.ApplicationContextProvider;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * implements a caching service using hazelcast
 */
public class DistributedMetadataCaching extends AbstractMetadataCaching {

    protected ConcurrentMap<String, SerializedEntityDescriptor> map;
    protected String cacheName;
    protected HazelcastInstanceInitializer hazelcastInstanceInitializer;

    @Override
    protected Map<String, SerializedEntityDescriptor> getMap(){
        if (map == null) {
            if (getCacheName() == null) {
                throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch");
            }
            HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(hazelcastInstanceInitializer.getHazelcastInstanceName());
            return instance.getMap(getCacheName());
        }
        return map;
    }

    public String getCacheName() {
        return cacheName;
    }
    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public HazelcastInstanceInitializer getHazelcastInstanceInitializer() {
        return hazelcastInstanceInitializer;
    }

    public void setHazelcastInstanceInitializer(HazelcastInstanceInitializer hazelcastInstanceInitializer) {
        this.hazelcastInstanceInitializer = hazelcastInstanceInitializer;
    }

}
