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

import javax.annotation.Nonnull;

import eu.eidas.auth.engine.metadata.*;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.SignableXMLObject;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.MarshallException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractMetadataCaching implements IMetadataCachingService {
    private static final String SIGNATURE_HOLDER_ID_PREFIX="signatureholder";

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetadataCaching.class);

    @Override
    public final EntityDescriptor getDescriptor(String url) throws EIDASMetadataProviderException {
        if(getMap()!=null){
            SerializedEntityDescriptor content=getMap().get(url);
            if(content!=null && !content.getSerializedEntityDescriptor().isEmpty()) {
                try {
                    return deserializeEntityDescriptor(content.getSerializedEntityDescriptor());
                } catch (UnmarshallException e) {
                    LOG.error("Unable to deserialize metadata entity descriptor from cache for "+url);
                    LOG.error(e.getStackTrace().toString());
                    throw new EIDASMetadataProviderException(e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public final void putDescriptor(String url, EntityDescriptor ed, EntityDescriptorType type) {
        if(getMap()!=null){
            if(ed==null){
                getMap().remove(url);
            }else {
                String content = serializeEntityDescriptor(ed);
                if (content != null && !content.isEmpty()) {
                    getMap().put(url, new SerializedEntityDescriptor(content, type));
                }
            }
        }
    }
    @Override
    public final EntityDescriptorType getDescriptorType(String url) {
        if (getMap() != null) {
            SerializedEntityDescriptor content = getMap().get(url);
            if (content != null) {
                return content.getType();
            }
        }
        return null;
    }

    private String serializeEntityDescriptor(XMLObject ed){
        try {
            return EidasStringUtil.toString(OpenSamlHelper.marshall(ed));
        } catch (MarshallException e) {
            throw new IllegalStateException(e);
        }
    }

    private EntityDescriptor deserializeEntityDescriptor(String content) throws UnmarshallException {
    	EntityDescriptorContainer container = MetadataUtil.deserializeEntityDescriptor(content);
        return container.getEntityDescriptors().isEmpty()?null:container.getEntityDescriptors().get(0);
    }

    protected abstract Map<String, SerializedEntityDescriptor> getMap();

    @Override
	public void putDescriptorSignatureHolder(String url, SignableXMLObject container){
    	getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(serializeEntityDescriptor(container), EntityDescriptorType.NONE));
	}

    @Override
	public void putDescriptorSignatureHolder(String url, EntityDescriptorContainer container){
    	if(container.getSerializedEntitesDescriptor()!=null){
    		getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(EidasStringUtil.toString(container.getSerializedEntitesDescriptor()), EntityDescriptorType.SERIALIZED_SIGNATURE_HOLDER));
    	}else{
    		putDescriptorSignatureHolder(url, container.getEntitiesDescriptor());
    	}
    }
}
