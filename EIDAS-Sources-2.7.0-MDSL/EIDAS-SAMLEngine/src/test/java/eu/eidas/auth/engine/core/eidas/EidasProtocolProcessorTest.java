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
package eu.eidas.auth.engine.core.eidas;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.AbstractAttributeValue;
import eu.eidas.auth.commons.attribute.impl.LiteralStringAttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.exceptions.EIDASServiceException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.AbstractPostalAddressAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddress;
import eu.eidas.auth.commons.protocol.eidas.impl.PostalAddressAttributeValue;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.DefaultCoreProperties;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.SAMLCore;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.auth.engine.metadata.samlobjects.SPType;
import eu.eidas.auth.engine.util.tests.TestingConstants;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Extensions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextImpl;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;

import javax.xml.namespace.QName;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.isA;

/**
 * Test class for {@link EidasProtocolProcessor}
 */
public class EidasProtocolProcessorTest {

    /**
     * Attribute Definition
     */
    private static final AttributeDefinition<String> PERSON_IDENTIFIER =
            new AttributeDefinition.Builder<String>().nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier")
                    .friendlyName("PersonIdentifier")
                    .personType(PersonType.NATURAL_PERSON)
                    .required(true)
                    .uniqueIdentifier(true)
                    .xmlType("http://eidas.europa.eu/attributes/naturalperson", "PersonIdentifierType", "eidas-natural")
                    .attributeValueMarshaller(new StringAttributeValueMarshaller())
                    .build();

    private static final String SIGNATURE_CERT_SUBJECT = "C=EU, O=European Commission, OU=DIGIT, CN=rootCAMetadata";
    private static final String SIGNATURE_CERTIFICATE =
            "MIIFjjCCA3agAwIBAgIJAKmSfYIA+TEAMA0GCSqGSIb3DQEBBQUAMFQxCzAJBgNV\n" +
                    "BAYTAkVVMRwwGgYDVQQKDBNFdXJvcGVhbiBDb21taXNzaW9uMQ4wDAYDVQQLDAVE\n" +
                    "SUdJVDEXMBUGA1UEAwwOcm9vdENBTWV0YWRhdGEwHhcNMTkwNzI5MDcyODA3WhcN\n" +
                    "MzAxMjMxMjM1OTU5WjBUMQswCQYDVQQGEwJFVTEcMBoGA1UECgwTRXVyb3BlYW4g\n" +
                    "Q29tbWlzc2lvbjEOMAwGA1UECwwFRElHSVQxFzAVBgNVBAMMDnJvb3RDQU1ldGFk\n" +
                    "YXRhMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAyRFNpe2quaNhiyrV\n" +
                    "nngPkTb5hIScZQ+Dxq+7Gl9wS8c2hRCwew28sbFeLcv/hwwxfxwIt6+04Xh/kaTk\n" +
                    "8CQlRxcsV89dDMWOq4j+n7VvlaCPOhu/5FI2d0Fe02Z63u6EZHYszSmqBxPn05eb\n" +
                    "UJ0rG0uYNCOpaVoQAwvYyQZPRcbKGGgltnM+fBIXzK0eLtXXvJJuzK8Jl36xyTEs\n" +
                    "A/D2UcWfT7PaUQI+tvpKwdtPy9qKOauytZSM0ZN1ITmSA1pzksvbgynk7yaIDCN7\n" +
                    "vA6/NLCSrL9pmSn00+gnxFuXsamDnjenIfLe5VD21OpwP70Akv6PtJ0XKuTEPJxt\n" +
                    "3LCK/L6pfKAnJA0RZFlKfv76d99ogmWCjM/dNabH5AglJ/NTTTSgxh8rWcSzYTGg\n" +
                    "LRs9l69IqHR7XlpIHIePod9fr1sw/HFfwbNAzG0d/ZTsx5OD5A2QQYfibXfzCSJ8\n" +
                    "nAz1/jS7IU9A329B4hmKK4nJbygylLVotEHnXdwjv/369NdQuSnd95XISJQVSBTS\n" +
                    "aQghvsQM6zVC9zbMUYTAQjUqAuR1kw98xvA+GrCz6o0zQMtWnAK+JUhgtVkgS1VM\n" +
                    "UdYfesYA4zh0n4UOj+QH5GmY8Rv5kOHueOYworPw9y9IQrpbp675zGxAEO9T8bwC\n" +
                    "imuGHSrMs2HXJGZcmbFm2NC0hQMCAwEAAaNjMGEwDgYDVR0PAQH/BAQDAgEGMA8G\n" +
                    "A1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFHMK47MFhe0Dabpe2XPDob2jbkprMB8G\n" +
                    "A1UdIwQYMBaAFHMK47MFhe0Dabpe2XPDob2jbkprMA0GCSqGSIb3DQEBBQUAA4IC\n" +
                    "AQCBIdVGckDSdY+D5vloOp/0Gk9e2NFf/VOzun470aVNUv9Ye1tSWt1lkBu+koiT\n" +
                    "SYTmxgUeesaeHYq78fXsxEFZ6oQWEik8v/WaSvJAyQEaIe6R1ksbs7SnSKXDZaPw\n" +
                    "DHIVxW7P44XroBn60JzjxOQ3FPSKDEAsmCfAIaLjvZm7szkSE8eV8Iw8j8Tr1Kud\n" +
                    "s7OzNsGRS61VwaoqMAHY0ldspfTGN75BKdwOvasDsRHXxjTuGjtN3fioLXoTHXuS\n" +
                    "F9bukbBfxUROOa/e9YRxDZ30T52Yl7CuDRrhbNd5hLswnzD3YErHgzujGvPMTOFI\n" +
                    "zN9HOsQleacYsrWAjCG69sAuegBUYoEbjMMBApndyf7sTANQB/z+QGDKm6+FpzuX\n" +
                    "hyMr5GBaEgfvIjtMf/OxorlqOk2np1rHeXw7wOM1lUapW2AxBk6bHBHIJZsXwN9G\n" +
                    "SrVtjQi7PX+RJaxCTQiI4xsXBXqZ+7MImxkGMA/62pnOwIEsH5KKP/AkY05q0Tf9\n" +
                    "VFeF1pumPxPXTRLIv+QTC2CQQ/bLkh87J1KNCCyz94VDkotlbxnt1k7LAme15gVv\n" +
                    "2pkroOChq6bJO0k+1karFCWvyok8ZsDnV6Q7HKJ4eisMunipRbxhNKCMQ4GVMrWX\n" +
                    "+a30q1OIQN8CKPPeEgra3pWMGvwAc04Ru3gvnNkXFPFOew==";


    private static final ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceProtocolVersioning");

    private static final ProtocolEngineI protocolEngineRequesterId
            = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceRequesterId");

    private static final ProtocolEngineI protocolEngineMetadataFetcherThrowsException
            = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceMetadataFetcherThrowsException");

    private final static String PROXY_SERVICE = "proxyService";

    private final IAuthenticationRequest iAuthenticationRequest = buildIAuthenticationRequest();

    private final IAuthenticationResponse iAuthenticationResponse = buildIAuthenticateResponse();

    private final String ipAddress = "128.128.128.128";

    private final DefaultCoreProperties defaultCoreProperties = createDefaultCoreProperties(null);

    private final DateTime currentTime = DateTime.now();

    private ProtocolProcessorI protocolProcessor;

    private final MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
    private final MetadataSignerI metadataSigner = Mockito.mock(MetadataSignerI.class);
    private final MetadataClockI metadataClock = Mockito.mock(MetadataClockI.class);

    private final String SP_DEMO_REQUESTER_ID = "http://localhost:8080/SP";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        protocolProcessor = new EidasProtocolProcessor(metadataFetcher, metadataSigner, metadataClock);
    }

    @After
    public void tearDown() {
        Mockito.reset(metadataFetcher, metadataSigner, metadataClock);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the request issuer is having the element node country {@see EIDASValues#EIDAS_NODE_COUNTRY}
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromMetadataOfRequestIssuer() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn("EU");

        AuthnRequest samlRequest = Mockito.mock(AuthnRequest.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlRequest.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);

        String countryCode = protocolProcessor.getCountryCode(samlRequest);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the response issuer is having the element node country {@see EIDASValues#EIDAS_NODE_COUNTRY}
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromMetadataOfResponseIssuer() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn("EU");

        Response samlResponse = Mockito.mock(Response.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlResponse.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);

        String countryCode = protocolProcessor.getCountryCode(samlResponse);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the request issuer is not having the NodeCountry element
     * Then the country is fetch from the signature certificate of the request.
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromRequestSignatureCertificate() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn(null);

        Response samlResponse = Mockito.mock(Response.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlResponse.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        Mockito.when(samlResponse.getSignature()).thenReturn(mockSignature);

        String countryCode = protocolProcessor.getCountryCode(samlResponse);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the response issuer is not having the NodeCountry element
     * Then the country is fetch from the signature certificate of the response issuer.
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromResponseSignatureCertificate() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn(null);

        Response samlResponse = Mockito.mock(Response.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlResponse.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);
        Signature mockSignature = mockSignature(SIGNATURE_CERTIFICATE);
        Mockito.when(samlResponse.getSignature()).thenReturn(mockSignature);

        String countryCode = protocolProcessor.getCountryCode(samlResponse);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the request issuer is not having the NodeCountry element
     * And the certificate is not present in the Request
     * Then the country is fetch from the subjectDN of the metadata signature certificate.
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromMetadataSignatureCertificateWhenRequest() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn(null);

        AuthnRequest samlRequest = Mockito.mock(AuthnRequest.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlRequest.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);
        Signature mockSignature = mockEmptySignature();
        Mockito.when(samlRequest.getSignature()).thenReturn(mockSignature);

        X509Certificate mockSignatureCertificate = Mockito.mock(X509Certificate.class);
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(mockSignatureCertificate.getSubjectDN()).thenReturn(principal);
        Mockito.when(principal.toString()).thenReturn(SIGNATURE_CERT_SUBJECT);

        EidasMetadataRoleParametersI mockSPRoleDescriptor = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockSPRoleDescriptor.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockSPRoleDescriptor.getSigningCertificate()).thenReturn(mockSignatureCertificate);

        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockSPRoleDescriptor));

        String countryCode = protocolProcessor.getCountryCode(samlRequest);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getCountryCode(org.opensaml.saml.common.SignableSAMLObject)}
     * when the metadata of the response issuer is not having the NodeCountry element
     * And the certificate is not present in the Response
     * Then the country is fetch from the subjectDN of the metadata signature certificate.
     * <p>
     * Must succeed
     */
    @Test
    public void testGetCountryCodeFromMetadataSignatureCertificateWhenResponse() throws Exception {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getNodeCountry()).thenReturn(null);

        Response samlResponse = Mockito.mock(Response.class);
        Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(samlResponse.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn(metadataUrl);
        Signature mockSignature = mockEmptySignature();
        Mockito.when(samlResponse.getSignature()).thenReturn(mockSignature);

        X509Certificate mockSignatureCertificate = Mockito.mock(X509Certificate.class);
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(mockSignatureCertificate.getSubjectDN()).thenReturn(principal);
        Mockito.when(principal.toString()).thenReturn(SIGNATURE_CERT_SUBJECT);

        EidasMetadataRoleParametersI mockIDPRoleDescriptor = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockIDPRoleDescriptor.getRole()).thenReturn(MetadataRole.IDP);
        Mockito.when(mockIDPRoleDescriptor.getSigningCertificate()).thenReturn(mockSignatureCertificate);

        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockIDPRoleDescriptor));

        String countryCode = protocolProcessor.getCountryCode(samlResponse);

        String expectedCountryCode = "EU";
        Assert.assertEquals(expectedCountryCode, countryCode);
    }

    /**
     * Test method for  {@link EidasProtocolProcessor#getMetadataProtocolVersions(String)}
     * when the metadata are having eidas protocol versions
     * <p>
     * Must succeed
     */
    @Test
    public void testGetMetadataProtocolVersions() throws EIDASMetadataException {
        String metadataUrl = "testUrl";
        List<String> protocolVersionsList = Arrays.asList("1.2", "1.1");
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersionsList);

        List<EidasProtocolVersion> actualProtocolVersions = protocolProcessor.getMetadataProtocolVersions(metadataUrl);

        List<EidasProtocolVersion> expectedProtocolVersions = Arrays.asList(EidasProtocolVersion.PROTOCOL_VERSION_1_2, EidasProtocolVersion.PROTOCOL_VERSION_1_1);
        Assert.assertEquals(expectedProtocolVersions, actualProtocolVersions);
    }

    /**
     * Test method for  {@link EidasProtocolProcessor#getMetadataProtocolVersions(String)}
     * when the metadata are not having eidas protocol versions
     * <p>
     * Must succeed
     */
    @Test
    public void testGetMetadataProtocolVersionsEmpty() throws EIDASMetadataException {
        String metadataUrl = "testUrl";
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(null);

        List<EidasProtocolVersion> actualProtocolVersions = protocolProcessor.getMetadataProtocolVersions(metadataUrl);

        List<EidasProtocolVersion> expectedProtocolVersions = new ArrayList<>();
        Assert.assertEquals(expectedProtocolVersions, actualProtocolVersions);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentationResponse(ImmutableAttributeMap)}
     * when the attributes from the response do not contain representative ones
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckRepresentationResponseValidNotRepresentationCase() {
        AttributeDefinition<String> identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeValue<String> stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentationResponse(ImmutableAttributeMap)}
     * when the attributes from the response matches with a representation case with the 2 Minimum Data Sets (MDS) needed
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckRepresentationResponseValid() {
        AttributeDefinition<String> identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition<String> repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue<String> stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * when the requested attributes contain a representative attribute
     * <p>
     * Must succeed
     */
    @Test
    public void checkRepresentativeAttributesIncludesRepresentative() {
        AttributeDefinition<String> identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition<String> repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue<String> stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentativeAttributes(attributesMap);

        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * when the requested attributes  do not contain a representative attribute with the 2 Minimum Data Sets (MDS) needed
     * <p>
     * Must succeed
     */
    @Test
    public void checkRepresentativeAttributesNoRepresentative() {
        AttributeDefinition<String> identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeValue<String> stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentativeAttributes(attributesMap);

        Assert.assertTrue(actualResult);
    }


    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing only one level of assurance and level of assurance is a notified level of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestWithNotifiedLevelOfAssurance() throws EIDASSAMLEngineException {
        final String expectedFirstNotifiedLoA = NotifiedLevelOfAssurance.LOW.stringValue();

        AuthnRequest authnRequest = buildAuthnRequest(expectedFirstNotifiedLoA);
        Assert.assertNotNull(authnRequest);

        RequestedAuthnContext actualRequestedAuthnContext = authnRequest.getRequestedAuthnContext();

        AuthnContextComparisonTypeEnumeration expectedLoaComparison = AuthnContextComparisonTypeEnumeration.MINIMUM;
        AuthnContextComparisonTypeEnumeration actualLoaComparison = actualRequestedAuthnContext.getComparison();
        Assert.assertEquals(expectedLoaComparison, actualLoaComparison);

        assertNumberLoasInAuthnContextDeclRefsIsZero(actualRequestedAuthnContext);
        assertNumberLoasInAuthnContextClassRefsIsOne(actualRequestedAuthnContext);

        final String actualFirstLoa = getFirstLoa(actualRequestedAuthnContext);
        Assert.assertEquals(expectedFirstNotifiedLoA, actualFirstLoa);
    }


    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing only one level of assurance and level of assurance is a nonNotified level of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestWithNonNotifiedLevelOfAssurance() throws EIDASSAMLEngineException {
        final String expectedFirstNonNotifiedLoA = "http://eidas.europa.eu/nonNotified/LoA/low";

        AuthnRequest authnRequest = buildAuthnRequest(expectedFirstNonNotifiedLoA);
        Assert.assertNotNull(authnRequest);

        RequestedAuthnContext actualRequestedAuthnContext = authnRequest.getRequestedAuthnContext();
        assertLoaComparisonIsExact(actualRequestedAuthnContext);

        assertNumberLoasInAuthnContextDeclRefsIsZero(actualRequestedAuthnContext);
        assertNumberLoasInAuthnContextClassRefsIsOne(actualRequestedAuthnContext);

        final String actualFirstLoa = getFirstLoa(actualRequestedAuthnContext);
        Assert.assertEquals(expectedFirstNonNotifiedLoA, actualFirstLoa);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing multiple levels of assurance with a high notified level of assurance and a nonNotified level of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestWithLevelsOfAssurance() throws EIDASSAMLEngineException {
        String expectedFirstNotifiedLoA = NotifiedLevelOfAssurance.HIGH.stringValue();
        String expectedSecondNonNotifiedLoA = "http://eidas.europa.eu/nonNotified/LoA/low";

        AuthnRequest authnRequest = buildAuthnRequest(expectedFirstNotifiedLoA, expectedSecondNonNotifiedLoA);
        Assert.assertNotNull(authnRequest);

        RequestedAuthnContext actualRequestedAuthnContext = authnRequest.getRequestedAuthnContext();
        assertLoaComparisonIsExact(actualRequestedAuthnContext);

        assertNumberLoasInAuthnContextDeclRefsIsZero(actualRequestedAuthnContext);
        assertNumberLoasInAuthnContextClassRefsIsTwo(actualRequestedAuthnContext);

        final String actualFirstLoa = getFirstLoa(actualRequestedAuthnContext);
        final String actualSecondLoa = getSecondLoa(actualRequestedAuthnContext);
        Assert.assertEquals(expectedFirstNotifiedLoA, actualFirstLoa);
        Assert.assertEquals(expectedSecondNonNotifiedLoA, actualSecondLoa);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing multiple levels of assurance with a substantial notified level of assurance and a nonNotified level of assurance
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestWithExtrapolatedNotifiedLevelsOfAssurance() throws EIDASSAMLEngineException {
        String expectedFirstLoaNotifiedSubstancial = NotifiedLevelOfAssurance.SUBSTANTIAL.stringValue();
        String expectedThirdLoaNonNotified = "http://eidas.europa.eu/nonNotified/LoA/low";

        AuthnRequest authnRequest = buildAuthnRequest(expectedFirstLoaNotifiedSubstancial, expectedThirdLoaNonNotified);
        Assert.assertNotNull(authnRequest);

        RequestedAuthnContext actualRequestedAuthnContext = authnRequest.getRequestedAuthnContext();
        assertLoaComparisonIsExact(actualRequestedAuthnContext);

        assertNumberLoasInAuthnContextDeclRefsIsZero(actualRequestedAuthnContext);
        assertNumberLoasInAuthnContextClassRefsIsThree(actualRequestedAuthnContext);

        String expectedSecondLoaNotifiedHigh = NotifiedLevelOfAssurance.HIGH.stringValue();

        final String actualFirstLoa = getFirstLoa(actualRequestedAuthnContext);
        final String actualSecondLoa = getSecondLoa(actualRequestedAuthnContext);
        final String actualThirdLoa = getThirdLoa(actualRequestedAuthnContext);
        Assert.assertEquals(expectedFirstLoaNotifiedSubstancial, actualFirstLoa);
        Assert.assertEquals(expectedSecondLoaNotifiedHigh, actualSecondLoa);
        Assert.assertEquals(expectedThirdLoaNonNotified, actualThirdLoa);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and {@link EidasMetadataParametersI#isRequesterIdFlag()} is true
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestNotEmptyRequesterIdWithTrueRequesterIdFlag() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestRequesterIdWithRequesterIdFlagSet();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNotNull(scoping);

        List<RequesterID> requesterIDs = scoping.getRequesterIDs();
        int actualNumberRequesterIDs = requesterIDs.size();
        assertOneRequesterId(actualNumberRequesterIDs);

        assertFirstRequesterId(requesterIDs, SP_DEMO_REQUESTER_ID);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestNOtEmptyRequesterIdWithFalseRequesterIdFlag() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestRequesterIdWithRequesterIdFlagNotSet();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestNotEmptyRequesterIdWithFalseRequesterIdFlag() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestEmptyRequesterIdWithTrueRequesterIdFlag();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is  empty and {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * <p>
     * Must succeed
     */
    @Test
    public void marshallRequestEmptyRequesterIdWithFlaseNotSetRequesterId() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestEmptyRequesterIdWithFalseRequesterIdFlag();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)} throws exceptions
     * <p>
     * Must fail
     */
    @Test
    public void marshallRequestWithMetadataFetcherThrowsException() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage("demo exception");

        buildAuthnRequestMetadataFetcherThrowsException();
    }

    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * when iEidasAuthenticationRequest to be unmarshalled doesn't contain a requesterId.
     * <p>
     * Must succeed
     */
    @Test
    public void unmarshallRequestRequesterIdNotPresent() throws EIDASSAMLEngineException {
        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();
        IEidasAuthenticationRequest iEidasAuthenticationRequest = buildDummyEidasAuthenticationRequest();

        final AuthnRequest authnRequest = eidasProtocolProcessor.marshallRequest(iEidasAuthenticationRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);

        final IAuthenticationRequest iAuthenticationRequest = eidasProtocolProcessor.unmarshallRequest("CA", authnRequest, "CA");

        final String actualRequesterId = iAuthenticationRequest.getRequesterId();
        Assert.assertNull(actualRequesterId);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * when iEidasAuthenticationRequest to be unmarshalled contains a requesterId not empty.
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshallRequestRequesterIdNotEmpty() throws EIDASSAMLEngineException {
        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();
        final String expectedRequesterId = SP_DEMO_REQUESTER_ID;
        IEidasAuthenticationRequest iEidasAuthenticationRequest = buildiEidasAuthenticationRequestWithNorEmptyRequesterId(expectedRequesterId);

        final AuthnRequest authnRequest = eidasProtocolProcessor.marshallRequest(iEidasAuthenticationRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);

        final IAuthenticationRequest iAuthenticationRequest = eidasProtocolProcessor.unmarshallRequest("CA", authnRequest, "CA");
        final String actualRequesterId = iAuthenticationRequest.getRequesterId();

        assertUnmarshalledRequestContainsExpectedRequesterID(expectedRequesterId, actualRequesterId);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * when at least one Level of Assurance does not follow the required URI "protocol:hostname" format
     * <p>
     * Must fail.
     */
    @Test
    public void unmarshallRequestwithInvalidSyntaxLevelOfAssurance() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.COLLEAGUE_REQ_INVALID_LOA.toString());
        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();
        final String expectedRequesterId = SP_DEMO_REQUESTER_ID;
        IEidasAuthenticationRequest iEidasAuthenticationRequest = buildiEidasAuthenticationRequestWithNorEmptyRequesterId(expectedRequesterId);

        final AuthnRequest authnRequest = eidasProtocolProcessor.marshallRequest(iEidasAuthenticationRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);
        authnRequest.setRequestedAuthnContext(createRequestedAuthContext(AuthnContextComparisonTypeEnumeration.EXACT, "loa", "loa:loa"));

        final IAuthenticationRequest iAuthenticationRequest = eidasProtocolProcessor.unmarshallRequest("CA", authnRequest, "CA");
        final String actualRequesterId = iAuthenticationRequest.getRequesterId();

        assertUnmarshalledRequestContainsExpectedRequesterID(expectedRequesterId, actualRequesterId);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * when iEidasAuthenticationRequest to be unmarshalled contains a representative attribute.
     * <p>
     * Must fail
     */
    @Test
    public void unmarshallRequestContainingRepresentativeAttribute() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();
        AttributeDefinition<String> identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition<String> repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue<String> stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        IEidasAuthenticationRequest iEidasAuthenticationRequest = buildDummyEidasAuthenticationRequestWithAttributes(attributesMap);

        final AuthnRequest authnRequest = eidasProtocolProcessor.marshallRequest(iEidasAuthenticationRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);

        eidasProtocolProcessor.unmarshallRequest("CA", authnRequest, "CA");
    }


    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * When requested attribute is of type XSString and request builder throws an illegal argument exception
     * <p>
     * Must fail
     */
    @Test
    public void testUnmarshallRequestContainingXSStringValueErrorBuildingRequest() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();

        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder.buildObject(org.opensaml.saml.saml2.core.AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue("123 456");

        List<XMLObject> xmlObjectList = new ArrayList<>();
        xmlObjectList.add(stringValue);

        RequestedAttribute mockAttribute = Mockito.mock(RequestedAttribute.class);
        Mockito.when(mockAttribute.getName()).thenReturn("http://eidas.europa.eu/attributes/legalperson/D-2012-17-EUIdentifier");
        Mockito.when(mockAttribute.getOrderedChildren()).thenReturn(xmlObjectList);

        List<RequestedAttribute> requestedAttributeList = new ArrayList<>();
        requestedAttributeList.add(mockAttribute);

        RequestedAttributes mockRequestedAttributes = Mockito.mock(RequestedAttributes.class);
        Mockito.when(mockRequestedAttributes.getAttributes()).thenReturn(requestedAttributeList);

        List<XMLObject> mockedRequestedAttributesList = new ArrayList<>();
        mockedRequestedAttributesList.add(mockRequestedAttributes);

        List<XMLObject> mockSpTypeList = Mockito.mock(ArrayList.class);
        SPType spTypeObject = Mockito.mock(SPType.class);
        Mockito.when(spTypeObject.getSPType()).thenReturn(SpType.PUBLIC.toString());
        Mockito.when(mockSpTypeList.get(0)).thenReturn(spTypeObject);

        Extensions mockExtensions = Mockito.mock(Extensions.class);
        Mockito.when(mockExtensions.getUnknownXMLObjects(any())).thenReturn(mockedRequestedAttributesList, mockSpTypeList);

        final AuthnRequest mockRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(mockRequest.getExtensions()).thenReturn(mockExtensions);

        final Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(mockRequest.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn("testValue");
        Mockito.when(mockRequest.getDestination()).thenReturn("");

        eidasProtocolProcessor.unmarshallRequest("CA", mockRequest, "CA");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#unmarshallRequest(String, AuthnRequest, String)}
     * When requested attribute is of type XSString and attribute unmarshaller throws an AttributevalueMarshallingException
     * <p>
     * Must fail
     */
    @Test
    public void testUnmarshallRequestContainingXSSStringValueErrorUnmarshallingValue() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();

        XSStringBuilder stringBuilder = (XSStringBuilder) XMLObjectProviderRegistrySupport.getBuilderFactory()
                .getBuilder(XSString.TYPE_NAME);
        XSString stringValue = stringBuilder.buildObject(org.opensaml.saml.saml2.core.AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
        stringValue.setValue("123 456");

        List<XMLObject> xmlObjectList = new ArrayList<>();
        xmlObjectList.add(stringValue);

        RequestedAttribute mockAttribute = Mockito.mock(RequestedAttribute.class);
        Mockito.when(mockAttribute.getName()).thenReturn("http://eidas.europa.eu/attributes/naturalperson/Gender");
        Mockito.when(mockAttribute.getOrderedChildren()).thenReturn(xmlObjectList);

        List<RequestedAttribute> requestedAttributeList = new ArrayList<>();
        requestedAttributeList.add(mockAttribute);

        RequestedAttributes mockRequestedAttributes = Mockito.mock(RequestedAttributes.class);
        Mockito.when(mockRequestedAttributes.getAttributes()).thenReturn(requestedAttributeList);

        List<XMLObject> mockedRequestedAttributesList = new ArrayList<>();
        mockedRequestedAttributesList.add(mockRequestedAttributes);

        List<XMLObject> mockSpTypeList = Mockito.mock(ArrayList.class);
        SPType spTypeObject = Mockito.mock(SPType.class);
        Mockito.when(spTypeObject.getSPType()).thenReturn(SpType.PUBLIC.toString());
        Mockito.when(mockSpTypeList.get(0)).thenReturn(spTypeObject);

        Extensions mockExtensions = Mockito.mock(Extensions.class);
        Mockito.when(mockExtensions.getUnknownXMLObjects(any())).thenReturn(mockedRequestedAttributesList, mockSpTypeList);

        final AuthnRequest mockRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(mockRequest.getExtensions()).thenReturn(mockExtensions);

        final Issuer mockIssuer = Mockito.mock(Issuer.class);
        Mockito.when(mockRequest.getIssuer()).thenReturn(mockIssuer);
        Mockito.when(mockIssuer.getValue()).thenReturn("testValue");
        Mockito.when(mockRequest.getDestination()).thenReturn("");

        eidasProtocolProcessor.unmarshallRequest("CA", mockRequest, "CA");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateRequestAgainstMetadata(IAuthenticationRequest, String, SamlEngineCoreProperties)}
     * When an exception is thrown building the updated request
     * <p>
     * Must fail
     */
    @Test
    public void testValidateRequestAgainstMetadataExceptionBuildingUpdatedRequest() throws NoSuchMethodException, EIDASMetadataException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateRequestAgainstMetadataMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateRequestAgainstMetadata", IAuthenticationRequest.class, String.class, SamlEngineCoreProperties.class);
        validateRequestAgainstMetadataMethod.setAccessible(true);


        Set<String> mockSupportedAttributes = new HashSet<>();
        mockSupportedAttributes.add("http://eidas.europa.eu/attributes/naturalperson/Optional");

        ImmutableSortedSet.Builder builder;
        builder = new ImmutableSortedSet.Builder(Ordering.natural());
        builder.addAll(mockSupportedAttributes);
        ImmutableSortedSet<String> supportedAttributesSet = builder.build();

        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.IDP);
        Mockito.when(mockRoleParameters.getSupportedAttributes()).thenReturn(supportedAttributesSet);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");


        IAuthenticationRequest mockRequest = getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .providerName("DEMO_SP")
                .issuer("https://test.eu")
                .destination("placeholder")
                .build();
        Field destinationField = AbstractAuthenticationRequest.class.getDeclaredField("destination");
        destinationField.setAccessible(true);
        destinationField.set(mockRequest, "");
        SamlEngineCoreProperties mockProperties = Mockito.mock(SamlEngineCoreProperties.class);

        validateRequestAgainstMetadataMethod.invoke(protocolProcessor, mockRequest, "anyUrl", mockProperties);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateParamResponseFail(IAuthenticationRequest, IAuthenticationResponse)}
     * When status code in response is blank
     * <p>
     * Must fail
     */
    @Test
    public void testValidateParamResponseFailStatusCodeBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateParamResponseFailMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateParamResponseFail", IAuthenticationRequest.class, IAuthenticationResponse.class);
        validateParamResponseFailMethod.setAccessible(true);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        IAuthenticationResponse mockResponse = Mockito.mock(IAuthenticationResponse.class);

        Mockito.when(mockResponse.getStatusCode()).thenReturn("");

        validateParamResponseFailMethod.invoke(protocolProcessor, mockRequest, mockResponse);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateParamResponseFail(IAuthenticationRequest, IAuthenticationResponse)}
     * When Id in request is blank
     * <p>
     * Must fail
     */
    @Test
    public void testValidateParamResponseFailIdBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateParamResponseFailMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateParamResponseFail", IAuthenticationRequest.class, IAuthenticationResponse.class);
        validateParamResponseFailMethod.setAccessible(true);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        IAuthenticationResponse mockResponse = Mockito.mock(IAuthenticationResponse.class);

        Mockito.when(mockResponse.getStatusCode()).thenReturn("fail");
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("anyUrl");
        Mockito.when(mockRequest.getId()).thenReturn("");

        validateParamResponseFailMethod.invoke(protocolProcessor, mockRequest, mockResponse);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     * when the enable address attribute subject confirmation data parameter as true
     * <p>
     * Must succeed
     */
    @Test
    public void marshallResponseWithIpAddress() throws EIDASSAMLEngineException {
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put(SamlEngineCoreProperties.ENABLE_ADDRESS_ATTRIBUTE_SUBJECT_CONFIRMATION_DATA, "true");
        DefaultCoreProperties defaultCoreProperties = createDefaultCoreProperties(additionalProps);

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(1, actualNumberAssertion);

        Assertion assertion = assertions.get(0);
        Assert.assertNotNull(assertion.getSubject());
        Assert.assertEquals(1, assertion.getSubject().getSubjectConfirmations().size());
        Assert.assertNotNull(assertion.getSubject().getSubjectConfirmations().get(0));
        SubjectConfirmation subjectConfirmation = assertion.getSubject().getSubjectConfirmations().get(0);
        Assert.assertNotNull(subjectConfirmation.getSubjectConfirmationData());
        // Must NOT be null when property is true
        Assert.assertNotNull(subjectConfirmation.getSubjectConfirmationData().getAddress());
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime)}
     * when the enable address attribute subject confirmation data parameter as false
     * <p>
     * Must succeed
     */
    @Test
    public void marshallResponseWithoutIpAddress() throws EIDASSAMLEngineException {
        Map<String, String> additionalProps = new HashMap<>();
        additionalProps.put(SamlEngineCoreProperties.ENABLE_ADDRESS_ATTRIBUTE_SUBJECT_CONFIRMATION_DATA, "false");
        DefaultCoreProperties defaultCoreProperties = createDefaultCoreProperties(additionalProps);

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(1, actualNumberAssertion);

        Assertion assertion = assertions.get(0);
        Assert.assertNotNull(assertion.getSubject());
        Assert.assertEquals(1, assertion.getSubject().getSubjectConfirmations().size());
        Assert.assertNotNull(assertion.getSubject().getSubjectConfirmations().get(0));
        SubjectConfirmation subjectConfirmation = assertion.getSubject().getSubjectConfirmations().get(0);
        Assert.assertNotNull(subjectConfirmation.getSubjectConfirmationData());
        // Must be null when property is false
        Assert.assertNull(subjectConfirmation.getSubjectConfirmationData().getAddress());
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier matches the one where assertion must be included in the reponse.
     * <p>
     * Must succeed.
     */
    @Test
    @Deprecated
    public void marshallErrorResponseWithAssertion() throws EIDASSAMLEngineException {
        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.2");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(1, actualNumberAssertion);
    }


    /**
     * Test method for {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier doe not matches the one where assertion must be included in the response.
     * <p>
     * Must succeed.
     */
    @Test
    @Deprecated
    public void marshallErrorResponseWithoutAssertion() throws EIDASSAMLEngineException {
        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();
        includeAssertionApplicationIdentifiers.add("CEF:eIDAS-ref:2.1");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }


    /**
     * Test method for {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is null.
     * <p>
     * Must succeed.
     */
    @Test
    @Deprecated
    public void marshallErrorResponseApplicationIdentifiersNull() throws EIDASSAMLEngineException {
        List<String> includeAssertionApplicationIdentifiers = null;

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is empty.
     * <p>
     * Must succeed.
     */
    @Test
    @Deprecated
    public void marshallErrorResponseApplicationIdentifiersEmpty() throws EIDASSAMLEngineException {
        List<String> includeAssertionApplicationIdentifiers = new ArrayList<>();

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        Response response = eidasProtocolProcessor.marshallErrorResponse(iAuthenticationRequest, iAuthenticationResponse, ipAddress, defaultCoreProperties, currentTime, includeAssertionApplicationIdentifiers);

        List<Assertion> assertions = response.getAssertions();
        int actualNumberAssertion = assertions.size();
        Assert.assertEquals(0, actualNumberAssertion);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#addExtensionSPType(IEidasAuthenticationRequest, Extensions)}
     * When sp type does not exist
     * <p>
     * Must fail
     */
    @Test
    public void testAddExtensionSPTypeBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method addExtensionSPTypeMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("addExtensionSPType", IEidasAuthenticationRequest.class, Extensions.class);
        addExtensionSPTypeMethod.setAccessible(true);

        IEidasAuthenticationRequest mockRequest = Mockito.mock(IEidasAuthenticationRequest.class);
        Extensions mockExtensions = Mockito.mock(Extensions.class);

        Mockito.when(mockRequest.getSpType()).thenReturn("nonexistant");

        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        addExtensionSPTypeMethod.invoke(eidasProtocolProcessor, mockRequest, mockExtensions);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateRequestType(IAuthenticationRequest)}
     * When request is not of type IEidasAuthenticationRequest
     * <p>
     * Must fail
     */
    @Test
    public void testValidateRequestTypeInvalid() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateRequestTypeMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateRequestType", IAuthenticationRequest.class);
        validateRequestTypeMethod.setAccessible(true);
        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);

        validateRequestTypeMethod.invoke(protocolProcessor, mockRequest);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateId(IAuthenticationRequest)}
     * When Id in request is blank
     * <p>
     * Must fail
     */
    @Test
    public void testValidateIdBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateIdMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateId", IAuthenticationRequest.class);
        validateIdMethod.setAccessible(true);
        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);

        validateIdMethod.invoke(protocolProcessor, mockRequest);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#validateIssuer(IAuthenticationRequest)}
     * When issuer in request is blank
     * <p>
     * Must fail
     */
    @Test
    public void testValidateIssuer() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method validateIssuerMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("validateIssuer", IAuthenticationRequest.class);
        validateIssuerMethod.setAccessible(true);
        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);

        validateIssuerMethod.invoke(protocolProcessor, mockRequest);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#extractLevelsOfAssurance(AuthnRequest)}
     * When loa comparison is empty
     * <p>
     * Must fail
     */
    @Test
    public void testExtractLevelsOfAssuranceComparisonEmpty() throws EIDASSAMLEngineException {
        EidasProtocolProcessor mockProtocolProcessor = new EidasProtocolProcessor(metadataFetcher, metadataSigner, metadataClock);

        RequestedAuthnContextImpl mockAuthnContext = Mockito.mock(RequestedAuthnContextImpl.class);
        AuthnRequest mockRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(mockRequest.getRequestedAuthnContext()).thenReturn(mockAuthnContext);
        Mockito.when(mockAuthnContext.getComparison()).thenReturn(null);

        mockProtocolProcessor.extractLevelsOfAssurance(mockRequest);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#fillRequestedAttributes(IAuthenticationRequest, RequestedAttributes)}
     * When an exception is thrown during marshalling
     * <p>
     * Must fail
     */
    @Test
    public void testFillRequestedAttributesExceptionMarshalling() throws AttributeValueMarshallingException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        AttributeValueMarshaller mockMarshaller = Mockito.mock(AbstractPostalAddressAttributeValueMarshaller.class);
        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        RequestedAttributes mockRequestedAttributes = Mockito.mock(RequestedAttributes.class);
        AbstractAttributeValue<PostalAddress> mockAttributeValue = new PostalAddressAttributeValue(PostalAddress.builder()
                .build());
        AttributeDefinition mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) mockMarshaller)
                .friendlyName("not set").nameUri("https://test.be").personType(PersonType.NATURAL_PERSON).required(false)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("https://test.be", "eidas-natural", "not set"))
                .build();
        ImmutableAttributeMap mockAttributeMap = ImmutableAttributeMap.of(mockDefinition, mockAttributeValue);
        Mockito.when(mockMarshaller.marshal(any())).thenThrow(AttributeValueMarshallingException.class);
        Mockito.when(mockRequest.getRequestedAttributes()).thenReturn(mockAttributeMap);

        Method fillRequestedAttributesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("fillRequestedAttributes", IAuthenticationRequest.class, RequestedAttributes.class);
        fillRequestedAttributesMethod.setAccessible(true);
        fillRequestedAttributesMethod.invoke(protocolProcessor, mockRequest, mockRequestedAttributes);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#filterSupportedAttributeNames(ImmutableAttributeMap, Set, String, String)}
     * When flow is normal
     * <p>
     * Must succeed
     */
    @Test
    public void testFilterSupportedAttributeNames() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();
        AttributeDefinition<Object> mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
                .friendlyName("not set").nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier").personType(PersonType.NATURAL_PERSON).required(true)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("https://nothing.here", "eidas-natural", "not set"))
                .build();
        ImmutableAttributeMap mockRequestedAttributes = ImmutableAttributeMap.of(mockDefinition);
        Set<String> mockSupportedAttributes = new HashSet<>();
        mockSupportedAttributes.add("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier");
        String mockRequestIssuer = "mockIssuer";
        String mockServiceMetadataUrl = "https://nothing.here";

        Method filterSupportedAttributeNamesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("filterSupportedAttributeNames", ImmutableAttributeMap.class, Set.class, String.class, String.class);
        filterSupportedAttributeNamesMethod.setAccessible(true);
        ImmutableAttributeMap result = (ImmutableAttributeMap) filterSupportedAttributeNamesMethod.invoke(protocolProcessor, mockRequestedAttributes, mockSupportedAttributes, mockRequestIssuer, mockServiceMetadataUrl);
        Assert.assertEquals(result.size(), 1);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#filterSupportedAttributeNames(ImmutableAttributeMap, Set, String, String)}
     * When requested attribute is marked as required but is not supported
     * <p>
     * Must fail
     */
    @Test
    public void testFilterSupportedAttributeNamesNotSupportedButRequired() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();
        AttributeDefinition<Object> mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
                .friendlyName("not set").nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier").personType(PersonType.NATURAL_PERSON).required(true)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("https://nothing.here", "eidas-natural", "not set"))
                .build();
        ImmutableAttributeMap mockRequestedAttributes = ImmutableAttributeMap.of(mockDefinition);
        Set<String> mockSupportedAttributes = new HashSet<>();
        mockSupportedAttributes.add("https://something.else");
        String mockRequestIssuer = "mockIssuer";
        String mockServiceMetadataUrl = "https://nothing.here";

        Method filterSupportedAttributeNamesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("filterSupportedAttributeNames", ImmutableAttributeMap.class, Set.class, String.class, String.class);
        filterSupportedAttributeNamesMethod.setAccessible(true);
        filterSupportedAttributeNamesMethod.invoke(protocolProcessor, mockRequestedAttributes, mockSupportedAttributes, mockRequestIssuer, mockServiceMetadataUrl);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#filterSupportedAttributeNames(ImmutableAttributeMap, Set, String, String)}
     * When requested attribute is marked as not required and is not supported
     * <p>
     * Must succeed
     */
    @Test
    public void testFilterSupportedAttributeNamesNotSupportedNotRequired() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();
        AttributeDefinition<Object> mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
                .friendlyName("not set").nameUri("http://eidas.europa.eu/attributes/naturalperson/PersonIdentifier").personType(PersonType.NATURAL_PERSON).required(false)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("https://nothing.here", "eidas-natural", "not set"))
                .build();
        ImmutableAttributeMap mockRequestedAttributes = ImmutableAttributeMap.of(mockDefinition);
        Set<String> mockSupportedAttributes = new HashSet<>();
        mockSupportedAttributes.add("https://something.else");
        String mockRequestIssuer = "mockIssuer";
        String mockServiceMetadataUrl = "https://nothing.here";

        Method filterSupportedAttributeNamesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("filterSupportedAttributeNames", ImmutableAttributeMap.class, Set.class, String.class, String.class);
        filterSupportedAttributeNamesMethod.setAccessible(true);
        ImmutableAttributeMap result = (ImmutableAttributeMap) filterSupportedAttributeNamesMethod.invoke(protocolProcessor, mockRequestedAttributes, mockSupportedAttributes, mockRequestIssuer, mockServiceMetadataUrl);
        Assert.assertEquals(result.size(), 0);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#filterSupportedAttributeNames(ImmutableAttributeMap, Set, String, String)}
     * When attribute names are not modified
     * <p>
     * Must succeed
     */
    @Test
    public void testFilterSupportedAttributeNamesNotModified() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();
        AttributeDefinition<Object> mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
                .friendlyName("not set").nameUri("http://eidas.europa.eu/attributes/naturalperson/Optional").personType(PersonType.NATURAL_PERSON).required(false)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("https://nothing.here", "eidas-natural", "not set"))
                .build();
        ImmutableAttributeMap mockRequestedAttributes = ImmutableAttributeMap.of(mockDefinition);
        Set<String> mockSupportedAttributes = new HashSet<>();
        mockSupportedAttributes.add("http://eidas.europa.eu/attributes/naturalperson/Optional");
        String mockRequestIssuer = "mockIssuer";
        String mockServiceMetadataUrl = "https://nothing.here";

        Method filterSupportedAttributeNamesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("filterSupportedAttributeNames", ImmutableAttributeMap.class, Set.class, String.class, String.class);
        filterSupportedAttributeNamesMethod.setAccessible(true);
        ImmutableAttributeMap result = (ImmutableAttributeMap) filterSupportedAttributeNamesMethod.invoke(protocolProcessor, mockRequestedAttributes, mockSupportedAttributes, mockRequestIssuer, mockServiceMetadataUrl);
        Assert.assertEquals(1, result.size());
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRequiredAttributeCompiles(AttributeDefinition, RequestedAttribute)}
     * When required flags do not match between request and definition
     * <p>
     * Must fail
     */
    @Test
    public void testCheckRequiredAttributeCompiles() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        Method checkRequiredAttributeCompilesMethod = EidasProtocolProcessor.class
                .getDeclaredMethod("checkRequiredAttributeCompiles", AttributeDefinition.class, RequestedAttribute.class);
        checkRequiredAttributeCompilesMethod.setAccessible(true);
        RequestedAttribute mockRequestedAttribute = Mockito.mock(RequestedAttribute.class);
        AttributeValueMarshaller<?> MARSHALLER = new LiteralStringAttributeValueMarshaller();
        AttributeDefinition mockDefinition = AttributeDefinition.builder()
                .attributeValueMarshaller((AttributeValueMarshaller<Object>) MARSHALLER)
                .friendlyName("D-2012-17-EUIdentifier").nameUri("http://eidas.europa.eu/attributes/legalperson/D-2012-17-EUIdentifier").personType(PersonType.LEGAL_PERSON).required(false)
                .transliterationMandatory(false).uniqueIdentifier(false)
                .xmlType(new QName("http://eidas.europa.eu/attributes/legalperson", "D-2012-17-EUIdentifierType", "eidas-legal"))
                .build();
        Mockito.when(mockRequestedAttribute.isRequired()).thenReturn(true);

        checkRequiredAttributeCompilesMethod.invoke(protocolProcessor, mockDefinition, mockRequestedAttribute);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#getAttributeDefinitionNullable(String)}
     * When attribute name is blank
     * <p>
     * Must succeed
     */
    @Test
    public void testGetAttributeDefinitionNullable() {
        expectedException.expect(InternalErrorEIDASException.class);

        protocolProcessor = new EidasProtocolProcessor(metadataFetcher, metadataSigner, metadataClock);
        protocolProcessor.getAttributeDefinitionNullable("");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When metadata fetcher is null
     * <p>
     * Must succeed
     */
    @Test
    public void testIsAcceptableHttpRequestMetadataFetcherNull() throws NoSuchFieldException, IllegalAccessException, EIDASSAMLEngineException {
        setMetadataFetcher(null);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        Boolean isAcceptable = protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
        Assert.assertTrue(isAcceptable);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When metadata parameters are null
     * <p>
     * Must succeed
     */
    @Test
    public void testIsAcceptableHttpRequestMetadataParametersNull() throws NoSuchFieldException, IllegalAccessException, EIDASSAMLEngineException, EIDASMetadataException {
        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(null);

        Boolean isAcceptable = protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
        Assert.assertTrue(isAcceptable);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When assertionConsumerServiceUrl parameters are mismatched
     * <p>
     * Must fail
     */
    @Test
    public void testIsAcceptableHttpRequestMismatchedAssertionConsumerServiceURL() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);

        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("somethingDifferent");

        protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#filterSupportedAttributeNames(ImmutableAttributeMap, Set, String, String)}
     * When SP Type is not available
     * <p>
     * Must fail
     */
    @Test
    public void testIsAcceptableHttpRequestNoSpType() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);

        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Set<String> mockBindings = new HashSet<>();
        mockBindings.add("GET");
        Mockito.when(mockRoleParameters.getProtocolBindings()).thenReturn(mockBindings);
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("assertionConsumer");

        protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When http binding is invalid
     * <p>
     * Must succeed
     */
    @Test
    public void testIsAcceptableHttpRequestInvalidBinding() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);

        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Set<String> mockBindings = new HashSet<>();
        mockBindings.add("invalid");
        Mockito.when(mockRoleParameters.getProtocolBindings()).thenReturn(mockBindings);
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("assertionConsumer");

        protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When SP type is present both in request and in metadata
     * <p>
     * Must fail
     */
    @Test
    public void testIsAcceptableHttpRequestSPTypeInRequestAndMetadata() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);

        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Set<String> mockBindings = new HashSet<>();
        mockBindings.add("GET");
        Mockito.when(mockRoleParameters.getProtocolBindings()).thenReturn(mockBindings);
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("assertionConsumer");
        Mockito.when(mockEidasMetadataParameters.getSpType()).thenReturn(SpType.PUBLIC.toString());
        Mockito.when(mockRequest.getSpType()).thenReturn(SpType.PUBLIC.toString());

        protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * When metadataFetcher throws an exception
     * <p>
     * Must fail
     */
    @Test
    public void testIsAcceptableHttpRequestExceptionInMetadataFetcher() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);

        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);

        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenThrow(EIDASMetadataException.class);

        protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
    }

    /**
     * Test method for {@link EidasProtocolProcessor#isAcceptableHttpRequest(IAuthenticationRequest, String)}
     * <p>
     * Must succeed
     */
    @Test
    public void testIsAcceptableHttpRequest() throws EIDASSAMLEngineException, NoSuchFieldException, IllegalAccessException, EIDASMetadataException {
        MetadataFetcherI mockMetadataFetcher = Mockito.mock(MetadataFetcherI.class);

        setMetadataFetcher(mockMetadataFetcher);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(mockMetadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(mockEidasMetadataParameters);
        EidasMetadataRoleParametersI mockRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockRoleParameters));
        Mockito.when(mockRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");
        Set<String> mockBindings = new HashSet<>();
        mockBindings.add("GET");
        Mockito.when(mockRoleParameters.getProtocolBindings()).thenReturn(mockBindings);
        Mockito.when(mockRequest.getAssertionConsumerServiceURL()).thenReturn("assertionConsumer");
        Mockito.when(mockEidasMetadataParameters.getSpType()).thenReturn(SpType.PUBLIC.toString());

        Boolean result = protocolProcessor.isAcceptableHttpRequest(mockRequest, "GET");
        Assert.assertTrue(result);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * When request is not an instance of IEidasAuthenticationRequest
     * <p>
     * Must succeed
     */
    @Test
    public void testMarshallRequestNotEidasAuthenticationRequest() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);
        SamlEngineCoreProperties mockProperties = Mockito.mock(SamlEngineCoreProperties.class);
        protocolProcessor.marshallRequest(mockRequest, "issuer", mockProperties);
    }

    private Signature mockEmptySignature() {
        return mockSignature(null);
    }

    private Signature mockSignature(String signCertificateValue) {
        Signature signature = Mockito.mock(Signature.class);

        KeyInfo mockKeyInfo = Mockito.mock(KeyInfo.class);
        when(signature.getKeyInfo()).thenReturn(mockKeyInfo);
        if (signCertificateValue == null) {
            when(mockKeyInfo.getX509Datas()).thenReturn(Collections.emptyList());
        } else {
            X509Data x509Data = Mockito.mock(X509Data.class);
            when(mockKeyInfo.getX509Datas()).thenReturn(Collections.singletonList(x509Data));
            org.opensaml.xmlsec.signature.X509Certificate x509Certificate = Mockito.mock(org.opensaml.xmlsec.signature.X509Certificate.class);
            when(x509Certificate.getValue()).thenReturn(signCertificateValue);
            when(x509Data.getX509Certificates()).thenReturn(Collections.singletonList(x509Certificate));
        }

        return signature;
    }

    /**
     * Creates a {@link DefaultCoreProperties}
     *
     * @return the {@link DefaultCoreProperties} instance
     */
    private DefaultCoreProperties createDefaultCoreProperties(Map<String, String> additionalProperties) {
        Properties properties = new Properties();
        properties.setProperty(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        properties.setProperty(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        properties.setProperty("timeNotOnOrAfter", "0");
        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            for (String key : additionalProperties.keySet()) {
                properties.setProperty(key, additionalProperties.get(key));
            }
        }
        return new DefaultCoreProperties(properties);
    }

    private EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder() {
        final EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();

        eidasAuthenticationRequestBuilder
                .id(TestingConstants.SAML_ID_CONS.toString())
                .destination(TestingConstants.REQUEST_DESTINATION_CONS.toString())
                .issuer(TestingConstants.REQUEST_ISSUER_CONS.toString())
                .citizenCountryCode(TestingConstants.CITIZEN_COUNTRY_CODE_CONS.toString())
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue())
                .assertionConsumerServiceURL(TestingConstants.ASSERTION_URL_CONS.toString());

        return eidasAuthenticationRequestBuilder;
    }

    /**
     * Creates a {@link RequestedAuthnContext} without validation restrictions
     */
    private RequestedAuthnContext createRequestedAuthContext(AuthnContextComparisonTypeEnumeration comparison, String... loas) {
        RequestedAuthnContext authnContext = new RequestedAuthnContextBuilder().buildObject();
        authnContext.setComparison(comparison);
        for (String loaString : loas) {
            AuthnContextClassRef authnContextClassRef = new AuthnContextClassRefBuilder().buildObject();
            authnContextClassRef.setURI(loaString);
            authnContext.getAuthnContextClassRefs().add(authnContextClassRef);
        }
        return authnContext;
    }

    /**
     * Methods that builds a {@link IAuthenticationRequest}
     *
     * @return the {@link IAuthenticationRequest}
     */
    private IAuthenticationRequest buildIAuthenticationRequest() {

        final EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder =
                getEidasAuthenticationRequestBuilder();

        return eidasAuthenticationRequestBuilder.build();
    }

    /**
     * Methods that builds a {@link IAuthenticationResponse}
     *
     * @return the {@link IAuthenticationResponse}
     */
    private IAuthenticationResponse buildIAuthenticateResponse() {
        AuthenticationResponse.Builder eidasAuthnResponseBuilder = new AuthenticationResponse.Builder();
        eidasAuthnResponseBuilder
                .country("UK")
                .id("QDS2QFD")
                .audienceRestriction("PUBLIC")
                .inResponseTo("6E97069A1754ED")
                .failure(false)
                .statusCode(EIDASStatusCode.REQUESTER_URI.toString())
                .subStatusCode(EIDASSubStatusCode.AUTHN_FAILED_URI.toString())
                .statusMessage("TEST")
                .notBefore(new DateTime())
                .notOnOrAfter(new DateTime())
                .ipAddress("123.123.123.123")
                .issuer("http://issuer.eu")
                .levelOfAssurance(LevelOfAssurance.EIDAS_LOA_LOW)
                .subject("subject")
                .subjectNameIdFormat("subjectNameIdFormat");

        ImmutableAttributeMap.Builder mapBuilder = ImmutableAttributeMap.builder();
        mapBuilder.put(PERSON_IDENTIFIER, Collections.<AttributeValue<String>>emptySet());
        eidasAuthnResponseBuilder.attributes(mapBuilder.build());

        return eidasAuthnResponseBuilder.build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequest(String nonNotifiedLoA) {
        return getEidasAuthenticationRequestBuilder()
                .levelsOfAssurance(Arrays.asList(LevelOfAssurance.build(nonNotifiedLoA)))
                .build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequest(String notifiedLoA, String nonNotifiedLoA) {
        return getEidasAuthenticationRequestBuilder()
//                .levelOfAssurance(notifiedLoA)
//                .levelOfAssurance(nonNotifiedLoA)
                .levelsOfAssurance(Arrays.asList(LevelOfAssurance.build(notifiedLoA), LevelOfAssurance.build(nonNotifiedLoA)))
                .build();
    }

    private AuthnRequest buildAuthnRequest(IEidasAuthenticationRequest eidasAuthnRequest) throws EIDASSAMLEngineException {
        ProtocolProcessorI eidasProtocolProcessor = protocolEngine.getProtocolProcessor();
        return eidasProtocolProcessor.marshallRequest(eidasAuthnRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);
    }

    private AuthnRequest buildAuthnRequestWithTrueRequesterIdFlag(IEidasAuthenticationRequest eidasAuthnRequest) throws EIDASSAMLEngineException {
        ProtocolProcessorI eidasProtocolProcessor = protocolEngineRequesterId.getProtocolProcessor();
        return eidasProtocolProcessor.marshallRequest(eidasAuthnRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);
    }

    private AuthnRequest buildAuthnRequestWithMetadataFetcherThrowsException(IEidasAuthenticationRequest eidasAuthnRequest) throws EIDASSAMLEngineException {
        ProtocolProcessorI eidasProtocolProcessor = protocolEngineMetadataFetcherThrowsException.getProtocolProcessor();
        return eidasProtocolProcessor.marshallRequest(eidasAuthnRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);
    }

    private void assertUnmarshalledRequestContainsExpectedRequesterID(String expectedRequesterId, String actualRequesterId) {
        Assert.assertEquals(expectedRequesterId, actualRequesterId);
    }

    private void assertLoaComparisonIsExact(RequestedAuthnContext actualRequestedAuthnContext) {
        Assert.assertTrue(actualRequestedAuthnContext.getComparison().toString().equalsIgnoreCase("exact"));
    }

    private void assertNumberLoasInAuthnContextClassRefsIsOne(final RequestedAuthnContext actualRequestedAuthnContext) {
        final int expectedNumberOfNotifiedLoas = 1;
        assertNumberLoasInAuthnContextClassRefs(expectedNumberOfNotifiedLoas, actualRequestedAuthnContext);
    }

    private void assertNumberLoasInAuthnContextClassRefsIsTwo(final RequestedAuthnContext actualRequestedAuthnContext) {
        final int expectedNumberOfNotifiedLoas = 2;
        assertNumberLoasInAuthnContextClassRefs(expectedNumberOfNotifiedLoas, actualRequestedAuthnContext);
    }

    private void assertNumberLoasInAuthnContextClassRefsIsThree(final RequestedAuthnContext actualRequestedAuthnContext) {
        final int expectedNumberOfNotifiedLoas = 3;
        assertNumberLoasInAuthnContextClassRefs(expectedNumberOfNotifiedLoas, actualRequestedAuthnContext);
    }

    private void assertNumberLoasInAuthnContextClassRefs(final int expectedNumberOfNotifiedLoas, final RequestedAuthnContext actualRequestedAuthnContext) {
        final int actualNumberNotifiedLoas = actualRequestedAuthnContext.getAuthnContextClassRefs().size();
        Assert.assertEquals(expectedNumberOfNotifiedLoas, actualNumberNotifiedLoas);
    }

    private void assertNumberLoasInAuthnContextDeclRefsIsZero(final RequestedAuthnContext actualRequestedAuthnContext) {
        final int expectedNumberNonNotifiedLoas = 0;
        assertNumberLoasInAuthnContextDeclRefs(expectedNumberNonNotifiedLoas, actualRequestedAuthnContext);
    }

    private void assertNumberLoasInAuthnContextDeclRefs(final int expectedNumberNonNotifiedLoas, final RequestedAuthnContext actualRequestedAuthnContext) {
        final int actualNumberNonNotifiedLoas = actualRequestedAuthnContext.getAuthnContextDeclRefs().size();
        Assert.assertEquals(expectedNumberNonNotifiedLoas, actualNumberNonNotifiedLoas);
    }

    private void assertFirstRequesterId(List<RequesterID> requesterIDs, String expectedRequesterId) {
        String actualRequesterID = requesterIDs.get(0).getRequesterID();
        Assert.assertEquals(expectedRequesterId, actualRequesterID);
    }

    private void assertOneRequesterId(int actualNumberRequesterIDs) {
        Assert.assertEquals(1, actualNumberRequesterIDs);
    }

    private String getFirstLoa(RequestedAuthnContext actualRequestedAuthnContext) {
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(0).getURI();
    }

    private String getSecondLoa(RequestedAuthnContext actualRequestedAuthnContext) {
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(1).getURI();
    }

    private String getThirdLoa(RequestedAuthnContext actualRequestedAuthnContext) {
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(2).getURI();
    }

    private AuthnRequest buildAuthnRequest(String firstLoaToAdd, String secondLoaToAdd) throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest eidasAuthnRequest = buildiEidasAuthenticationRequest(firstLoaToAdd, secondLoaToAdd);
        AuthnRequest authnRequest = buildAuthnRequest(eidasAuthnRequest);

        return authnRequest;
    }

    private AuthnRequest buildAuthnRequest(String loaToAdd) throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest eidasAuthnRequest = buildiEidasAuthenticationRequest(loaToAdd);
        AuthnRequest authnRequest = buildAuthnRequest(eidasAuthnRequest);

        return authnRequest;
    }

    private AuthnRequest buildAuthnRequestRequesterIdWithRequesterIdFlagSet() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .requesterId(SP_DEMO_REQUESTER_ID)
                .spType(SpType.PRIVATE.getValue())
                .build();

        return buildAuthnRequestWithTrueRequesterIdFlag(iEidasAuthenticationRequest);
    }

    private AuthnRequest buildAuthnRequestRequesterIdWithRequesterIdFlagNotSet() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .requesterId(SP_DEMO_REQUESTER_ID)
                .build();

        return buildAuthnRequest(iEidasAuthenticationRequest);
    }

    private AuthnRequest buildAuthnRequestEmptyRequesterIdWithTrueRequesterIdFlag() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .build();

        return buildAuthnRequestWithTrueRequesterIdFlag(iEidasAuthenticationRequest);
    }

    private AuthnRequest buildAuthnRequestEmptyRequesterIdWithFalseRequesterIdFlag() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .build();

        return buildAuthnRequest(iEidasAuthenticationRequest);
    }

    private AuthnRequest buildAuthnRequestMetadataFetcherThrowsException() throws EIDASSAMLEngineException {
        IEidasAuthenticationRequest iEidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .build();

        return buildAuthnRequestWithMetadataFetcherThrowsException(iEidasAuthenticationRequest);
    }

    private IEidasAuthenticationRequest buildDummyEidasAuthenticationRequest() {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .providerName("DEMO_SP")
                .build();
    }

    private IEidasAuthenticationRequest buildDummyEidasAuthenticationRequestWithAttributes(ImmutableAttributeMap attributeMap) {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .providerName("DEMO_SP")
                .requestedAttributes(attributeMap)
                .build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithNorEmptyRequesterId(String expectedRequesterId) {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .requesterId(expectedRequesterId)
                .providerName("DEMO_SP")
                .spType(SpType.PRIVATE.getValue())
                .build();
    }

    private void setMetadataFetcher(MetadataFetcherI fetcher) throws NoSuchFieldException, IllegalAccessException {
        Field metaDataFetcherField = EidasProtocolProcessor.class.getDeclaredField("metadataFetcher");
        metaDataFetcherField.setAccessible(true);
        metaDataFetcherField.set(protocolProcessor, fetcher);
    }
}