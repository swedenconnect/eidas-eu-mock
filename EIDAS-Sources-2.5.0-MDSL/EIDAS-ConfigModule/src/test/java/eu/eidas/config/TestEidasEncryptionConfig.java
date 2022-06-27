/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.config;

import eu.eidas.config.node.EIDASNodeCountry;
import eu.eidas.samlengineconfig.impl.tools.EidasConfigManagerUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import java.util.Properties;

import static org.junit.Assert.*;

/**
 * write a node configuration, also an encryptionConf.xml file
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
public class TestEidasEncryptionConfig {
    private static final String FILEREPO_DIR_READ="src/test/resources/config/";
    private static final String FILEREPO_DIR_WRITE="target/test/samplenodeconfig/";
    @Autowired
    private EIDASNodeMasterConfiguration nodeMasterConfiguration = null;
    @Autowired
    private EidasConfigManagerUtil configUtil = null;

    @Before
    public void setUp(){
        assertNotNull(nodeMasterConfiguration);
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        sampleNodeRepo.mkdirs();
        (new java.io.File(FILEREPO_DIR_WRITE+"samlengine/")).mkdirs();
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR_READ);
    }
    @After
    public void removeDir(){
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
    }

    @Test
    public void testWriteNodeXMLConfig(){
        nodeMasterConfiguration.getNodeConfiguration().load();
        Properties nodeProps = nodeMasterConfiguration.getNodeConfiguration().getEidasProperties();
        assertNotNull(nodeProps);
        assertFalse(nodeProps.isEmpty());
        assertNotNull(nodeMasterConfiguration.getNodeConfiguration().getNodeParameters());
        assertFalse(nodeMasterConfiguration.getNodeConfiguration().getNodeParameters().isEmpty());
        assertNotNull(nodeMasterConfiguration.getNodeConfiguration().getEidasCountries());
        assertFalse(nodeMasterConfiguration.getNodeConfiguration().getEidasCountries().isEmpty());
        assertEquals(2, nodeMasterConfiguration.getNodeConfiguration().getEidasCountries().size());
        for(int i=0;i<2;i++){
            EIDASNodeCountry country = nodeMasterConfiguration.getNodeConfiguration().getEidasCountries().get(i);
            country.setEncryptionTo(true);
            country.setEncryptionFrom(false);
        }
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR_WRITE);
        nodeMasterConfiguration.getNodeConfiguration().save();
    }

}
