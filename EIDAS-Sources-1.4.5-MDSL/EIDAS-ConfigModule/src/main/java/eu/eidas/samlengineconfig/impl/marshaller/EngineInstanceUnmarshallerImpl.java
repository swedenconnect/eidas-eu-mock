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
package eu.eidas.samlengineconfig.impl.marshaller;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.impl.file.FileService;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;
import eu.eidas.samlengineconfig.impl.EngineInstanceImpl;
import eu.eidas.samlengineconfig.impl.InstanceConfigurationImpl;
import eu.eidas.samlengineconfig.impl.JAXBConfigurationParameter;
import eu.eidas.samlengineconfig.impl.SamlEngineConfigurationImpl;
import eu.eidas.samlengineconfig.impl.tools.EidasConfigManagerUtil;

/**
 * serialize/deserialize an EngineInstance
 */
public class EngineInstanceUnmarshallerImpl {
    private static final Class JAXB_CLASSES[]={SamlEngineConfigurationImpl.class, EngineInstanceImpl.class, InstanceConfigurationImpl.class, JAXBConfigurationParameter.class};
    private static final Logger LOG = LoggerFactory.getLogger(EngineInstanceUnmarshallerImpl.class.getName());
    private FileService fileService;
    private String directory;
    public SamlEngineConfiguration readEngineInstanceFromString( String config ){
        StringReader reader = new StringReader(config);
        Object unmarshallResult=null;
        try {
            JAXBContext context = JAXBContext.newInstance(JAXB_CLASSES);
            Unmarshaller um = context.createUnmarshaller();
            unmarshallResult = um.unmarshal(reader);
        }catch(Exception exc){
            LOG.error("ERROR : error reading engine instance "+exc.getMessage());
            LOG.debug("ERROR : error reading engine instance "+exc);
        }

        if(unmarshallResult instanceof SamlEngineConfiguration) {
            return (SamlEngineConfiguration) unmarshallResult;
        }else{
            LOG.error("ERROR : unmarshalling result is not an EngineConfiguration object");
            return null;
        }
    }
    public SamlEngineConfiguration readEngineInstanceFromFile( String fileName ){
        if(!EidasConfigManagerUtil.getInstance().existsFile(fileName)){
            return null;
        }
        return readEngineInstanceFromString(EidasConfigManagerUtil.getInstance().loadFileAsString(fileName));
    }

    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
