/*
 *
 *  Copyright (c) 2019 by European Commission
 *
 *  Licensed under the EUPL, Version 1.2 or - as soon they will be
 *  approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence
 *
 */

package eu.eidas.auth.engine.core.impl;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.security.Provider;
import java.security.Security;

import static eu.eidas.auth.commons.EidasParameterKeys.BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class BouncyCastleBootstrapTest {

    /**
     * Ensure that the BouncyCastleProvider is removed before each test
     * Since we are trying to test the initialization of BouncyCastle provider
     */
    @Before
    public void setup() {
        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
    }

    /**
     * Ensure BouncyCastleProvider can be installed again after this class tests.
     * No mather the order of the tests.
     */
    @AfterClass
    public static void tearDown() {
        System.setProperty(BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL.toString(), "false");
    }

    @Test
    public void bootstrapTest () {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertThat (bouncyCastleProvider, is(nullValue()));
        BouncyCastleBootstrap.bootstrap();

        bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertThat (bouncyCastleProvider, is(notNullValue()));
    }

    @Test
    public void allowReInstallOfSecurityProviderTest () {
        System.setProperty(BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL.toString(), "false");
        BouncyCastleBootstrap.reInstallSecurityProvider();

        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertThat (bouncyCastleProvider, is(notNullValue()));
    }

    @Test
    public void blockReInstallOfSecurityProviderTest () {
        System.setProperty(BLOCK_BOUNCY_CASTLE_PROVIDER_REINSTALL.toString(), "true");
        BouncyCastleBootstrap.reInstallSecurityProvider();

        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        assertThat (bouncyCastleProvider, is(nullValue()));
    }

}