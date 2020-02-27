/*
 * Copyright (c) 2019 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 */
package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.IStaticMetadataChangeListener;
import eu.eidas.auth.engine.metadata.MetadataLoaderPlugin;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.impl.file.FileService;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
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

    List<IStaticMetadataChangeListener> listeners = new ArrayList<>();

    /**
     * @return a list of entity descriptors read from the current directory (repositoryPath)
     * @throws EIDASMetadataProviderException if the url is invalid
     */
    public List<EntityDescriptorContainer> getEntityDescriptors() throws EIDASMetadataProviderException {
        List<EntityDescriptorContainer> list = new ArrayList<EntityDescriptorContainer>();
        List<String> files = getFiles();
        String entityID;
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
                List<String> ids = new ArrayList<>();
                for (EntityDescriptor ed : descriptors.getEntityDescriptors()) {
                    entityID = ed.getEntityID();
                    if (!StringEscapeUtils.escapeJava(entityID).equals(entityID)) {
                        throw new EIDASMetadataProviderException(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode(),
                                EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage(),
                                "No entity descriptor for URL " + entityID);
                    }

                    ids.add(entityID);
                    LOG.info("Added entity descriptor for " + entityID);
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

    public void addListenerContentChanged( IStaticMetadataChangeListener listener){
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

}
