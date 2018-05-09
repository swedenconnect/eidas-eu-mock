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

package eu.eidas.specificcommunication.protocol;

import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.specificcommunication.BinaryLightTokenHelper;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceImpl;

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for {@link SpecificProxyserviceCommunicationServiceImpl}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:specificCommunicationDefinitionApplicationContext.xml")
public class SpecificProxyserviceCommunicationServiceImplTest {

    private static ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/specificCommunicationDefinitionApplicationContext.xml");

    private String SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE_BEAN_NAME = "springManagedSpecificProxyserviceCommunicationService";

    private final String VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64 =
            "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvblByb3h5c2VydmljZVJlcXVlc3R8NmE4NDlkZDAtYjkyZS00MWVkLTk3OGYtYTUyMzY4Y2IzNDVlfDIwMTctMTItMTIgMDg6Mzg6MDIgMTIyfGRKZ1N3eGFLZGNQUXhFaXdhaGQ2WDliV1Y0MDgxeGVjTkxlQWgzcENPbk09";

    private final String VALID_BINARY_LIGHT_TOKEN_RESPONSE_BASE64 =
            "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvblByb3h5c2VydmljZVJlc3BvbnNlfDgyOGQ1YmU1LTczMzUtNGZhMi1hNjYxLWJmZDhjZTgxNWRkOXwyMDE3LTEyLTEyIDA4OjM4OjM3IDM3NXw2K0VvbTd3RDJKL0Y2MDJNVHdZdC82ejZmTVpLdDRFNG9oVlFrL3FFMlhVPQ==";

    private final String INVALID_BINARY_LIGHT_TOKEN = "notAToken";

    private SpecificProxyserviceCommunicationServiceImpl specificProxyserviceCommunicationService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void initialize() {
        specificProxyserviceCommunicationService = (SpecificProxyserviceCommunicationServiceImpl) applicationContext.getBean(SPECIFIC_PROXYSERVICE_COMMUNICATION_SERVICE_BEAN_NAME);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#putRequest(ILightRequest)}
     * <p/>
     * Must succeed.
     */
    @Test
    public void testPutRequestInCommunicationCache() throws SpecificCommunicationException {
        final ILightRequest iLightRequestIn = LightRequestTestHelper.createDefaultLightRequest();
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putRequest(iLightRequestIn);
        Assert.assertNotNull(binaryLightToken);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#putRequest(ILightRequest)}
     * when parameter is null.
     * <p/>
     * Must fail.
     */
    @Test
    public void testPutRequestInCommunicationCacheNullILightRequest() throws Exception {
        thrown.expect(NullPointerException.class);

        final ILightRequest iLightRequestIn = null;
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putRequest(iLightRequestIn);
        Assert.assertNotNull(binaryLightToken);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveRequest(String)}
     * to remove the {@link ILightRequest} put before by a successful call to
     * {@link SpecificProxyserviceCommunicationServiceImpl#putRequest(ILightRequest)}.
     * <p/>
     * Must succeed.
     */
    @Test
    public void testRemoveRequestFromCommunicationCache() throws Exception {
        final ILightRequest iLightRequestIn = LightRequestTestHelper.createDefaultLightRequest();
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putRequest(iLightRequestIn);
        final String tokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        final ILightRequest iLightRequestOut = getSpecificCommunicationService().getAndRemoveRequest(tokenBase64,iLightRequestIn.getRequestedAttributes().getDefinitions());
        assertNotNull(iLightRequestOut);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveRequest(String)}
     * using a valid {@link BinaryLightToken} but which does not exist as key in the cache.
     * <p/>
     * Must succeed.
     */
    @Test
    public void testRemoveRequestFromCommunicationCacheValidTokenEmptyCache() throws Exception {
        final String tokenBase64 = VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64;
        final ILightRequest iLightRequest = getSpecificCommunicationService().getAndRemoveRequest(tokenBase64,null);
        Assert.assertNull(iLightRequest);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveRequest(String)}
     * to remove the {@link ILightRequest} using a valid {@link BinaryLightToken} in Base64
     * created with an incorrect secret, algorithm e.g. the ones used for a {@link ILightResponse}
     * instead of the correct ones used to validate the {@link ILightRequest}.
     * <p/>
     * Must fail.
     */
    @Test
    public void testRemoveRequestFromCommunicationCacheValidResponseToken() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken digest failure");

        final String tokenBase64 = VALID_BINARY_LIGHT_TOKEN_RESPONSE_BASE64;
        final ILightRequest iLightRequest = getSpecificCommunicationService().getAndRemoveRequest(tokenBase64,null);
        Assert.assertNull(iLightRequest);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveRequest(String)}
     * using an invalid {@link BinaryLightToken} in Base64.
     * <p/>
     * Must fail.
     */
    @Test
    public void testRemoveRequestFromCommunicationCacheInvalidToken() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken parse error");

        final String tokenBase64 = INVALID_BINARY_LIGHT_TOKEN;
        getSpecificCommunicationService().getAndRemoveRequest(tokenBase64,null);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#putResponse(ILightResponse)}
     * <p/>
     * Must succeed.
     */
    @Test
    public void testPutResponseInCommunicationCache() throws Exception {
        final ILightResponse iLightResponseIn = LightResponseTestHelper.createDefaultLightResponse();
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putResponse(iLightResponseIn);
        Assert.assertNotNull(binaryLightToken);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#putResponse(ILightResponse)}
     * when ILightResponse parameter is null.
     * <p/>
     * Must fail.
     */
    @Test
    public void testPutResponseInCommunicationCacheNullILightResponse() throws Exception {
        thrown.expect(NullPointerException.class);

        final ILightResponse iLightResponseIn = null;
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putResponse(iLightResponseIn);
        Assert.assertNotNull(binaryLightToken);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveResponse(String)}
     * tor remove the {@link ILightResponse} put before by a successful call to
     * {@link SpecificProxyserviceCommunicationServiceImpl#putResponse(ILightResponse)}.
     * <p/>
     * Must succeed.
     */
    @Test
    public void testRemoveResponseFromCommunicationCache() throws Exception {
        final ILightResponse iLightResponseIn = LightResponseTestHelper.createDefaultLightResponse();
        final BinaryLightToken binaryLightToken = getSpecificCommunicationService().putResponse(iLightResponseIn);
        final String tokenBase64 = BinaryLightTokenHelper.encodeBinaryLightTokenBase64(binaryLightToken);
        final ILightResponse iLightResponseOut = getSpecificCommunicationService().getAndRemoveResponse(tokenBase64,iLightResponseIn.getAttributes().getDefinitions());
        //Assert.assertSame(iLightResponseIn, iLightResponseOut);
        Assert.assertNotNull( iLightResponseOut);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveResponse(String)}
     * using a valid {@link BinaryLightToken} but which does not exist as key in the cache.
     * <p/>
     * Must succeed.
     */
    @Test
    public void testRemoveResponseFromCommunicationCacheValidTokenEmptyCache() throws Exception {
        final String tokenBase64 = VALID_BINARY_LIGHT_TOKEN_RESPONSE_BASE64;
        final ILightResponse iLightResponse = getSpecificCommunicationService().getAndRemoveResponse(tokenBase64,null);
        Assert.assertNull(iLightResponse);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveResponse(String)}
     * to remove the {@link ILightResponse} using a valid {@link BinaryLightToken} in Base64
     * created with an incorrect secret, algorithm e.g. the ones used for a {@link ILightRequest}
     * instead of the correct ones used to validate the {@link ILightResponse}.
     * <p/>
     * Must fail.
     */
    @Test
    public void testRemoveResponseFromCommunicationCacheValidRequestToken() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken digest failure");

        final String tokenBase64 = VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64;
        final ILightResponse iLightResponse = getSpecificCommunicationService().getAndRemoveResponse(tokenBase64,null);
        Assert.assertNull(iLightResponse);
    }

    /**
     * Test method for
     * {@link SpecificProxyserviceCommunicationServiceImpl#getAndRemoveResponse(String)}
     * using an invalid {@link BinaryLightToken} in Base64.
     * <p/>
     * Must fail.
     */
    @Test
    public void testRemoveResponseFromCommunicationCacheInvalidToken() throws Exception {
        thrown.expect(SecurityEIDASException.class);
        thrown.expectMessage("LightToken parse error");

        final String tokenBase64 = INVALID_BINARY_LIGHT_TOKEN;
        getSpecificCommunicationService().getAndRemoveResponse(tokenBase64,null);
    }

    public SpecificProxyserviceCommunicationServiceImpl getSpecificCommunicationService() {
        return specificProxyserviceCommunicationService;
    }

}