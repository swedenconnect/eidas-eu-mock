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


import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
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
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
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
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.signature.Signature;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Random;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;


/**
 * Tests for {@link ProtocolEngine}
 */
public final class ProtocolEngineTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final MetadataFetcherI metadataFetcher = Mockito.mock(MetadataFetcherI.class);
    private final MetadataSignerI metadataSigner = Mockito.mock(MetadataSignerI.class);
    private final MetadataClockI metadataClock = Mockito.mock(MetadataClockI.class);
    private ProtocolEngineConfiguration defaultProtocolEngineConfiguration;
    private ProtocolConfigurationAccessor mockProtocolConfigurationAccessor;
    private EidasProtocolProcessor protocolProcessor;
    private ProtocolEngine protocolEngine;

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
    public void setup() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ProtocolEngineConfigurationException {
        defaultProtocolEngineConfiguration = DefaultProtocolEngineConfigurationFactory.getInstance().getConfiguration("METADATATEST");
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
        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.isRequesterIdFlag()).thenReturn(false);
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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. internalError.code) processing request : internalError.message - null");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. internalError.code) processing request : internalError.message - Error (no. null) processing request : null - null");

        final ProtocolSignerI signer = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(signer.sign(any())).thenThrow(EIDASSAMLEngineException.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .signer(signer).build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.isRequesterIdFlag()).thenReturn(false);
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
    public void generateResponseMessage() throws EIDASSAMLEngineException {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        Assert.assertNotNull(responseMessage);
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when signAssertion is set to true
     * <p>
     * Must succeed.
     */
    @Test
    public void generateResponseMessageSignAssertion() throws EIDASSAMLEngineException {
        final boolean signAssertion = true;
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, signAssertion, "127.0.0.1");
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
    public void generateResponseMessageResponseSignAssertionsEnabledResponseDomNull() throws EIDASSAMLEngineException {
        exception.expect(ConstraintViolationException.class);

        final ProtocolSignerI signer = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(signer.isResponseSignAssertions()).thenReturn(true);
        Mockito.when(signer.sign(any())).thenReturn(Mockito.mock(Assertion.class));
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .signer(signer).build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();

        protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateResponseMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when the engine configuration is set to sign the assertions of an {@link IAuthenticationResponse}
     * but the {@link ProtocolEngine#signAssertion(Response)} throws an {@link EIDASSAMLEngineException}
     * <p>
     * Must fail
     * and throw {@link EIDASSAMLEngineException}
     *
     */
    @Test
    public void generateResponseMessageSignAssertionException() throws EIDASSAMLEngineException {
        exception.expect(EIDASSAMLEngineException.class);

        final boolean signAssertion = true;
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).build();
        final ProtocolEngine protocolEngineSpy = Mockito.spy(protocolEngine);
        Mockito.doThrow(EIDASSAMLEngineException.class).when(protocolEngineSpy).signAssertion(any());

        final IResponseMessage responseMessage = protocolEngineSpy.generateResponseMessage(request, response, signAssertion, "127.0.0.1");
        Assert.assertNotNull(responseMessage);
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
    public void encryptAndSignAndMarshallResponseTryCatchEIDASSAMLEngineException() throws EIDASSAMLEngineException {
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. samlengine.invalid.certificate.code) processing request : samlengine.invalid.certificate.message - null");

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

        protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
    }

    /**
     * Test method for
     * {@link ProtocolEngine#generateResponseErrorMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * when isFailure is true
     * <p>
     * Must succeed.
     */
    @Test
    @Deprecated
    public void generateResponseErrorMessage() throws EIDASSAMLEngineException, EIDASMetadataException {
        final EidasAuthenticationRequest request = getEidasAuthenticationRequestBuilder().build();
        final AuthenticationResponse response = getAuthenticationResponseBuilder(request).statusCode(EIDASStatusCode.RESPONDER_URI.toString()).build();

        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.getEidasApplicationIdentifier()).thenReturn("CEF:eIDAS-ref:2.1");
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);

        final IResponseMessage responseMessage = protocolEngine.generateResponseErrorMessage(request, response, "127.0.0.1", Arrays.asList("CEF:eIDAS-ref:2.1"));
        Assert.assertNotNull(responseMessage);
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
        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.isRequesterIdFlag()).thenReturn(false);
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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : message.validation.error.code - Saml request bytes are null.");
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
        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.isRequesterIdFlag()).thenReturn(false);
        Mockito.when(metadataFetcher.getEidasMetadata(any(), any(), any())).thenReturn(params);
        final IRequestMessage requestMessage = protocolEngine.generateRequestMessage(request, "https://destination.europa.eu/metadata");
        final byte[] rawMessage = requestMessage.getMessageBytes();
        final AuthnRequest authnRequest = protocolEngine.unmarshallRequest(rawMessage);
        Assert.assertNotNull(authnRequest);
        Assert.assertEquals(request.getIssuer(), authnRequest.getIssuer().getValue());

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
    public void unmarshallRequestAndValidateRequestIsNull() throws EIDASSAMLEngineException, EIDASMetadataException {
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : message.validation.error.code - Saml authentication request is null.");

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
    public void unmarshallRequestAndValidateRequestIsTooLarge() throws EIDASSAMLEngineException, EIDASMetadataException {
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("SAML AuthnRequest exceeds max size.");

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
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        final Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : message.validation.error.code - Saml response bytes are null.");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : No signature - null");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : No signature - null");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : Null Issuer.");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. null) processing request : invalidReceivedSignAlgo.error.code - null");

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

        Mockito.when(eidasProtocolProcessor.getResponseSignatureCertificate(any())).thenThrow(EIDASSAMLEngineException.class);
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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Format has an invalid value.");

        final SamlEngineCoreProperties samlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        defaultProtocolEngineConfiguration = new ProtocolEngineConfiguration.Builder(defaultProtocolEngineConfiguration)
                .coreProperties(samlEngineCoreProperties)
                .build();
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(defaultProtocolEngineConfiguration);

        final EidasMetadataParametersI params = Mockito.mock(EidasMetadataParametersI.class);
        Mockito.when(params.isRequesterIdFlag()).thenReturn(false);
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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Number of Assertion in Response 2, differs from number of allowed ones:1.");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : No signature - null");

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
    public void validateSignatureAndDecryptAndValidateAssertionSignaturesValidateSignatureDisabled() throws EIDASSAMLEngineException {
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. null) processing request : null - null");

        final SamlEngineCoreProperties samlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        Mockito.when(samlEngineCoreProperties.isValidateSignature())
                .thenReturn(false);
        final EidasProtocolProcessor eidasProtocolProcessor = Mockito.mock(EidasProtocolProcessor.class);
        Mockito.when(eidasProtocolProcessor.getResponseSignatureCertificate(any()))
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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : Invalid issuer. - null");

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
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. message.validation.error.code) processing request : Invalid issuer. - null");

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
        final IResponseMessage responseMessage = protocolEngine.generateResponseMessage(request, response, "127.0.0.1");
        final Response samlResponse = (Response) OpenSamlHelper.unmarshall(responseMessage.getMessageBytes());
        assertFalse(samlResponse.getEncryptedAssertions().isEmpty());
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
        final EidasProtocolProcessor eidasProtocolProcessor = Mockito.mock(EidasProtocolProcessor.class);
        Mockito.when(eidasProtocolProcessor.getResponseSignatureCertificate(any()))
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
    public void validateAssertionSignaturesTryCatchEIDASSAMLEngineException() throws EIDASSAMLEngineException {
        exception.expect(EIDASSAMLEngineException.class);
        exception.expectMessage("Error (no. invalidSamlAssertionSignature.error.code) processing request : " +
                "invalidSamlAssertionSignature.error.message - " +
                "Error (no. null) processing request : invalidReceivedSignAlgo.error.code - null");

        final Assertion assertion = Mockito.mock(Assertion.class);
        Mockito.when(assertion.isSigned()).thenReturn(true);
        Mockito.when(assertion.getSignature()).thenReturn(Mockito.mock(Signature.class));

        final Response response = Mockito.mock(Response.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(response.getStatus().getStatusCode().getValue()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());
        Mockito.when(response.getAssertions()).thenReturn(Arrays.asList(assertion));

        validateAssertionSignatures(response);
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

    private AuthenticationResponse.Builder getAuthenticationResponseBuilder(EidasAuthenticationRequest request) {
        return AuthenticationResponse.builder()
                .statusCode(EIDASStatusCode.SUCCESS_URI.toString())
                .id("_2")
                .inResponseTo(request.getId())
                .issuer("https://destination.europa.eu/metadata")
                .subject("UK/UK/Bankys")
                .subjectNameIdFormat("urn:oasis:names:tc:saml2:2.0:nameid-format:persistent")
                .attributes(ImmutableAttributeMap.of(EidasSpec.Definitions.PERSON_IDENTIFIER,
                        new StringAttributeValue("LU/BE/1", false)))
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
}
