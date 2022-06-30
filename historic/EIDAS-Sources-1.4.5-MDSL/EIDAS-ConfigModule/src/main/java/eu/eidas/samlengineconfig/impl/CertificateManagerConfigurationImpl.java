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
package eu.eidas.samlengineconfig.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import eu.eidas.config.ConfigurationException;
import eu.eidas.impl.file.FileService;
import eu.eidas.samlengineconfig.AbstractCertificateConfigurationManager;
import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceUnmarshallerImpl;
import eu.eidas.samlengineconfig.impl.tools.EidasConfigManagerUtil;

/**
 * implementation for certificate manager configuration
 */
public class CertificateManagerConfigurationImpl extends AbstractCertificateConfigurationManager {
    private SamlEngineConfiguration samlEngineConfiguration =null;
    private boolean active=false;

    @Override
    public void addConfiguration(String name, String type, Map<String, String> props, boolean replaceExisting) {
        throw new ConfigurationException("","not yet implemented");
    }
    @Override
    public void setLocation(String location){
        super.setLocation(location);
    }

    @Override
    public EngineInstance getInstance(String name) {
        return null;
    }
    @Override
    public Map<String, EngineInstance> getConfiguration() {
        if(samlEngineConfiguration ==null && isActive()){
            readConfiguration();
        }
        return samlEngineConfiguration==null?new HashMap<String, EngineInstance>():((SamlEngineConfigurationImpl) samlEngineConfiguration).getInstanceMap();
    }

    private void readConfiguration() {
        //configurationName=SamlEngine.xml
        String masterFileName=configurationName;
        samlEngineConfiguration = engineUnmarshaller.readEngineInstanceFromFile(masterFileName);
    }

    public boolean isActive(){
        if(!active) {
            FileService fileService = EidasConfigManagerUtil.getInstance().getFileService();
            if(getLocation()!=null && fileService!=null && (fileService.getRepositoryDir()==null || !fileService.existsFile(""))){
                fileService.setRepositoryDir(getLocation());
            }
            active = fileService!=null &&fileService.existsFile("");
        }
        return active;
    }
    private EngineInstanceUnmarshallerImpl engineUnmarshaller;
    private String configurationName;

    public EngineInstanceUnmarshallerImpl getEngineUnmarshaller() {
        return engineUnmarshaller;
    }

    public void setEngineUnmarshaller(EngineInstanceUnmarshallerImpl engineUnmarshaller) {
        this.engineUnmarshaller = engineUnmarshaller;
    }

    public String getConfigurationName() {
        return configurationName;
    }

    public void setConfigurationName(String configurationName) {
        this.configurationName = configurationName;
    }

    public Properties loadEncryptionConfiguration(){
        return EidasConfigManagerUtil.getInstance().loadProps("encryptionConf.xml");
    }
    public void saveEncryptionConfiguration(Properties encryptionConf){
        EidasConfigManagerUtil.getInstance().saveProps("encryptionConf.xml", encryptionConf);
    }
}
