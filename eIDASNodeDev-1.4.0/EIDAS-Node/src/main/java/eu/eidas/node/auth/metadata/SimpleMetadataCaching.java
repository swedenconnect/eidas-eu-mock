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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;

/**
 * implements a caching service using hashmap
 */
public final class SimpleMetadataCaching extends AbstractMetadataCaching {

    private final ConcurrentMap<String, SerializedEntityDescriptor> map;

    SimpleMetadataCaching(long retentionPeriod) {
        map = CacheBuilder.newBuilder()
                .expireAfterAccess(retentionPeriod, TimeUnit.SECONDS)
                .maximumSize(10000L).<String, SerializedEntityDescriptor>build().asMap();
    }

    @Override
    protected Map<String, SerializedEntityDescriptor> getMap() {
        return map;
    }

}
