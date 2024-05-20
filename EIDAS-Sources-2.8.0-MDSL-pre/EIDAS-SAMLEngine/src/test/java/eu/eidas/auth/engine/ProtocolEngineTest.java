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
package eu.eidas.auth.engine;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.FakeMetadata;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.EidasMetadataParameters;
import eu.eidas.auth.engine.xml.opensaml.CorrelatedResponse;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import net.shibboleth.utilities.java.support.logic.ConstraintViolationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Test class for {@link ProtocolEngine}
 */
public class ProtocolEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolEngineTest.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private final MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
    private final MetadataSignerI metadataSigner = Mockito.mock(MetadataSignerI.class);
    private final MetadataClockI metadataClock = Mockito.mock(MetadataClockI.class);
    private ProtocolEngineConfiguration defaultProtocolEngineConfiguration;
    private ProtocolConfigurationAccessor mockProtocolConfigurationAccessor;
    private EidasProtocolProcessor protocolProcessor;
    private ProtocolEngine protocolEngine;

    protected String getEngineConfigurationInstance(){
        return "METADATATEST"; // sha512-rsa-MGF1 for signature algorithm
    }

    /**
     * Initialize OpenSaml context.
     */
    @BeforeClass
    public static void setUpClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        OpenSamlHelper.initialize();
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Before
    public void setup() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ProtocolEngineConfigurationException, EIDASMetadataException {
        defaultProtocolEngineConfiguration = DefaultProtocolEngineConfigurationFactory.getInstance().getConfiguration(getEngineConfigurationInstance());
        protocolProcessor = new EidasProtocolProcessor(metadataFetcher, metadataSigner, metadataClock);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .protocolProcessor(protocolProcessor).build();

        mockProtocolConfigurationAccessor = Mockito.mock(ProtocolConfigurationAccessor.class);
        protocolEngine = new ProtocolEngine(mockProtocolConfigurationAccessor);
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateRequestMessage(IAuthenticationRequest, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void generateRequestMessage() throws EIDASSAMLEngineException, EIDASMetadataException {
        final EidasMetadataParametersI params = FakeMetadata.proxyService();
        params.setRequesterIdFlag(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();

        final IRequestMessage requestMessage = protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");

        Assert.assertNotNull(requestMessage);
        final IAuthenticationRequest authenticationRequest = requestMessage.getRequest();
        Assert.assertEquals(request.getDestination(), authenticationRequest.getDestination());
        Assert.assertEquals("https://source.europa.eu/metadata", authenticationRequest.getIssuer());
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateRequestMessage(IAuthenticationRequest, String)}
     * when the {@link IAuthenticationRequest} is null,
     *
     * <p>
     * Must fail
     * and throw {@link EIDASSAMLEngineException}
     */
    @Test
    public void generateRequestMessageNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INTERNAL_ERROR.errorMessage());

        final EidasAuthenticationRequest request = null;
        protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateRequestMessage(IAuthenticationRequest, String)}
     * when the {@link ProtocolEngine#signAndMarshallRequest(AuthnRequest)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw {@link EIDASSAMLEngineException}
     */
    @Test
    public void generateRequestMessageTryCatchEIDASSAMLEngineException() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INTERNAL_ERROR.errorMessage());

        final ProtocolSignerI signer = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(signer.sign(any())).thenThrow(EIDASSAMLEngineException.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .signer(signer).build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final EidasMetadataParametersI params = FakeMetadata.proxyService();
        params.setRequesterIdFlag(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);
        protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void generateResponseMessage() throws EIDASSAMLEngineException, EIDASMetadataException {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.connector());
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        Assert.assertNotNull(responseMessage);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when the engine configuration is set to sign the assertions of an {@link IAuthenticationResponse}
     * but the {@link IAuthenticationResponse} does not have a DOM representation
     * <p>
     * Must fail
     * and throw {@link ConstraintViolationException}
     */
    @Test
    public void generateResponseMessageResponseSignAssertionsEnabledResponseDomNull() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(ConstraintViolationException.class);

        final ProtocolSignerI signer = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(signer.isResponseSignAssertions()).thenReturn(true);
        Mockito.when(signer.sign(any())).thenReturn(Mockito.mock(Assertion.class));
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .signer(signer).build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        Mockito.when(metadataFetcher.getEidasMetadata(eq(response.getIssuer()), any(), any())).thenReturn(FakeMetadata.proxyService());
        Mockito.when(metadataFetcher.getEidasMetadata(eq(request.getIssuer()), any(), any())).thenReturn(FakeMetadata.connector());

        protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#encryptAndSignAndMarshallResponse(IAuthenticationRequest, IAuthenticationResponse, Response)}
     * when {@link ProtocolEngine#signAndMarshallResponse(IAuthenticationRequest, Response)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw {@link EIDASSAMLEngineException}
     */
    @Test
    public void encryptAndSignAndMarshallResponseTryCatchEIDASSAMLEngineException() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INTERNAL_ERROR.errorMessage());

        final ProtocolSignerI signer = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(signer.isResponseSignAssertions()).thenReturn(true);
        Mockito.when(signer.sign(any(), any())).thenReturn(Mockito.mock(Assertion.class));

        final ProtocolEncrypterI cipher = Mockito.mock(ProtocolEncrypterI.class);
        Mockito.when(cipher.isResponseEncryptionMandatory()).thenReturn(true);
        Mockito.when(cipher.isEncryptionEnabled(anyString())).thenReturn(true);

        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .cipher(cipher)
                .signer(signer)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().originCountryCode("eu").build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.connector());

        protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallRequest(byte[])}
     * convert xml into an {@link AuthnRequest}
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshallRequest() throws EIDASSAMLEngineException, EIDASMetadataException {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final EidasMetadataParametersI params = FakeMetadata.proxyService();
        params.setRequesterIdFlag(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);
        final IRequestMessage requestMessage = protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
        final byte[] rawMessage = requestMessage.getMessageBytes();

        AuthnRequest result = protocolEngine.unmarshallRequest(rawMessage);

        Assert.assertNotNull(result);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallRequest(byte[])}
     * when the requestBytes are null
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void unmarshallRequestMessageBytesNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());
        final byte[] rawMessage = null;
        protocolEngine.unmarshallRequest(rawMessage);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallRequestAndValidate(byte[], String)}
     * convert and validate xml into an {@link AuthnRequest}
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshallRequestAndValidate() throws EIDASSAMLEngineException, EIDASMetadataException {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final EidasMetadataParametersI params = FakeMetadata.proxyService();
        params.setRequesterIdFlag(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);
        final IRequestMessage requestMessage = protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
        final byte[] rawMessage = requestMessage.getMessageBytes();
        final AuthnRequest authnRequest = protocolEngine.unmarshallRequest(rawMessage);
        Assert.assertNotNull(authnRequest);
        Assert.assertEquals(request.getIssuer(), authnRequest.getIssuer().getValue());

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.connector());
        protocolEngine.unmarshallRequestAndValidate(rawMessage, "BE");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallRequestAndValidate(byte[], String)}
     * when the requestBytes are null,
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void unmarshallRequestAndValidateRequestIsNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final byte[] binarySamlRequest = null;

        protocolEngine.unmarshallRequestAndValidate(binarySamlRequest, "BE");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallRequestAndValidate(byte[], String)}
     * when the requestBytes are to large in byte size,
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void unmarshallRequestAndValidateRequestIsTooLarge() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage("SAML AuthnRequest exceeds max size.");

        final byte[] binarySamlRequest = new byte[1000000];
        new Random().nextBytes(binarySamlRequest);

        protocolEngine.unmarshallRequestAndValidate(binarySamlRequest, "BE");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallResponse(byte[])}
     * convert and validate xml into a {@link CorrelatedResponse}
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshallResponse() throws Exception {
        final String ISSUER = "https://source.europa.eu/metadata";
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder()
                .issuer(ISSUER)
                .assertionConsumerServiceURL(ISSUER)
                .build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.connector());
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        final Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        final Correlated correlated = protocolEngine.unmarshallResponse(responseMessage.getMessageBytes());

        final CorrelatedResponse correlatedResponse = (CorrelatedResponse) correlated;
        Assert.assertEquals(response.getIssuer(), correlatedResponse.getResponse().getIssuer().getValue());
        Assert.assertEquals(response.getStatusCode(), correlatedResponse.getResponse().getStatus().getStatusCode().getValue());
    }

    /**
     * Test method for
     * {@link ProtocolEngine#unmarshallResponse(byte[])}
     * when the responseBytes are null,
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void unmarshallResponseIsNull() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        protocolEngine.unmarshallResponse(null);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignature(AuthnRequest)}
     * when the {@link AuthnRequest} claims to have no signature
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureNoSignature() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(authnRequest.isSigned()).thenReturn(false);
        Mockito.when(authnRequest.getSignature()).thenReturn(Mockito.mock(Signature.class));

        validateSignature(authnRequest);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignature(AuthnRequest)}
     * when the {@link AuthnRequest} claims to have a signature but cannot provide it
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureNullSignature() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(authnRequest.isSigned()).thenReturn(true);
        Mockito.when(authnRequest.getSignature()).thenReturn(null);

        validateSignature(authnRequest);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignature(AuthnRequest)}
     * when the {@link AuthnRequest} cannot provide the {@link Issuer}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureNoIssuer() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(authnRequest.isSigned()).thenReturn(true);
        Mockito.when(authnRequest.getSignature()).thenReturn(Mockito.mock(Signature.class));
        Mockito.when(authnRequest.getIssuer()).thenReturn(null);

        validateSignature(authnRequest);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignature(AuthnRequest)}
     * when {@link EidasProtocolProcessor#getResponseSignatureCertificate} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureTryCatchEidasSamlEngineException() throws Exception {
        expectedException.expect(EIDASSAMLEngineException.class);
//        expectedException.expectMessage(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorCode());

        final SamlEngineCoreProperties samlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        final EidasProtocolProcessor eidasProtocolProcessor = Mockito.mock(EidasProtocolProcessor.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .coreProperties(samlEngineCoreProperties)
                .protocolProcessor(eidasProtocolProcessor)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final AuthnRequest authnRequest = Mockito.mock(AuthnRequest.class);
        Mockito.when(authnRequest.isSigned()).thenReturn(true);
        Mockito.when(authnRequest.getSignature()).thenReturn(Mockito.mock(Signature.class));
        Mockito.when(authnRequest.getIssuer()).thenReturn(Mockito.mock(Issuer.class));
        Mockito.when(samlEngineCoreProperties.isValidateSignature()).thenReturn(true);
        Mockito.when(eidasProtocolProcessor.getMetadataParameters(any())).thenThrow(EIDASSAMLEngineException.class);

        validateSignature(authnRequest);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateRequestWithValidatorSuite(AuthnRequest)}
     * when a request is generated with an Issuer NameIDFormat that is not allowed
     * the {@link ProtocolEngine#validateRequestWithValidatorSuite(AuthnRequest)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateRequestWithValidatorSuiteWrongIssuerNameIDFormat() throws EIDASMetadataException, EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final SamlEngineCoreProperties samlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .coreProperties(samlEngineCoreProperties)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final EidasMetadataParametersI params = FakeMetadata.proxyService();
        params.setRequesterIdFlag(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);

        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        Mockito.when(samlEngineCoreProperties.getFormatEntity())
                .thenReturn("invalid:NameIdFormat:For:issuer");
        final IRequestMessage requestMessage = protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
        final byte[] rawMessage = requestMessage.getMessageBytes();
        Mockito.verify(samlEngineCoreProperties, Mockito.times(1)).getFormatEntity();

        Mockito.when(samlEngineCoreProperties.isValidateSignature())
                .thenReturn(false);
        protocolEngine.unmarshallRequestAndValidate(rawMessage, "be");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateResponseWithValidatorSuite(Response)}
     * when a response has multiple assertions
     * the {@link ProtocolEngine#validateResponseWithValidatorSuite(Response)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateResponseWithValidatorSuiteMultipleAssertions() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getStatus().getStatusCode().getValue()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());
        Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(
                Mockito.mock(Assertion.class),
                Mockito.mock(Assertion.class)
        ));

        validateResponseWithValidatorSuite(response);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignatureAndDecryptAndValidateAssertionSignatures(Response)}
     * when the {@link AuthnRequest} claims to have no signature
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureAndDecryptAndValidateAssertionSignaturesValidateSignatureEnabledNoSignature() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.isSigned()).thenReturn(false);
        validateSignatureAndDecryptAndValidateAssertionSignatures(responseMock);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignatureAndDecryptAndValidateAssertionSignatures(Response)}
     * when {@link ProtocolEngine#validateSignatureAndDecrypt(Response)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureAndDecryptAndValidateAssertionSignaturesValidateSignatureDisabled() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final SamlEngineCoreProperties samlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        Mockito.when(samlEngineCoreProperties.isValidateSignature())
                .thenReturn(false);
        final EidasProtocolProcessor eidasProtocolProcessor = Mockito.mock(EidasProtocolProcessor.class);
        Mockito.when(eidasProtocolProcessor.getMetadataParameters(any()))
                .thenThrow(EIDASSAMLEngineException.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .coreProperties(samlEngineCoreProperties)
                .protocolProcessor(eidasProtocolProcessor)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);
        final Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.getIssuer()).thenReturn(Mockito.mock(Issuer.class));

        validateSignatureAndDecryptAndValidateAssertionSignatures(responseMock);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignatureAndDecryptAndValidateAssertionSignatures(Response)}
     * when {@link ProtocolEngine#validateSignatureAndDecrypt(Response)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureAndDecryptAndValidateAssertionSignaturesIssuerMissing() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.isSigned()).thenReturn(true);
        Mockito.when(responseMock.getSignature()).thenReturn(Mockito.mock(Signature.class));
        Mockito.when(responseMock.getIssuer()).thenReturn(null);

        validateSignatureAndDecryptAndValidateAssertionSignatures(responseMock);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateSignatureAndDecryptAndValidateAssertionSignatures(Response)}
     * when the {@link AuthnRequest} cannot provide the {@link Issuer}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateSignatureAndDecryptAndValidateAssertionSignatures() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final Response responseMock = Mockito.mock(Response.class);
        Mockito.when(responseMock.isSigned()).thenReturn(true);
        Mockito.when(responseMock.getSignature()).thenReturn(Mockito.mock(Signature.class));
        Mockito.when(responseMock.getIssuer()).thenReturn(null);
        validateSignatureAndDecryptAndValidateAssertionSignatures(responseMock);
    }


    /**
     * Test method for
     * {@link ProtocolEngine#validateUnmarshalledResponse(Correlated, String, long, long, String)}
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshallResponseAndValidate() throws Exception {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        Mockito.when(metadataFetcher.getEidasMetadata(anyString(), any(), any())).thenReturn(FakeMetadata.connector());
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        final Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
        Mockito.when(metadataFetcher.getEidasMetadata(anyString(), any(), any())).thenReturn(FakeMetadata.proxyService());
        final Correlated correlated = protocolEngine.unmarshallResponse(responseMessage.getMessageBytes());

        final IAuthenticationResponse authenticationResponse = protocolEngine.validateUnmarshalledResponse(
                correlated,
                "127.0.0.1",
                0L,
                0L,
                null);
        assertFalse(authenticationResponse.getStatus().isFailure());
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateAssertionSignatures(Correlated, String, long, long, String)}
     * when signatureCertificate is defined
     * <p>
     * Must succeed.
     */
    @Test
    public void validateAssertionSignaturesWithTrustedCertificates() throws EIDASSAMLEngineException {
        final EidasProtocolProcessor eidasProtocolProcessor = Mockito.mock(EidasProtocolProcessor.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(eidasProtocolProcessor.getMetadataParameters(any()).getResponseSignatureCertificate())
                .thenReturn(Mockito.mock(X509Certificate.class));
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .protocolProcessor(eidasProtocolProcessor)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final Assertion assertion = Mockito.mock(Assertion.class);
        Mockito.when(assertion.isSigned()).thenReturn(false);

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getStatus().getStatusCode().getValue()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());
        Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(assertion));

        validateAssertionSignatures(response);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#validateAssertionSignatures(Response)}
     * when {@link ProtocolEngine#validateSignature(AuthnRequest)} throws {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw an {@link EIDASSAMLEngineException}
     */
    @Test
    public void validateAssertionSignaturesTryCatchEIDASSAMLEngineException() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.INVALID_ASSERTION_SIGNATURE.errorMessage());

        final Assertion assertion = Mockito.mock(Assertion.class);
        Mockito.when(assertion.isSigned()).thenReturn(true);
        Mockito.when(assertion.getSignature()).thenReturn(Mockito.mock(Signature.class));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getStatus().getStatusCode().getValue()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());
        Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(assertion));

        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(FakeMetadata.proxyService());
        validateAssertionSignatures(response);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains an invalid Key Transport algorithm for 1.4 spec
     * and the protocol version contains 1.4 value.
     * <p>
     * <p>
     * Must fail
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithInvalidAlgorithmAndProtocolVersion1_4() throws EIDASMetadataException, EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_transport_mgf1p.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        final List<String> protocolVersions = new ArrayList<>();
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_3.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_4.toString());

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains an invalid Key Transport algorithm for 1.4 spec
     * and the protocol version is empty.
     * <p>
     * <p>
     * Must fail
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithInvalidAlgorithmAndEmptyProtocolVersions() throws EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_transport_mgf1p.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(Collections.emptyList());

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains a valid Key Transport algorithm for 1.4 spec
     * and the protocol version contains 1.4 value.
     * <p>
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithValidAlgorithmAndProtocolVersion1_4() throws EIDASSAMLEngineException, EIDASMetadataException {
        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_transport_rsa-oaep.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        final List<String> protocolVersions = new ArrayList<>();
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_3.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_4.toString());

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains a valid Key Transport algorithm before 1.4 spec
     * and the protocol version contains 1.2 and 1.3 values.
     * <p>
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithValidAlgorithmAndProtocolVersion1_2and1_3() throws EIDASSAMLEngineException, EIDASMetadataException {
        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_transport_mgf1p.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        final List<String> protocolVersions = new ArrayList<>();
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_3.toString());

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains a valid Key Transport algorithm for 1.4 spec
     * and the protocol version is empty.
     * <p>
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithValidAlgorithmAndEmptyProtocolVersions() throws EIDASSAMLEngineException, EIDASMetadataException {
        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_transport_rsa-oaep.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(Collections.emptyList());

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains a valid Key Agreement algorithm for 1.4 spec
     * and the protocol version contains 1.4 value.
     * <p>
     * <p>
     * Must succeed
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithValidAgreementAlgorithmAndProtocolVersion1_4() throws EIDASMetadataException, EIDASSAMLEngineException {
        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_agreement_kw_aes256.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        final List<String> protocolVersions = new ArrayList<>();
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_3.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_4.toString());

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     * when the provided SAML response contains an invalid Key Agreement algorithm for 1.4 spec
     * and the protocol version contains 1.4 value.
     * <p>
     * <p>
     * Must fail
     */
    @Test
    public void testCheckKeyTransportAlgorithmWithInvalidAgreementAlgorithmAndProtocolVersion1_4x() throws EIDASMetadataException, EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        final String issuer = "proxyIssuer";
        final Response response = createResponse("src/test/resources/responses/saml_response_key_agreement_mgf1p.xml");
        final EidasMetadataParameters mockEidasMetadataParameters = Mockito.mock(EidasMetadataParameters.class);

        final List<String> protocolVersions = new ArrayList<>();
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_2.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_3.toString());
        protocolVersions.add(EidasProtocolVersion.PROTOCOL_VERSION_1_4.toString());

        Mockito.when(metadataFetcher.getEidasMetadata(issuer, metadataSigner, metadataClock)).thenReturn(mockEidasMetadataParameters);
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        checkKeyTransportAlgorithmMethod(response, issuer);
    }

    /**
     * Reflection for * {@link ProtocolEngine#validateSignatureAndDecryptAndValidateAssertionSignatures(Response)}
     */
    private Response validateSignatureAndDecryptAndValidateAssertionSignatures(Response response) throws EIDASSAMLEngineException {
        try {
            final Method method = ProtocolEngine.class.getDeclaredMethod("validateSignatureAndDecryptAndValidateAssertionSignatures", Response.class);
            method.setAccessible(true);
            return (Response) method.invoke(protocolEngine, response);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof EIDASSAMLEngineException) {
                throw (EIDASSAMLEngineException) cause; // method has signature EIDASSAMLEngineException
            } else throw (RuntimeException) targetException.getCause();
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for ProtocolEngine.validateSignatureAndDecryptAndValidateAssertionSignatures(Response) " +
                    testException.getMessage());
            return null;
        }
    }

    /**
     * Reflection for * {@link ProtocolEngine#validateResponseWithValidatorSuite(Response)}
     */
    private Response validateResponseWithValidatorSuite(Response response) throws EIDASSAMLEngineException {
        try {
            final Method method = ProtocolEngine.class.getDeclaredMethod("validateResponseWithValidatorSuite", Response.class);
            method.setAccessible(true);
            return (Response) method.invoke(protocolEngine, response);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof EIDASSAMLEngineException) {
                throw (EIDASSAMLEngineException) cause; // method has signature EIDASSAMLEngineException
            } else throw (RuntimeException) targetException.getCause();
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for ProtocolEngine.validateResponseWithValidatorSuite(Response) " +
                    testException.getMessage());
            return null;
        }
    }

    /**
     * Reflection for * {@link ProtocolEngine#validateAssertionSignatures(Response)}
     */
    private Response validateAssertionSignatures(Response response) throws EIDASSAMLEngineException {
        try {
            final Method method = ProtocolEngine.class.getDeclaredMethod("validateAssertionSignatures", Response.class);
            method.setAccessible(true);
            return (Response) method.invoke(protocolEngine, response);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof EIDASSAMLEngineException) {
                throw (EIDASSAMLEngineException) cause; // method has signature EIDASSAMLEngineException
            } else throw (RuntimeException) targetException.getCause();
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for ProtocolEngine.validateResponseWithValidatorSuite(Response) " +
                    testException.getMessage());
            return null;
        }
    }

    /**
     * Reflection for * {@link ProtocolEngine#validateSignature(AuthnRequest)}
     */
    private AuthnRequest validateSignature(AuthnRequest request) throws EIDASSAMLEngineException {
        try {
            final Method method = ProtocolEngine.class.getDeclaredMethod("validateSignature", AuthnRequest.class);
            method.setAccessible(true);
            return (AuthnRequest) method.invoke(protocolEngine, request);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof EIDASSAMLEngineException) {
                throw (EIDASSAMLEngineException) cause; // method has signature EIDASSAMLEngineException
            } else throw (RuntimeException) targetException.getCause();
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for ProtocolEngine.validateSignature(Response) " +
                    testException.getMessage());
            return null;
        }
    }

    /**
     * Reflection for * {@link ProtocolEngine#checkKeyTransportAlgorithm(Response, String)}
     */
    private Response checkKeyTransportAlgorithmMethod(Response response, String issuer) throws EIDASSAMLEngineException {
        try {
            final Method method = ProtocolEngine.class.getDeclaredMethod("checkKeyTransportAlgorithm", Response.class, String.class);
            method.setAccessible(true);
            return (Response) method.invoke(protocolEngine, response, issuer);
        } catch (InvocationTargetException targetException) {
            Throwable cause = targetException.getCause();
            if (cause instanceof EIDASSAMLEngineException) {
                throw (EIDASSAMLEngineException) cause; // method has signature EIDASSAMLEngineException
            } else throw (RuntimeException) targetException.getCause();
        } catch (ReflectiveOperationException testException) {
            Assert.fail("Reflection failed for ProtocolEngine.checkKeyTransportAlgorithm(response, issuer) " +
                    testException.getMessage());
            return null;
        }
    }

    private AuthenticationResponse.Builder getAuthenticationResponseBuilder(EidasAuthenticationRequest request) {
        return AuthenticationResponse.builder()
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .id("_2")
                .inResponseTo(request.getId())
                .issuer("https://destination.europa.eu/metadata")
                .subject("UK/UK/Bankys")
                .subjectNameIdFormat("urn:oasis:names:tc:saml2:2.0:nameid-format:persistent")
                .attributes(ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                        new StringAttributeValue("LU/BE/1")))
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW.stringValue());
    }

    private EidasAuthenticationRequest.Builder getEidasAuthenticationRequestBuilder() {
        return EidasAuthenticationRequest.builder()
                .id("_1")
                .issuer("https://source.europa.eu/metadata")
                .destination("https://destination.europa.eu")
                .citizenCountryCode("BE")
                .originCountryCode("BE")
                .providerName("Prov")
                .levelOfAssurance(NotifiedLevelOfAssurance.LOW)
                .assertionConsumerServiceURL("https://source.europa.eu/metadata")
                .requestedAttributes(ImmutableAttributeMap.of(
                        EidasSpec.Definitions.PERSON_IDENTIFIER, new StringAttributeValue[]{}));
    }

    /**
     * Creates a mock SAML response object from the specified file.
     *
     * @param fileInputPath The file path of the XML file containing the SAML response.
     * @return A mock SAML response object parsed from the XML file.
     * @throws RuntimeException If the mock response cannot be loaded or parsed.
     */
    private Response createResponse(String fileInputPath) {
        try {
            InputStream mockResponseXML = new FileInputStream(fileInputPath);
            Document mockResponseDocument = DocumentBuilderFactoryUtil.parse(mockResponseXML);
            XMLObject mockResponseXmlObject = OpenSamlHelper.unmarshallFromDom(mockResponseDocument);
            return (Response) mockResponseXmlObject;
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

}
