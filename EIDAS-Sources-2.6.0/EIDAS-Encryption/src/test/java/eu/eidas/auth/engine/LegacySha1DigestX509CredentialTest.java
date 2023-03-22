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

package eu.eidas.auth.engine;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opensaml.security.x509.X509Credential;

import java.security.cert.X509Certificate;


/**
 * Test class for {@link LegacySha1DigestX509Certificate}
 */
public class LegacySha1DigestX509CredentialTest {

    /**
     * Test method for
     * {@link LegacySha1DigestX509Credential#LegacySha1DigestX509Credential(X509Credential)}
     * <p>
     * Must succeed.
     */
    @Test
    public void newInstanceOfLegacySha1DigestX509CredentialTest() {
        final X509Credential x509CredentialMock = Mockito.mock(X509Credential.class);
        Mockito.when(x509CredentialMock.getEntityCertificate()).thenReturn(Mockito.mock(X509Certificate.class));
        final LegacySha1DigestX509Credential legacySha1DigestX509Credential = new LegacySha1DigestX509Credential(x509CredentialMock);

        Assert.assertNotNull(legacySha1DigestX509Credential);
    }
}