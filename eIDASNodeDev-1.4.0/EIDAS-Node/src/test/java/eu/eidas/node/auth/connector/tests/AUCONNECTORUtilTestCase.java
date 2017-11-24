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

package eu.eidas.node.auth.connector.tests;

import com.hazelcast.core.Hazelcast;
import eu.eidas.auth.commons.*;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDefaultImpl;
import eu.eidas.auth.commons.cache.ConcurrentMapServiceDistributedImpl;
import eu.eidas.auth.commons.cache.HazelcastInstanceInitializer;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.util.Properties;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Functional testing class to {@link AUCONNECTORUtil}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com,
 *         luis.felix@multicert.com
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
    private static Properties EIDASUTILS_CONFIGS = new Properties();

    private static final String ANTIREPLAY_SAML_ID_A = "SAML_ID_A";

    @After
    public void after() throws Exception {

        Hazelcast.shutdownAll();
    }

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
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
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
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertNull(auconnectorutil.loadConfig(EidasParameterKeys.EIDAS_ASK_CONSENT_VALUE
                .toString()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#loadConfigServiceURL(String)} . Must Return
     * null.
     */
    @Test
    public void testLoadConfigNodeURLMissing() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setConfigs(CONFIGS);
        CONFIGS.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(),
                TestingConstants.ONE_CONS.toString());
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_PREFIX.index(1),
                TestingConstants.LOCAL_CONS.toString());
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_PREFIX.name(1),
                TestingConstants.LOCAL_CONS.toString());

        Assert.assertNull(auconnectorutil.loadConfigServiceURL(TestingConstants.LOCAL_CONS
                .toString()));
    }

    /**
     * Test method for {@link AUCONNECTORUtil#loadConfigServiceURL(String)} . Must an
     * URL.
     */
    @Test
    public void testLoadConfigNodeURL() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setConfigs(CONFIGS);
        CONFIGS.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(),
                TestingConstants.ONE_CONS.toString());
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_PREFIX.index(1),
                TestingConstants.LOCAL_CONS.toString());
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_PREFIX.name(1),
                TestingConstants.LOCAL_CONS.toString());
        CONFIGS.setProperty(EIDASValues.EIDAS_SERVICE_PREFIX.url(1),
                TestingConstants.LOCAL_URL_CONS.toString());

        Assert.assertEquals(TestingConstants.LOCAL_URL_CONS.toString(),
                auconnectorutil.loadConfigServiceURL(TestingConstants.LOCAL_CONS.toString()));
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
        when(mockRequestState.getQaa()).thenReturn(
                TestingConstants.MAX_QAA_CONS.toString());
        when(mockRequestState.getSpId()).thenReturn(
                TestingConstants.SPID_CONS.toString());

        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setMaxQAA(TestingConstants.MAX_QAA_CONS.intValue());
        auconnectorutil.setMinQAA(TestingConstants.MIN_QAA_CONS.intValue());

        auconnectorutil.setConfigs(CONFIGS);

        Assert.assertFalse(auconnectorutil.validateSP(mockParameters));
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
        when(mockRequestState.getQaa()).thenReturn(
                TestingConstants.QAALEVEL_CONS.toString());
        when(mockRequestState.getSpId()).thenReturn(
                TestingConstants.SPID_CONS.toString());

        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setMaxQAA(TestingConstants.MAX_QAA_CONS.intValue());
        auconnectorutil.setMinQAA(TestingConstants.MIN_QAA_CONS.intValue());
        CONFIGS.put(TestingConstants.SPID_CONS.getQaaLevel(),
                TestingConstants.QAALEVEL_CONS.toString());
        auconnectorutil.setConfigs(CONFIGS);

        Assert.assertTrue(auconnectorutil.validateSP(mockParameters));
    }

    /**
     * Test method for
     * {@link AUCONNECTORUtil# checkContents(String, IPersonalAttributeList)} . Must
     * return false.
     */
    @Test
    @Ignore
    public void testCheckContentsNoPermission() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        CONFIGS.remove(TestingConstants.SPID_CONS.toString());
        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertFalse(auconnectorutil.checkContents(
                TestingConstants.SPID_CONS.toString(), ImmutableAttributeMap.of()));
    }

    /**
     * Test method for
     * {@link AUCONNECTORUtil# checkContents(String, IPersonalAttributeList)} . Must
     * return true.
     */
    @Ignore
    @Test
    public void testCheckContentsAll() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        CONFIGS.put(EIDASValues.DEFAULT.toString(),
                TestingConstants.ALL_CONS.toString());
        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertTrue(auconnectorutil.checkContents(TestingConstants.SPID_CONS.toString(),
                                                        ImmutableAttributeMap.of()));
    }

    /**
     * Test method for
     * {@link AUCONNECTORUtil# checkContents(String, IPersonalAttributeList)} . Must
     * return false.
     */
    @Test
    @Ignore
    public void testCheckContentsNotAllowed() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        CONFIGS.put(TestingConstants.SPID_CONS.toString(),
                TestingConstants.EMPTY_CONS.toString());

        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertFalse(auconnectorutil.checkContents(
                TestingConstants.SPID_CONS.toString(), ImmutableAttributeMap.of()));
    }

    /**
     * Test method for
     * {@link AUCONNECTORUtil# checkContents(String, IPersonalAttributeList)} . Must
     * return true.
     */
    @Test
    @Ignore
    public void testCheckContents() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        CONFIGS.put(TestingConstants.SPID_CONS.toString(),
                TestingConstants.ALLOWED_ATTRIBUTES_CONS.toString());

        auconnectorutil.setConfigs(CONFIGS);
        Assert.assertTrue(auconnectorutil.checkContents(TestingConstants.SPID_CONS.toString(),
                                                        ImmutableAttributeMap.of()));
    }

    /**
     * Checks the default antireplay Cache.
     */
    @Test
    public void testDefaultAntiReplayMechanism() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        auconnectorutil.setConcurrentMapService(new ConcurrentMapServiceDefaultImpl());
        auconnectorutil.setAntiReplayCache(auconnectorutil.getConcurrentMapService().getConfiguredMapCache());
        auconnectorutil.flushReplayCache();
        // This is the first case a SAML id is submitted in the cache
        Assert.assertTrue("FIRST pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
        // Second submission of same value, replay attack must be detected
        Assert.assertFalse("Second pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
    }
    /**
     * Checks the default anti-replay Cache.
     */
    @Test
    public void testHazelcastAntiReplayMechanism() {
        final AUCONNECTORUtil auconnectorutil = new AUCONNECTORUtil();
        ConcurrentMapServiceDistributedImpl hazelCache = new ConcurrentMapServiceDistributedImpl();
        HazelcastInstanceInitializer initializer = new HazelcastInstanceInitializer();
        initializer.setHazelcastInstanceName("TEST");
        try {
            initializer.initializeInstance();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        hazelCache.setCacheName("myTestCache");
        hazelCache.setHazelcastInstanceInitializer(initializer);
        auconnectorutil.setConcurrentMapService(hazelCache);
        auconnectorutil.setAntiReplayCache(auconnectorutil.getConcurrentMapService().getConfiguredMapCache());
        auconnectorutil.flushReplayCache();
        // This is the first case a SAML id is submitted in the cache
        Assert.assertTrue("FIRST pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
        // Second submission of same value, replay attack must be detected
        Assert.assertFalse("Second pass of replay attack", auconnectorutil.checkNotPresentInCache(ANTIREPLAY_SAML_ID_A, "EU"));
    }

    @Test(expected=InvalidParameterEIDASException.class)
    public void testHazelCastAntiReplayMechanismFailByNullCache(){
        ConcurrentMapServiceDistributedImpl hazelCache = new ConcurrentMapServiceDistributedImpl();
        hazelCache.getConfiguredMapCache();
    }
}