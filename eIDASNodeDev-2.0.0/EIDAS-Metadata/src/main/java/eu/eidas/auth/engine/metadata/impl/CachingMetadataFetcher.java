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

import eu.eidas.auth.commons.EIDASUtil;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The implementation of the {@link MetadataFetcherI} interface for the Node.
 *
 * @since 1.1
 */
public class CachingMetadataFetcher extends AbstractCachingMetadataFetcher implements IStaticMetadataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(CachingMetadataFetcher.class);

    private boolean httpRetrievalEnabled = false;

    /**
     * when restrictHttp is true, remote metadata is accepted only through https otherwise, metadata retrieved using
     * http protocol is also accepted
     */
    private boolean restrictHttp = false;

    /**
     * SSL/TLS enabled protocols
     */
    private String tlsEnabledProtocols;

    /**
     * SSL/TLS enabled ciphers suites
     */
    private String tlsEnabledCipherSuites;

    /**
     * whether to enable the signature validation for EntityDescriptors
     */
    private boolean validateEntityDescriptorSignature = true;

    /**
     * initialized with a list of urls corresponding to EntityDescriptor not needing signature validation
     */
    private String trustedEntityDescriptors = "";

    private MetadataLoaderPlugin metadataLoaderPlugin;

    private Set<String> trustedEntityDescriptorsSet = new HashSet<String>();

    private IMetadataCachingService cache;

    @Override
    public void add(EntityDescriptor ed, MetadataSignerI metadataSigner) throws EIDASMetadataException {
        String url = ed.getEntityID();
        if (mustValidateSignature(url)) {
            try {
                metadataSigner.validateMetadataSignature(ed);
            } catch (EIDASMetadataException e) {
                LOG.error("Signature validation failed for " + url);
                LOG.error(e.toString());
                throw(e);
            }
        }
        if (null != cache) {
            EidasMetadataParametersI eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
            cache.putEidasMetadataParameters(ed.getEntityID(), eidasMetadataParameters);
        }
    }

    public IMetadataCachingService getCache() {
        return cache;
    }

    protected MetadataLoaderPlugin getMetadataLoaderPlugin() {
        return metadataLoaderPlugin;
    }

    @Override
    protected EidasMetadataParametersI getFromCache(String url, MetadataSignerI metadataSigner) throws EIDASMetadataException {
        EidasMetadataParametersI metadataParameters = null;
        if (null != cache) {
            metadataParameters = cache.getEidasMetadataParameters(url);
        }
        if (null == metadataParameters) {
            if (getMetadataLoaderPlugin() != null) {
                metadataParameters = loadWithPlugin(url, metadataSigner);
            }
        }
        return metadataParameters;
    }

    protected EidasMetadataParametersI loadWithPlugin(String url, MetadataSignerI metadataSigner) throws EIDASMetadataException {
        EidasMetadataParametersI eidasMetadataParameters = null;
        if (getMetadataLoaderPlugin() != null) {
            List<EntityDescriptorContainer> fileStoredDescriptors = getMetadataLoaderPlugin().getEntityDescriptors();
            if (getCache() != null) {
                for (EntityDescriptorContainer edc : fileStoredDescriptors) {
                    for (EntityDescriptor ed : edc.getEntityDescriptors()) {
                        if (ed.getEntityID().equals(url)) {
                            add(ed, metadataSigner);
                            if (edc.getEntitiesDescriptor() != null && ed.getSignature() == null) {
                                eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
                                getCache().putEidasMetadataParameters(ed.getEntityID(), eidasMetadataParameters);
                            }
                            eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
                        }
                    }
                }
            }
        }
        return eidasMetadataParameters;
    }

    /**
     * perform post construct task, eg populating the cache with file based metadata
     */
    public void initProcessor() {
        if (getMetadataLoaderPlugin() != null) {
            getMetadataLoaderPlugin().addListenerContentChanged(this);
        }
    }

    @Override
    public boolean isHttpRetrievalEnabled() {
        return httpRetrievalEnabled;
    }

    public boolean isValidateEidasMetadataSignature() {
        return validateEntityDescriptorSignature;
    }

    @Override
    protected boolean mustUseHttps() {
        return restrictHttp;
    }

    @Override
    protected String[] getTlsEnabledProtocols() {
        return super.getTlsEnabledProtocols(tlsEnabledProtocols);
    }

    @Override
    public String[] getTlsEnabledCiphers() {return super.getTlsEnabledCiphers(tlsEnabledCipherSuites); }

    @Override
    protected boolean mustValidateSignature(@Nonnull String url) {
        return isValidateEidasMetadataSignature() && !trustedEntityDescriptors.contains(url);
    }

    @Override
    protected void putInCache(String url, EidasMetadataParametersI metadataParameters) {
        if (null != cache && null != metadataParameters) {
            cache.putEidasMetadataParameters(url, metadataParameters);
        }
    }

    @Override
    public void remove(String entityID) {
        removeFromCache(entityID);
    }

    @Override
    protected void removeFromCache(String url) {
        if (null != cache) {
            cache.putEidasMetadataParameters(url, null);
        }
    }

    public void setCache(IMetadataCachingService cache) {
        this.cache = cache;
    }

    public void setMetadataLoaderPlugin(MetadataLoaderPlugin metadataLoaderPlugin) {
        this.metadataLoaderPlugin = metadataLoaderPlugin;
    }

    public void setHttpRetrievalEnabled(boolean httpRetrievalEnabled) {
        this.httpRetrievalEnabled = httpRetrievalEnabled;
    }

    public void setRestrictHttp(boolean restrictHttp) {
        this.restrictHttp = restrictHttp;
    }

    public void setTlsEnabledProtocols(String tlsEnabledProtocols) {
        this.tlsEnabledProtocols = tlsEnabledProtocols;
    }

    public void setTlsEnabledCiphers(String tlsEnabledCiphers) {
        this.tlsEnabledCipherSuites = tlsEnabledCiphers;
    }

    public void setTrustedEidasMetadataUrls(String trustedEidasMetadataUrls) {
        this.trustedEntityDescriptors = trustedEidasMetadataUrls;
        setTrustedEntityDescriptorsSet(EIDASUtil.parseSemicolonSeparatedList(trustedEidasMetadataUrls));
    }

    public void setTrustedEntityDescriptorsSet(Set<String> trustedEntityDescriptorsSet) {
        this.trustedEntityDescriptorsSet = trustedEntityDescriptorsSet;
    }

    public void setValidateEidasMetadataSignature(boolean validateEidasMetadataSignature) {
        this.validateEntityDescriptorSignature = validateEidasMetadataSignature;
    }
}
