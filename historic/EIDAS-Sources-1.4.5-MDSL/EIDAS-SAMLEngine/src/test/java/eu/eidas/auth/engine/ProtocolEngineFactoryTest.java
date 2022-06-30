package eu.eidas.auth.engine;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.annotation.Nonnull;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.eidas.auth.commons.Constants;
import eu.eidas.auth.commons.io.ResourceLocator;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ProtocolCipherI;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * ProtocolEngineFactoryTest
 *
 * @since 1.1
 */
public final class ProtocolEngineFactoryTest {

    private static void createWorkingCopy(@Nonnull String fileName) throws IOException, URISyntaxException {
        File file = getFile(fileName);
        File workingCopy = new File(file.getParentFile(), fileName + "_workingCopy.xml");
        FileUtils.copyFile(file, workingCopy);
    }

    private static void deleteWorkingCopy(@Nonnull String fileName) throws IOException, URISyntaxException {
        getWorkingCopy(fileName).delete();
    }

    private static File getWorkingCopy(@Nonnull String fileName) throws IOException, URISyntaxException {
        URL resource = ResourceLocator.getResource(fileName + "_workingCopy.xml");
        return new File(resource.toURI());
    }

    private static File getFile(@Nonnull String fileName) throws IOException, URISyntaxException {
        URL resource = ResourceLocator.getResource(fileName + ".xml");
        return new File(resource.toURI());
    }

    private static ProtocolCipherI getCipher(@Nonnull ProtocolEngineFactory protocolEngineFactory) {

        ProtocolEngineI protocolEngine = protocolEngineFactory.getProtocolEngine("DOM-test");

        return protocolEngine.getCipher();
    }

    @Before
    public void setUp() throws Exception {
        createWorkingCopy("SamlEngine_DOM-test");
        createWorkingCopy("SamlEngine_DOM-test_true");
        createWorkingCopy("eidas_DOM-test");
        createWorkingCopy("eidas_DOM-test_true");
        createWorkingCopy("EncryptModule_DOM-test_false");
        createWorkingCopy("EncryptModule_DOM-test_empty");
        createWorkingCopy("EncryptModule_DOM-test_true");
    }

    @After
    public void tearDown() throws Exception {
        deleteWorkingCopy("SamlEngine_DOM-test");
        deleteWorkingCopy("SamlEngine_DOM-test_true");
        deleteWorkingCopy("eidas_DOM-test");
        deleteWorkingCopy("eidas_DOM-test_true");
        deleteWorkingCopy("EncryptModule_DOM-test_false");
        deleteWorkingCopy("EncryptModule_DOM-test_empty");
        deleteWorkingCopy("EncryptModule_DOM-test_true");
    }

    @Test
    public void testReloadByUpdatingSamlEngineXmlTargetEncryptModule() throws Exception {

        ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(
                new ProtocolEngineConfigurationFactory("SamlEngine_DOM-test_workingCopy.xml",
                                                       "eidas_DOM-test_workingCopy.xml", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        String fileContent = FileUtils.readFileToString(samlEngineFile, Constants.UTF8);

        assertTrue(fileContent.contains("EncryptModule_DOM-test_empty.xml"));

        String updatedContent =
                fileContent.replace("EncryptModule_DOM-test_empty.xml", "EncryptModule_DOM-test_true_workingCopy.xml");

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Constants.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());
    }

    @Test
    public void testReloadByUpdatingSamlEngineXmlProp() throws Exception {

        ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(
                new ProtocolEngineConfigurationFactory("SamlEngine_DOM-test_workingCopy.xml",
                                                       "eidas_DOM-test_workingCopy.xml", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        File samlEngineFileTrue = getWorkingCopy("SamlEngine_DOM-test_true");

        String updatedContent = FileUtils.readFileToString(samlEngineFileTrue, Constants.UTF8);

        assertTrue(updatedContent.contains("<parameter name=\"response.encryption.mandatory\" value=\"true\" />"));

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Constants.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());
    }

    @Test
    public void testReloadByUpdatingEncryptModuleXml() throws Exception {

        ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(
                new ProtocolEngineConfigurationFactory("SamlEngine_DOM-test_workingCopy.xml",
                                                       "eidas_DOM-test_workingCopy.xml", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        String fileContent = FileUtils.readFileToString(samlEngineFile, Constants.UTF8);

        assertTrue(fileContent.contains("EncryptModule_DOM-test_empty.xml"));

        String updatedContent =
                fileContent.replace("EncryptModule_DOM-test_empty.xml", "EncryptModule_DOM-test_empty_workingCopy.xml");

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Constants.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());

        File encryptModuleFile = getWorkingCopy("EncryptModule_DOM-test_empty");

        long lastModified3 = encryptModuleFile.lastModified();

        String encryptModuleFileContent = FileUtils.readFileToString(encryptModuleFile, Constants.UTF8);

//        assertTrue(encryptModuleFileContent.contains("<entry key=\"response.encryption.mandatory\">true</entry>"));
        assertFalse(encryptModuleFileContent.contains("response.encryption.mandatory"));

        String encryptModuleFileUpdatedContent =
                FileUtils.readFileToString(getWorkingCopy("EncryptModule_DOM-test_true"), Constants.UTF8);

        FileUtils.writeStringToFile(encryptModuleFile, encryptModuleFileUpdatedContent, Constants.UTF8);

        long lastModified4 = encryptModuleFile.lastModified();

        assertTrue(lastModified4 > lastModified3);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());

        encryptModuleFileUpdatedContent =
                FileUtils.readFileToString(getWorkingCopy("EncryptModule_DOM-test_false"), Constants.UTF8);

        Thread.sleep(1000);
        FileUtils.writeStringToFile(encryptModuleFile, encryptModuleFileUpdatedContent, Constants.UTF8);

        long lastModified5 = encryptModuleFile.lastModified();

        assertTrue(lastModified5 > lastModified4);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());
    }
}
