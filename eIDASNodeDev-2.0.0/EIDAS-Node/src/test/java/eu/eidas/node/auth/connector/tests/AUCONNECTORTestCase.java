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

import eu.eidas.auth.commons.Country;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.node.auth.connector.AUCONNECTOR;
import eu.eidas.node.auth.util.tests.TestingConstants;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

/**
 * Functional testing class to {@link AUCONNECTOR}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com
 * @version $Revision: $, $Date:$
 */
public class AUCONNECTORTestCase {

    /**
     * Properties values for testing proposes.
     */
    private static final Properties CONFIGS = new Properties();

    /**
     * Country List dummy values for testing proposes.
     */
    private static final List<Country> COUNTRY_LIST = new ArrayList<Country>(1);

    /**
     * byte[] dummy SAML token.
     */
    private static final byte[] SAML_TOKEN_ARRAY = {
            1, 23, -86, -71, -21, 45, 0, 0, 0, 3, -12, 94, -86, -25, -84, 122, -53, 64};

    /**
     * byte[] dummy Native SAML token.
     */
    private static final byte[] SAML_NATIVE_TOKEN_ARRAY = {
            1, 23, 86, 71, 21, 45, 0, 0, 0, 3, 12, 94, 86, 25, 84, 122, 53, 64};

    /**
     * Initialising class variables.
     *
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        COUNTRY_LIST.add(new Country(TestingConstants.LOCAL_CONS.toString(), TestingConstants.LOCAL_CONS.toString()));

        CONFIGS.setProperty(EidasErrorKey.INVALID_SESSION.errorCode(), TestingConstants.ERROR_CODE_CONS.toString());
        CONFIGS.setProperty(EidasErrorKey.INVALID_SESSION.errorMessage(), TestingConstants.ERROR_MESSAGE_CONS.toString());
        //EIDASUtil.setConfigs(CONFIGS);
    }

    /**
     * Test method for {@link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}. Testing null value. Must
     * throw {@link NullPointerException}.
     */
    @Test(expected = NullPointerException.class)
    public void testSendRedirectNullToken() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        auconnector.sendRedirect(null);
    }

    /**
     * Test method for {@link eu.eidas.node.auth.connector.AUCONNECTOR#sendRedirect(byte[])}. Must succeed.
     */
    @Test
    public void testSendRedirect() {
        AUCONNECTOR auconnector = new AUCONNECTOR();
        assertEquals("ARequestAAAAA/RequesestA", EidasStringUtil.toString(auconnector.sendRedirect(SAML_TOKEN_ARRAY)));
    }
}
