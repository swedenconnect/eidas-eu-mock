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

package eu.eidas.auth.engine.core.impl;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;

/**
 * Test class for the {@link CertificateValidator}
 */
public class CertificateValidatorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateValidityPeriod(X509Certificate)}
     * when the certificate period is valid
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckCertificateValidityPeriod() throws EIDASSAMLEngineException {
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getNotBefore()).thenReturn(new Date("7-Jun-2020"));
        Mockito.when(mockX509Certificate.getNotAfter()).thenReturn(new Date("7-Jun-2100"));

        CertificateValidator.checkCertificateValidityPeriod(mockX509Certificate);
    }

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateValidityPeriod(boolean, X509Certificate)}
     * when the certificate period is valid and isCheckedValidityPeriod is set to true
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckCertificateValidityPeriodWithCheckedValidityPeriod() throws EIDASSAMLEngineException {
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getNotBefore()).thenReturn(new Date("7-Jun-2020"));
        Mockito.when(mockX509Certificate.getNotAfter()).thenReturn(new Date("7-Jun-2100"));

        CertificateValidator.checkCertificateValidityPeriod(true, mockX509Certificate);
    }

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateIssuer(boolean, X509Certificate)}
     * when the certificate is not self signed
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckCertificateIssuerWithCertificateNotSelfSigned() throws EIDASSAMLEngineException {
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=Mario"));
        Mockito.when(mockX509Certificate.getIssuerX500Principal()).thenReturn(new X500Principal("CN=John"));

        CertificateValidator.checkCertificateIssuer(true, mockX509Certificate);
    }

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateIssuer(boolean, X509Certificate)}
     * when the certificate is self signed and the method throws EIDASSAMLEngineException
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckCertificateIssuerWithCertificateSelfSigned() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=John"));
        Mockito.when(mockX509Certificate.getIssuerX500Principal()).thenReturn(new X500Principal("CN=John"));

        CertificateValidator.checkCertificateIssuer(true, mockX509Certificate);
    }

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateIssuer(Map, X509Certificate)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckCertificateIssuerWithPropertiesParameter() throws EIDASSAMLEngineException {
        final Map mockMap = Mockito.mock(Map.class);
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getSubjectX500Principal()).thenReturn(new X500Principal("CN=John"));

        CertificateValidator.checkCertificateIssuer(mockMap, mockX509Certificate);
    }

    /**
     * Test method for
     * {@link CertificateValidator#checkCertificateValidityPeriod(Map, X509Certificate)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckCertificateValidityPeriodWithPropertiesParameter() throws EIDASSAMLEngineException {
        final Map mockMap = Mockito.mock(Map.class);
        final X509Certificate mockX509Certificate = Mockito.mock(X509Certificate.class);

        Mockito.when(mockX509Certificate.getNotBefore()).thenReturn(new Date("7-Jun-2020"));
        Mockito.when(mockX509Certificate.getNotAfter()).thenReturn(new Date("7-Jun-2100"));

        CertificateValidator.checkCertificateValidityPeriod(mockMap, mockX509Certificate);
    }

}