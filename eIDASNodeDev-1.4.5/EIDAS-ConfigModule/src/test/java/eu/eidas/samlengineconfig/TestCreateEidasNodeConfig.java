package eu.eidas.samlengineconfig;

import eu.eidas.samlengineconfig.EngineInstance;
import eu.eidas.samlengineconfig.InstanceConfiguration;
import eu.eidas.samlengineconfig.PropsParameter;
import eu.eidas.samlengineconfig.SamlEngineConfiguration;
import eu.eidas.samlengineconfig.StringParameter;
import eu.eidas.samlengineconfig.impl.EngineInstanceImpl;
import eu.eidas.samlengineconfig.impl.InstanceConfigurationImpl;
import eu.eidas.samlengineconfig.impl.SamlEngineConfigurationImpl;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceMarshallerImpl;
import eu.eidas.samlengineconfig.impl.marshaller.EngineInstanceUnmarshallerImpl;
import eu.eidas.samlengineconfig.impl.tools.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

import static org.junit.Assert.*;

/**
 * creates a complete eidas config
 *
 * currently, the EIDAS configuration (one EngineConfiguration object) is made up from
 * 4 instances (EngineInstance), named EIDASService, SP-EIDASConnector, EIDASConnector-EIDASService, Specific
 * Each of these instances has 3 InstanceConfiguration: SamlEngineConf, SignatureConf, EncryptionConf
 * SamlEngineConf has one parameter named fileConfiguration of type PropsParameter
 * SignatureConf has one parameter named fileConfiguration of type PropsParameter and one
 *    parameter named class, of type String
 * EncryptionConf has 3 parameters:
 *     - fileConfiguration, like the other 2 InstanceConfiguration
 *     - class, like SignatureConf
 *     - fileActivationConfiguration - of type String (the value contains the complete path
 *     to an xml file which enable/disable encryption in eidas nodes communication)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="/testcontext.xml")
public class TestCreateEidasNodeConfig {
    private static final String FILEREPO_DIR="target/test/sampleeidasconfig/";
    private static final String SAML_ENGINE_NAME= "SamlEngine.xml";
    private static final String ENGINE_INSTANCE_NAMES[]={"EIDASService", "SP-EIDASConnector", "EIDASConnector-EIDASService", "Specific"};
    private static final String FILECONFIGURATION_NAME="fileConfiguration";
    @Autowired
    private EidasConfigManagerUtil configUtil = null;
    @Autowired
    private EngineInstanceMarshallerImpl engineMarshaller;
    @Autowired
    private EngineInstanceUnmarshallerImpl engineUnmarshaller;

    @Before
    public void setUp(){
        assertNotNull(configUtil);
        java.io.File sampleEidasRepo=new java.io.File(FILEREPO_DIR);
        FileSystemUtils.deleteRecursively(sampleEidasRepo);
        sampleEidasRepo.mkdirs();
        (new java.io.File(FILEREPO_DIR+"samlengine/")).mkdirs();
        configUtil.getFileService().setRepositoryDir(FILEREPO_DIR);
    }

    @After
    public void removeDir(){
        java.io.File sampleEidasRepo=new java.io.File(FILEREPO_DIR);
        FileSystemUtils.deleteRecursively(sampleEidasRepo);
    }
    @Test
    public void createEidasConfig(){
        SamlEngineConfiguration samlEngineConfiguration =new SamlEngineConfigurationImpl();
        EngineInstance[] engineInstances =  populateEngineInstance();
        for(int i=0;i<engineInstances.length;i++) {
            samlEngineConfiguration.addInstance(engineInstances[i]);
        }
        engineMarshaller.writeEngineInstanceToFile(SAML_ENGINE_NAME, samlEngineConfiguration);

        SamlEngineConfiguration ec = engineUnmarshaller.readEngineInstanceFromFile(SAML_ENGINE_NAME);
        assertNotNull(ec);
        assertEquals(ec.getInstances().size(), 4);
        assertEquals(ec.getInstances().get(0).getConfigurations().size(), 3);
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getName());
        assertEquals(ec.getInstances().get(0).getConfigurations().get(1).getName(), "SignatureConf");
        assertNotNull(ec.getInstances().get(0).getConfigurations().get(0).getParameters());
        assertFalse(ec.getInstances().get(0).getConfigurations().get(0).getParameters().isEmpty());
        assertTrue(ec.getInstances().get(0).getConfigurations().get(1).getParameters().get(1) instanceof StringParameter);
        assertNotNull(((PropsParameter) ec.getInstances().get(0).getConfigurations().get(0).getParameters().get(0)).getFileName());
    }
    private EngineInstance[] populateEngineInstance(){
        EngineInstance engineInstances[]=new EngineInstance[ENGINE_INSTANCE_NAMES.length];
        for(int i=0;i<ENGINE_INSTANCE_NAMES.length;i++){
            engineInstances[i]=new EngineInstanceImpl();
            engineInstances[i].setName(ENGINE_INSTANCE_NAMES[i]);
            prepareEngineInstance(engineInstances[i]);
        }
        return engineInstances;
    }

    private void prepareEngineInstance(EngineInstance engineInstance){
        engineInstance.addConfiguration(createSamlEngineConf(engineInstance));
        engineInstance.addConfiguration(createSignatureConf(engineInstance));
        engineInstance.addConfiguration(createEncryptionConf(engineInstance));
    }

    private InstanceConfiguration createSamlEngineConf(EngineInstance engineInstance){
        InstanceConfiguration config=new InstanceConfigurationImpl("SamlEngineConf", null);
        createFileConfigurationParameter(config, engineInstance, "EidasSamlEngine");
        return config;
    }

    private void createFileConfigurationParameter(InstanceConfiguration config, EngineInstance engineInstance, String namePrefix){
        PropsParameter fileConfiguration=new PropsParameter();
        fileConfiguration.setName(FILECONFIGURATION_NAME);
        fileConfiguration.setFileName(namePrefix+"_" + engineInstance.getName()+".xml");
        Properties props=getSampleProps();
        fileConfiguration.setValue(props);
        EidasConfigManagerUtil.getInstance().saveProps(fileConfiguration.getFileName(), (Properties) fileConfiguration.getValue());
        config.getParameters().add(fileConfiguration);
    }
    private void createEncryptionConfigurationParameter(InstanceConfiguration config){
        PropsParameter fileConfiguration=new PropsParameter();
        fileConfiguration.setName("fileActivationConfiguration");
        fileConfiguration.setFileName("encryptionConf.xml");
        Properties props=getSampleEncryptionProps();
        fileConfiguration.setValue(props);
        EidasConfigManagerUtil.getInstance().saveProps(fileConfiguration.getFileName(), (Properties) fileConfiguration.getValue());
        config.getParameters().add(fileConfiguration);
    }

    private InstanceConfiguration createSignatureConf(EngineInstance engineInstance){
        InstanceConfiguration config=new InstanceConfigurationImpl("SignatureConf", null);
        createFileConfigurationParameter(config, engineInstance, "SignModule");
        StringParameter classParam=new StringParameter();
        classParam.setName("class");
        classParam.setValue("eu.eidas.auth.engine.core.impl.SignSW");
        config.getParameters().add(classParam);
        return config;
    }
    private InstanceConfiguration createEncryptionConf(EngineInstance engineInstance){
        InstanceConfiguration config=new InstanceConfigurationImpl("EncryptionConf", null);
        createFileConfigurationParameter(config, engineInstance, "EncryptModule");
        StringParameter classParam=new StringParameter();
        classParam.setName("class");
        classParam.setValue("eu.eidas.auth.engine.core.impl.EncryptionSW");
        config.getParameters().add(classParam);

        createEncryptionConfigurationParameter(config);
        return config;
    }

    private Properties getSampleProps(){
        return loadFromFile("/samlengine/SamlEngine_SP-EIDASConnector.xml");
    }
    private Properties getSampleEncryptionProps(){
        return loadFromFile("/samlengine/encryptionConf.xml");
    }

    private Properties loadFromFile(String filename){
        Properties props=new Properties();
        InputStream is=null;
        try{
            is = TestCreateEidasNodeConfig.class.getResourceAsStream(filename);
            props.loadFromXML(is);
        }catch(FileNotFoundException fnfe){
            fail("FileNotFoundException: "+ fnfe);
        }catch(InvalidPropertiesFormatException ipfe){
            fail("InvalidPropertiesFormatException: "+ ipfe);
        }catch(IOException ioe){
            fail("IOException: "+ ioe);
        }finally{
            safeClose(is);
        }

        return props;

    }
    private void safeClose(Closeable c){
        if(c!=null){
            try{
                c.close();
            }catch(IOException ioe) {
                fail("IOException while closing inputstream: "+ ioe);
            }

        }
    }

}
