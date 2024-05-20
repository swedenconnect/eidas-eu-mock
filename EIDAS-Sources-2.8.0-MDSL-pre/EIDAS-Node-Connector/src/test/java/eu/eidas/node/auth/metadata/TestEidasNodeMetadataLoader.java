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

import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Provider;
import java.security.Security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestEidasNodeMetadataLoader {

    private static final String FILEREPO_DIR_READ="src/test/resources/EntityDescriptors1/";
    private static final String FILEREPO_SIGNATURE="src/test/resources/SignatureCheck/";
    private static final String CONNECTOR_ENTITY_ID = "http://EidasNode:8888/EidasNode/ConnectorMetadata";
    private static final String PROXY_SERVICE_ENTITY_ID = "http://localhost:8080/EidasNode/ServiceMetadata";
    private final String SAML_ENGINE_METADATA_INSTANCE = "METADATA";

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Test(expected = EIDASMetadataProviderException.class)
    public void testgetEntityDescriptors() throws EIDASMetadataException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath(FILEREPO_DIR_READ);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.setCache(new SimpleMetadataCaching(86400));
        fetcher.initProcessor();
        fetcher.getEidasMetadata(CONNECTOR_ENTITY_ID, null, null);
    }

    /**
     * Tests if the signature in the metadata file is valid.
     *
     * @throws EIDASMetadataException when signature is invalid
     */
    @Test
    public void testValidateSignature() throws EIDASMetadataException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        fetcher.setCache(new SimpleMetadataCaching(86400));
        loader.setRepositoryPath(FILEREPO_SIGNATURE);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.initProcessor();

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine(SAML_ENGINE_METADATA_INSTANCE);
        MetadataSignerI metadataSigner = (MetadataSignerI) engine.getSigner();

        MetadataClockI mockMetadataClockI = mock(MetadataClockI.class);
            // one second less that the validity 2019-05-23T15:28:05.965Z at test/resources/ed.xml
        DateTime dateTime = new DateTime(2019, 5, 23, 15, 28, 04, DateTimeZone.UTC);
        when(mockMetadataClockI.getCurrentTime()).thenReturn(dateTime);

        fetcher.getEidasMetadata(PROXY_SERVICE_ENTITY_ID, metadataSigner, mockMetadataClockI);
    }
}
