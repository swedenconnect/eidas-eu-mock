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
package eu.eidas.node.auth.service.tests;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IResponseMessage;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.ProtocolEngine;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.configuration.ProtocolConfigurationAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.core.ProtocolEncrypterI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.auth.service.AUSERVICESAML;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.auth.service.ResponseCarryingServiceException;
import eu.eidas.node.auth.util.tests.TestingConstants;
import eu.eidas.node.utils.ReflectionUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.w3c.dom.Document;

import javax.cache.Cache;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Test class for {@link AUSERVICESAML}
 */
@RunWith(MockitoJUnitRunner.class)
public class AUSERVICESAMLTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AUSERVICESAMLTest.class);
    private static final String SERVICE_METADATA_URL = "http://localhost:8888/EidasNode/ServiceMetadata";
    private static final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/responses/saml_response.xml";
    private static String SAML_TOKEN =
            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6c3Rvcms9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6YXNzZXJ0aW9uIiB4bWxuczpzdG9ya3A9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6cHJvdG9jb2wiIEFzc2VydGlvbkNvbnN1bWVyU2VydmljZVVSTD0iaHR0cDovL3NwLmxvY2FsOjkwOTAvU1AvUmV0dXJuUGFnZSIgQ29uc2VudD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNvbnNlbnQ6dW5zcGVjaWZpZWQiIERlc3RpbmF0aW9uPSJodHRwOi8vcGVwcy5sb2NhbDo5MDkwL1BFUFMvU2VydmljZVByb3ZpZGVyIiBGb3JjZUF1dGhuPSJ0cnVlIiBJRD0iX2QwNDhjYjMxNzgxMzg0NWIzMmE3YTJiNzVmM2JhZDU5IiBJc1Bhc3NpdmU9ImZhbHNlIiBJc3N1ZUluc3RhbnQ9IjIwMTEtMTEtMzBUMTY6MDk6NTcuODI0WiIgUHJvdG9jb2xCaW5kaW5nPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YmluZGluZ3M6SFRUUC1QT1NUIiBQcm92aWRlck5hbWU9IkRFTU8tU1AiIFZlcnNpb249IjIuMCI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+aHR0cDovL1MtUEVQUy5nb3YueHg8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2QwNDhjYjMxNzgxMzg0NWIzMmE3YTJiNzVmM2JhZDU5Ij48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxkczpEaWdlc3RWYWx1ZT5LeGhHTnJtdVN3MGxaVXBiUHpldm9EOUk0V3M9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPkg0Rng0QkRaMXJFQWwvS09YOUpIM1dHcCtheWVGci9GTGtLOE5kU2hycE1JNXJXL2lvTFY4ZlV5UXFnMlY5L0tiQkF5cVhaTDFNM0loWjFCMDNuaXZhR3FXUVdiUklOT2syMStvMzZLcDI4a3JRQ2ZqdkhhVVNsK0FHNXVpZGFJT0FSZUdPQkRuQ1VKRkd6aEUvdzc1ZXNaMm5KcmJrR2NVYklRamlJbHBqMExvbnZyUWp0bzgveWpkMFJNNW01SUpDRCtCb0VUZ2tZNkxmajR0UXgzYjVkRytqb1JyU3g4R2ZiQ1Y1TXZwcTRndHgyd0cxSHVEcllDWUE2NUhxSkhJbE5xTWRMeG1BRXEzdkNhMjdMQU9FRWU0ZkxDc2hyVW0ySUZWSGVhWWVQV2NONUhSRStVRGt5MVllQ1pSbWlsa2graUFwS3UvZExqQzdxcXpLaUt1Zz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlESnpDQ0FnOENCRXVvbmJJd0RRWUpLb1pJaHZjTkFRRUZCUUF3V0RFTE1Ba0dBMVVFQmhNQ1JWTXhEakFNQmdOVkJBZ01CVk53DQpZV2x1TVE4d0RRWURWUVFIREFaTllXUnlhV1F4RGpBTUJnTlZCQW9NQlVsdVpISmhNUmd3RmdZRFZRUUREQTlzYjJOaGJDMWtaVzF2DQpMV05sY25Rd0hoY05NVEF3TXpJek1UQTFNek00V2hjTk1UQXdOakF4TVRBMU16TTRXakJZTVFzd0NRWURWUVFHRXdKRlV6RU9NQXdHDQpBMVVFQ0F3RlUzQmhhVzR4RHpBTkJnTlZCQWNNQmsxaFpISnBaREVPTUF3R0ExVUVDZ3dGU1c1a2NtRXhHREFXQmdOVkJBTU1EMnh2DQpZMkZzTFdSbGJXOHRZMlZ5ZERDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBSmZkUTEvbWtNOXNxcWFiDQpyaThxcXFYNHMwSjZVRUVYeUYrMEFqTFU4UkM0V01lWUZTWjV0ZndueUxzb1hZMC85YlpMWG5CeFNpYlFZYWY0U25wWkpobFZBNHNVDQowZDhxeUVXQTJPdlhHRENtN3VzcEdIRzNDUlFhN2ZwSHNtSG1mSUFxaG85WERsd3B3SmR4NWdLNEVkYzZhQXJjTVFmanFNaHkxczFuDQo2T2YxaTFsTUdHc0dyRVJJUlk3YmlJUXUvOG5JVGJISDE4c1VBWk1HMXUvUTBBdmk5TzNMV3lzd0hYVW91WjgxOGZXd0c2eHJ1Mk41DQp5NnZ0Vk8vU0wzZG9SU2RxWWtFbXM5M1RneFR0YUtnOFhOZTI0emhOVHRlNm52V0xhS2VzaTJLelpHQzU3SFU0N0hCRkVzOE5Xazd6DQo5QkRmOHVMeVB6OVZEYWh3Vkt6TXRvOENBd0VBQVRBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQWdwdVJlWFE3RHNmZG9lNXp6eTJGDQo2a24xcXF0TWtSR3NCUEtuREZDSS9Ha0lacEJpcWxmd2RoNnNpcG5GS1dmS0VEbFBLTjFrRWhia0RSMkdBMUNwNEY0WlNML0h1bVpNDQpwV2FiUmhEeGhUUHZISUZiYlNoUERKWTkzK2p3L3lRZWFXZ011aHczV3pkSDlUclZvUlpIMFE3c0cxcElKbzUvNklvZ0lad0Z2SkhUDQovTkR1dEttdXJVNkx3OFZuZGU4UGZuUWQrRlRFaHowU0VHeUtrV2pBdWhHYkpmc2VCeS96M0wrTUpxMXJkU1E5UEY3d1hEdldOekpxDQp5YU5CVVdXQlYxVHNrdmtOWlhjYWd1cm9WUHkyWGhBMWFpeGxBYWpXRVhMazZVdWo1VVlxYWxyVi9yZU5ZdkR2WTBCVjJDSW41MXI3DQpQcG04SUZWVGs4bVlmWDgvanc9PTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6RXh0ZW5zaW9ucz48c3Rvcms6UXVhbGl0eUF1dGhlbnRpY2F0aW9uQXNzdXJhbmNlTGV2ZWw+Mzwvc3Rvcms6UXVhbGl0eUF1dGhlbnRpY2F0aW9uQXNzdXJhbmNlTGV2ZWw+PHN0b3JrOnNwU2VjdG9yPkRFTU8tU1AtU0VDVE9SPC9zdG9yazpzcFNlY3Rvcj48c3Rvcms6c3BJbnN0aXR1dGlvbj5ERU1PLVNQPC9zdG9yazpzcEluc3RpdHV0aW9uPjxzdG9yazpzcEFwcGxpY2F0aW9uPkRFTU8tU1AtQVBQTElDQVRJT048L3N0b3JrOnNwQXBwbGljYXRpb24+PHN0b3JrOnNwQ291bnRyeT5QVDwvc3Rvcms6c3BDb3VudHJ5PjxzdG9ya3A6ZUlEU2VjdG9yU2hhcmU+dHJ1ZTwvc3RvcmtwOmVJRFNlY3RvclNoYXJlPjxzdG9ya3A6ZUlEQ3Jvc3NTZWN0b3JTaGFyZT50cnVlPC9zdG9ya3A6ZUlEQ3Jvc3NTZWN0b3JTaGFyZT48c3RvcmtwOmVJRENyb3NzQm9yZGVyU2hhcmU+dHJ1ZTwvc3RvcmtwOmVJRENyb3NzQm9yZGVyU2hhcmU+PHN0b3JrcDpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxzdG9yazpSZXF1ZXN0ZWRBdHRyaWJ1dGUgTmFtZT0iaHR0cDovL3d3dy5zdG9yay5nb3YuZXUvMS4wL2dpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48c3Rvcms6UmVxdWVzdGVkQXR0cmlidXRlIE5hbWU9Imh0dHA6Ly93d3cuc3RvcmsuZ292LmV1LzEuMC9lSWRlbnRpZmllciIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48L3N0b3JrcDpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxzdG9ya3A6QXV0aGVudGljYXRpb25BdHRyaWJ1dGVzPjxzdG9ya3A6VklEUEF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz48c3RvcmtwOkNpdGl6ZW5Db3VudHJ5Q29kZT5MTzwvc3RvcmtwOkNpdGl6ZW5Db3VudHJ5Q29kZT48c3RvcmtwOlNQSW5mb3JtYXRpb24+PHN0b3JrcDpTUElEPkRFTU8tU1A8L3N0b3JrcDpTUElEPjwvc3RvcmtwOlNQSW5mb3JtYXRpb24+PC9zdG9ya3A6VklEUEF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz48L3N0b3JrcDpBdXRoZW50aWNhdGlvbkF0dHJpYnV0ZXM+PC9zYW1sMnA6RXh0ZW5zaW9ucz48L3NhbWwycDpBdXRoblJlcXVlc3Q+";
    byte[] saml = EidasStringUtil.decodeBytesFromBase64(SAML_TOKEN);

    @Rule
    public ExpectedException exceptionThrown = ExpectedException.none();

    @InjectMocks
    private AUSERVICESAML auServiceSaml;
    private Response response;

    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private AUSERVICEUtil mockAuServiceUtil;
    @Mock
    private ProtocolEngineFactory mockProtocolEngineFactory;
    @Mock
    private MetadataFetcherI mockMetadataFetcherI;
    @Mock
    private ProtocolProcessorI mockProtocolProcessorI;

    private EidasMetadataParametersI mockEidasMetadataParameters;

    @Before
    public void setUp() throws Exception {
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockAuServiceUtil.checkNotPresentInCache(anyString(), anyString())).thenReturn(Boolean.TRUE);

        mockAuServiceUtilProperties();
        mockAntiReplayCache();
        mockEidasMetadataParameters = mockEidasMetadataRoleParameters();

        auServiceSaml.setSamlEngineInstanceName(TestingConstants.SAML_INSTANCE_CONS.toString());
        auServiceSaml.setCountryCode("EU");
        auServiceSaml.setServiceMetadataUrl(SERVICE_METADATA_URL);
        response = createResponse();
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * In this method, we check if the AUSERVICESAML#processConnectorRequest is correctly executed when module is active.
     */
    @Test
    public void processConnectorRequestWithActiveProxyServiceModule() throws Exception {
        IAuthenticationRequest mockAuthenticationRequest = getDefaultTestAuthenticationRequest();
        mockProtocolEngineFactoryWith(mockAuthenticationRequest);

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Public sector Relying party and without a requesterID.
     * And requesterId is required
     *
     * Must succeed because requesterId is only required for Private Sector
     */
    @Test
    public void processConnectorRequestFromPublicSectorMissingRequesterID() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions()).thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPublicSectorAuthenticationRequest();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("true");

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Public sector Relying party with requesterID provided.
     * and requesterId is not required
     *
     * Must succeed
     */
    @Test
    public void processConnectorRequestFromPublicSectorWithRequesterIDAndNotRequired() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPublicSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("false");

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Private sector Relying party but without a requesterID provided.
     * And requesterId is required
     *
     * Must throw a {@link ResponseCarryingServiceException}
     */
    @Test
    public void processConnectorRequestFromPrivateSectorMissingRequesterId() throws Exception {
        exceptionThrown.expect(ResponseCarryingServiceException.class);
        String expectedErrorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_MISSING_REQUESTER_ID.errorMessage());
        exceptionThrown.expectMessage(expectedErrorMessage);

        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequest();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("true");

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Private sector Relying party but without a requesterID provided.
     * And requesterId is required but connector doesn't comply with more than specs 1.1
     *
     * Must succeed
     */
    @Test
    public void processConnectorRequestFromPrivateSectorMissingRequesterIdButSpecs1_1() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequest();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Private sector Relying party but without a requesterID provided.
     * and requesterId is not required
     *
     * Must succeed
     */
    @Test
    public void processConnectorRequestFromPrivateSectorMissingRequesterIDButNotRequired() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequest();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("false");

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Private sector Relying party with requesterID provided.
     * and requesterId is required
     *
     * Must succeed
     */
    @Test
    public void processConnectorRequestFromPrivateSectorWithRequesterID() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("true");

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request from a Private sector Relying party with requesterID provided.
     * and requesterId is not required
     *
     * Must succeed
     */
    @Test
    public void processConnectorRequestFromPrivateSectorWithRequesterIDAndNotRequired() throws Exception {
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));

        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString())).thenReturn("false");

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request is send from a Spec 1.1 connector and the first loa is also the highest notified
     * <p>
     * Must succeed
     */
    @Test
    public void processConnectorRequestBackwardsCompatibleHighestNotifiedLoaFirst() throws Exception {

        final List<String> publishedLoAs = Arrays.asList(
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                "test:nonNotifiedLoa",
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_LOW);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()))
                .thenReturn(String.join(";", publishedLoAs));
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.1"));
        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request is send from a Spec 1.2 connector and the first loa is something else
     * <p>
     * Must succeed
     */
    @Test
    public void processConnectorRequestForwardsCompatibleHighestNotifiedLoaNotFirst() throws Exception {
        final List<String> publishedLoAs = Arrays.asList(
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                "test:nonNotifiedLoa",
                ILevelOfAssurance.EIDAS_LOA_LOW);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()))
                .thenReturn(String.join(";", publishedLoAs));
        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString()))
                .thenReturn("false");
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));
        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request is send from a Spec 1.2 connector and the first loa is something else
     * <p>
     * Must succeed
     */
    @Test
    public void processConnectorRequestForwardsCompatibleNonNotifiedIsFirst() throws Exception {
        final List<String> publishedLoAs = Arrays.asList(
                "test:nonNotifiedLoa",
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                ILevelOfAssurance.EIDAS_LOA_LOW);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()))
                .thenReturn(String.join(";", publishedLoAs));
        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.REQUESTER_ID_FLAG.toString()))
                .thenReturn("false");
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.2", "1.1"));
        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        IAuthenticationRequest request = auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");

        Assert.assertNotNull(request);
        Assert.assertEquals(mockIAuthenticationRequest.getId(), request.getId());
        Assert.assertEquals(mockIAuthenticationRequest.getIssuer(), request.getIssuer());
        Assert.assertEquals(mockIAuthenticationRequest.getRequesterId(), request.getRequesterId());
    }

    /**
     * Test method for {@link AUSERVICESAML#processConnectorRequest(String, byte[], String, String)}.
     * When saml request is send from a Spec 1.1 connector but the first loa is not the HighestNotified
     * <p>
     * Must fail
     */
    @Test
    public void processConnectorRequestBackwardsCompatibleHighestNotifiedLoaNotFirstError() throws Exception {
        exceptionThrown.expect(ResponseCarryingServiceException.class);
        exceptionThrown.expectMessage("serviceProviderRequest.invalidLoA (000009)");

        final List<String> publishedLoAs = Arrays.asList(
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                "test:nonNotifiedLoa",
                ILevelOfAssurance.EIDAS_LOA_LOW);

        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString()))
                .thenReturn(String.join(";", publishedLoAs));
        Mockito.when(mockEidasMetadataParameters.getEidasProtocolVersions())
                .thenReturn(Arrays.asList("1.1"));
        IAuthenticationRequest mockIAuthenticationRequest = getPrivateSectorAuthenticationRequestWithRequesterId();
        mockProtocolEngineFactoryWith(mockIAuthenticationRequest);

        auServiceSaml.processConnectorRequest("POST", saml, TestingConstants.USER_IP_CONS.toString(), "relayState");
    }

    /**
     * Test method for {@link ProtocolEngine#generateResponseErrorMessage(IAuthenticationRequest, IAuthenticationResponse, String)}
     * which is called in {@link AUSERVICESAML#generateResponseErrorMessage(IAuthenticationRequest, ProtocolEngineI, AuthenticationResponse.Builder, String)}
     *
     * In this method, we test the generation of the Response error message.
     * Should succeed
     */
    @Test
    public void testGenerateResponseErrorMessage() throws EIDASSAMLEngineException {
        ProtocolConfigurationAccessor mockProtocolConfigurationAccessor = Mockito.mock(ProtocolConfigurationAccessor.class);
        ProtocolEngine protocolEngine = new ProtocolEngine(mockProtocolConfigurationAccessor);

        ProtocolEngineConfiguration mockProtocolEngineConfiguration = mockProtocolEngineConfiguration(mockProtocolConfigurationAccessor);
        mockProtocolEncrypter(mockProtocolEngineConfiguration);

        Response mockResponse = mockResponse();
        IAuthenticationResponse mockIAuthenticationResponse = Mockito.mock(IAuthenticationResponse.class);
        Mockito.when(mockProtocolProcessorI.marshallErrorResponse(any(), any(), anyString(), any(), any())).thenReturn(mockResponse);
        Mockito.when(mockProtocolProcessorI.unmarshallErrorResponse(any(), any(), anyString(), any())).thenReturn(mockIAuthenticationResponse);

        ProtocolSignerI mockProtocolSignerI = mockProtocolSigner(mockProtocolEngineConfiguration);
        Mockito.when(mockProtocolSignerI.sign(mockResponse, Boolean.TRUE)).thenReturn(response);

        IAuthenticationRequest mockIAuthenticationRequest = Mockito.mock(IAuthenticationRequest.class);
        IResponseMessage responseMessage = protocolEngine.generateResponseErrorMessage(mockIAuthenticationRequest, mockIAuthenticationResponse, "128.128.128.128");
        Assert.assertNotNull(responseMessage);
    }


    private ProtocolEngineI mockProtocolEngineFactoryWith(IAuthenticationRequest mockedAuthRequest) throws EIDASSAMLEngineException {
        ProtocolEngineI mockProtocolEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(anyString())).thenReturn(mockProtocolEngine);
        Mockito.when(mockProtocolEngine.unmarshallRequestAndValidate(Mockito.any(), Mockito.any()))
            .thenReturn(mockedAuthRequest);
        IResponseMessage mockErrorResponseMessage = Mockito.mock(IResponseMessage.class);
        Mockito.when(mockErrorResponseMessage.getMessageBytes()).thenReturn("testErrorMessage".getBytes());
        Mockito.when(mockProtocolEngine.generateResponseErrorMessage(any(), any(), anyString()))
                .thenReturn(mockErrorResponseMessage);

        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(anyString()).getProtocolProcessor()).thenReturn(mockProtocolProcessorI);
        Mockito.when(mockProtocolProcessorI.isAcceptableHttpRequest(mockedAuthRequest, "POST"))
            .thenReturn(true);

        return mockProtocolEngine;
    }

    private EidasAuthenticationRequest getDefaultTestAuthenticationRequest() {
        return getTestAuthenticationRequestBuilder().build();
    }

    private EidasAuthenticationRequest getPublicSectorAuthenticationRequest() {
        return getTestAuthenticationRequestBuilder()
                .spType(SpType.PUBLIC.getValue())
                .build();
    }

    private EidasAuthenticationRequest getPublicSectorAuthenticationRequestWithRequesterId() {
        return getTestAuthenticationRequestBuilder()
                .spType(SpType.PUBLIC.getValue())
                .requesterId("requesterId")
                .build();
    }

    private EidasAuthenticationRequest getPrivateSectorAuthenticationRequest() {
        return getTestAuthenticationRequestBuilder()
                .spType(SpType.PRIVATE.getValue())
                .build();
    }

    private EidasAuthenticationRequest getPrivateSectorAuthenticationRequestWithRequesterId() {
        return getTestAuthenticationRequestBuilder()
                .spType(SpType.PRIVATE.getValue())
                .requesterId("requesterId")
                .build();
    }

    private EidasAuthenticationRequest.Builder getTestAuthenticationRequestBuilder() {
        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        return new EidasAuthenticationRequest.Builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .destination("postDestination")
                .issuer("issuer")
                .citizenCountryCode("EU")
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH.stringValue())
                .assertionConsumerServiceURL("assertionConsumer")
                .requestedAttributes(mockImmutableAttributeMap);
    }

    private EidasMetadataParametersI mockEidasMetadataRoleParameters() throws EIDASMetadataException {
        EidasMetadataParametersI mockEidasMetadataParameters = mockEidasMetadataParameters();

        EidasMetadataRoleParametersI mockEidasMetadataRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockEidasMetadataRoleParameters));
        Mockito.when(mockEidasMetadataRoleParameters.getRole()).thenReturn(MetadataRole.SP);
        Mockito.when(mockEidasMetadataRoleParameters.getDefaultAssertionConsumerUrl()).thenReturn("assertionConsumer");

        return mockEidasMetadataParameters;
    }

    private EidasMetadataParametersI mockEidasMetadataParameters() throws EIDASMetadataException {
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);

        Mockito.when(mockMetadataFetcherI.getEidasMetadata(anyString(), any(), any()))
                .thenReturn(mockEidasMetadataParameters);

        return mockEidasMetadataParameters;
    }

    private void mockAuServiceUtilProperties() {
        Mockito.when(mockAuServiceUtil.getProperty(EidasParameterKeys.VALIDATE_BINDING.toString())).thenReturn("true");
        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_LOA.toString())).thenReturn("http://eidas.europa.eu/LoA/high");
        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_POST_URIDEST.toString())).thenReturn("postDestination");
        Mockito.when(mockAuServiceUtil.getProperty(EIDASValues.EIDAS_SERVICE_REDIRECT_URIDEST.toString())).thenReturn("postDestination");
    }

    private void mockAntiReplayCache() {
        final Cache antiReplayCache = Mockito.mock(Cache.class);
        mockAuServiceUtil.setAntiReplayCache(antiReplayCache);
        mockAuServiceUtil.flushReplayCache();
    }

    private ProtocolSignerI mockProtocolSigner(ProtocolEngineConfiguration mockProtocolEngineConfiguration) {
        ProtocolSignerI mockProtocolSignerI = Mockito.mock(ProtocolSignerI.class);
        Mockito.when(mockProtocolEngineConfiguration.getSigner()).thenReturn(mockProtocolSignerI);
        Mockito.when(mockProtocolSignerI.isResponseSignWithKey()).thenReturn(Boolean.TRUE);

        return mockProtocolSignerI;
    }

    private void mockProtocolEncrypter(ProtocolEngineConfiguration mockProtocolEngineConfiguration) {
        ProtocolEncrypterI mockProtocolEncrypterI = Mockito.mock(ProtocolEncrypterI.class);
        Mockito.when(mockProtocolEngineConfiguration.getCipher()).thenReturn(mockProtocolEncrypterI);
    }

    private ProtocolEngineConfiguration mockProtocolEngineConfiguration(ProtocolConfigurationAccessor mockProtocolConfigurationAccessor) throws ProtocolEngineConfigurationException {
        ProtocolEngineConfiguration mockProtocolEngineConfiguration = Mockito.mock(ProtocolEngineConfiguration.class);
        SamlEngineCoreProperties mockSamlEngineCoreProperties = Mockito.mock(SamlEngineCoreProperties.class);
        SamlEngineClock mockSamlEngineClock = Mockito.mock(SamlEngineClock.class);
        DateTime currentTime = DateTime.now();

        Mockito.when(mockSamlEngineClock.getCurrentTime()).thenReturn(currentTime);
        Mockito.when(mockProtocolConfigurationAccessor.get()).thenReturn(mockProtocolEngineConfiguration);
        Mockito.when(mockProtocolEngineConfiguration.getClock()).thenReturn(mockSamlEngineClock);
        Mockito.when(mockProtocolEngineConfiguration.getCoreProperties()).thenReturn(mockSamlEngineCoreProperties);
        Mockito.when(mockProtocolEngineConfiguration.getProtocolProcessor()).thenReturn(mockProtocolProcessorI);

        return mockProtocolEngineConfiguration;
    }

    private Response createResponse() {
        try {
            InputStream mockResponseXML = getMockResponseXML();
            Document mockResponseDocument = DocumentBuilderFactoryUtil.parse(mockResponseXML);
            XMLObject mockResponseXmlObject = OpenSamlHelper.unmarshallFromDom(mockResponseDocument);
            return (Response) mockResponseXmlObject;
        } catch (Exception e) {
            LOGGER.error("Mock response could not be loaded!");
            throw new RuntimeException(e);
        }
    }

    protected InputStream getMockResponseXML() throws Exception {
        return new FileInputStream(MOCK_RESPONSE_FILE_PATH);
    }

    private Response mockResponse() {
        Response mockResponse = Mockito.mock(Response.class);
        StatusCode mockStatusCode = Mockito.mock(StatusCode.class);
        Status mockStatus = Mockito.mock(Status.class);

        Mockito.when(mockStatus.getStatusCode()).thenReturn(mockStatusCode);
        Mockito.when(mockStatusCode.getValue()).thenReturn(StatusCode.SUCCESS);
        Mockito.when(mockResponse.getStatus()).thenReturn(mockStatus);

        return mockResponse;
    }

}
