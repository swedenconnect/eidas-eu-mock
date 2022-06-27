/*
 * Copyright (c) 2020 by European Commission
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

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
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
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml.saml2.core.RequesterID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Scoping;
import org.opensaml.saml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Data;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.mockito.Mockito.when;

/**
 *
 * Test class for {@link EidasProtocolProcessor}
 *
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


    private static ProtocolEngineI protocolEngine = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceProtocolVersioning");

    private static ProtocolEngineI protocolEngineRequesterId
            = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceRequesterId");

    private static ProtocolEngineI protocolEngineMetadataFetcherThrowsException
            = ProtocolEngineFactory.getDefaultProtocolEngine("ServiceMetadataFetcherThrowsException");

    private final static String PROXY_SERVICE = "proxyService";

    private IAuthenticationRequest iAuthenticationRequest = buildIAuthenticationRequest();

    private IAuthenticationResponse iAuthenticationResponse = buildIAuthenticateResponse();

    private String ipAddress = "128.128.128.128";

    private DefaultCoreProperties defaultCoreProperties = createDefaultCoreProperties();

    private DateTime currentTime = DateTime.now();

    private ProtocolProcessorI protocolProcessor;

    private MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
    private MetadataSignerI metadataSigner = Mockito.mock(MetadataSignerI.class);
    private MetadataClockI metadataClock = Mockito.mock(MetadataClockI.class);

    private String SP_DEMO_REQUESTER_ID = "http://localhost:8080/SP";

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
     * Successful scenario
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
     *
     * Successful scenario
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
     *
     * Successful scenario
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
     *
     * Successful scenario
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
     *
     * Successful scenario
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
     *
     * Successful scenario
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
     *
     * Must succeed
     */
    @Test
    public void testGetMetadataProtocolVersions() throws EIDASMetadataException {
        String metadataUrl = "testUrl";
        List<String> protocolVersionsList = Arrays.asList("1.2","1.1");
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(metadataFetcher.getEidasMetadata(metadataUrl, metadataSigner, metadataClock))
                .thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersionsList);

        List<EidasProtocolVersion> actualProtocolVersions = protocolProcessor.getMetadataProtocolVersions(metadataUrl);

        List<EidasProtocolVersion> expectedProtocolVersions =
                Arrays.asList(EidasProtocolVersion.PROTOCOL_VERSION_1_2, EidasProtocolVersion.PROTOCOL_VERSION_1_1);
        Assert.assertEquals(expectedProtocolVersions, actualProtocolVersions);
    }

    /**
     * Test method for  {@link EidasProtocolProcessor#getMetadataProtocolVersions(String)}
     * when the metadata are not having eidas protocol versions
     *
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
     * In other words, it is not a representation use case
     *
     * Successful scenario
     */
    @Test
    public void testCheckRepresentationResponseValidNotRepresentationCase() {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentationResponse(ImmutableAttributeMap)}
     * when the attributes from the response matches with a representation case
     * with the 2 Minimum Data Sets (MDS) needed
     *
     * Successful scenario
     */
    @Test
    public void testCheckRepresentationResponseValid() {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertTrue(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentationResponse(ImmutableAttributeMap)}
     * when the attributes from the response matches with a representation response
     * but with 3 Minimum Data Sets (MDS).
     *
     * Must succeed - error scenario
     */
    @Test
    public void testCheckRepresentationResponseInvalidTooManyMDS() {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeDefinition repvLegalIdentifier = EidasSpec.Definitions.REPV_LEGAL_PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .put(repvLegalIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * when the requested attributes contain a representative attribute
     * <p>
     * Must return false
     */
    @Test
    public void checkRepresentativeAttributesIncludesRepresentative() {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentativeAttributes(attributesMap);

        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentativeAttributes(ImmutableAttributeMap)}
     * when the requested attributes  do not contain a representative attribute
     * with the 2 Minimum Data Sets (MDS) needed
     * <p>
     * Must return true
     */
    @Test
    public void checkRepresentativeAttributesNoRepresentative() {
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentativeAttributes(attributesMap);

        Assert.assertTrue(actualResult);
    }


    /**
     * Test method for {@link EidasProtocolProcessor#checkRepresentationResponse(ImmutableAttributeMap)}
     * when the attributes from the response matches with a representation response
     * but with only one Minimum Data Sets (MDS).
     * <p>
     * Must succeed - error scenario
     */
    @Test
    public void testCheckRepresentationResponseInvalidTooFewMDS() {
        AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(repvIdentifier, stringValue)
                .build();

        boolean actualResult = protocolProcessor.checkRepresentationResponse(attributesMap);

        Assert.assertFalse(actualResult);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing only one level of assurance
     * and level of assurance is a notified level of assurance
     *
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing only one level of assurance
     * and level of assurance is a nonNotified level of assurance
     *
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing multiple levels of assurance
     * with a high notified level of assurance and a nonNotified level of assurance
     *
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when request is containing multiple levels of assurance
     * with a substantial notified level of assurance and a nonNotified level of assurance
     *
     * The marshalled request should contain the High notified level of assurance as well since comparison type will be
     * equivalent to EXACT due to the presence of the Non notified level of assurance.
     *
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and
     * when {@link EidasMetadataParametersI#isRequesterIdFlag()} is true
     * see {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     *
     * Must succeed and eiDAS request must have Scoping with RequesterId
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

        String expectedRequesterId = SP_DEMO_REQUESTER_ID;
        assertFirstRequesterId(requesterIDs, expectedRequesterId);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and
     * when {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * see {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     *
     * Must succeed and eiDAS request must not have Scoping with RequesterId
     */
    @Test
    public void marshallRequestNOtEmptyRequesterIdWithFalseRequesterIdFlag() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestRequesterIdWithRequesterIdFlagNotSet();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is not empty and
     * when {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * see {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     *
     * Must succeed and eiDAS request must not have Scoping with RequesterId
     */
    @Test
    public void marshallRequestNotEmptyRequesterIdWithFalseRequesterIdFlag() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestEmptyRequesterIdWithTrueRequesterIdFlag();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link IAuthenticationRequest#getRequesterId()} is  empty and
     * when {@link EidasMetadataParametersI#isRequesterIdFlag()} is false
     * see {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     *
     * Must succeed and eiDAS request must not have Scoping with RequesterId
     */
    @Test
    public void marshallRequestEmptyRequesterIdWithFlaseNotSetRequesterId() throws EIDASSAMLEngineException {
        AuthnRequest authnRequest = buildAuthnRequestEmptyRequesterIdWithFalseRequesterIdFlag();
        Assert.assertNotNull(authnRequest);

        Scoping scoping = authnRequest.getScoping();
        Assert.assertNull(scoping);
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallRequest(IEidasAuthenticationRequest, String, SamlEngineCoreProperties, DateTime)}
     * when {@link TestMetadataFetcher#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * used by protocol engine throws exceptions
     * <p>
     * Must fail and throw EIDASSAMLEngineException
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
     *
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
     *
     * <p/>
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
     * <p/>
     * Must fail.
     */
    @Test
    public void unmarshallRequestwithInvalidSyntaxLevelOfAssurance() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage("LevelOfAssurance URI missing protocol prefix");
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
        AttributeDefinition identifier = EidasSpec.Definitions.PERSON_IDENTIFIER;
        AttributeDefinition repvIdentifier = EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
        AttributeValue stringValue = new StringAttributeValue("1234 4321");

        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.builder()
                .put(identifier, stringValue)
                .put(repvIdentifier, stringValue)
                .build();

        IEidasAuthenticationRequest iEidasAuthenticationRequest = buildDummyEidasAuthenticationRequestWithAttributes(attributesMap);

        final AuthnRequest authnRequest = eidasProtocolProcessor.marshallRequest(iEidasAuthenticationRequest, PROXY_SERVICE,
                defaultCoreProperties, currentTime);

        final IAuthenticationRequest iAuthenticationRequest = eidasProtocolProcessor.unmarshallRequest("CA", authnRequest, "CA");
    }

    /**
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier matches the one where assertion must be included in the reponse.
     * <p/>
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier doe not matches the one where assertion must be included in the response.
     * <p/>
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is null.
     * <p/>
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
     * Test method for
     * {@link EidasProtocolProcessor#marshallErrorResponse(IAuthenticationRequest, IAuthenticationResponse, String, SamlEngineCoreProperties, DateTime, List)}
     * when the application identifier list is empty.
     * <p/>
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
    private DefaultCoreProperties createDefaultCoreProperties() {
        Properties properties = new Properties();
        properties.setProperty(SAMLCore.PROT_BINDING_TAG.getValue(), "HTTP-POST");
        properties.setProperty(SAMLCore.CONSENT_AUTHN_RES.getValue(), "obtained");
        properties.setProperty("timeNotOnOrAfter", "0");
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
            authnContextClassRef.setAuthnContextClassRef(loaString);
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
                .issuer("issuer")
                .levelOfAssurance("assuranceLevel")
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
        Assert.assertEquals(1,actualNumberRequesterIDs);
    }

    private String getFirstLoa(RequestedAuthnContext actualRequestedAuthnContext){
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
    }

    private String getSecondLoa(RequestedAuthnContext actualRequestedAuthnContext){
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(1).getAuthnContextClassRef();
    }

    private String getThirdLoa(RequestedAuthnContext actualRequestedAuthnContext){
        return actualRequestedAuthnContext.getAuthnContextClassRefs().get(2).getAuthnContextClassRef();
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

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithNonNotifiedLoa() {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance("nonNotified")
                .levelOfAssuranceComparison("exact")
                .providerName("DEMO_SP")
                .build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithNotifiedAndNonNotifiedLoa() {
        return getEidasAuthenticationRequestBuilder()
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW)
                .levelOfAssurance("nonNotified")
                .levelOfAssuranceComparison("exact")
                .providerName("DEMO_SP")
                .build();
    }

    private IEidasAuthenticationRequest buildiEidasAuthenticationRequestWithEmptyLoa() throws NoSuchFieldException, IllegalAccessException {
        EidasAuthenticationRequest eidasAuthenticationRequest = getEidasAuthenticationRequestBuilder()
                .levelsOfAssurance(Collections.emptyList())
                .levelOfAssuranceComparison("exact")
                .providerName("DEMO_SP")
                .build();

        return eidasAuthenticationRequest;
    }
}