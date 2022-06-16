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

package eu.eidas.auth.engine.core;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;

import java.security.cert.X509Certificate;

/**
 * Test class for {@link SigningContext}
 */
public class SigningContextTest {

    /**
     * Test method for
     * {@link SigningContext#getSigningCredential()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getSigningCredential() {
        final SigningContext.Builder signingContextBuilder = new SigningContext.Builder();
        final X509Certificate x509CertificateMock = Mockito.mock(X509Certificate.class);
        final BasicX509Credential expectedSigningCredential = new BasicX509Credential(x509CertificateMock);
        signingContextBuilder.setSigningCredential(expectedSigningCredential);
        final SigningContext signingContext = signingContextBuilder.build();

        final X509Credential actualSigningCredential = signingContext.getSigningCredential();
        Assert.assertEquals(expectedSigningCredential,actualSigningCredential);
    }

    /**
     * Test method for
     * {@link SigningContext#getSigningAlgorithm()}
     * <p>
     * Must succeed.
     */
    @Test
    public void getSignAlgorithm() {
        final SigningContext.Builder signingContextBuilder = new SigningContext.Builder();
        final String expectedSigningAlgorithm = "http://www.w3.org/2001/04/xmldsig-more#ecdsa-sha512";
        signingContextBuilder.setSigningAlgorithm(expectedSigningAlgorithm);
        final SigningContext signingContext = signingContextBuilder.build();

        final String actualSigningAlgorithm = signingContext.getSigningAlgorithm();
        Assert.assertEquals(expectedSigningAlgorithm, actualSigningAlgorithm);
    }

    /**
     * Test method for
     * {@link SigningContext#isSignWithKeyValue()}
     * <p>
     * Must succeed.
     */
    @Test
    public void isSignWithKeyValue() {
        final SigningContext.Builder signingContextBuilder = new SigningContext.Builder();
        final boolean expectedIsSignWithKeyValue = false;
        signingContextBuilder.setSignWithKeyValue(expectedIsSignWithKeyValue);
        final SigningContext signingContext = signingContextBuilder.build();

        boolean actualIsSignWithKeyValue = signingContext.isSignWithKeyValue();
        Assert.assertEquals(expectedIsSignWithKeyValue, actualIsSignWithKeyValue);
    }
}