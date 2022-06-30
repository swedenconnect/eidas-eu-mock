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
package eu.eidas.sp.metadata;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.IStaticMetadataChangeListener;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.sp.SPUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

/**
 * The implementation of the {@link MetadataFetcherI} interface for SP.
 *
 * @since 1.1
 */
public class SPCachingMetadataFetcher extends CachingMetadataFetcher implements IStaticMetadataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(SPCachingMetadataFetcher.class);

    public SPCachingMetadataFetcher() {
        super();
        setCache(new SPMetadataCache());
        if (StringUtils.isNotEmpty(SPUtil.getMetadataRepositoryPath())) {
            FileMetadataLoader fp = new FileMetadataLoader();
            fp.setRepositoryPath(SPUtil.getMetadataRepositoryPath());
            setMetadataLoaderPlugin(fp);
        }
        initProcessor();
    }

    @Override
    public boolean isHttpRetrievalEnabled() {
        return SPUtil.isMetadataHttpFetchEnabled();
    }

    @Override
    protected boolean mustUseHttps() {
        return false;
    }

    @Override
    protected boolean mustValidateSignature(@Nonnull String url) {
        setTrustedEntityDescriptors(SPUtil.getTrustedEntityDescriptors());
        return super.mustValidateSignature(url);
    }

}
