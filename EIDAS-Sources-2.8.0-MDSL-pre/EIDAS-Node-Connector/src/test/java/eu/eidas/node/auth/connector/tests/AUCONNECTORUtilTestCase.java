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
package eu.eidas.node.auth.connector.tests;

import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.RequestState;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUCONNECTORUtil}.
 * @version $Revision: $, $Date:$
 */
public class AUCONNECTORUtilTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTORUtilTestCase.class.getName());

    /**
     * Properties values for testing proposes.
     */
    private static Properties CONFIGS = new Properties();

    /**
     * Properties values for EIDASUtil testing proposes.
     */

    private static final String ANTIREPLAY_SAML_ID_A = "SAML_ID_A";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

  /**
   * Initialize the CONFIGS properties for each test to avoid
   * inherited configurations
   */
    @Before
    public void initialize(){
      CONFIGS = new Properties();
      CONFIGS.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(),
              TestingConstants.ONE_CONS.toString());
    }
    /**
     * Test method for {@link AUCONNECTORUtil#loadConfig(String)} . Must Succeed.
     */
    @Test
    public void testLoadConfig() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertEquals(TestingConstants.ONE_CONS.toString(),
                auconnectorutil.loadConfig(EidasParameterKeys.EIDAS_NUMBER.toString()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#loadConfig(String)} . Must return null.
     */
    @Test
    public void testLoadConfigMissing() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertNull(auconnectorutil.loadConfig(EidasParameterKeys.CITIZEN_COUNTRY_CODE.toString()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil# validateSP(Map)} .
     * Must return false.
     */
    @Test
    public void testValidateSPMissing() {

        final WebRequest mockParameters = mock(WebRequest.class);
        RequestState mockRequestState = mock(RequestState.class);
        when(mockParameters.getRequestState()).thenReturn(mockRequestState);
        when(mockRequestState.getSpId()).thenReturn(
                TestingConstants.SPID_CONS.toString());

        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();

        auconnectorutil.setConfigs(CONFIGS);

        Assert.assertFalse(auconnectorutil.validateRequestHasLoas(mockParameters.getRequestState()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil# validateSP(Map)} .
     * Must return true.
     */
    @Test
    public void testValidateSP() {

        final WebRequest mockParameters = mock(WebRequest.class);
        RequestState mockRequestState = mock(RequestState.class);
        when(mockParameters.getRequestState()).thenReturn(mockRequestState);
        when(mockRequestState.getSpId()).thenReturn(
                TestingConstants.SPID_CONS.toString());
        when(mockRequestState.getLevelsOfAssurance()).thenReturn(
                Arrays.asList(TestingConstants.LEVEL_OF_ASSURANCE_LOW_CONS.toString()));

        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConfigs(CONFIGS);

        Assert.assertTrue(auconnectorutil.validateRequestHasLoas(mockParameters.getRequestState()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#checkNotPresentInCache(String, String)}
     * using the JCache-Dev antireplay Cache implementation {@link ConcurrentMapJcacheServiceDefaultImpl} ..
     */
    @Test
    public void testDefaultAntiReplayMechanism() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        final Cache antiReplayCache = new ConcurrentMapJcacheServiceDefaultImpl().getConfiguredCache();
        auconnectorutil.setAntiReplayCache(antiReplayCache);
        auconnectorutil.flushReplayCache();
        // This is the first case a SAML id is submitted in the cache
        Assert.assertTrue("FIRST pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
        // Second submission of same value, replay attack must be detected
        Assert.assertFalse("Second pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#checkNotPresentInCache(String, String)}
     * that checks if called exactly once with putIfAbsent with correct Key
     */
    @Test
    public void testAntiReplayMechanismCorrectKey() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        final Cache antiReplayCache = mock(Cache.class);
        auconnectorutil.setAntiReplayCache(antiReplayCache);
        auconnectorutil.flushReplayCache();
        when(antiReplayCache.putIfAbsent(anyString(), anyBoolean())).thenReturn(true);

        auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU");

        verify(antiReplayCache, times(1)).putIfAbsent( "EU" + "/" + ANTIREPLAY_SAML_ID_A,true);
    }

    /**
     * Test method for {@link AUCONNECTORUtil#checkNotPresentInCache(String, String)}
     * Expect an exception when the cache is null
     */
    @Test(expected = EIDASSAMLEngineRuntimeException.class)
    public void testAntiReplayMechanismThrowWhenCacheIsNull() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setAntiReplayCache(null);
        auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU");
    }

    /**
     * Test method for {@link AUCONNECTORUtil#checkNotPresentInCache(String, String)}
     * Call with messageId == null
     */
    @Test
    public void testAntiReplayMechanismIgnoreWhenMessageCodeIsNull() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        final Cache antiReplayCache = mock(Cache.class);
        auconnectorutil.setAntiReplayCache(antiReplayCache);
        Assert.assertTrue(auconnectorutil.checkNotPresentInCache(null, "EU"));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#checkNotPresentInCache(String, String)}
     * Checks the antiReplayMechanism using a {@link HashMap} for cache
     */
    @Test
    public void testAntiReplayMechanism() {
        Map <String,Boolean> map  = new HashMap <String,Boolean>();
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        final Cache antiReplayCache = mock(Cache.class);
        auconnectorutil.setAntiReplayCache(antiReplayCache);
        auconnectorutil.flushReplayCache();
        when(antiReplayCache.putIfAbsent(anyString(), anyBoolean())).thenAnswer((Answer<Boolean>) invocationOnMock -> {
            String key = invocationOnMock.getArgument(0);
            return null == map.putIfAbsent(key, true); // counter-intuitive, read the HashMap javadoc
        });

        // This is the first case a SAML id is submitted in the cache
        Assert.assertTrue("FIRST pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
        // Second submission of same value, replay attack must be detected
        Assert.assertFalse("Second pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));

    }

    /**
     * Test method for {@link AUCONNECTORUtil#getCountryCode(ILightRequest, WebRequest)}
     * Checks the country code is fetched from the webRequest when not available in the LightRequest
     *
     * Must succeed
     */
    @Test
    public void testGetCountryCode() {
        WebRequest webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY)).thenReturn("BE");
        ILightRequest lightRequest = Mockito.mock(ILightRequest.class);
        Mockito.when(lightRequest.getCitizenCountryCode()).thenReturn(null);

        AUCONNECTORUtil auconnectorUtil = new AUCONNECTORUtil();
        String countryCode = auconnectorUtil.getCountryCode(lightRequest, webRequest);

        String expectedCountryCode = "BE";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link AUCONNECTORUtil#getCountryCode(ILightRequest, WebRequest)}
     * Checks the country code is fetched from the lightRequest when available it
     *
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromLightRequest() {
        WebRequest webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY)).thenReturn("BE");
        ILightRequest lightRequest = Mockito.mock(ILightRequest.class);
        Mockito.when(lightRequest.getCitizenCountryCode()).thenReturn("BE2");

        AUCONNECTORUtil auconnectorUtil = new AUCONNECTORUtil();
        String countryCode = auconnectorUtil.getCountryCode(lightRequest, webRequest);

        String expectedCountryCode = "BE2";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link AUCONNECTORUtil#getCountryCode(ILightRequest, WebRequest)}
     * Checks the suffix of the country code is removed before returning the country code
     *
     * Must succeed
     */
    @Test
    public void testGetCountryCodeWithoutSuffix() {
        WebRequest webRequest = Mockito.mock(WebRequest.class);
        Mockito.when(webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY)).thenReturn("BE");
        ILightRequest lightRequest = Mockito.mock(ILightRequest.class);
        Mockito.when(lightRequest.getCitizenCountryCode()).thenReturn("BE" + EIDASValues.EIDAS_SERVICE_SUFFIX.toString());

        AUCONNECTORUtil auconnectorUtil = new AUCONNECTORUtil();
        String countryCode = auconnectorUtil.getCountryCode(lightRequest, webRequest);

        String expectedCountryCode = "BE";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }
}