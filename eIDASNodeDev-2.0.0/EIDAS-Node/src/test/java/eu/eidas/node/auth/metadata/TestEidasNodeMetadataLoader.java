/*
 * Copyright (c) 2017 by European Commission
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
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.util.tests.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

import java.io.File;


public class TestEidasNodeMetadataLoader {
    private static final String FILEREPO_DIR_READ="src/test/resources/EntityDescriptors1/";
    private static final String FILEREPO_DIR_WRITE="target/test/EntityDescriptors/";
    private static final String FILEREPO_DIR_WRITE_EMPTY="target/test/EntityDescriptorsEmpty/";
    private static final String ENTITY_ID="http://EidasNode:8888/EidasNode/ConnectorMetadata";
    private static final String FILEREPO_SIGNATURE="src/test/resources/SignatureCheck/";
    private static final String CONNECTOR_ENTITY_ID =ENTITY_ID;
    @Before
    public void setUp(){
        File sampleNodeRepo=new File(FILEREPO_DIR_WRITE);
        FileSystemUtils.deleteRecursively(sampleNodeRepo);
        sampleNodeRepo.mkdirs();
        FileUtils.copyFile(new File(FILEREPO_DIR_READ), sampleNodeRepo);
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
    public void testValidatesignature(){
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        fetcher.setCache(new SimpleMetadataCaching(86400));
        loader.setRepositoryPath(FILEREPO_SIGNATURE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.initProcessor();
        try{
            ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine("METADATA");
            MetadataSignerI metadataSigner = (MetadataSignerI) engine.getSigner();
            MetadataClockI metadataClock = (MetadataClockI) engine.getClock();
            fetcher.getEidasMetadata(CONNECTOR_ENTITY_ID, metadataSigner, metadataClock);
        } catch (EIDASMetadataException e) {
            Assert.fail("got error checking the signature: "+ e);
            e.printStackTrace();
        }
    }
}
