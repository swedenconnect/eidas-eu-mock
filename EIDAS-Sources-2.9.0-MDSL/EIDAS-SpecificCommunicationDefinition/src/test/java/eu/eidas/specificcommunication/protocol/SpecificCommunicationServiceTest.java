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

package eu.eidas.specificcommunication.protocol;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.RequestSpecificCommunicationServiceImpl;
import eu.eidas.specificcommunication.protocol.impl.ResponseSpecificCommunicationServiceImpl;
import eu.eidas.specificcommunication.protocol.impl.SpecificCommunicationAdapterService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.util.Collection;
import java.util.SortedSet;

/**
 * Tests for the implementation of the {@link SpecificCommunicationService}.
 */
public class SpecificCommunicationServiceTest {

    SpecificCommunicationService specificCommunicationService;

    private String LIGHT_TOKEN_REQUEST_ISSUER_NAME = "Issuer";
    private String LIGHT_TOKEN_REQUEST_SECRET = "Secret";
    private String LIGHT_TOKEN_REQUEST_ALGORITHM = "SHA-512";

    private String LIGHT_TOKEN_RESPONSE_ISSUER_NAME = "Issuer";
    private String LIGHT_TOKEN_RESPONSE_SECRET = "Secret";
    private String LIGHT_RESPONSE_ALGORITHM = "SHA-256";

    private CommunicationCache requestCache;
    private CommunicationCache responseCache;

    private static final SortedSet<AttributeDefinition<?>> REGISTRY = EidasSpec.REGISTRY.getAttributes();


    @Before
    public void setUp() throws Exception {
        requestCache = HelperUtil.createHashMapCommunicationCacheMock();
        responseCache = HelperUtil.createHashMapCommunicationCacheMock();
        specificCommunicationService = new SpecificCommunicationAdapterService(
                new RequestSpecificCommunicationServiceImpl(
                        LIGHT_TOKEN_REQUEST_ISSUER_NAME,
                        LIGHT_TOKEN_REQUEST_SECRET,
                        LIGHT_TOKEN_REQUEST_ALGORITHM,
                        requestCache
                ),
                new ResponseSpecificCommunicationServiceImpl(
                        LIGHT_TOKEN_RESPONSE_ISSUER_NAME,
                        LIGHT_TOKEN_RESPONSE_SECRET,
                        LIGHT_RESPONSE_ALGORITHM,
                        responseCache
                )
        );
    }

    /**
     * Test method for
     * {@link SpecificCommunicationService#putRequest(ILightRequest)}
     * When a {@link ILightRequest} is passed to the service, the cache contains this request.
     * <p>
     * Must succeed.
     */
    @Test
    public void putRequest() throws SpecificCommunicationException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();

        final BinaryLightToken binaryLightToken = specificCommunicationService.putRequest(originalLightRequest);

        final String cachedRequest = requestCache.get(binaryLightToken.getToken().getId());
        Assert.assertNotNull(cachedRequest);
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getId()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getRelayState()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getLevelsOfAssurance().get(0).getValue()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getIssuer()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getCitizenCountryCode()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationService#getAndRemoveRequest(String, Collection)}
     * When the {@link ILightRequest} is get and removed from the service, it is returned.
     * When the {@link ILightRequest} is get and removed from the service, the cache no longer contains this request.
     * <p>
     * Must succeed.
     */
    @Test
    public void getAndRemoveRequest() throws SpecificCommunicationException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();
        final BinaryLightToken binaryLightToken = specificCommunicationService.putRequest(originalLightRequest);
        Assert.assertNotNull(requestCache.get(binaryLightToken.getToken().getId()));
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final ILightRequest cachedLightRequest = specificCommunicationService.getAndRemoveRequest(token, REGISTRY);

        Assert.assertEquals(originalLightRequest, cachedLightRequest);
        Assert.assertNull(requestCache.get(binaryLightToken.getToken().getId()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationService#putResponse(ILightResponse)}
     * When a {@link ILightResponse} is passed to the service, the cache contains this response.
     * <p>
     * Must succeed.
     */
    @Test
    public void putResponse() throws SpecificCommunicationException {
        final ILightResponse originalLightRequest = LightResponseTestHelper.createDefaultLightResponse();

        final BinaryLightToken binaryLightToken = specificCommunicationService.putResponse(originalLightRequest);

        Assert.assertNotNull(responseCache.get(binaryLightToken.getToken().getId()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationService#getAndRemoveResponse(String, Collection)}
     * When the {@link ILightResponse} is get and removed from the service, it is returned.
     * When the {@link ILightResponse} is get and removed from the service, the cache no longer contains this request.
     * <p>
     * Must succeed.
     */
    @Test
    public void getAndRemoveResponse() throws SpecificCommunicationException, JAXBException {
        final ILightResponse originalLightRequest = LightResponseTestHelper.createDefaultLightResponse();
        final BinaryLightToken binaryLightToken = specificCommunicationService.putResponse(originalLightRequest);
        Assert.assertNotNull(responseCache.get(binaryLightToken.getToken().getId()));
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final ILightResponse cachedLightResponse = specificCommunicationService.getAndRemoveResponse(token, REGISTRY);

        Assert.assertEquals(originalLightRequest, cachedLightResponse);
        Assert.assertNull(responseCache.get(binaryLightToken.getToken().getId()));
    }
}