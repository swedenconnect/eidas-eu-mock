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
 * limitations under the Licence.
 */

package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.Matchers.containsString;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:whitelist-context.xml"})
public class XmlConfigurationMetadataFetcherTest {

    @Mock
    MetadataSignerI mockMetadataSigner;

    @Autowired
    public MetadataFetcherI whitelistMetadataFetcher;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Test for {@link MetadataFetcherI#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * when URL not is not allowed  see {@link BaseMetadataFetcher#isAllowedMetadataUrl(String)}
     *
     * @throws EIDASMetadataException when metadata could not be retrieved.
     *
     * Must fail.
     */
    @Test
    public void testURLNotAllowed() throws EIDASMetadataException {
        thrown.expect(EIDASMetadataProviderException.class);
        thrown.expectMessage(containsString("Metadata URL is not secure"));

        whitelistMetadataFetcher.getEidasMetadata("notWhitelistedUrl", mockMetadataSigner, null);
    }

    /**
     * Test for {@link MetadataFetcherI#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * when URL not in whitelist     *
     *
     * @throws EIDASMetadataException when metadata could not be retrieved.
     *
     * Must fail.
     */
    @Test
    public void testURLNotWhitelisted() throws EIDASMetadataException {
        thrown.expect(EIDASMetadataProviderException.class);
        thrown.expectMessage(containsString("Metadata URL is not whitelisted"));

        whitelistMetadataFetcher.getEidasMetadata("http://not.whitlist.url/metadata", mockMetadataSigner, null);
    }

}
