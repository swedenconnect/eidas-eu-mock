/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */
package eu.eidas.config.impl.samlmetadata;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import eu.eidas.auth.commons.EidasErrorKey;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.config.ConfigurationException;
import eu.eidas.config.samlmetadata.MetadataItem;
import eu.eidas.config.samlmetadata.MetadataRepository;
import eu.eidas.impl.file.FileService;

public class MetadataRepositoryImpl implements MetadataRepository {

    FileService fileService;

    /**
     * saml metadata namespace and schema related info
     */
    private static final String SAML_METADATA_NAMEPSACE = "urn:oasis:names:tc:SAML:2.0:metadata";

    private static final String SAML_METADATA_DESCRIPTOR = "EntityDescriptor";

    private static final String SAML_METADATA_DESCRIPTORID = "entityID";

    /**
     * the name of the eidas.xml parameter holding the location of saml metadata files
     */
    public static final String SAML_METADATA_LOCATION = "metadata.file.repository";

    @Override
    public List<String> getIDs() {
        //imple detail: the IDs are in fact filenames
        return getFileService().getFileList(false);
    }

    @Override
    public MetadataItem getMetadataItem(String id) {
        String metadataContent = getFileService().fileToString(id);
        if (metadataContent == null || metadataContent.isEmpty()) {
            throw new ConfigurationException("", "empty metadata");
        }
        MetadataItem item = parseContent(metadataContent);
        if (item != null) {
            item.setId(id);
        }
        return item;
    }

    @Override
    public void removeItem(String id) throws ConfigurationException {
        if (fileService.existsFile(id)) {
            String absoluteFilePath = fileService.getAbsoluteFileName(id);
            if (!(new File(absoluteFilePath).delete())) {
                throw new ConfigurationException("", "the file " + absoluteFilePath + " cannot be removed");
            }
        } else {
            throw new ConfigurationException("", "the file to be removed does not exist");
        }
    }

    @Override
    public void addItemFromFile(File content, String newFileName) throws ConfigurationException {
        MetadataItem newMetadata = getMetadataItem(content.getAbsolutePath());
        if (newMetadata != null) {
            List<String> ids = getIDs();
            if (ids.contains(newFileName) || ids.contains(fileService.getAbsoluteFileName(newFileName))) {
                throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_FILE_ALREADY_EXISTS.errorCode(),
                                                 "the filename is already used in the metadata repository");
            }
            for (String id : ids) {
                MetadataItem metadata = getMetadataItem(id);
                if (metadata.getIssuerUrl().equalsIgnoreCase(newMetadata.getIssuerUrl())) {
                    throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_ISSUER_ALREADY_EXISTS.errorCode(),
                                                     "the Issuer is already used in the metadata repository");
                }
            }
            byte b[] = fileService.loadBinaryFile(content.getAbsolutePath());
            fileService.saveBinaryFile(newFileName, b);
        }
    }

    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    private MetadataItem parseContent(String content) throws ConfigurationException {
        MetadataItem item = null;
        try {
            Document document = DocumentBuilderFactoryUtil.parse(content);
            final NodeList list = document.getElementsByTagNameNS(SAML_METADATA_NAMEPSACE, SAML_METADATA_DESCRIPTOR);
            if (list != null && list.getLength() > 0) {
                NamedNodeMap attributes = list.item(0).getAttributes();
                Node entityID = attributes.getNamedItem(SAML_METADATA_DESCRIPTORID);
                if (entityID != null) {
                    item = new MetadataItem();
                    item.setIssuerUrl(entityID.getNodeValue());
                }
            }

        } catch (UnsupportedEncodingException e) {
            throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_FILE_PARSING.errorCode(),
                                             "error parsing metadata", e);
        } catch (SAXException e) {
            throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_FILE_PARSING.errorCode(),
                                             "error parsing metadata", e);
        } catch (IOException e) {
            throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_FILE_PARSING.errorCode(),
                                             "error parsing metadata", e);
        } catch (ParserConfigurationException e) {
            throw new ConfigurationException(EidasErrorKey.CONSOLE_METADATA_FILE_PARSING.errorCode(),
                                             "error parsing metadata", e);
        }
        return item;
    }
}
