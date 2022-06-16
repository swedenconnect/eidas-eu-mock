/*
 * Copyright (c) 2022 by European Commission
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

package eu.eidas.encryption.config;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignature;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.security.Provider;
import java.security.Security;

/**
 * Test class for {@link EidasJCENameConfiguration}
 */
public class EidasJCENameConfigurationTest {

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * Test method for
     * {@link EidasJCENameConfiguration#setJCENameBasedOnSecurityProviderOrder()}
     * when BouncyCastle is loaded and provides the RSA MGF1 Signature algorithms
     * <p>
     * Must succeed.
     */
    @Test
    public void setJCENameBasedOnSecurityProviderOrder() {
        EidasJCENameConfiguration.setJCENameBasedOnSecurityProviderOrder();

        Assert.assertEquals("SHA256WITHRSAANDMGF1", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1));
        Assert.assertEquals("SHA384WITHRSAANDMGF1", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1));
        Assert.assertEquals("SHA512WITHRSAANDMGF1", JCEMapper.translateURItoJCEID(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1));
    }
}