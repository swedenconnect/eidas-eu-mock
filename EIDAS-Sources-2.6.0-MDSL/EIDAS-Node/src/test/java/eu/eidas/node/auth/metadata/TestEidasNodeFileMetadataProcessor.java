/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.node.auth.metadata;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.util.tests.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runners.MethodSorters;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Provider;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


@FixMethodOrder(MethodSorters.JVM)
public class TestEidasNodeFileMetadataProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEidasNodeFileMetadataProcessor.class.getName());

    private static final String FILEREPO_DIR_READ="src/test/resources/EntityDescriptors2/";
    private static final String FILEREPO_DIR_WRITE1="target/test/EntityDescriptors1/";
    private static final String FILEREPO_DIR_WRITE2="target/test/EntityDescriptors2/";
    private static final String FILE_PATH_WRITE2 ="target/test/EntityDescriptors2/test.xml";
    private static final String FILEREPO_DIR_WRITE_EMPTY="target/test/EntityDescriptorsEmpty/";
    private static final String FILEREPO_DIR_READ_UPD="src/test/resources/EntityDescriptors1/";
    private static final String FILE_PATH_READ_UPD ="src/test/resources/EntityDescriptors1/test.xml";
    private static final String FILEREPO_DIR_READ_COMBO="src/test/resources/EntityDescriptors3/";
    private static final String FILEREPO_DIR_WRITE3="target/test/EntityDescriptors3/";
    private static final String FILEREPO_DIR_READ_COMBO_4="src/test/resources/EntityDescriptors4/";
    private static final String FILEREPO_DIR_WRITE4="target/test/EntityDescriptors4/";
    private static final String FILEREPO_DIR_READ_JAVACHAR_ID="src/test/resources/EntityDescriptorsJavaCharsInID/";
    private static final String FILEREPO_DIR_WRITE_JAVACHAR_ID="target/test/EntityDescriptorsJavaCharsInID/";

    private static final String ENTITY_ID_1="http://peps:8888/PEPS/SPEPSMetadata";
    private static final String ENTITY_ID_2="eumiddleware";

    private ProtocolEngineI getEngine() {
        ProtocolEngineI engine = null;
        try {
            engine = ProtocolEngineFactory.createProtocolEngine("METADATA", EidasProtocolProcessor.INSTANCE);
        } catch (EIDASSAMLEngineException exc) {
            Assert.assertTrue(false);
        }
        return engine;
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws IOException {
        LOGGER.debug("initializing directory "+FILEREPO_DIR_WRITE1);
        initWorkFolder(FILEREPO_DIR_READ, FILEREPO_DIR_WRITE1);
        LOGGER.debug("initializing directory "+FILEREPO_DIR_WRITE2);
        initWorkFolder(FILEREPO_DIR_READ, FILEREPO_DIR_WRITE2);
        Files.createDirectories(Paths.get(FILEREPO_DIR_WRITE_EMPTY));
        initWorkFolder(FILEREPO_DIR_READ_COMBO, FILEREPO_DIR_WRITE3);
        initWorkFolder(FILEREPO_DIR_READ_COMBO_4, FILEREPO_DIR_WRITE4);
        initWorkFolder(FILEREPO_DIR_READ_JAVACHAR_ID, FILEREPO_DIR_WRITE_JAVACHAR_ID);
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        OpenSamlHelper.initialize();
    }
    private static void initWorkFolder(String sourceFolder, String folderName) throws IOException {
        File sampleNodeRepo = new File(folderName);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        Files.createDirectories(Paths.get(folderName));
        FileUtils.copyFolder(Paths.get(sourceFolder), Paths.get(folderName));
    }

    @AfterClass
    public static void removeDir(){
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE1));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE2));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE3));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE4));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE_JAVACHAR_ID));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE_EMPTY));
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * Test method for
     * {@link FileMetadataLoader#getEntityDescriptors()}
     * When EntityDescriptor is empty
     * <p>
     * Must fail.
     */
    @Test
    public void testGetEntityDescriptorsEmpty() throws EIDASMetadataProviderException {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE_EMPTY);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();
        Assert.assertTrue(list == null || list.isEmpty());
    }

    /**
     * Test method for
     * {@link FileMetadataLoader#getEntityDescriptors()}
     * When EntityDescriptor contains an EntityID which contains a character that must be escaped
     * <p>
     * Must fail.
     */
    @Test
    public void testGetEntityDescriptorsJavaCharsInID() throws EIDASMetadataProviderException {
        expectedException.expect(EIDASMetadataProviderException.class);
        expectedException.expectMessage("No entity descriptor for URL");

        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE_JAVACHAR_ID);
        processor.getEntityDescriptors();
    }

    /**
     * Test method for
     * {@link FileMetadataLoader#getEntityDescriptors()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetEntityDescriptors() throws EIDASMetadataProviderException {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE1);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();
        Assert.assertTrue(list.size()==2);

        Map<String,EntityDescriptor> entityDescriptors = new HashMap<>();
        entityDescriptors.put(list.get(0).getEntityDescriptors().get(0).getEntityID(), list.get(0).getEntityDescriptors().get(0));
        entityDescriptors.put(list.get(1).getEntityDescriptors().get(0).getEntityID(), list.get(1).getEntityDescriptors().get(0));

        final EntityDescriptor ed1 = entityDescriptors.get(ENTITY_ID_1);
        Assert.assertNotNull(ed1);
        Assert.assertFalse(ed1.isValid());

        final EntityDescriptor ed2 = entityDescriptors.get(ENTITY_ID_2);
        Assert.assertNotNull(ed2);
        Assert.assertTrue(ed2.isValid());
    }

    /**
     * Test method for
     * {@link FileMetadataLoader#getEntityDescriptors()}
     * When EntityDescriptor get updated between calls
     * <p>
     * Must succeed.
     */
    @Test
    public void testUpdateEntityDescriptors() throws IOException, EIDASMetadataProviderException, InterruptedException {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE2);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();
        Assert.assertTrue(list.size()==2);
        Files.copy(Paths.get(FILE_PATH_READ_UPD), Paths.get(FILE_PATH_WRITE2), REPLACE_EXISTING);
        Thread.sleep(3000);
        list = processor.getEntityDescriptors();
        Assert.assertTrue(list.size()==3);
    }

    /**
     * Test method for
     * {@link FileMetadataLoader#getEntityDescriptors()}
     * When Signing Entity Descriptors
     * <p>
     * Must succeed.
     */
    @Test
    public void testCombo() throws Exception {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE3);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();
        Assert.assertTrue(list.size()==2);
        XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
        EntitiesDescriptor eds = (EntitiesDescriptor)builderFactory.getBuilder(EntitiesDescriptor.DEFAULT_ELEMENT_NAME).buildObject(EntitiesDescriptor.DEFAULT_ELEMENT_NAME);
        for(EntityDescriptorContainer edc:list){
        	eds.getEntityDescriptors().addAll(edc.getEntityDescriptors());
        }
        MetadataSignerI signer = (MetadataSignerI) getEngine().getSigner();
        EntitiesDescriptor entitiesDescriptor = signer.signMetadata(eds);
        String s = EidasStringUtil.toString(OpenSamlHelper.marshall(entitiesDescriptor));
        //String s=SAMLEngineUtils.serializeObject(eds);
        Assert.assertFalse(s.isEmpty());

		EntityDescriptorContainer edc = MetadataUtil.deserializeEntityDescriptor(s);

        signer.validateMetadataSignature(edc.getEntitiesDescriptor());
    }


    /**
     * Test method for {@link FileMetadataLoader#getEntityDescriptors()}.
     *
     * It should ignore all files except well-formed XML and with the .xml extension
     *
     * It must succeed and throw no exception.
     *
     * @throws Exception
     */
    @Test
    public void testComboFirstXmlFileInvalid() throws Exception {
        FileMetadataLoader processor=new FileMetadataLoader();
        processor.setRepositoryPath(FILEREPO_DIR_WRITE4);
        List<EntityDescriptorContainer> list = processor.getEntityDescriptors();

        Assert.assertTrue("Check if only one file is XML well-formed and has .xml extension.",list.size()==1);

        final String entityIDExpectedWellFormedXmlExtensionFile = "http://localhost:8080/EidasNode/ServiceMetadata";
        final String entityID = list.get(0).getEntityDescriptors().get(0).getEntityID();
        final boolean isCorrectEntityId= StringUtils.equals(entityID, entityIDExpectedWellFormedXmlExtensionFile);

        Assert.assertTrue("check if entityID matches the one of XML well-formed and with .xml extension file.",isCorrectEntityId);
    }


}
