package eu.eidas.config;

import eu.eidas.config.EIDASNodeMasterConfiguration;
import eu.eidas.config.impl.FileConfigurationRepository;
import eu.eidas.config.impl.FileEidasNodeConfiguration;
import eu.eidas.impl.file.FileService;
import eu.eidas.samlengineconfig.*;
import eu.eidas.samlengineconfig.impl.EngineInstanceImpl;
import eu.eidas.samlengineconfig.impl.InstanceConfigurationImpl;
import eu.eidas.samlengineconfig.impl.SamlEngineConfigurationImpl;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceMarshallerImpl;
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

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * reads an eIDAS Node configuration
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
@FixMethodOrder(MethodSorters.JVM)
public class TestEidasNodeConfig {
    private static final String FILEREPO_DIR="src/test/resources/config/";
    private static final String FILEREPO_DIR_INCORRECT_COUNTRYNUMBER="src/test/resources/configIncorrectCountryNumber/";
    private static final String FILEREPO_DIR_INVALID_COUNTRYNUMBER="src/test/resources/configInvalidCountryNumber/";
    @Autowired
    private EIDASNodeMasterConfiguration eidasNodeMasterConfiguration = null;

    @Before
    public void setUp(){
        assertNotNull(eidasNodeMasterConfiguration);
    }
    @Test
    public void testReadEidasXMLConfig(){
        ((FileConfigurationRepository)(eidasNodeMasterConfiguration.getRepository())).getFileService().setRepositoryDir(FILEREPO_DIR);
        eidasNodeMasterConfiguration.getNodeConfiguration().load();
        Properties nodeProps = eidasNodeMasterConfiguration.getNodeConfiguration().getEidasProperties();
        assertNotNull(nodeProps);
        assertFalse(nodeProps.isEmpty());
        assertNotNull(eidasNodeMasterConfiguration.getNodeConfiguration().getNodeParameters());
        assertFalse(eidasNodeMasterConfiguration.getNodeConfiguration().getNodeParameters().isEmpty());
        assertNotNull(eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries());
        assertFalse(eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries().isEmpty());
        assertEquals(2, eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries().size());
    }
    @Test
    public void testReadEidasXMLConfigIncorrectServiceNumber(){
        ((FileConfigurationRepository)(eidasNodeMasterConfiguration.getRepository())).getFileService().setRepositoryDir(FILEREPO_DIR_INCORRECT_COUNTRYNUMBER);
        eidasNodeMasterConfiguration.getNodeConfiguration().load();
        Properties nodeProps = eidasNodeMasterConfiguration.getNodeConfiguration().getEidasProperties();
        assertNotNull(nodeProps);
        assertFalse(nodeProps.isEmpty());
        assertNotNull(eidasNodeMasterConfiguration.getNodeConfiguration().getNodeParameters());
        assertFalse(eidasNodeMasterConfiguration.getNodeConfiguration().getNodeParameters().isEmpty());
        assertNotNull(eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries());
        assertFalse(eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries().isEmpty());
        assertEquals(2, eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries().size());
        assertEquals("8", nodeProps.getProperty("service.number"));
    }
    @Test
    public void testReadEidasXMLConfigInvalidServiceNumber(){
        ((FileConfigurationRepository)(eidasNodeMasterConfiguration.getRepository())).getFileService().setRepositoryDir(FILEREPO_DIR_INVALID_COUNTRYNUMBER);
        eidasNodeMasterConfiguration.getNodeConfiguration().load();
        Properties nodeProps = eidasNodeMasterConfiguration.getNodeConfiguration().getEidasProperties();
        assertTrue(eidasNodeMasterConfiguration.getNodeConfiguration().getEidasCountries().isEmpty());
        assertEquals("a", nodeProps.getProperty("service.number"));
    }


}
