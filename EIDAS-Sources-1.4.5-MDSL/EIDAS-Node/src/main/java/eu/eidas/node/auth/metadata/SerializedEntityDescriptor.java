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

import java.io.Serializable;

import com.google.common.annotations.Beta;
import eu.eidas.auth.engine.metadata.EntityDescriptorType;

/**
 * a serialized image of a metadata EntityDescriptor
 *
 * @deprecated this is probably the worst Java serialized form one can choose regarding space (the java.lang.String of
 * an XML).
 */
@Deprecated
@Beta
public class SerializedEntityDescriptor implements Serializable {

    /**
     * the entitydescriptor serialized as xml
     */
    private String serializedEntityDescriptor;

    /**
     * the type/origin (either statically loaded or retrieved from the network)
     */
    private EntityDescriptorType type;

    public SerializedEntityDescriptor(String descriptor, EntityDescriptorType type) {
        setSerializedEntityDescriptor(descriptor);
        setType(type);
    }

    public String getSerializedEntityDescriptor() {
        return serializedEntityDescriptor;
    }

    public void setSerializedEntityDescriptor(String serializedEntityDescriptor) {
        this.serializedEntityDescriptor = serializedEntityDescriptor;
    }

    public EntityDescriptorType getType() {
        return type;
    }

    public void setType(EntityDescriptorType type) {
        this.type = type;
    }
}
