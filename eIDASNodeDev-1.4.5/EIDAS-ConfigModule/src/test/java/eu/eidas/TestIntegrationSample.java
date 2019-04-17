package eu.eidas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.config.EIDASNodeMasterConfiguration;
import eu.eidas.config.impl.FileConfigurationRepository;
import eu.eidas.config.node.EIDASNodeParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * example how this module can be integrated on a configuration reader side.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
public class TestIntegrationSample {
    private static final String FILEREPO_DIR="target/test/config/";
    private static final String FILEREPO_SOURCE_DIR="src/test/resources/config/";
    private static final String PARAM_NAME="connector.id";
    private static final String COUNTRY_VALUE="CB";
    private static final String NEW_COUNTRY_VALUE="OC";
    @Autowired
    EIDASNodeMasterConfiguration eidasMasterConfiguration=null;

    @Before
    public void setUp(){
        java.io.File sampleEidasRepo=new java.io.File(FILEREPO_DIR);
        FileSystemUtils.deleteRecursively(sampleEidasRepo);
        sampleEidasRepo.mkdirs();
        FileUtils.copyFile(new File(FILEREPO_SOURCE_DIR), new File(FILEREPO_DIR));
    }


    @Test
    public void testRead(){
        assertNotNull(eidasMasterConfiguration);
        assertNotNull(eidasMasterConfiguration.getNodeConfiguration());
        ((FileConfigurationRepository)(eidasMasterConfiguration.getRepository())).getFileService().setRepositoryDir(FILEREPO_DIR);
        eidasMasterConfiguration.getNodeConfiguration().load();//loads parameters
        assertNotNull(eidasMasterConfiguration.getNodeConfiguration().getMetadataProvider());
        assertFalse(eidasMasterConfiguration.getNodeConfiguration().getMetadataProvider().getCategories().isEmpty());
    }

    @Test
    public void testWrite(){
        ((FileConfigurationRepository)(eidasMasterConfiguration.getRepository())).getFileService().setRepositoryDir(FILEREPO_DIR);
        eidasMasterConfiguration.getNodeConfiguration().load();//loads parameters
        eidasMasterConfiguration.getNodeConfiguration().getMetadataProvider().getCategories();
        assertEquals(eidasMasterConfiguration.getNodeConfiguration().getMetadataProvider().getMetadata(PARAM_NAME).getName(), PARAM_NAME);
        EIDASNodeParameter param = eidasMasterConfiguration.getNodeConfiguration().getNodeParameters().get(PARAM_NAME);
        assertEquals(param.getValue(), COUNTRY_VALUE);
        param.setValue(NEW_COUNTRY_VALUE);
        eidasMasterConfiguration.getNodeConfiguration().save();
        checkFileIsChanged();
        param.setValue(COUNTRY_VALUE);
        eidasMasterConfiguration.getNodeConfiguration().save();
    }

    private static final String CONTROL_SAVE="<entry key=\""+PARAM_NAME+"\">"+NEW_COUNTRY_VALUE+"</entry>";
    private void checkFileIsChanged(){
        try {
            FileInputStream fis = new FileInputStream(FILEREPO_DIR + "eidas.xml");
            byte data[]=new byte[fis.available()];
            fis.read(data);
            String content = EidasStringUtil.toString(data);
            assertTrue(content.contains(CONTROL_SAVE));
            fis.close();
        }catch(IOException ioe){
            fail("cannot check eidas.xml");
        }
    }
}
