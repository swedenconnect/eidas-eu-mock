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

import javax.annotation.Nonnull;

import eu.eidas.auth.engine.metadata.EntityDescriptorType;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.signature.SignableXMLObject;

import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;

/**
 * provides caching services for SAML metadata entity descriptors
 */
public interface IMetadataCachingService {
    /**
     *
     * @param url
     * @return the descriptor (stored in the cache) associated with url
     */
    EntityDescriptor getDescriptor(String url) throws EIDASMetadataProviderException;
    /**
     *
     * @param url
     * @return the descriptor type of the descriptor (stored in the cache) associated with url
     */
    EntityDescriptorType getDescriptorType(String url);

    /**
     * add a descriptor in the cache
     * @param url
     * @param ed
     * @param type - the type (origin) of the descriptor
     */
    void putDescriptor(String url, EntityDescriptor ed, EntityDescriptorType type);
    /**
    *
    * @param url
    * @return either the descriptor (stored in the cache) associated with url or its container (holding the signature)
    *//*
   SignableXMLObject getDescriptorSignatureHolder(@Nonnull String url);*/
   /**
   *
   * @param url
   * @param signableObject - the object holding the signature for the entity identified by the @url
   */
  void putDescriptorSignatureHolder(String url, SignableXMLObject signableObject);
  /**
  *
  * @param url
  * @param container - the object holding the signature for the entity identified by the @url
  */
 void putDescriptorSignatureHolder(String url, EntityDescriptorContainer container);
}
