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

import eu.eidas.engine.exceptions.EIDASMetadataException;

import java.util.List;

/**
 * EIDAS metadata loader interface. The Loader is providing metadata fetching capabilities additional/different than the
 * HTTP/HTTPS client fetcher. Mainly used for testing, but it can help connecting to a centralized metadata provider
 * infrastructure, so no direct HTTP/HTTPS connections needed to get remote metadata.
 *
 */
public interface MetadataLoaderPlugin {

    void addListenerContentChanged( IStaticMetadataChangeListener listener);

    List<EntityDescriptorContainer> getEntityDescriptors() throws EIDASMetadataException;

}
