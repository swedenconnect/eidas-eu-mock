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
package eu.eidas.auth.engine.metadata.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opensaml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Base implementation of the {@link MetadataFetcherI} interface with caching capabilities.
 *
 * @since 1.1
 */
public abstract class AbstractCachingMetadataFetcher extends BaseMetadataFetcher {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCachingMetadataFetcher.class);

    @Nonnull
    @Override
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        EntityDescriptor cachedEntityDescriptor = getFromCache(url, metadataSigner);

        if (null != cachedEntityDescriptor) {
            if (cachedEntityDescriptor.isValid()) {
                return cachedEntityDescriptor;
            } else {
                // cached metadata has expired:
                removeFromCache(url);
            }
        }

        if (isHttpRetrievalEnabled() && isAllowedMetadataUrl(url)) {
            EntityDescriptor fetchedEntityDescriptor = super.getEntityDescriptor(url, metadataSigner);

            LOG.debug("Obtained entity descriptor from metadata retrieved from url " + url);

            putInCache(url, fetchedEntityDescriptor);

            return fetchedEntityDescriptor;
        }

        throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                                                 EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                                                 "No entity descriptor for URL " + url);
    }

    @Nullable
    protected abstract EntityDescriptor getFromCache(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner) throws EIDASSAMLEngineException;

    protected boolean isHttpRetrievalEnabled() {
        return true;
    }

    protected abstract void putInCache(@Nonnull String url, @Nonnull EntityDescriptor entityDescriptor);

    protected abstract void removeFromCache(@Nonnull String url);
}
