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

package eu.eidas.auth.engine.metadata.impl;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class AbstractCachingMetadataFetcherTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Test method for
     * {@link AbstractCachingMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * when the cached metadata is expired
     * and new metadata from BaseMetadataFetcher is valid
     * <p>
     * Must succeed.
     */
    @Test
    public void getEidasMetadataFetchNewMetadata() throws EIDASMetadataException {
        final MetadataSignerI mockMetadataSigner = mock(MetadataSignerI.class);
        doReturn(mock(SignableXMLObject.class)).when(mockMetadataSigner).validateMetadataSignature(any(EntityDescriptor.class));
        final MetadataClockI mockMetadataClock = mock(MetadataClockI.class);
        final AbstractCachingMetadataFetcher spyCachingMetadataFetcher = spy(AbstractCachingMetadataFetcher.class);
        doReturn(mock(EidasMetadataParametersI.class)).when(spyCachingMetadataFetcher).getFromCache(anyString(), any());
        doReturn(mock(EntityDescriptor.class)).when(spyCachingMetadataFetcher).fetchEntityDescriptor(anyString());
        final Queue<Boolean> isValidUntilNowBehaviourQueue = new ArrayDeque<>(Arrays.asList(
                false, // when cached metadataParameters have expired
                true   // when new metadataParameters have been fetched by BaseMetadataFetcher
        ));
        doAnswer(invocationOnMock -> isValidUntilNowBehaviourQueue.remove())
                .when(spyCachingMetadataFetcher).isValidUntilNow(any(), any());

        spyCachingMetadataFetcher.getEidasMetadata("https://url.be", mockMetadataSigner, mockMetadataClock);
    }

    /**
     * Test method for
     * {@link AbstractCachingMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * when the cached metadata has expired
     * and new metadata from BaseMetadataFetcher has also expired
     * <p>
     * Must Fail.
     */
    @Test
    public void getEidasMetadataNoValidNewMetadata() throws EIDASMetadataException {
        exception.expect(EIDASMetadataProviderException.class);
        exception.expectMessage("No entity descriptor for URL https://url.be");

        final MetadataSignerI mockMetadataSigner = mock(MetadataSignerI.class);
        doReturn(mock(SignableXMLObject.class)).when(mockMetadataSigner).validateMetadataSignature(any(EntityDescriptor.class));
        final MetadataClockI mockMetadataClock = mock(MetadataClockI.class);
        final AbstractCachingMetadataFetcher spyCachingMetadataFetcher = spy(AbstractCachingMetadataFetcher.class);
        doReturn(false)  // when metadataParameters have expired
                .when(spyCachingMetadataFetcher).isValidUntilNow(any(), any());
        doReturn(mock(EidasMetadataParametersI.class)).when(spyCachingMetadataFetcher).getFromCache(anyString(), any());
        doReturn(mock(EntityDescriptor.class)).when(spyCachingMetadataFetcher).fetchEntityDescriptor(anyString());

        spyCachingMetadataFetcher.getEidasMetadata("https://url.be", mockMetadataSigner, mockMetadataClock);
    }
}