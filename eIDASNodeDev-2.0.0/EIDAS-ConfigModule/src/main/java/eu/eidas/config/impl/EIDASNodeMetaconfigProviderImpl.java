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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.config.impl.marshaller.EIDASMetadataUnmarshallerImpl;
import eu.eidas.config.node.EIDASNodeMetaconfigProvider;
import eu.eidas.config.node.EIDASNodeParameterCategory;
import eu.eidas.config.node.EIDASNodeParameterMeta;

/**
 *
 */
public class EIDASNodeMetaconfigProviderImpl extends EIDASNodeMetaconfigProvider {
    private static final Logger LOG = LoggerFactory.getLogger(EIDASNodeMetaconfigProviderImpl.class.getName());
    private static final String DEFAULT_EIDAS_NODE_CONF_FILENAME="eidas.xml";
    private EIDASNodeConfFile defaultEidasNodeConfFile=null;

    private static final String EIDASNODE_METACONFIG = "/EIDASNodemetadata.xml";

    @Override
    public Map<String, List<EIDASNodeParameterMeta>> getCategorizedParameters() {
        if(getCategories().isEmpty()) {
            loadData();
        }

        return super.getCategorizedParameters();
    }

    @Override
    public List<EIDASNodeParameterCategory> getCategories() {
        if(super.getCategories().isEmpty()) {
            loadData();
        }

        return super.getCategories();
    }

    private void loadData(){
        //load the info from the resurce stream
        EIDASNodeMetaconfigHolderImpl holder = loadHolder();
        if (holder !=null && holder.getCategoryList() != null) {
            super.getCategories().clear();
            for (EIDASNodeParameterCategory c : holder.getCategoryList().getCategories()) {
                super.getCategories().add(c);
            }
        }
        if (holder !=null && holder.getFileList() != null) {
            fillFileList(holder);
        }

        if (holder !=null && holder.getNodeMetadataList() != null) {
            for (EIDASNodeParameterMeta m : holder.getNodeMetadataList().getNodeParameterMetadaList()) {
                super.addMetadata(m.getName(), m);
            }
        }

    }
    private void fillFileList(EIDASNodeMetaconfigHolderImpl holder){
        fileList.clear();
        for (EIDASNodeConfFile f : holder.getFileList().getFiles()) {
            if(DEFAULT_EIDAS_NODE_CONF_FILENAME.equalsIgnoreCase(f.getFileName())){
                defaultEidasNodeConfFile=f;
            }
            fileList.add(f);
        }

    }
    private EIDASNodeMetaconfigHolderImpl loadHolder(){
        EIDASNodeMetaconfigHolderImpl holder = null;
        InputStream is = null;
        try{
            is = EIDASNodeMetaconfigProviderImpl.class.getResourceAsStream(EIDASNODE_METACONFIG);
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            for (int c = is.read(); c != -1; c = br.read()) sb.append((char)c);
            holder = new EIDASMetadataUnmarshallerImpl().readNodeMetadataFromString(sb.toString());
        }catch(IOException ioe){
            LOG.error("error loading parameter metadata", ioe.getMessage());
            LOG.debug("error loading parameter metadata", ioe);
        }finally{
            if(is!=null){
                try {
                    is.close();
                }catch(IOException ioe){
                    LOG.error("error loading parameter metadata", ioe.getMessage());
                    LOG.debug("error loading parameter metadata", ioe);
                }
            }
        }
        return holder;
    }

    private List<EIDASNodeConfFile> fileList=new ArrayList<EIDASNodeConfFile>();

    public List<EIDASNodeConfFile> getFileList() {
        if(fileList.isEmpty()) {
            loadData();
        }
        return fileList;
    }

    public void setFileList(List<EIDASNodeConfFile> fileList) {
        this.fileList = fileList;
    }

    @Override
    public EIDASNodeConfFile getDefaultConfFile(){
        return defaultEidasNodeConfFile;
    }
}
