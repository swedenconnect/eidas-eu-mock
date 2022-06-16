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

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DefaultProtocolEngineConfigurationFactory;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import static org.hamcrest.Matchers.isA;

/**
 * Test class for the {@link AbstractProtocolEngine}
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractProtocolEngineTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProtocolEngineTest.class);
    private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_response.xml";

    private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";

    private AbstractProtocolEngine abstractProtocolEngine;
    private ProtocolConfigurationAccessor mockProtocolConfigurationAccessor;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BOUNCY_CASTLE_PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BOUNCY_CASTLE_PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BOUNCY_CASTLE_PROVIDER_NAME);
        }
    }

    @Before
    public void setUp() {
        mockProtocolConfigurationAccessor = Mockito.mock(ProtocolConfigurationAccessor.class);
        abstractProtocolEngine = new ProtocolEngine(mockProtocolConfigurationAccessor);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#getConfiguration()}
     * when ProtocolEngineConfiguration is null
     * <p>
     * Must fail.
     */
    @Test
    public void getConfigurationWithProtocolEngineConfigurationNull() {
        expectedException.expect(EIDASSAMLEngineRuntimeException.class);

        abstractProtocolEngine.getConfiguration();
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#unmarshall(Document)}
     * when {@link UnmarshallException} thrown from within {@link AbstractProtocolEngine#unmarshall(Document)}
     * <p>
     * Must fail.
     */
    @Test
    public void testUnmarshallWhenUnmarshallExceptionIsCaught() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        abstractProtocolEngine.unmarshall(null);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#marshall(XMLObject)}
     * when {@link MarshallException} is thrown from within {@link AbstractProtocolEngine#marshall(XMLObject)}
     * <p>
     * Must fail.
     */
    @Test
    public void testMarshallWhenMarshallExceptionIsCaught() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final XMLObject mockXMLObject = Mockito.mock(XMLObject.class);

        abstractProtocolEngine.marshall(mockXMLObject);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#checkReceivingUnencryptedResponsesAllowed()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckReceivingUnencryptedResponsesAllowed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("METADATATEST");
        final Method checkReceivingUnencryptedResponsesAllowed = getMethod("checkReceivingUnencryptedResponsesAllowed");

        checkReceivingUnencryptedResponsesAllowed.invoke(protocolEngineInterceptor);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#checkReceivingUnencryptedResponsesAllowed()}
     * when encryption is enabled for the given country
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckReceivingUnencryptedResponsesAllowedWithEncryptionEnabled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("METADATATESTO");
        final Method checkReceivingUnencryptedResponsesAllowed = getMethod("checkReceivingUnencryptedResponsesAllowed");

        checkReceivingUnencryptedResponsesAllowed.invoke(protocolEngineInterceptor);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#checkSendingUnencryptedResponsesAllowed()}
     * <p>
     * Must succeed.
     */
    @Test
    public void testCheckSendingUnencryptedResponsesAllowed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("METADATATEST");
        final Method checkSendingUnencryptedResponsesAllowed = getMethod("checkSendingUnencryptedResponsesAllowed");

        checkSendingUnencryptedResponsesAllowed.invoke(protocolEngineInterceptor);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#checkSendingUnencryptedResponsesAllowed()}
     * when encryption is enabled for the given country
     * <p>
     * Must fail.
     */
    @Test
    public void testCheckSendingUnencryptedResponsesAllowedWithEncryptionEnabled() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(EIDASSAMLEngineException.class));

        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("METADATATESTO");
        final Method checkSendingUnencryptedResponsesAllowed = getMethod("checkSendingUnencryptedResponsesAllowed");

        checkSendingUnencryptedResponsesAllowed.invoke(protocolEngineInterceptor);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#signResponse(IAuthenticationRequest, Response)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testSignResponse() throws EIDASSAMLEngineException {
        final IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);
        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("CONF2");
        final Response response = createResponse();

        protocolEngineInterceptor.signResponse(mockIAuthenticationRequest, response);
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#signResponse(IAuthenticationRequest, Response)}
     * when encryption is enabled for the given country,
     * when originCountryCode is null
     * <p>
     * Must fail.
     */
    @Test
    public void testSignResponseWithEncryptionEnabledAndCountryCodeNull() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        final IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);
        final ProtocolEngineInterceptor protocolEngineInterceptor = new ProtocolEngineInterceptor("METADATATESTO");

        protocolEngineInterceptor.signResponse(mockIAuthenticationRequest, mockResponse());
    }

    /**
     * Test method for
     * {@link AbstractProtocolEngine#getEncryptionCertificate(String, String)}
     * when issuer's metadata highest protocol versions is protocol versions 1.1
     *
     * Must succeed
     *
     * @throws EIDASSAMLEngineException
     */
    @Test
    public void testGetEncryptionCertificates() throws EIDASSAMLEngineException {
        ProtocolEngineConfiguration engineConfiguration = DefaultProtocolEngineConfigurationFactory
                .getDefaultConfiguration("ProtocolVersioningWithCipher");
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(engineConfiguration);

        X509Certificate certificate = abstractProtocolEngine
                .getEncryptionCertificate("testUrl", "BE");

        Assert.assertTrue(certificate instanceof LegacySha1DigestX509Certificate);

        Mockito.reset(mockProtocolConfigurationAccessor);
    }

    private class ProtocolEngineInterceptor extends AbstractProtocolEngine {

        ProtocolEngineInterceptor(String instanceName) {
            super(new ProtocolConfigurationAccessor() {
                @Nonnull
                @Override
                public ProtocolEngineConfiguration get() throws ProtocolEngineConfigurationException {
                    return DefaultProtocolEngineConfigurationFactory.getInstance().getConfiguration(instanceName);
                }
            });
        }
    }

    private Method getMethod(String methodName) throws NoSuchMethodException {
        final Method method = AbstractProtocolEngine.class.getDeclaredMethod(methodName);
        method.setAccessible(true);

        return method;
    }

    private Response mockResponse() {
        final Response mockResponse = Mockito.mock(Response.class);
        final StatusCode mockStatusCode = Mockito.mock(StatusCode.class);
        final Status mockStatus = Mockito.mock(Status.class);

        Mockito.when(mockStatus.getStatusCode()).thenReturn(mockStatusCode);
        Mockito.when(mockStatusCode.getValue()).thenReturn(StatusCode.SUCCESS);
        Mockito.when(mockResponse.getStatus()).thenReturn(mockStatus);

        return mockResponse;
    }

    private Response createResponse() {
        try {
            final InputStream mockResponseXML = getMockResponseXML();
            final Document mockResponseDocument = DocumentBuilderFactoryUtil.parse(mockResponseXML);
            final XMLObject mockResponseXmlObject = OpenSamlHelper.unmarshallFromDom(mockResponseDocument);

            return (Response) mockResponseXmlObject;
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

    protected InputStream getMockResponseXML() throws Exception {
        return new FileInputStream(MOCK_RESPONSE_FILE_PATH);
    }

}