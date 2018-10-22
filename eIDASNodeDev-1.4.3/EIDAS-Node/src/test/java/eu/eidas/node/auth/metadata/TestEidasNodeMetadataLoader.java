/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.auth.metadata;

import java.io.File;

import eu.eidas.auth.engine.metadata.MetadataLoaderPlugin;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.xml.ConfigurationException;
import org.springframework.util.FileSystemUtils;

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.xml.opensaml.SAMLBootstrap;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.auth.util.tests.FileUtils;


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
        try {
            SAMLBootstrap.bootstrap();
        }catch (ConfigurationException ce){
            Assert.assertTrue("opensaml configuration exception", false);
        }
    }
    @After
    public void removeDir(){
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE));
        FileSystemUtils.deleteRecursively(new File(FILEREPO_DIR_WRITE_EMPTY));
    }

    @Test(expected = EIDASMetadataProviderException.class)
    public void testgetEntityDescriptors() throws EIDASSAMLEngineException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath(FILEREPO_DIR_WRITE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.setCache(new SimpleMetadataCaching(86400));
        fetcher.initProcessor();
        fetcher.getEntityDescriptor(ENTITY_ID, null);
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
            fetcher.getEntityDescriptor(CONNECTOR_ENTITY_ID, metadataSigner);
        } catch (EIDASSAMLEngineException e) {
            Assert.fail("got error checking the signature: "+ e);
            e.printStackTrace();
        }
    }
}
