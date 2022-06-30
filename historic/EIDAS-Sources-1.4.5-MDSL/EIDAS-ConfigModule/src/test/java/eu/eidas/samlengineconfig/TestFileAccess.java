package eu.eidas.samlengineconfig;
import eu.eidas.samlengineconfig.PropsParameter;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;
import eu.eidas.samlengineconfig.StringParameter;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceUnmarshallerImpl;
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

import java.io.File;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * testing reading configuration from xml files
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
public class TestFileAccess {
    private static final String TEST_FILE1= "SignModule_EIDASService.xml";
    private static final String FILEREPO_DIR="src/test/resources/";
    private static final String FILEREPO_DIR_WRITE="target/test/sampleeidasconfig/";
    @Autowired
    private EidasConfigManagerUtil configUtil = null;
    @Autowired
    private EngineInstanceUnmarshallerImpl engineUnmarshaller;
    @Before
    public void setUp(){
        assertNotNull(configUtil);
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR);
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR_WRITE+"/samlengine");
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        sampleNodeRepo.mkdirs();
    }
    @After
    public void removeDir(){
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
    }
    @Test
    public void testDeserializeProps(){
        assertNotNull(configUtil);
        assertTrue(configUtil.existsFile(TEST_FILE1));
        Properties p=configUtil.loadProps(TEST_FILE1);
        assertNotNull(p);
        assertTrue(p.size() > 0);
    }


    @Test
    public void testDeserialize(){
        SamlEngineConfiguration ec = engineUnmarshaller.readEngineInstanceFromFile("SamlEngine.xml");
        assertNotNull(ec);
        assertEquals(ec.getInstances().size(), 2);
        assertEquals(ec.getInstances().get(0).getConfigurations().size(), 1);
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getName());
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getParameters());
        assertFalse(ec.getInstances().get(0).getConfigurations().get(0).getParameters().isEmpty());
        assertTrue(ec.getInstances().get(0).getConfigurations().get(0).getParameters().get(0) instanceof StringParameter);
        assertNotNull(((PropsParameter) ec.getInstances().get(0).getConfigurations().get(0).getParameters().get(1)).getFileName());
    }

    @Test
    public void testSerializeProps(){
        Properties p=configUtil.loadProps(TEST_FILE1);
        assertNotNull(p);
        assertTrue(p.size() > 0);
        int initialSize=p.size();
        p.setProperty("newkey", "newvalue");
        String fileName= UUID.randomUUID().toString();
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR_WRITE);
        configUtil.saveProps(fileName, p);
        assertEquals(initialSize + 1, configUtil.loadProps(fileName).size());
    }


}
