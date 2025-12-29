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

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataRoleParameters;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import org.bouncycastle.util.encoders.Base64;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.DEREncodedKeyValue;
import org.opensaml.xmlsec.signature.KeyValue;
import org.opensaml.xmlsec.signature.RSAKeyValue;
import org.opensaml.xmlsec.signature.impl.DEREncodedKeyValueBuilder;
import org.opensaml.xmlsec.signature.impl.ExponentBuilder;
import org.opensaml.xmlsec.signature.impl.KeyValueBuilder;
import org.opensaml.xmlsec.signature.impl.ModulusBuilder;
import org.opensaml.xmlsec.signature.impl.RSAKeyValueBuilder;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Set;

public class HighLevelMetadataParamsTest {

    private X509Certificate selfSignedCert1;
    private X509Certificate selfSignedCert2;
    private X509Certificate selfSignedCert3;
    private X509Certificate selfSignedCert4;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final KeyStore pkcs12 = KeyStore.getInstance("PKCS12");
        pkcs12.load(new FileInputStream("src/test/resources/" + "certificates.p12"), ("local-demo").toCharArray());
        selfSignedCert1 = (X509Certificate) pkcs12.getCertificate("selfsigned1");
        selfSignedCert2 = (X509Certificate) pkcs12.getCertificate("selfsigned2");
        selfSignedCert3 = (X509Certificate) pkcs12.getCertificate("selfsigned3");
        selfSignedCert4 = (X509Certificate) pkcs12.getCertificate("selfsigned4 (selfsigned1)");
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getRequestSignatureCertificate(AuthnRequest)}
     * when the connector metadata contains multiple certificates
     * return the one matching the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetRequestSignatureCertificate() throws CertificateException {
        final AuthnRequest request = Mockito.mock(AuthnRequest.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(request.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue())
                .thenReturn(new String(Base64.encode(selfSignedCert1.getEncoded())));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.SP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getRequestSignatureCertificate(request);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getRequestSignatureCertificate(AuthnRequest)}
     * when the connector metadata contains multiple certificates
     * return null when no matching
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetRequestSignatureCertificate_NoMatching() throws CertificateException {
        final AuthnRequest request = Mockito.mock(AuthnRequest.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(request.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue())
                .thenReturn(new String(Base64.encode(selfSignedCert1.getEncoded())));


        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.SP, List.of(
                selfSignedCert2,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getRequestSignatureCertificate(request);

        Assert.assertNull(requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates
     * return the one matching the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate() throws CertificateEncodingException {
        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue())
                .thenReturn(new String(Base64.encode(selfSignedCert1.getEncoded())));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates
     * return null when no matching
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_NoMatching() throws CertificateEncodingException {
        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue())
                .thenReturn(new String(Base64.encode(selfSignedCert1.getEncoded())));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertNull(requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates
     * return the first certificate if no keyInfo is provided in the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_NoKeyInfo() throws CertificateEncodingException {
        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getSignature().getKeyInfo())
                .thenReturn(null);

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert2, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates
     * return the one matching the DER encoded public key in the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_DER() throws CertificateEncodingException {
        final DEREncodedKeyValue derEncodedKeyValue = new DEREncodedKeyValueBuilder().buildObject();
        derEncodedKeyValue.setValue(new String(Base64.encode(selfSignedCert1.getPublicKey().getEncoded())));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getSignature().getKeyInfo().getDEREncodedKeyValues())
                .thenReturn(List.of(derEncodedKeyValue));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates
     * return the one matching the RSAKeyValue public key in the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_RSA() throws CertificateEncodingException {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) selfSignedCert1.getPublicKey();

        final RSAKeyValue rsaKeyValue = new RSAKeyValueBuilder().buildObject();
        rsaKeyValue.setModulus(new ModulusBuilder().buildObject());
        rsaKeyValue.setExponent(new ExponentBuilder().buildObject());
        rsaKeyValue.getModulus().setValue(new String(Base64.encode(rsaPublicKey.getModulus().toByteArray())));
        rsaKeyValue.getExponent().setValue(new String(Base64.encode(rsaPublicKey.getPublicExponent().toByteArray())));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        final KeyValue keyValue = new KeyValueBuilder().buildObject();
        keyValue.setRSAKeyValue(rsaKeyValue);
        Mockito.when(response.getSignature().getKeyInfo().getKeyValues())
                .thenReturn(List.of(keyValue));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates, some of which share a keypair
     * return the one matching the certificate in the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_CertificateWithSamePrivateKey() throws CertificateEncodingException {
        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getSignature().getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue())
                .thenReturn(new String(Base64.encode(selfSignedCert1.getEncoded())));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3,
                selfSignedCert4
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates, some of which share a keypair
     * return the one matching the RSAKeyValue public key in the message
     * <p>
     * Must fail.
     */
    @Test
    public void testGetResponseSignatureCertificate_PublicKeytoMetadataCertificatesWithSamePrivateKey() throws CertificateEncodingException {
        exception.expectMessage("Metadata contains multiple certificates for the saml message public key, no unique match.");

        final RSAPublicKey rsaPublicKey = (RSAPublicKey) selfSignedCert1.getPublicKey();

        final RSAKeyValue rsaKeyValue = new RSAKeyValueBuilder().buildObject();
        rsaKeyValue.setModulus(new ModulusBuilder().buildObject());
        rsaKeyValue.setExponent(new ExponentBuilder().buildObject());
        rsaKeyValue.getModulus().setValue(new String(Base64.encode(rsaPublicKey.getModulus().toByteArray())));
        rsaKeyValue.getExponent().setValue(new String(Base64.encode(rsaPublicKey.getPublicExponent().toByteArray())));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        final KeyValue keyValue = new KeyValueBuilder().buildObject();
        keyValue.setRSAKeyValue(rsaKeyValue);
        Mockito.when(response.getSignature().getKeyInfo().getKeyValues())
                .thenReturn(List.of(keyValue));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert3,
                selfSignedCert4
        ));
        highLevelMetadataParams.getResponseSignatureCertificate(response);
    }

    /**
     * Test method for
     * {@link HighLevelMetadataParams#getResponseSignatureCertificate(Response)}
     * when the connector metadata contains multiple certificates, some of which are duplicated
     * return the one matching the RSAKeyValue public key in the message
     * <p>
     * Must succeed.
     */
    @Test
    public void testGetResponseSignatureCertificate_PublicKeytoMetadataIdenticalCertificatesWithSamePrivateKey() throws CertificateEncodingException {
        final RSAPublicKey rsaPublicKey = (RSAPublicKey) selfSignedCert1.getPublicKey();

        final RSAKeyValue rsaKeyValue = new RSAKeyValueBuilder().buildObject();
        rsaKeyValue.setModulus(new ModulusBuilder().buildObject());
        rsaKeyValue.setExponent(new ExponentBuilder().buildObject());
        rsaKeyValue.getModulus().setValue(new String(Base64.encode(rsaPublicKey.getModulus().toByteArray())));
        rsaKeyValue.getExponent().setValue(new String(Base64.encode(rsaPublicKey.getPublicExponent().toByteArray())));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        final KeyValue keyValue = new KeyValueBuilder().buildObject();
        keyValue.setRSAKeyValue(rsaKeyValue);
        Mockito.when(response.getSignature().getKeyInfo().getKeyValues())
                .thenReturn(List.of(keyValue));

        final HighLevelMetadataParams highLevelMetadataParams = getHighLevelMetadataParams(MetadataRole.IDP, List.of(
                selfSignedCert2,
                selfSignedCert1,
                selfSignedCert1,
                selfSignedCert3
        ));
        final X509Certificate requestSignatureCertificate = highLevelMetadataParams.getResponseSignatureCertificate(response);

        Assert.assertEquals(selfSignedCert1, requestSignatureCertificate);
    }


    private @Nonnull HighLevelMetadataParams getHighLevelMetadataParams(
            MetadataRole metadataRole,
            List<X509Certificate> selfSignedCert2
    ) {
        final EidasMetadataParametersI eidasMetadataParameters = new EidasMetadataParameters();
        eidasMetadataParameters.setNodeCountry("EU");

        final EidasMetadataRoleParameters eidasMetadataRoleParameters = new EidasMetadataRoleParameters();
        eidasMetadataRoleParameters.setRole(metadataRole);
        eidasMetadataRoleParameters.setSigningCertificates(selfSignedCert2);

        eidasMetadataParameters.setRoleDescriptors(Set.of(eidasMetadataRoleParameters));

        return new HighLevelMetadataParams(eidasMetadataParameters);
    }
}