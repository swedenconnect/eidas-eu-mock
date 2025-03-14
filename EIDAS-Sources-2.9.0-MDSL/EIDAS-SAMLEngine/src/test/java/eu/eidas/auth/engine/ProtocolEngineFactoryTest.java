/*
 * Copyright (c) 2024 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package eu.eidas.auth.engine;

import eu.eidas.auth.commons.io.ResourceLocator;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.lang.Charsets;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.ProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import org.apache.commons.io.FileUtils;
import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link ProtocolEngineFactory}
 */
public final class ProtocolEngineFactoryTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
        createWorkingCopy("SamlEngine_DOM-test_false");
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
        deleteWorkingCopy("SamlEngine_DOM-test_false");
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
                        "", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        String fileContent = FileUtils.readFileToString(samlEngineFile, Charsets.UTF8);

        assertTrue(fileContent.contains("EncryptModule_DOM-test_empty.xml"));

        String updatedContent =
                fileContent.replace("EncryptModule_DOM-test_empty.xml", "EncryptModule_DOM-test_false_workingCopy.xml");

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Charsets.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());
    }

    @Test
    public void testReloadByUpdatingSamlEngineXmlProp() throws Exception {
        ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(
                new ProtocolEngineConfigurationFactory("SamlEngine_DOM-test_workingCopy.xml",
                        "", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        File samlEngineFileTrue = getWorkingCopy("SamlEngine_DOM-test_false");

        String updatedContent = FileUtils.readFileToString(samlEngineFileTrue, Charsets.UTF8);

        assertTrue(updatedContent.contains("<parameter name=\"response.encryption.mandatory\" value=\"false\" />"));

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Charsets.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());
    }

    @Test
    public void testReloadByUpdatingEncryptModuleXml() throws Exception {
        ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(
                new ProtocolEngineConfigurationFactory("SamlEngine_DOM-test_workingCopy.xml",
                        "", null));

        ProtocolCipherI cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());

        File samlEngineFile = getWorkingCopy("SamlEngine_DOM-test");

        long lastModified1 = samlEngineFile.lastModified();

        String fileContent = FileUtils.readFileToString(samlEngineFile, Charsets.UTF8);

        assertTrue(fileContent.contains("EncryptModule_DOM-test_empty.xml"));

        String updatedContent =
                fileContent.replace("EncryptModule_DOM-test_empty.xml", "EncryptModule_DOM-test_empty_workingCopy.xml");

        FileUtils.writeStringToFile(samlEngineFile, updatedContent, Charsets.UTF8);

        long lastModified2 = samlEngineFile.lastModified();

        assertTrue(lastModified2 > lastModified1);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertTrue(cipher.isResponseEncryptionMandatory());

        File encryptModuleFile = getWorkingCopy("EncryptModule_DOM-test_empty");

        long lastModified3 = encryptModuleFile.lastModified();

        String encryptModuleFileContent = FileUtils.readFileToString(encryptModuleFile, Charsets.UTF8);

//        assertTrue(encryptModuleFileContent.contains("<entry key=\"response.encryption.mandatory\">true</entry>"));
        assertFalse(encryptModuleFileContent.contains("response.encryption.mandatory"));

        String encryptModuleFileUpdatedContent =
                FileUtils.readFileToString(getWorkingCopy("EncryptModule_DOM-test_false"), Charsets.UTF8);

        FileUtils.writeStringToFile(encryptModuleFile, encryptModuleFileUpdatedContent, Charsets.UTF8);

        long lastModified4 = encryptModuleFile.lastModified();

        assertTrue(lastModified4 > lastModified3);

        // trigger the reload
        // cipher = getCipher(protocolEngineFactory);

        assertFalse(cipher.isResponseEncryptionMandatory());

        encryptModuleFileUpdatedContent =
                FileUtils.readFileToString(getWorkingCopy("EncryptModule_DOM-test_true"), Charsets.UTF8);

        Thread.sleep(1000);
        FileUtils.writeStringToFile(encryptModuleFile, encryptModuleFileUpdatedContent, Charsets.UTF8);

        long lastModified5 = encryptModuleFile.lastModified();

        assertTrue(lastModified5 > lastModified4);

        assertTrue(cipher.isResponseEncryptionMandatory());
    }

    /**
     * Test method for
     * {@link ProtocolEngineFactory#ProtocolEngineFactory(SingletonAccessor)}
     * when the {@link SingletonAccessor#get()} throws an {@link IOException}
     * when called in {@link ProtocolEngineFactory#createProtocolEngine(String)}
     * <p>
     * Must fail.
     */
    @Test
    public void testConstructorAccessorIOException() throws ProtocolEngineConfigurationException, IOException {
        expectedException.expect(ProtocolEngineConfigurationException.class);
        expectedException.expectMessage("samlEngine.configuration.error.message");

        final SingletonAccessor<Map<String, ProtocolEngineConfiguration>> singletonAccessor = Mockito.mock(SingletonAccessor.class);
        Mockito.doThrow(new IOException()).when(singletonAccessor).get();
        new ProtocolEngineFactory(singletonAccessor);
    }

    /**
     * Test method for
     * {@link ProtocolEngineFactory#createProtocolEngine(String)}
     * when the {@link SingletonAccessor#get()} throws an {@link IOException}
     * when called in {@link ProtocolEngineFactory#createProtocolEngine(String)}
     *
     * <p>
     * Must fail.
     */
    @Test
    public void testCreateProtocolEngineAccessorIOException() throws ProtocolEngineConfigurationException, IOException {
        expectedException.expect(EIDASSAMLEngineRuntimeException.class);
        expectedException.expectCause(Is.isA(ProtocolEngineConfigurationException.class));

        final SingletonAccessor<Map<String, ProtocolEngineConfiguration>> singletonAccessor = Mockito.mock(SingletonAccessor.class);
        Mockito.when(singletonAccessor.get()).thenReturn(getConfigurationMapAccessorInstance());
        final ProtocolEngineFactory protocolEngineFactory = new ProtocolEngineFactory(singletonAccessor);
        Mockito.when(singletonAccessor.get()).thenThrow(IOException.class);

        getCipher(protocolEngineFactory);
    }

    private Map<String, ProtocolEngineConfiguration> getConfigurationMapAccessorInstance() throws IOException {
        return new ProtocolEngineConfigurationFactory(
                "SamlEngine_DOM-test_workingCopy.xml",
                "",
                null
        ).getConfigurationMapAccessor().get();
    }
}
