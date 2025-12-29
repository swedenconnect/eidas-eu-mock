/*
 * Copyright (c) 2021 by European Commission
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
import org.apache.commons.lang.StringEscapeUtils;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MetadataLoaderPlugin sample implementation for testing only (not robust)
 * the source metadata: all readable xml files found in a configured directory
 */
public class FileMetadataLoader implements MetadataLoaderPlugin {

    private String repositoryPath;

    private boolean isRepositoryPathNullOrBlank = false;

    private static final Logger LOG = LoggerFactory.getLogger(FileMetadataLoader.class.getName());

    List<IStaticMetadataChangeListener> listeners = new ArrayList<>();

    /**
     * @return a list of entity descriptors read from the current directory (repositoryPath)
     * @throws EIDASMetadataProviderException if the url is invalid
     */
    public List<EntityDescriptorContainer> getEntityDescriptors() throws EIDASMetadataProviderException {
        if (isRepositoryPathNullOrBlank) {
            return Collections.emptyList();
        }

        List<EntityDescriptorContainer> list = new ArrayList<>();
        List<Path> files = getFiles();
        String entityID;
        for (Path filePath : files) {
            EntityDescriptorContainer descriptors = null;
            try {
                descriptors = loadDescriptors(filePath);
            } catch (UnmarshallException e) {
                LOG.error("Failed to unmarshall entity descriptors from static metadata file '"+filePath+"'");
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
        isRepositoryPathNullOrBlank = isRepositoryPathNullOrBlank(repositoryPath);

        if (!isRepositoryPathNullOrBlank) {
            try {
                String normalizedRepositoryPath = normalizeFilePath(Paths.get(repositoryPath));
                this.repositoryPath = normalizedRepositoryPath;
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid repository path", e);
            }
        }
    }

    private boolean isRepositoryPathNullOrBlank(String repositoryPath) {
        return repositoryPath == null || repositoryPath.isBlank();
    }

    private String normalizeFilePath(Path filePath) throws IOException {
       return filePath
               .toFile()
               .getCanonicalPath();
    }

    private List<Path> getFiles(){
        Path repositoryPath = Paths.get(this.repositoryPath);
        try (Stream<Path> filePaths = Files.list(repositoryPath)){
            return filePaths.collect(Collectors.toList());
        } catch (IOException e) {
            LOG.warn("Failed to fetch files at: " + this.repositoryPath, e);
        }
        return new ArrayList<>();
    }

    private EntityDescriptorContainer loadDescriptors(Path filePath) throws UnmarshallException {
        if (filePath == null || !filePath.toString().endsWith(".xml")) {
            LOG.info("Ignored file: " + filePath + ". Has not a xml extension ");
            return null;
        }

        LOG.info("Loading entity descriptors from file "+ filePath);
        try {
            byte[] content = Files.readAllBytes(filePath);
            return MetadataUtil.deserializeEntityDescriptor(EidasStringUtil.toString(content));
        } catch (IOException e) {
            LOG.error("Failed to fetch the descriptor: " + filePath, e);
            return null;
        }
    }

    public void addListenerContentChanged( IStaticMetadataChangeListener listener){
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

}

