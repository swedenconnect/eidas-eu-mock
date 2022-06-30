/*
 * Copyright (c) 2018 by European Commission
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

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.SamlEngineSystemClock;
import eu.eidas.auth.engine.configuration.dom.ReloadableProtocolConfigurationInvocationHandler;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.node.auth.util.tests.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests for testing the behaviour of the static metadata loading without validation of metadata signature enabled.
 */
public class TestEidasNodeFileMetadataProcessorTrustChain {
    private static final String FILEREPO_DIR_READ_COMBO="src/test/resources/EntityDescriptors6/";
    private static final String FILEREPO_DIR_WRITE ="target/test/EntityDescriptors6/";

    private static final String ENTITY_ID_1="http://domain:8080/EidasNode/ConnectorMetadataIntermediateCaMetadataRootCaMetadata";
    private static final String ENTITY_ID_2="http://domain:8080/EidasNode/ConnectorMetadataMetadataNodeRootCaMetadata";

    private static FileMetadataLoader fileMetadataLoader;

    @BeforeClass
    public static void setUp() throws IOException {
        initWorkFolder(FILEREPO_DIR_READ_COMBO, FILEREPO_DIR_WRITE);
        OpenSamlHelper.initialize();
        initFileMetadataLoader();
    }

    @AfterClass
    public static void removeDir(){
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE));
    }

    /**
     * Test method for
     * {@link CachingMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * when validUntil is valid
     * when signature is invalid
     * when validation of metadata signature is disabled.
     * <p/>
     * Must succeed.
     *
     * @throws EIDASMetadataException
     */
    @Test
    public void testGetEidasMetadataWithoutSignatureValidation() throws EIDASMetadataException {
        assertIsValidUntil();

        ProtocolEngineI protocolEngine = getEngine("METADATA_EMPTY_TRUST");
        MetadataSignerI checker = (MetadataSignerI) protocolEngine.getSigner();

        InvocationHandler invocationHandler = Proxy.getInvocationHandler(protocolEngine.getClock());
        ReloadableProtocolConfigurationInvocationHandler ih = (ReloadableProtocolConfigurationInvocationHandler) invocationHandler;
        SamlEngineSystemClock clock = (SamlEngineSystemClock) ih.getProxiedObject();

        CachingMetadataFetcher cachingMetadataFetcher = getCachingMetadataFetcher();
        //disable metadata signature validation
        cachingMetadataFetcher.setValidateEidasMetadataSignature(false);

        cachingMetadataFetcher.getEidasMetadata(ENTITY_ID_1, checker , clock);
        cachingMetadataFetcher.getEidasMetadata(ENTITY_ID_2, checker , clock);
    }

    private void assertIsValidUntil() throws EIDASMetadataProviderException {
        for (EntityDescriptorContainer entityDescriptorContainer : fileMetadataLoader.getEntityDescriptors()) {
            Assert.assertTrue(entityDescriptorContainer.getEntityDescriptors().get(0).isValid());
        }
    }

    /**
     * Creates and configures and instance of {@link CachingMetadataFetcher}.
     *
     * @return the configured instance of {@link CachingMetadataFetcher}
     */
    private CachingMetadataFetcher getCachingMetadataFetcher() {
        CachingMetadataFetcher cachingMetadataFetcher = new CachingMetadataFetcher();
        cachingMetadataFetcher.setMetadataLoaderPlugin(fileMetadataLoader);
        cachingMetadataFetcher.setCache(new SimpleMetadataCaching(86400));
        cachingMetadataFetcher.initProcessor();
        return cachingMetadataFetcher;
    }

    /**
     * Creates the protocol engine related to the name instance passed as parameter.
     *
     * @param nameInstance the name instance of the protocol engine to be created.
     * @return the Protocol Engine instance
     */
    private static ProtocolEngineI getEngine(final String nameInstance) {
        return ProtocolEngineFactory.getDefaultProtocolEngine(nameInstance);
    }

    private static void initFileMetadataLoader() {
        fileMetadataLoader = new FileMetadataLoader();
        fileMetadataLoader.setRepositoryPath(FILEREPO_DIR_WRITE);
    }


    private static void initWorkFolder(String sourceFolder, String folderName) throws IOException {
        File sampleNodeRepo = new File(folderName);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        Files.createDirectories(Paths.get(folderName));
        FileUtils.copyFolder(Paths.get(sourceFolder), Paths.get(folderName));
    }

}
