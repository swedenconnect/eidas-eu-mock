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
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.RequestSpecificCommunicationServiceImpl;
import eu.eidas.specificcommunication.protocol.impl.ResponseSpecificCommunicationServiceImpl;
import eu.eidas.specificcommunication.protocol.impl.SpecificCommunicationAdapterService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.SortedSet;

/**
 * Tests for the implementation of the {@link SpecificCommunicationLoggingService}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:specificCommunicationDefinitionLoggerApplicationContext.xml"})
public class SpecificCommunicationLoggingServiceTest {

    SpecificCommunicationLoggingService specificCommunicationLoggingService;

    private String LIGHT_TOKEN_REQUEST_ISSUER_NAME= "issuer as part of token";
    private String LIGHT_TOKEN_RESPONSE_ISSUER_NAME= "issuer as part of token";
    @Value("${lightToken.connector.request.secret}")
    private String LIGHT_TOKEN_REQUEST_SECRET;
    @Value("${lightToken.connector.request.algorithm}")
    private String LIGHT_TOKEN_REQUEST_ALGORITHM;
    @Value("${lightToken.connector.response.secret}")
    private String LIGHT_TOKEN_RESPONSE_SECRET;
    @Value("${lightToken.connector.response.algorithm}")
    private String LIGHT_TOKEN_RESPONSE_ALGORITHM;

    private static final SortedSet<AttributeDefinition<?>> REGISTRY = EidasSpec.REGISTRY.getAttributes();

    @Before
    public void setUp() throws Exception {
        specificCommunicationLoggingService = new SpecificCommunicationAdapterService(
                new RequestSpecificCommunicationServiceImpl(
                        LIGHT_TOKEN_REQUEST_ISSUER_NAME,
                        LIGHT_TOKEN_REQUEST_SECRET,
                        LIGHT_TOKEN_REQUEST_ALGORITHM,
                        HelperUtil.createHashMapCommunicationCacheMock()
                ),
                new ResponseSpecificCommunicationServiceImpl(
                        LIGHT_TOKEN_RESPONSE_ISSUER_NAME,
                        LIGHT_TOKEN_RESPONSE_SECRET,
                        LIGHT_TOKEN_RESPONSE_ALGORITHM,
                        HelperUtil.createHashMapCommunicationCacheMock()
                )
        );
    }

    /**
     * Test method for
     * {@link SpecificCommunicationLoggingService#getRequest(String, Collection)}
     * Returns an object of {@link ILightRequest}
     * When a token and {@link EidasSpec#REGISTRY} attributes are provided.
     * <p>
     * Must succeed.
     */
    @Test
    public void getRequest() throws SpecificCommunicationException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();
        BinaryLightToken binaryLightToken = ((SpecificCommunicationAdapterService) specificCommunicationLoggingService)
                .putRequest(originalLightRequest);
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final ILightRequest cachedRequest = specificCommunicationLoggingService.getRequest(token, REGISTRY);

        Assert.assertEquals(originalLightRequest, cachedRequest);
    }

    /**
     * Test method for
     * {@link SpecificCommunicationLoggingService#getRequest(String)}
     * Must return a LightRequest {@link String}
     * When a token is provided.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetRequest() throws SpecificCommunicationException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();
        BinaryLightToken binaryLightToken = ((SpecificCommunicationAdapterService) specificCommunicationLoggingService)
                .putRequest(originalLightRequest);
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final String cachedRequest = specificCommunicationLoggingService.getRequest(token);
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getId()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getRelayState()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getLevelsOfAssurance().get(0).getValue()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getIssuer()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getCitizenCountryCode()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationLoggingService#getResponse(String, Collection)}
     * Returns an object of {@link ILightResponse}
     * When a token and {@link EidasSpec#REGISTRY} attributes are provided
     * <p>
     * Must succeed.
     */
    @Test
    public void getResponse() throws SpecificCommunicationException {
        final ILightResponse originalLightResponse = LightResponseTestHelper.createDefaultLightResponse();
        BinaryLightToken binaryLightToken = ((SpecificCommunicationAdapterService) specificCommunicationLoggingService)
                .putResponse(originalLightResponse);
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final ILightResponse cachedRequest = specificCommunicationLoggingService.getResponse(token, REGISTRY);

        Assert.assertEquals(originalLightResponse, cachedRequest);
    }

    /**
     * Test method for
     * {@link SpecificCommunicationLoggingService#getResponse(String)}
     * Must return a LightResponse {@link String}
     * When a token is provided.
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponse() throws SpecificCommunicationException {
        final ILightResponse originalLightResponse = LightResponseTestHelper.createDefaultLightResponse();
        BinaryLightToken binaryLightToken = ((SpecificCommunicationAdapterService) specificCommunicationLoggingService)
                .putResponse(originalLightResponse);
        final String token = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);

        final String cachedResponse = specificCommunicationLoggingService.getResponse(token);

        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getId()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getSubject()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getSubjectNameIdFormat()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getIssuer()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getStatus().getStatusCode()));
    }
}