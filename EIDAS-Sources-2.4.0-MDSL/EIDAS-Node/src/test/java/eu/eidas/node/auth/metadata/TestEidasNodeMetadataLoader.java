/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.node.auth.metadata;

import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.util.tests.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestEidasNodeMetadataLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEidasNodeMetadataLoader.class.getName());

    private static final String FILEREPO_DIR_READ="src/test/resources/EntityDescriptors1/";
    private static final String FILEREPO_DIR_WRITE="target/test/EntityDescriptors/";
    private static final String FILEREPO_DIR_WRITE_EMPTY="target/test/EntityDescriptorsEmpty/";
    private static final String ENTITY_ID="http://EidasNode:8888/EidasNode/ConnectorMetadata";
    private static final String FILEREPO_SIGNATURE="src/test/resources/SignatureCheck/";
    private static final String CONNECTOR_ENTITY_ID =ENTITY_ID;

    @Before
    public void setUp() throws IOException {
        File sampleNodeRepo = new File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        Files.createDirectories(Paths.get(FILEREPO_DIR_WRITE));
        FileUtils.copyFolder(Paths.get(FILEREPO_DIR_READ), Paths.get(FILEREPO_DIR_WRITE));
        new File(FILEREPO_DIR_WRITE_EMPTY).mkdirs();

        OpenSamlHelper.initialize();
    }

    @After
    public void removeDir(){
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE_EMPTY));
    }

    @Test(expected = EIDASMetadataProviderException.class)
    public void testgetEntityDescriptors() throws EIDASSAMLEngineException, EIDASMetadataException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath(FILEREPO_DIR_WRITE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.setCache(new SimpleMetadataCaching(86400));
        fetcher.initProcessor();
        fetcher.getEidasMetadata(ENTITY_ID, null, null);
    }

    @Test
    public void testValidatesignature() throws EIDASMetadataException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        fetcher.setCache(new SimpleMetadataCaching(86400));
        loader.setRepositoryPath(FILEREPO_SIGNATURE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.initProcessor();

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
        MetadataSignerI metadataSigner = (MetadataSignerI) engine.getSigner();

        MetadataClockI mockMetadataClockI = mock(MetadataClockI.class);
            // one second less that the validity 2019-05-23T15:28:05.965Z at test/resources/ed.xml
        DateTime dateTime = new DateTime(2019, 5, 23, 15, 28, 04, DateTimeZone.UTC);
        when(mockMetadataClockI.getCurrentTime()).thenReturn(dateTime);

        fetcher.getEidasMetadata(CONNECTOR_ENTITY_ID, metadataSigner, mockMetadataClockI);
    }
}
