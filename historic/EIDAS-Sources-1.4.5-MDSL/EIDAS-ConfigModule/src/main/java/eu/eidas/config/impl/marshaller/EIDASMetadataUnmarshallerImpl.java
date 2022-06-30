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
package eu.eidas.config.impl.marshaller;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.config.impl.CategoryImpl;
import eu.eidas.config.impl.CategoryListImpl;
import eu.eidas.config.impl.EIDASNodeConfFile;
import eu.eidas.config.impl.EIDASNodeMetaconfigHolderImpl;
import eu.eidas.config.impl.EIDASNodeMetaconfigListImpl;
import eu.eidas.config.impl.EIDASParameterMetaImpl;
import eu.eidas.config.impl.FileListImpl;
import eu.eidas.impl.file.FileService;
import eu.eidas.samlengineconfig.impl.tools.EidasConfigManagerUtil;

/**
 * serialize/deserialize a Metadata configuration
 */
public class EIDASMetadataUnmarshallerImpl {
    private static final Class JAXB_CLASSES[]={EIDASNodeMetaconfigHolderImpl.class, CategoryListImpl.class, CategoryImpl.class,
            EIDASNodeMetaconfigListImpl.class, EIDASParameterMetaImpl.class, EIDASNodeConfFile.class, FileListImpl.class};
    private static final Logger LOG = LoggerFactory.getLogger(EIDASMetadataUnmarshallerImpl.class.getName());
    private FileService fileService;
    private String directory;
    public EIDASNodeMetaconfigHolderImpl readNodeMetadataFromString(String config) {
        StringReader reader = new StringReader(config);
        Object unmarshallResult = null;
        try {
            JAXBContext context = JAXBContext.newInstance(JAXB_CLASSES);
            Unmarshaller um = context.createUnmarshaller();
            unmarshallResult = um.unmarshal(reader);
        } catch (Exception exc) {
            LOG.error("ERROR : error reading node metadata " + exc.getMessage());
            LOG.debug("ERROR : error reading node metadata " + exc);
        }

        if (unmarshallResult instanceof EIDASNodeMetaconfigHolderImpl){
            EIDASNodeMetaconfigHolderImpl holder = (EIDASNodeMetaconfigHolderImpl) unmarshallResult;
            return holder;
        }else{
            LOG.error("ERROR : unmarshalling result is not an EIDASNodeMetadataHolder object");
            return null;
        }
    }
    public EIDASNodeMetaconfigHolderImpl readEIDASNodeMetadataFromFile( String fileName ){
        if(!EidasConfigManagerUtil.getInstance().existsFile(fileName)){
            return null;
        }
        return readNodeMetadataFromString(EidasConfigManagerUtil.getInstance().loadFileAsString(fileName));
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
