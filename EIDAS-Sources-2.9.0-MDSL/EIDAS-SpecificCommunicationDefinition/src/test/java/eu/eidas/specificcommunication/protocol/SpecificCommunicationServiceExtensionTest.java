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

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightToken;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.LightTokenEncoder;
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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Tests for the implementation of the {@link SpecificCommunicationServiceExtension}.
 */
public class SpecificCommunicationServiceExtensionTest {

    SpecificCommunicationServiceExtension specificCommunicationServiceExtension;

    private String LIGHT_TOKEN_REQUEST_ISSUER_NAME = "Issuer";
    private String LIGHT_TOKEN_REQUEST_SECRET = "Secret";
    private String LIGHT_TOKEN_REQUEST_ALGORITHM = "SHA-512";

    private String LIGHT_TOKEN_RESPONSE_ISSUER_NAME = "Issuer";
    private String LIGHT_TOKEN_RESPONSE_SECRET = "Secret";
    private String LIGHT_RESPONSE_ALGORITHM = "SHA-256";


    private CommunicationCache requestCache;
    private CommunicationCache responseCache;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        requestCache = HelperUtil.createHashMapCommunicationCacheMock();
        responseCache = HelperUtil.createHashMapCommunicationCacheMock();
        specificCommunicationServiceExtension = new SpecificCommunicationAdapterService(
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
     * {@link SpecificCommunicationServiceExtension#putRequest(String, ILightRequest)}
     * When a {@link ILightRequest} is passed to the service, the cache contains this request.
     * <p>
     * Must succeed.
     */
    @Test
    public void putRequest() throws SpecificCommunicationException, NoSuchAlgorithmException {
        final ILightRequest originalLightRequest = LightRequestTestHelper.createDefaultLightRequest();
        String tokenId ="mytoken";
        String extensionRequestToken = extensionRequestToken(tokenId);
        specificCommunicationServiceExtension.putRequest(extensionRequestToken, originalLightRequest);

        Assert.assertNotNull(requestCache.get(tokenId));
        final String cachedRequest = requestCache.get(tokenId);
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getId()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getRelayState()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getLevelsOfAssurance().get(0).getValue()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getIssuer()));
        MatcherAssert.assertThat(cachedRequest, Matchers.containsString(originalLightRequest.getCitizenCountryCode()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationServiceExtension#putResponse(String, ILightResponse)}
     * When a {@link ILightResponse} is passed to the service, the cache contains this request.
     * <p>
     * Must succeed.
     */
    @Test
    public void putResponse() throws SpecificCommunicationException, NoSuchAlgorithmException {
        final ILightResponse originalLightResponse = LightResponseTestHelper.createDefaultLightResponse();
        String internalCacheKey ="myInternalCacheKey";
        String browserLightToken = extensionResponseToken(internalCacheKey);
        specificCommunicationServiceExtension.putResponse(browserLightToken, originalLightResponse);

        Assert.assertNotNull(responseCache.get(internalCacheKey));
        final String cachedResponse = responseCache.get(internalCacheKey);
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getId()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getSubject()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getSubjectNameIdFormat()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getIssuer()));
        MatcherAssert.assertThat(cachedResponse, Matchers.containsString(originalLightResponse.getStatus().getStatusCode()));
    }

    /**
     * Test method for
     * {@link SpecificCommunicationServiceExtension#putResponse(String, ILightResponse)}
     * When a {@link ILightResponse} is passed to the service and the token is constructed with other parameters.
     * <p>
     * Must fail.
     */
    @Test
    public void putResponseWrongDigest() throws SpecificCommunicationException, NoSuchAlgorithmException {
        expectedException.expect(SecurityEIDASException.class);
        expectedException.expectMessage("LightToken digest failure");

        final ILightResponse originalLightResponse = LightResponseTestHelper.createDefaultLightResponse();
        String internalCacheKey ="myInternalCacheKey";
        String browserLightToken = extensionRequestToken(internalCacheKey);
        specificCommunicationServiceExtension.putResponse(browserLightToken, originalLightResponse);
    }

    /**
     * Test method for
     * {@link SpecificCommunicationServiceExtension}
     *
     * <p>
     * Must succeed.
     */
    @Test
    public void equalKeys() throws SpecificCommunicationException, NoSuchAlgorithmException {
        // Specific Communication Interface, returns BinaryLightToken
        final BinaryLightToken binaryLightTokenService = BinaryLightTokenHelper.createBinaryLightToken(
                LIGHT_TOKEN_RESPONSE_ISSUER_NAME,
                LIGHT_TOKEN_RESPONSE_SECRET,
                LIGHT_RESPONSE_ALGORITHM
        );
        final String internalCacheKeyLightTokenId = binaryLightTokenService.getToken().getId();

        // BinaryLightToken gets encoded to LightToken for use in browser
        final String externalBrowserLightToken = EidasStringUtil.encodeToBase64(LightTokenEncoder.encode(
                binaryLightTokenService.getToken(),
                LIGHT_TOKEN_RESPONSE_SECRET,
                LIGHT_RESPONSE_ALGORITHM)
                .getTokenBytes());

        // Specific Communication Extension Interface, consumes externalBrowserLightToken
        final String extensionLightTokenId = BinaryLightTokenHelper.getBinaryLightTokenId(
                externalBrowserLightToken,
                LIGHT_TOKEN_RESPONSE_SECRET,
                LIGHT_RESPONSE_ALGORITHM
        );
        Assert.assertEquals(internalCacheKeyLightTokenId, extensionLightTokenId);
    }

    private String extensionRequestToken(String seed) throws SpecificCommunicationException, NoSuchAlgorithmException {
        return extensionToken(
                LIGHT_TOKEN_REQUEST_ISSUER_NAME,
                LIGHT_TOKEN_REQUEST_SECRET,
                LIGHT_TOKEN_REQUEST_ALGORITHM,
                seed
        );
    }

    private String extensionResponseToken(String seed) throws SpecificCommunicationException, NoSuchAlgorithmException {
        return extensionToken(
                LIGHT_TOKEN_RESPONSE_ISSUER_NAME,
                LIGHT_TOKEN_RESPONSE_SECRET,
                LIGHT_RESPONSE_ALGORITHM,
                seed
        );
    }
    private String extensionToken(String issuer, String secret, String algorithm, String seed) throws SpecificCommunicationException, NoSuchAlgorithmException {
        final LightToken lightToken = new LightToken.Builder().id(seed)
                .issuer(issuer)
                .createdOn(ZonedDateTime.now().truncatedTo(ChronoUnit.MILLIS))
                .build();

        return EidasStringUtil.encodeToBase64(LightTokenEncoder.encode(lightToken, secret, algorithm).getTokenBytes());
    }
}