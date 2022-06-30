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
package eu.eidas.config.impl;

import java.util.Properties;

import eu.eidas.config.ConfigurationException;
import eu.eidas.config.ConfigurationRepository;
import eu.eidas.impl.file.FileService;

/**
 * file based configuration repository implementation
 */
public class FileConfigurationRepository implements ConfigurationRepository {

    private FileService fileService;

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAddress() {
        return fileService.getRepositoryDir();
    }

    public Properties loadPropertiesFromXML(String fileName){
        return fileService.loadPropsFromXml(fileName);
    }

    public Properties loadPropsFromTextFile(String fileName){
        return fileService.loadPropsFromTextFile(fileName);
    }

    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
    public void savePropertiesToXML(String fileName, Properties props){
        fileService.saveToXMLFile(fileName, props);
    }
    public void savePropertiesToTextFile(String fileName, Properties props){
        fileService.saveToPropsFile(fileName, props);
    }
    public byte[] getRawContent(String url){
        return fileService.loadBinaryFile(url);
    }
    public void setRawContent(String url, byte[] data){
        fileService.saveBinaryFile(url, data);
    }

    @Override
    public void backup() throws ConfigurationException {
        getFileService().backup();
    }
}
