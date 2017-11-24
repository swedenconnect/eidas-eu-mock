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

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.IStaticMetadataChangeListener;
import eu.eidas.auth.engine.metadata.MetadataLoaderPlugin;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.impl.file.FileService;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * MetadataLoaderPlugin sample implementation for testing only (not robust)
 * the source metadata: all readable xml files found in a configured directory
 */
public class FileMetadataLoader implements MetadataLoaderPlugin {
    private String repositoryPath;
    private FileService fileService = new FileService();

    private static final Logger LOG = LoggerFactory.getLogger(FileMetadataLoader.class.getName());

    /**
     * @return a list of entity descriptors read from the current directory (repositoryPath)
     */
    public List<EntityDescriptorContainer> getEntityDescriptors() throws EIDASMetadataProviderException {
        List<EntityDescriptorContainer> list = new ArrayList<EntityDescriptorContainer>();
        List<String> files = getFiles();
        for (String fileName : files) {
            EntityDescriptorContainer descriptors = null;
            try {
                descriptors = loadDescriptors(fileName);
            } catch (UnmarshallException e) {
                LOG.error("Failed to unmarshall entity descriptors from static metadata file '"+fileName+"'");
                LOG.error(e.toString());
            }
            if (descriptors != null) {
                list.add(descriptors);
                List<String> ids = new ArrayList<String>();
                for (EntityDescriptor ed : descriptors.getEntityDescriptors()) {
                    LOG.info("Added entity descriptor for "+ed.getEntityID());
                    ids.add(ed.getEntityID());
                }
            }
        }
        return list;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
        if (StringUtils.isNotBlank(repositoryPath)) {
            fileService.setRepositoryDir(repositoryPath);
        }
    }

    private List<String> getFiles(){
        return fileService.getFileList(false);
    }

    private EntityDescriptorContainer loadDescriptors(String fileName) throws UnmarshallException {
        if (!fileName.endsWith(".xml")) {
            LOG.info("Ignored file: " + fileName + ". Has not a xml extension ");
            return null;
        }

        LOG.info("Loading entity descriptors from file "+ fileName);
        byte[] content = fileService.loadBinaryFile(fileName);
        return content==null?null : MetadataUtil.deserializeEntityDescriptor(EidasStringUtil.toString(content));
    }

    List<IStaticMetadataChangeListener> listeners=new ArrayList<IStaticMetadataChangeListener>();
    public void addListenerContentChanged( IStaticMetadataChangeListener listener){
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

}
