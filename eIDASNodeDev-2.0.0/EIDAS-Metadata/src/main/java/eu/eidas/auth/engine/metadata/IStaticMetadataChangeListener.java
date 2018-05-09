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
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * observer of changes in a static metadata repository
 */
public interface IStaticMetadataChangeListener {
    /**
     * notifies the adding of a new/updated entity descriptor
     * @param ed
     */
    void add(EntityDescriptor ed, MetadataSignerI metadataSigner) throws EIDASMetadataException;
    /**
     * notifies the removal of an entity descriptor
     * @param entityID
     */
    void remove(String entityID);
}
