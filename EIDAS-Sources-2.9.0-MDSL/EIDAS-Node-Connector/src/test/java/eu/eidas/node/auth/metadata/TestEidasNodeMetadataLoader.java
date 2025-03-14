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

package eu.eidas.node.auth.metadata;

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.cache.metadata.SimpleMetadataCaching;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.FileMetadataLoader;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

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
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
    }

    @Test(expected = EIDASMetadataProviderException.class)
    public void testgetEntityDescriptors() throws EIDASMetadataException {
        CachingMetadataFetcher fetcher = new CachingMetadataFetcher();
        FileMetadataLoader loader = new FileMetadataLoader();
        loader.setRepositoryPath(FILEREPO_DIR_READ);
        fetcher.setMetadataLoaderPlugin(loader);
        fetcher.setCache(new eu.eidas.auth.cache.metadata.SimpleMetadataCaching(86400));
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

        ProtocolEngineI engine = ProtocolEngineFactory.getDefaultProtocolEngine(SAML_ENGINE_METADATA_INSTANCE);
        MetadataSignerI metadataSigner = (MetadataSignerI) engine.getSigner();

        MetadataClockI mockMetadataClockI = mock(MetadataClockI.class);
            // one second less that the validity 2019-05-23T15:28:05.965Z at test/resources/ed.xml
        ZonedDateTime dateTime = ZonedDateTime.of(2019, 5, 23, 15, 28, 4, 0, ZoneOffset.UTC);
        when(mockMetadataClockI.getCurrentTime()).thenReturn(dateTime);

        fetcher.getEidasMetadata(PROXY_SERVICE_ENTITY_ID, metadataSigner, mockMetadataClockI);
    }
}
