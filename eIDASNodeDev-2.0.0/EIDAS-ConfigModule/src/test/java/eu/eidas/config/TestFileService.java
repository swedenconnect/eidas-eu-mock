package eu.eidas.config;

import eu.eidas.FileUtils;
import eu.eidas.config.EIDASNodeMasterConfiguration;
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
import java.util.List;

import static org.junit.Assert.*;

/**
 * write a node configuration, also an encryptionConf.xml file
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
public class TestFileService {
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
        FileUtils.copyFile(new File(FILEREPO_DIR_READ), new File(FILEREPO_DIR_WRITE));
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR_WRITE);
    }
    @After
    public void removeDir(){
        java.io.File sampleNodeRepo=new java.io.File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
    }

    @Test
    public void testFileList(){
        List<String> fileList= configUtil.getFileService().getFileList(true);
        assertFalse(fileList.isEmpty());
        assertTrue(fileList.size()==1);
    }

    @Test
    public void testBackup(){
        List<String> fileList= configUtil.getFileService().getFileList(true);
        assertTrue(fileList.size()==1);
        configUtil.getFileService().backup();
        fileList= configUtil.getFileService().getFileList(false);
        assertTrue(fileList.size()>2);
    }

}
