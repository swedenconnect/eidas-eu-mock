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
package eu.eidas.node.auth.connector.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IncomingRequest;
import eu.eidas.auth.commons.RequestState;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValue;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidSessionEIDASException;
import eu.eidas.auth.commons.light.ILevelOfAssurance;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.auth.commons.light.IResponseStatus;
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.auth.commons.light.impl.LevelOfAssurance;
import eu.eidas.auth.commons.light.impl.LightRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.SpType;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;
import eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec;
import eu.eidas.auth.commons.tx.AuthenticationExchange;
import eu.eidas.auth.commons.tx.FlowIdCache;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.auth.engine.Correlated;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EidasMetadataRoleParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.auth.engine.metadata.impl.MetadataRole;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.connector.AUCONNECTORSAML;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.util.tests.TestingConstants;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.SessionHolder;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.NameIDType;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.cache.Cache;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER;
import static eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec.Definitions.PERSON_IDENTIFIER;
import static eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec.Definitions.REPV_LEGAL_PERSON_IDENTIFIER;
import static eu.eidas.auth.commons.protocol.eidas.spec.EidasSpec.Definitions.REPV_PERSON_IDENTIFIER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;

import static org.hamcrest.core.Is.isA;

/**
 * Test class for {@link AUCONNECTORSAML}
 */
@RunWith(MockitoJUnitRunner.class)
public class AUCONNECTORSAMLTest {

    private static final String SAML_RESPONSE = "testResponse";
    private static final String IN_RESPONSE_TO_ID = "InResponseToID";
    private static final String DESTINATION = "proxyService";
    private static final String RESPONSE_ID = "repID";
    private static final String REQUEST_ID = "requestID";
    private static final String ISSUER = "issuer";
    private static final String SUBJECT = "subject";
    private static final String SUBJECT_FORMAT_ID = NameID.UNSPECIFIED;
    private static final String INVALID_SUBJECT_FORMAT_ID = "invalid_subject_format_id";
    private static final String CITIZEN_COUNTRY_CODE = "EU";
    private static final String REMOTE_IP = "testIP";
    private static final String RELAY_STATE = "relayState";

    private AUCONNECTORSAML auConnectorSaml;
    private Properties properties;
    private Set<String> idpNameIDFormats;
    private ApplicationContext oldContext = null;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private AUCONNECTORUtil mockAuthConnectorUtil;
    @Mock
    private ApplicationContext mockApplicationContext;
    @Mock
    private ILightRequest mockILightRequest;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        SessionHolder.clear();
        initProperties();
        idpNameIDFormats = new HashSet(Arrays.asList(NameID.PERSISTENT, NameID.TRANSIENT, NameID.UNSPECIFIED));

        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        Mockito.when(mockAuthConnectorUtil.getConfigs()).thenReturn(properties);
        auConnectorSaml = new AUCONNECTORSAML();

        Field validatorBoolean = auConnectorSaml.getClass().getDeclaredField("validatePrefixCountryCodeIdentifiers");
        validatorBoolean.setAccessible(true);
        validatorBoolean.set(auConnectorSaml, true);

        mockProtocolEngine(null);
    }

    @After
    public void tearDown(){
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    private void initProperties() {
        properties = new Properties();
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest} is correctly executed when Connector module is active.
     *
     * Must succeed.
     */
    @Test
    public void processSpRequestWithActiveConnectorModule() throws EIDASMetadataException {
        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final LightRequest LightRequest = buildLightRequest(requestedLoAs).build();
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        HttpSession mockSession = Mockito.mock(HttpSession.class);
        HttpSession originalSession = SessionHolder.getId();
        SessionHolder.setId(mockSession);

        IAuthenticationRequest expectedRequest = mockAuthenticationRequest(LightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        IAuthenticationRequest request = auConnectorSaml.processSpRequest(LightRequest, mockWebRequest);
        assertRequeststate(mockWebRequest.getRequestState(), expectedRequest);
        Assert.assertNotNull(request);
        Assert.assertEquals(expectedRequest.getCitizenCountryCode(), request.getCitizenCountryCode());
        Assert.assertEquals(expectedRequest.getLevelsOfAssurance(), request.getLevelsOfAssurance());
        Assert.assertEquals(getNotifiedLoA(requestedLoAs), ((EidasAuthenticationRequest) request).getEidasLevelOfAssurance());
        Assert.assertEquals(getNotNotifiedLoAs(requestedLoAs), ((EidasAuthenticationRequest) request).getNonNotifiedLevelsOfAssurance());

        SessionHolder.setId(originalSession);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} verifies if the request correctly contains a
     * requester id
     *
     * Must succeed
     */
    @Test
    public void processSpRequestWithRequesterId() throws EIDASMetadataException {
        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(true);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        String requesterId = "testForRequestId";

        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        LightRequest lightRequest = buildLightRequest(requestedLoAs)
                .requesterId(requesterId)
                .spType(SpType.PRIVATE.getValue())
                .build();

        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest expectedRequest = mockAuthenticationRequest(lightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        IAuthenticationRequest request = auConnectorSaml.processSpRequest(lightRequest, mockWebRequest);
        assertRequeststate(mockWebRequest.getRequestState(), expectedRequest);
        Assert.assertNotNull(request);
        Assert.assertEquals(expectedRequest.getCitizenCountryCode(), request.getCitizenCountryCode());
        Assert.assertEquals(expectedRequest.getLevelsOfAssurance(), request.getLevelsOfAssurance());
        Assert.assertEquals(getNotifiedLoA(requestedLoAs), ((EidasAuthenticationRequest) request).getEidasLevelOfAssurance());
        Assert.assertEquals(getNotNotifiedLoAs(requestedLoAs), ((EidasAuthenticationRequest) request).getNonNotifiedLevelsOfAssurance());

        String expectedRequesterId = requesterId;
        Assert.assertEquals(expectedRequesterId, request.getRequesterId());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when request has no requester id
     * and requesterId is needed
     * but request is made from Public Service provider
     *
     * Must succeed
     */
    @Test
    public void processSpRequestWithRequesterIdWhenNotNeeded() throws EIDASMetadataException {
        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(true);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        List<String> requestedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        LightRequest lightRequest = buildLightRequest(requestedLoAs)
                .spType(SpType.PUBLIC.getValue())
                .build();
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest expectedRequest = mockAuthenticationRequest(lightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        IAuthenticationRequest request = auConnectorSaml.processSpRequest(lightRequest, mockWebRequest);
        assertRequeststate(mockWebRequest.getRequestState(), expectedRequest);
        Assert.assertNotNull(request);
        Assert.assertEquals(expectedRequest.getCitizenCountryCode(), request.getCitizenCountryCode());
        Assert.assertEquals(expectedRequest.getLevelsOfAssurance(), request.getLevelsOfAssurance());
        Assert.assertEquals(getNotifiedLoA(requestedLoAs), ((EidasAuthenticationRequest) request).getEidasLevelOfAssurance());
        Assert.assertEquals(getNotNotifiedLoAs(requestedLoAs), ((EidasAuthenticationRequest) request).getNonNotifiedLevelsOfAssurance());

        Assert.assertNull(request.getRequesterId());
    }

    /**
     * Test method for 
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when the requesterId is invalid (more than 1024 characters)
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}  when a requesterId is needed
     * Must fail
     */
    @Test()
    public void processSpRequestWithRequesterIdInvalid() throws EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorMessage()));

        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(true);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        String requesterId = "http://www.testurl.com/" + StringUtils.repeat("TooLong-RequesterId-Value", 42);
        Mockito.when(mockILightRequest.getRequesterId()).thenReturn(requesterId);

        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when a requesterId is needed
     * but requesterId is invalid (not valid URI)
     *
     * Must fail
     */
    @Test()
    public void processSpRequestWithRequesterIdInvalidURI() throws EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorMessage()));

        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(true);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        String requesterId = "http://www.invalidtesturl.com#frag1#frag2";
        Mockito.when(mockILightRequest.getRequesterId()).thenReturn(requesterId);

        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for 
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when a requesterId is needed
     * but requesterId is missing in the request
     *
     * Must fail
     */
    @Test()
    public void processSpRequestWithRequesterIdMissing() throws EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorMessage()));

        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(true);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());

        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * but requesterId is present and invalid (more than 1024 characters)
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} when a requesterId is not needed
     * Must fail
     */
    @Test
    public void processSpRequestWithRequesterIdNotNeededButInvalid() throws EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.SPROVIDER_INVALID_REQUESTERID.errorMessage()));

        properties.setProperty(EidasParameterKeys.EIDAS_NUMBER.toString(), TestingConstants.ONE_CONS.toString());

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.isRequesterIdFlag()).thenReturn(false);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        String requesterId = "http://www.testurl.com/" + StringUtils.repeat("TooLong-RequesterId-Value", 42);
        Mockito.when(mockILightRequest.getRequesterId()).thenReturn(requesterId);

        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata only supports EIDAS Protocol version 1.1
     * 
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} removes Non-Notified LoAs
     * <p>
     * Must succeed
     */
    @Test()
    public void processSpRequestBackwardsCompatibleWithNonNotifiedLoa() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.1");
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);

        final List<ILevelOfAssurance> expectedLoAs = Collections.singletonList(LevelOfAssurance.build(ILevelOfAssurance.EIDAS_LOA_HIGH));
        Assert.assertEquals(expectedLoAs, iAuthenticationRequest.getLevelsOfAssurance());
        Assert.assertEquals(1, iAuthenticationRequest.getLevelsOfAssurance().size());
        Assert.assertEquals(LevelOfAssuranceComparison.MINIMUM, ((EidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata only supports EIDAS Protocol version 1.1
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} converts null NameIdFormat into unspecified URI
     * <p>
     * Must succeed
     */
    @Test()
    public void processSpRequestBackwardsCompatibleWithNameIdFormatUnspecified() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.1");
        final String nameIdFormat = null;

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        Mockito.when(mockILightRequest.getNameIdFormat()).thenReturn(nameIdFormat);
        mockLevelsOfAssurance(mockILightRequest, Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH));
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);

        Assert.assertEquals(NameID.UNSPECIFIED, iAuthenticationRequest.getNameIdFormat());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when all there is are Non-Notified LoAs
     * when service metadata only supports EIDAS Protocol version 1.1
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} removes Non-Notified LoAs
     * <p>
     * Must fail
     */
    @Test()
    public void processSpRequestBackwardsCompatibleWithOnlyNonNotifiedLoa() throws EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.toString());

        final List<String> protocolVersions = Arrays.asList("1.1");
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA");

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata claims to still support EIDAS Protocol version 1.1
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} 
     * checks if the first Published LoA is the highest
     * <p>
     * Must fail
     */
    @Test()
    public void processSpRequestBackwardsCompatibleFirstLoANotHighestNotified() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.1", "1.2");
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.toString());

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA");
        final List<String> publishedLoAs = Arrays.asList(
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                "loa:testLoA");

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata drops support for EIDAS Protocol version 1.1
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest) }
     * does not check if the first Published LoA is the highest
     * <p>
     * Must succeed
     */
    @Test()
    public void processSpRequestForwardsCompatibleFirstLoANotHighestNotified() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.2");

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA");
        final List<String> publishedLoAs = Arrays.asList(
                ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL,
                ILevelOfAssurance.EIDAS_LOA_HIGH,
                "loa:testLoA");

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);

        final List<ILevelOfAssurance> expectedLoAs = Collections.singletonList(LevelOfAssurance.build("loa:testLoA"));
        Assert.assertEquals(expectedLoAs, iAuthenticationRequest.getLevelsOfAssurance());
        Assert.assertEquals(1, iAuthenticationRequest.getLevelsOfAssurance().size());
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, ((EidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when all there is are Non-Notified LoAs
     * when service metadata only supports EIDAS Protocol version 1.1
     *
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} removes Non-Notified LoAs
     * <p>
     * Must fail
     */
    @Test()
    public void processSpRequestBackwardsCompatibleNoNotifiedLevels() throws EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.SERVICE_PROVIDER_INVALID_LOA.toString());

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA");
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH);

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata supports EIDAS Protocol version 1.1 and 1.2 (highest 1.2)
     * 
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} does not remove any LoAs
     * <p>
     * Must succeed
     */
    @Test()
    public void processSpRequestCurrentCompatibleWithNonNotifiedLoa() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.2", "1.1");
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);

        final List<ILevelOfAssurance> expectedLoAs = Arrays.asList(LevelOfAssurance.build(ILevelOfAssurance.EIDAS_LOA_HIGH), LevelOfAssurance.build("loa:testLoA"));
        Assert.assertEquals(expectedLoAs, iAuthenticationRequest.getLevelsOfAssurance());
        Assert.assertEquals(2, iAuthenticationRequest.getLevelsOfAssurance().size());
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, ((EidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when service metadata supports EIDAS Protocol version 1.1 and 1.2 or higher (eg 23.2)
     * 
     * In this method, we check if the {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)} does not remove any LoAs
     * <p>
     * Must succeed
     */
    @Test()
    public void processSpRequestForwardsCompatibleWithNonNotifiedLoa() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.1", "1.2", "23.3");
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(true).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);

        final List<ILevelOfAssurance> expectedLoAs = Arrays.asList(
                LevelOfAssurance.build(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL),
                LevelOfAssurance.build("loa:testLoA"),
                LevelOfAssurance.build(ILevelOfAssurance.EIDAS_LOA_HIGH));
        Assert.assertEquals(expectedLoAs, iAuthenticationRequest.getLevelsOfAssurance());
        Assert.assertEquals(3, iAuthenticationRequest.getLevelsOfAssurance().size());
        Assert.assertEquals(LevelOfAssuranceComparison.EXACT, ((EidasAuthenticationRequest) iAuthenticationRequest).getLevelOfAssuranceComparison());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when request state does not contain levels of assurance
     * <p>
     * Must fail
     */
    @Test()
    public void testProcessSpRequestNoLoaInRequestState() throws EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);

        final List<String> protocolVersions = Arrays.asList("1.1", "1.2", "23.3");
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL);

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();

        EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        mockILightRequest(mockILightRequest);
        Mockito.when(mockILightRequest.getSpType()).thenReturn(SpType.PRIVATE.getValue());
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();
        Mockito.doReturn(false).when(mockAuthConnectorUtil).validateRequestHasLoas(mockWebRequest.getRequestState());

        IAuthenticationRequest iAuthenticationRequest = auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }
    /**
     * Test method for
     * {@link AUCONNECTORSAML#processSpRequest(ILightRequest, WebRequest)}.
     * when an EIDASMetadataException is thrown during the call to
     * {@link MetadataFetcherI#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}
     * <p>
     * Must fail
     */
    @Test()
    public void testProcessSpRequestErrorFetchingMetadata() throws EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);

        MetadataFetcherI mockFetcher = Mockito.mock(MetadataFetcherI.class);
        auConnectorSaml.setMetadataFetcher(mockFetcher);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);

        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.getCountryCode(Mockito.any(), Mockito.any())).thenCallRealMethod();
        Mockito.when(mockFetcher.getEidasMetadata(any(),any(),any())).thenThrow(EIDASMetadataException.class);

        mockILightRequest(mockILightRequest);
        WebRequest mockWebRequest = newWebRequestWithSAMLResponse();

        auConnectorSaml.processSpRequest(mockILightRequest, mockWebRequest);
    }


    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the happy flow of the processing of a ProxyService response.
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseHappyFlow() throws EIDASMetadataException {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertNotNull(authenticationExchange.getConnectorResponse());
        Assert.assertEquals(response.getId(), authenticationExchange.getConnectorResponse().getId());
        Assert.assertEquals(response.getIssuer(), authenticationExchange.getConnectorResponse().getIssuer());
        assertResponseStatus(response.getStatus(), authenticationExchange.getConnectorResponse().getStatus());
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the received GenderAttribute is valid with the protocol versions we support.
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseGenderAttributeValidForProtocolVersion() throws AttributeValueMarshallingException, EIDASMetadataException {
        AttributeDefinition genderAttributeDefinition = EidasSpec.Definitions.GENDER;
        AttributeValue genderUnspecified = genderAttributeDefinition.getAttributeValueMarshaller()
                .unmarshal(Gender.UNSPECIFIED.getValue(), false);
        ImmutableAttributeMap attributes = ImmutableAttributeMap.of(genderAttributeDefinition, genderUnspecified);

        String protocolVersions = "1.2;1.1";
        properties.put(EIDASValues.EIDAS_PROTOCOL_VERSION.toString(), protocolVersions);

        IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", attributes);
        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");

        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA"));

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertNotNull(authenticationExchange.getConnectorResponse());
        Assert.assertEquals(response.getId(), authenticationExchange.getConnectorResponse().getId());
        Assert.assertEquals(response.getIssuer(), authenticationExchange.getConnectorResponse().getIssuer());
        assertResponseStatus(response.getStatus(), authenticationExchange.getConnectorResponse().getStatus());
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the received GenderAttribute is not valid with the protocol versions we support.
     *
     * Must fail
     */
    @Test
    public void processProxyServiceResponseGenderAttributeNotValidForProtocolVersion() throws AttributeValueMarshallingException, EIDASMetadataException {
        expectedException.expect(InvalidParameterEIDASException.class);
        expectedException.expectMessage("Invalid Gender attribute for the protocol versions");

        AttributeDefinition genderAttributeDefinition = EidasSpec.Definitions.GENDER;
        AttributeValue genderUnspecified = genderAttributeDefinition.getAttributeValueMarshaller()
                .unmarshal(Gender.UNSPECIFIED.getValue(), false);
        ImmutableAttributeMap attributes = ImmutableAttributeMap.of(genderAttributeDefinition, genderUnspecified);

        String protocolVersions = "1.1";
        properties.put(EIDASValues.EIDAS_PROTOCOL_VERSION.toString(), protocolVersions);

        IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", attributes); //countrycode eu
        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");

        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA"));

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);

        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * When the first two characters of the personIdentifier match the country code in the Request
     * <p>
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseIdentifierCountryCodeMatching() throws EIDASMetadataException, AttributeValueMarshallingException {
        final String proxyServiceCountryCode = "BE";
        final IAuthenticationResponse response = mockAuthenticationResponse(
                proxyServiceCountryCode, "loa:testLoA", personIdentifierAttributeMap("BE/CA/12345"));

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(proxyServiceCountryCode);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(eq("BE"))).thenReturn("uri:belgium");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest
                = mockAuthenticationRequest(mockILightRequest, proxyServiceCountryCode, NameID.UNSPECIFIED);

        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        final AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertNotNull(authenticationExchange.getConnectorResponse());
        Assert.assertEquals(response.getId(), authenticationExchange.getConnectorResponse().getId());
        Assert.assertEquals(response.getIssuer(), authenticationExchange.getConnectorResponse().getIssuer());
        assertResponseStatus(response.getStatus(), authenticationExchange.getConnectorResponse().getStatus());
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * When the first two characters of the personIdentifiers all match the country code in the Request
     * <p>
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseAllIdentifiersCountryCodeMatching() throws EIDASMetadataException, AttributeValueMarshallingException {

        final String proxyServiceCountryCode = "BE";

        final IAuthenticationResponse response = mockAuthenticationResponse(proxyServiceCountryCode, "loa:testLoA",
                new ImmutableAttributeMap.Builder()
                        .put(PERSON_IDENTIFIER, setAttributeDefinitionStringValue(PERSON_IDENTIFIER, "BE/CA/12345"))
                        .put(LEGAL_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(LEGAL_PERSON_IDENTIFIER, "BE/CA/12345"))
                        .put(REPV_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(REPV_PERSON_IDENTIFIER, "BE/CA/12345"))
                        .put(REPV_LEGAL_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(REPV_LEGAL_PERSON_IDENTIFIER, "BE/CA/12345"))
                        .build());

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(proxyServiceCountryCode);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(eq("BE"))).thenReturn("uri:belgium");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest
                = mockAuthenticationRequest(mockILightRequest, proxyServiceCountryCode, NameID.UNSPECIFIED);

        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        final AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertNotNull(authenticationExchange.getConnectorResponse());
        Assert.assertEquals(response.getId(), authenticationExchange.getConnectorResponse().getId());
        Assert.assertEquals(response.getIssuer(), authenticationExchange.getConnectorResponse().getIssuer());
        assertResponseStatus(response.getStatus(), authenticationExchange.getConnectorResponse().getStatus());
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * When prefix of the personIdentifier has an invalid format
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseIdentifierCountryCodeFormatInvalid() throws EIDASMetadataException, AttributeValueMarshallingException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));

        final String proxyServiceCountryCode = "BE";

        final IAuthenticationResponse response = mockAuthenticationResponse(proxyServiceCountryCode, "loa:testLoA",
                new ImmutableAttributeMap.Builder()
                        .put(PERSON_IDENTIFIER, setAttributeDefinitionStringValue(PERSON_IDENTIFIER, "USA/USA/12345"))
                        .build());

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(proxyServiceCountryCode);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(eq("BE"))).thenReturn("uri:belgium");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest
                = mockAuthenticationRequest(mockILightRequest, proxyServiceCountryCode, NameID.UNSPECIFIED);

        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * When the first two characters of the personIdentifier does not match the country code in the Request
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseIdentifierCountryCodeNotMatchingServiceMetadata() throws AttributeValueMarshallingException, EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));

        final String proxyServiceCountryCode = "BE";
        final IAuthenticationResponse response = mockAuthenticationResponse(
                proxyServiceCountryCode, "loa:testLoA", personIdentifierAttributeMap("CA/CA/12345"));

        final String protocolVersions = "1.2";
        properties.put(EIDASValues.EIDAS_PROTOCOL_VERSION.toString(), protocolVersions);

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(proxyServiceCountryCode);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("uri:belgium");

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);

        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, proxyServiceCountryCode, NameID.PERSISTENT);
        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * When the first two characters of the personIdentifiers, for at least one does not match the country code in the Request
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseOneIdentifierCountryCodeNotMatchingServiceMetadata() throws AttributeValueMarshallingException, EIDASSAMLEngineException, EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage(EidasErrors.get(EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML.errorMessage()));

        final String proxyServiceCountryCode = "BE";
        final IAuthenticationResponse response = mockAuthenticationResponse(proxyServiceCountryCode, "loa:testLoA",
                new ImmutableAttributeMap.Builder()
                        .put(PERSON_IDENTIFIER, setAttributeDefinitionStringValue(PERSON_IDENTIFIER, "BE/CA/12345"))
                        .put(LEGAL_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(LEGAL_PERSON_IDENTIFIER, "BE/CA/12345"))
                        .put(REPV_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(REPV_PERSON_IDENTIFIER, "CB/CA/12345"))
                        .put(REPV_LEGAL_PERSON_IDENTIFIER, setAttributeDefinitionStringValue(REPV_LEGAL_PERSON_IDENTIFIER, "BE/CA/12345"))
                        .build());

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        Mockito.when(mockProtocolProcessor.getMetadataNodeCountryCode(any())).thenReturn(Optional.empty());
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("uri:belgium");

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);

        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check if we correctly return an error because of incoherent LoA in response.
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseWithLoAsIncoherentToRequestLoAs() throws EIDASMetadataException {
        expectedException.expect(InvalidSessionEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE.toString());

        final List<String> requestLoAs, metadataLoAs;
        requestLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        metadataLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:NotNotifiedLoA");
        final IAuthenticationResponse response = mockAuthenticationResponse(
                CITIZEN_COUNTRY_CODE, "loa:NotNotifiedLoA", mockAttributeMap());

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(metadataLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the happy flow of the processing of a ProxyService response.
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseWithLoAsIncoherentToPublishedLoAs() throws EIDASMetadataException {
        expectedException.expect(InvalidSessionEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED.toString());

        final List<String> requestLoAs, metadataLoAs;
        requestLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:NotNotifiedLoA");
        metadataLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final IAuthenticationResponse response = mockAuthenticationResponse(
                CITIZEN_COUNTRY_CODE, "loa:NotNotifiedLoA", mockAttributeMap());

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(metadataLoAs);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the happy flow of the processing of a ProxyService response.
     * <p>
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithLoAsMissingExactPublishedLoAsBackwardsCompatible() throws EIDASMetadataException {
        final List<String> protocolVersions = Arrays.asList("1.1");
        final List<String> requestLoAs, metadataLoAs;
        requestLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_LOW, "loa:NotNotifiedLoA");
        metadataLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_LOW);
        final IAuthenticationResponse response = mockAuthenticationResponse(
                CITIZEN_COUNTRY_CODE, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, mockAttributeMap());

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(metadataLoAs);
        Mockito.when(mockedMetadataParameters.getEidasProtocolVersions()).thenReturn(protocolVersions);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertNotNull(authenticationExchange.getConnectorResponse());
        Assert.assertEquals(response.getId(), authenticationExchange.getConnectorResponse().getId());
        Assert.assertEquals(response.getIssuer(), authenticationExchange.getConnectorResponse().getIssuer());
        assertResponseStatus(response.getStatus(), authenticationExchange.getConnectorResponse().getStatus());
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check the happy flow of the processing of a ProxyService response.
     * <p>
     * Must fail
     */
    @Test
    public void processProxyServiceResponseWithLoAsMissingExactPublishedLoAs() throws EIDASMetadataException {
        expectedException.expect(InvalidSessionEIDASException.class);
        expectedException.expectMessage(EidasErrorKey.INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED.toString());

        final List<String> requestLoAs, metadataLoAs;
        requestLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_LOW, "loa:NotNotifiedLoA");
        metadataLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH, ILevelOfAssurance.EIDAS_LOA_LOW);
        final IAuthenticationResponse response = mockAuthenticationResponse(
                CITIZEN_COUNTRY_CODE, ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL, mockAttributeMap());

        final ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        final ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(metadataLoAs);

        final WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestLoAs);
        final StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        final Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        final IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        final StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        final Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check if the NameID format in the eIDAS response matches the NameID format from the eIDAS request
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithMatchingNameIdFormat() throws EIDASMetadataException {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertEquals(authRequest, authenticationExchange.getStoredRequest());
        Assert.assertEquals(authenticationExchange.getConnectorResponse().getStatusCode(), EIDASStatusCode.SUCCESS_URI.toString());
        //Assert.assertEquals(authenticationRequest.getNameIdFormat(), response.getSubjectNameIdFormat());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * In this method, we check if the NameID format in the eIDAS response does not match with the NameID format from the eIDAS request
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithRequestNameIdFormatDifferentFromResponseNameIdFormat() throws EIDASMetadataException {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameID.EMAIL);
        Mockito.when(response.getInResponseToId()).thenReturn(IN_RESPONSE_TO_ID);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        properties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.toString(), NameID.EMAIL);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);


        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertEquals(authenticationExchange.getConnectorResponse().getStatusCode(), EIDASStatusCode.RESPONDER_URI.toString());
        Assert.assertEquals(authenticationExchange.getConnectorResponse().getSubStatusCode(), EIDASSubStatusCode.INVALID_NAMEID_POLICY_URI.toString());
        Assert.assertNull(response.getStatusMessage());
        Assert.assertNotEquals(authenticationRequest.getNameIdFormat(), response.getSubjectNameIdFormat());
    }


    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when the NameIdFormat or Request and Response {@link NameID#PERSISTENT}
     * when the NameIdFormat value size is bigger than 256 characters.
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithNameIdFormatPersistentAndBiggerThanMaxSize256Characters() throws EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage("invalid.service.samlresp");

        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameIDType.PERSISTENT);

        String randomString = RandomStringUtils.random(500);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.PERSISTENT);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }


    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when the NameIdFormat or Request and Response {@link NameID#TRANSIENT}
     * when the NameIdFormat value size is bigger than 256 characters.
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithNameIdFormatTransientAndBiggerThanMaxSize256Characters() throws EIDASMetadataException {
        expectedException.expect(InternalErrorEIDASException.class);
        expectedException.expectMessage("invalid.service.samlresp");

        final int SUBJECT_CHARACTERS_LENGTH = 500;

        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        String randomString = RandomStringUtils.random(SUBJECT_CHARACTERS_LENGTH);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameIDType.TRANSIENT);

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.TRANSIENT);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }


    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when the NameIdFormat or Request and Response is neither {@link NameID#PERSISTENT} or {@link NameID#TRANSIENT} or {@link NameID#ENTITY}
     * when the NameIdFormat value size is bigger than all maximum values 256 or 1024 characters.
     *
     * Must succeed
     */
    @Test
    public void processProxyServiceResponseWithNameIdFormatTypeNotPersistentOrTransientOrEntity() throws EIDASMetadataException {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameIDType.EMAIL);
        Mockito.when(response.getInResponseToId()).thenReturn(IN_RESPONSE_TO_ID);

        String randomString = RandomStringUtils.random(3000);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        properties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.toString(), NameID.EMAIL);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertEquals(EIDASStatusCode.SUCCESS_URI.toString(), authenticationExchange.getConnectorResponse().getStatusCode());
        Assert.assertNull(response.getStatusMessage());
        Assert.assertNotEquals(authenticationRequest.getNameIdFormat(), response.getSubjectNameIdFormat());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when an exception is thrown during unmarshalling
     *
     * Must fail
     */
    @Test
    public void testProcessProxyServiceResponseUnmarshallException() {
        expectedException.expect(InternalErrorEIDASException.class);

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);

        try {
            Mockito.when(mockProtocolEngine.unmarshallResponse(SAML_RESPONSE.getBytes()))
                    .thenThrow(EIDASSAMLEngineException.class);
        } catch (Exception e) {
            Assert.fail("mock failed, test invalid");
        }

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when audience restriction fails validation
     *
     * Must fail
     */
    @Test
    public void testProcessProxyServiceResponseWithAudienceRestriction() throws EIDASMetadataException {
        expectedException.expect(InvalidSessionEIDASException.class);

        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getAudienceRestriction()).thenReturn("invalid");

        String randomString = RandomStringUtils.random(3000);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);
        final List<String> publishedLoAs = Arrays.asList(ILevelOfAssurance.EIDAS_LOA_HIGH, "loa:testLoA");

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(anyString())).thenReturn("serviceUrl");
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        properties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.toString(), NameID.EMAIL);
        final EidasMetadataParametersI mockedMetadataParameters = mockMetadataParameters();
        Mockito.when(mockedMetadataParameters.getAssuranceLevels()).thenReturn(publishedLoAs);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when there is no status message code in the failure message
     *
     * Must succeed
     */
    @Test
    public void testProcessProxyServiceResponseFailureNoStatusMessageCode() {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameIDType.EMAIL);
        Mockito.when(response.getInResponseToId()).thenReturn(IN_RESPONSE_TO_ID);
        Mockito.when(response.isFailure()).thenReturn(true);
        Mockito.when(response.getStatusMessage()).thenReturn(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString());

        MessageSource mockMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        auConnectorSaml.setMessageSource(mockMessageSource);

        String randomString = RandomStringUtils.random(3000);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        properties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.toString(), NameID.EMAIL);;

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertEquals(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.toString(), authenticationExchange.getConnectorResponse().getStatusMessage());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when there is a status message code in the failure message
     *
     * Must succeed
     */
    @Test
    public void testProcessProxyServiceResponseFailureHasStatusMessageCode() {
        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());
        Mockito.when(response.getSubjectNameIdFormat()).thenReturn(NameIDType.EMAIL);
        Mockito.when(response.getInResponseToId()).thenReturn(IN_RESPONSE_TO_ID);
        Mockito.when(response.isFailure()).thenReturn(true);
        Mockito.when(response.getStatusMessage()).thenReturn(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorCode());

        MessageSource mockMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        auConnectorSaml.setMessageSource(mockMessageSource);

        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenReturn(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage());

        String randomString = RandomStringUtils.random(3000);
        Mockito.when(response.getSubject()).thenReturn(randomString);
        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        auConnectorSaml.setConnectorMetadataUrl(ISSUER);
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.checkNotPresentInCache(any(), anyString())).thenReturn(true);
        properties.setProperty(EidasParameterKeys.EIDAS_CONNECTOR_NAMEID_FORMATS.toString(), NameID.EMAIL);;

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        AuthenticationExchange authenticationExchange = auConnectorSaml
                .processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);

        Assert.assertNotNull(authenticationExchange);
        Assert.assertEquals(EidasErrorKey.AUTHENTICATION_FAILED_ERROR.errorMessage(), authenticationExchange.getConnectorResponse().getStatusMessage());
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when the cache is empty
     *
     * Must fail
     */
    @Test
    public void testProcessProxyServiceResponseEmptyCache() {
        expectedException.expect(InvalidSessionEIDASException.class);

        final IAuthenticationResponse response =
                mockAuthenticationResponse(CITIZEN_COUNTRY_CODE, "loa:testLoA", mockAttributeMap());

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);
        mockResponseUnmarshalling(mockProtocolEngine, response);

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(null);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#processProxyServiceResponse(WebRequest, Cache, Cache)}
     * when the InResponseToId is blank
     *
     * Must fail
     */
    @Test
    public void testProcessProxyServiceResponseBlankInResponseToId() {
        expectedException.expect(InvalidSessionEIDASException.class);

        final List<String> requestedLoAs = Arrays.asList("loa:testLoA", ILevelOfAssurance.EIDAS_LOA_HIGH);

        ProtocolProcessorI mockProtocolProcessor = mockProtocolProcessor(CITIZEN_COUNTRY_CODE);
        ProtocolEngineI mockProtocolEngine = mockProtocolEngine(mockProtocolProcessor);

        Correlated mockCorrelated = Mockito.mock(Correlated.class);
        Mockito.when(mockCorrelated.getInResponseToId()).thenReturn("");
        try {
            Mockito.when(mockProtocolEngine.unmarshallResponse(SAML_RESPONSE.getBytes()))
                    .thenReturn(mockCorrelated);
        } catch (Exception e) {
            Assert.fail("mock failed, test invalid");
        }

        WebRequest webRequest = newWebRequestWithSAMLResponse();
        mockILightRequest(mockILightRequest);
        mockLevelsOfAssurance(mockILightRequest, requestedLoAs);
        StoredLightRequest lightRequest = mockStoredLightRequest(mockILightRequest);
        Cache<String, StoredLightRequest> lightRequestCache = mockCache(lightRequest);
        IAuthenticationRequest authenticationRequest = mockAuthenticationRequest(mockILightRequest, CITIZEN_COUNTRY_CODE, NameID.UNSPECIFIED);
        StoredAuthenticationRequest authRequest = mockStoredAuthenticationRequest(authenticationRequest);
        Cache<String, StoredAuthenticationRequest> authenticationRequestCache = mockCache(authRequest);

        auConnectorSaml.processProxyServiceResponse(webRequest, authenticationRequestCache, lightRequestCache);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#extractErrorMessage(String, String)}
     *
     * Must succeed
     */
    @Test
    public void testExtractErrorMessage() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MessageSource mockMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        auConnectorSaml.setMessageSource(mockMessageSource);

        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenReturn("newmessage");

        Method extractErrorMessageMethod = AUCONNECTORSAML.class.getDeclaredMethod("extractErrorMessage", String.class, String.class);
        extractErrorMessageMethod.setAccessible(true);
        String result = (String) extractErrorMessageMethod.invoke(auConnectorSaml,"message","authenticationFailed");
        Assert.assertNotEquals("message",result);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#extractErrorMessage(String, String)}
     * When the error message is not found
     *
     * Must succeed
     */
    @Test
    public void testExtractErrorMessageNotFound() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        MessageSource mockMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        auConnectorSaml.setMessageSource(mockMessageSource);

        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenThrow(NoSuchMessageException.class);

        Method extractErrorMessageMethod = AUCONNECTORSAML.class.getDeclaredMethod("extractErrorMessage", String.class, String.class);
        extractErrorMessageMethod.setAccessible(true);
        String result = (String) extractErrorMessageMethod.invoke(auConnectorSaml,"message","authenticationFailed");
        Assert.assertEquals("message",result);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#checkIdentifierFormat(IAuthenticationResponse)}
     * When format is legal person
     *
     * Must succeed
     */
    @Test
    public void testCheckIdentifierFormatLegalPerson() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        IAuthenticationResponse mockResponse = Mockito.mock(IAuthenticationResponse.class);
        ImmutableAttributeMap mockAttributes = Mockito.mock(ImmutableAttributeMap.class);
        AttributeValue<String> legalIdentifierAttribute = new StringAttributeValue("CA/CA/12345");
        ImmutableSet legalIdentifier = ImmutableSet.of(legalIdentifierAttribute);
        Mockito.when(mockResponse.getAttributes()).thenReturn(mockAttributes);
        Mockito.when(mockAttributes.getAttributeValuesByNameUri(EidasSpec.Definitions.PERSON_IDENTIFIER.getNameUri().toASCIIString()))
                .thenReturn(null);
        Mockito.when(mockAttributes.getAttributeValuesByNameUri(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER.getNameUri().toASCIIString()))
                .thenReturn(legalIdentifier);

        Method checkIdentifierFormatMethod = AUCONNECTORSAML.class.getDeclaredMethod("checkIdentifierFormat", IAuthenticationResponse.class);
        checkIdentifierFormatMethod.setAccessible(true);
        checkIdentifierFormatMethod.invoke(auConnectorSaml,mockResponse);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#checkIdentifierFormat(IAuthenticationResponse)}
     * When format is invalid
     *
     * Must succeed
     */
    @Test
    public void testCheckIdentifierFormatLegalPersonInvalid() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        IAuthenticationResponse mockResponse = Mockito.mock(IAuthenticationResponse.class);
        ImmutableAttributeMap mockAttributes = Mockito.mock(ImmutableAttributeMap.class);
        AttributeValue<String> legalIdentifierAttribute = new StringAttributeValue("Invalid");
        ImmutableSet legalIdentifier = ImmutableSet.of(legalIdentifierAttribute);
        Mockito.when(mockResponse.getAttributes()).thenReturn(mockAttributes);
        Mockito.when(mockAttributes.getAttributeValuesByNameUri(EidasSpec.Definitions.PERSON_IDENTIFIER.getNameUri().toASCIIString()))
                .thenReturn(null);
        Mockito.when(mockAttributes.getAttributeValuesByNameUri(EidasSpec.Definitions.LEGAL_PERSON_IDENTIFIER.getNameUri().toASCIIString()))
                .thenReturn(legalIdentifier);

        Method checkIdentifierFormatMethod = AUCONNECTORSAML.class.getDeclaredMethod("checkIdentifierFormat", IAuthenticationResponse.class);
        checkIdentifierFormatMethod.setAccessible(true);
        checkIdentifierFormatMethod.invoke(auConnectorSaml,mockResponse);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#isExpectedProxyServiceCountryCode(String, ImmutableSet)}
     * When value is not present
     *
     * Must fail
     */
    @Test
    public void testIsExpectedProxyServiceCountryCode() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        ImmutableSet testSet = Mockito.mock(ImmutableSet.class);

        Method isExpectedProxyServiceCountryCodeMethod = AUCONNECTORSAML.class.getDeclaredMethod("isExpectedProxyServiceCountryCode", String.class, ImmutableSet.class);
        isExpectedProxyServiceCountryCodeMethod.setAccessible(true);
        isExpectedProxyServiceCountryCodeMethod.invoke(auConnectorSaml,"CA",testSet);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#generateAuthenticationRequest(String, IAuthenticationRequest, String)}
     * When request is not an instance of IEidasAuthenticationRequest
     *
     * Must fail
     */
    @Test
    public void testGenerateAuthenticationRequestNotInstanceOfEidasAuthenticationRequest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(InvalidParameterEIDASException.class));

        IAuthenticationRequest mockRequest = Mockito.mock(IAuthenticationRequest.class);

        Method generateAuthenticationRequestMethod = AUCONNECTORSAML.class.getDeclaredMethod("generateAuthenticationRequest", String.class, IAuthenticationRequest.class, String.class);
        generateAuthenticationRequestMethod.setAccessible(true);
        generateAuthenticationRequestMethod.invoke(auConnectorSaml,"instance",mockRequest,"CA");
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#generateAuthenticationRequest(String, IAuthenticationRequest, String)}
     *
     * Must succeed
     */
    @Test
    public void testGenerateAuthenticationRequest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, EIDASSAMLEngineException {
        Field applicationContextField = BeanProvider.class.getDeclaredField("CONTEXT");
        applicationContextField.setAccessible(true);
        ApplicationContext oldContext = (ApplicationContext) applicationContextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        final List<ILevelOfAssurance> requestedLoAs = Arrays.asList(LevelOfAssurance.builder()
                .type(LevelOfAssuranceType.NOTIFIED.toString())
                .value(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL)
                .build());

        FlowIdCache mockCache = Mockito.mock(FlowIdCache.class);
        Mockito.when(mockApplicationContext.getBean(NodeBeanNames.EIDAS_CONNECTOR_FLOWID_CACHE.toString())).thenReturn(mockCache);
        Mockito.when(mockCache.get(any())).thenReturn("mockFlowId");

        AUCONNECTORUtil mockUtil = Mockito.mock(AUCONNECTORUtil.class);
        Mockito.when(mockApplicationContext.getBean(AUCONNECTORUtil.class)).thenReturn(mockUtil);
        Properties mockProperties = Mockito.mock(Properties.class);
        Mockito.when(mockUtil.getConfigs()).thenReturn(mockProperties);
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString())).thenReturn("true");

        IAuthenticationRequest mockRequest = Mockito.mock(IEidasAuthenticationRequest.class);
        Mockito.when(mockRequest.getDestination()).thenReturn("mockDestination");
        Mockito.when(mockRequest.getId()).thenReturn("mockId");
        Mockito.when(mockRequest.getIssuer()).thenReturn(ISSUER);
        Mockito.when(mockRequest.getCitizenCountryCode()).thenReturn("CA");
        Mockito.when(mockRequest.getLevelsOfAssurance()).thenReturn(requestedLoAs);

        ProtocolEngineFactory mockProtocolEngineFactory = Mockito.mock(ProtocolEngineFactory.class);
        auConnectorSaml.setSamlServiceInstance(TestingConstants.SAML_INSTANCE_CONS.toString());
        auConnectorSaml.setNodeProtocolEngineFactory(mockProtocolEngineFactory);
        ProtocolEngineI mockProtocolEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(anyString())).thenReturn(mockProtocolEngine);
        IRequestMessage mockRequestMessage = Mockito.mock(IRequestMessage.class);
        Mockito.when(mockProtocolEngine.generateRequestMessage(any(),any())).thenReturn(mockRequestMessage);
        Mockito.when(mockRequestMessage.getRequest()).thenReturn(mockRequest);

        auConnectorSaml.setConnectorMetadataUrl("MetadataIssuer");
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(any())).thenReturn(ISSUER);

        Method generateAuthenticationRequestMethod = AUCONNECTORSAML.class.getDeclaredMethod("generateAuthenticationRequest", String.class, IAuthenticationRequest.class, String.class);
        generateAuthenticationRequestMethod.setAccessible(true);
        generateAuthenticationRequestMethod.invoke(auConnectorSaml,"instance",mockRequest,"CA");

        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
    }

    /**
     * Test method for
     * {@link AUCONNECTORSAML#generateAuthenticationRequest(String, IAuthenticationRequest, String)}
     * When {@link ProtocolEngineI#generateRequestMessage(IAuthenticationRequest, String)}protocol engine throws an {@link EIDASSAMLEngineException}
     *
     * Must fail
     */
    @Test
    public void testGenerateAuthenticationRequestErrorInEngine() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, EIDASSAMLEngineException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        Field applicationContextField = BeanProvider.class.getDeclaredField("CONTEXT");
        applicationContextField.setAccessible(true);
        ApplicationContext oldContext = (ApplicationContext) applicationContextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        final List<ILevelOfAssurance> requestedLoAs = Arrays.asList(LevelOfAssurance.builder()
                .type(LevelOfAssuranceType.NOTIFIED.toString())
                .value(ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL)
                .build());

        AUCONNECTORUtil mockUtil = Mockito.mock(AUCONNECTORUtil.class);
        Mockito.when(mockApplicationContext.getBean(AUCONNECTORUtil.class)).thenReturn(mockUtil);
        Properties mockProperties = Mockito.mock(Properties.class);
        Mockito.when(mockUtil.getConfigs()).thenReturn(mockProperties);
        Mockito.when(mockProperties.getProperty(EidasParameterKeys.METADATA_ACTIVE.toString())).thenReturn("true");

        IAuthenticationRequest mockRequest = Mockito.mock(IEidasAuthenticationRequest.class);
        Mockito.when(mockRequest.getDestination()).thenReturn("mockDestination");
        Mockito.when(mockRequest.getId()).thenReturn("mockId");
        Mockito.when(mockRequest.getIssuer()).thenReturn(ISSUER);
        Mockito.when(mockRequest.getCitizenCountryCode()).thenReturn("CA");
        Mockito.when(mockRequest.getLevelsOfAssurance()).thenReturn(requestedLoAs);

        ProtocolEngineFactory mockProtocolEngineFactory = Mockito.mock(ProtocolEngineFactory.class);
        auConnectorSaml.setSamlServiceInstance(TestingConstants.SAML_INSTANCE_CONS.toString());
        auConnectorSaml.setNodeProtocolEngineFactory(mockProtocolEngineFactory);
        ProtocolEngineI mockProtocolEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(anyString())).thenReturn(mockProtocolEngine);
        Mockito.when(mockProtocolEngine.generateRequestMessage(any(),any())).thenThrow(EIDASSAMLEngineException.class);

        auConnectorSaml.setConnectorMetadataUrl("MetadataIssuer");
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);
        Mockito.when(mockAuthConnectorUtil.loadConfigServiceMetadataURL(any())).thenReturn(ISSUER);

        Method generateAuthenticationRequestMethod = AUCONNECTORSAML.class.getDeclaredMethod("generateAuthenticationRequest", String.class, IAuthenticationRequest.class, String.class);
        generateAuthenticationRequestMethod.setAccessible(true);
        generateAuthenticationRequestMethod.invoke(auConnectorSaml,"instance",mockRequest,"CA");

        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
    }

    /**
     * Test method for {@link AUCONNECTORSAML#generateAuthenticationRequest(String, IAuthenticationRequest, String)}
     * When ConnectorMetadataUrl is empty
     *
     * Must fail
     */
    @Test
    public void testGenerateAuthenticationRequestEmptyMetadataUrl() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException, EIDASSAMLEngineException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        IAuthenticationRequest mockRequest = Mockito.mock(IEidasAuthenticationRequest.class);

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);

        Method generateAuthenticationRequestMethod = AUCONNECTORSAML.class.getDeclaredMethod("generateAuthenticationRequest", String.class, IAuthenticationRequest.class, String.class);
        generateAuthenticationRequestMethod.setAccessible(true);
        generateAuthenticationRequestMethod.invoke(auConnectorSaml,"instance",mockRequest,"CA");
    }

    /**
     * Test method for {@link AUCONNECTORSAML#checkAudienceRestriction(String, String)}
     *
     * Must succeed
     */
    @Test
    public void testCheckAudienceRestriction() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method checkAudienceRestrictionMethod = AUCONNECTORSAML.class.getDeclaredMethod("checkAudienceRestriction", String.class, String.class);
        checkAudienceRestrictionMethod.setAccessible(true);
        checkAudienceRestrictionMethod.invoke(auConnectorSaml,"issuer","issuer");
    }

    /**
     * Test method for {@link AUCONNECTORSAML#validateSamlCoreNameIdFormatIdentifier(String)}
     *
     * Must succeed
     */
    @Test
    public void testValidateSamlCoreNameIdFormatIdentifier() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);

        Method validateSamlCoreNameIdFormatIdentifierMethod = AUCONNECTORSAML.class.getDeclaredMethod("validateSamlCoreNameIdFormatIdentifier",String.class);
        validateSamlCoreNameIdFormatIdentifierMethod.setAccessible(true);
        validateSamlCoreNameIdFormatIdentifierMethod.invoke(auConnectorSaml,NameIDType.PERSISTENT);
    }

    /**
     * Test method for {@link AUCONNECTORSAML#validateSamlCoreNameIdFormatIdentifier(String)}
     * When nameIdFormatIdentifier is invalid
     *
     * Must fail
     */
    @Test
    public void testValidateSamlCoreNameIdFormatIdentifierUnknown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        expectedException.expectCause(isA(InternalErrorEIDASException.class));

        auConnectorSaml.setConnectorUtil(mockAuthConnectorUtil);

        Method validateSamlCoreNameIdFormatIdentifierMethod = AUCONNECTORSAML.class.getDeclaredMethod("validateSamlCoreNameIdFormatIdentifier",String.class);
        validateSamlCoreNameIdFormatIdentifierMethod.setAccessible(true);
        validateSamlCoreNameIdFormatIdentifierMethod.invoke(auConnectorSaml,"Unknown");
    }

    private ProtocolEngineI mockProtocolEngine(ProtocolProcessorI protocolProcessor) {
        ProtocolEngineFactory mockProtocolEngineFactory = Mockito.mock(ProtocolEngineFactory.class);
        auConnectorSaml.setSamlServiceInstance(TestingConstants.SAML_INSTANCE_CONS.toString());
        auConnectorSaml.setNodeProtocolEngineFactory(mockProtocolEngineFactory);

        ProtocolEngineI mockProtocolEngine = Mockito.mock(ProtocolEngineI.class);
        Mockito.when(mockProtocolEngineFactory.getProtocolEngine(anyString())).thenReturn(mockProtocolEngine);
        Mockito.when(mockProtocolEngine.getProtocolProcessor()).thenReturn(protocolProcessor);
        return mockProtocolEngine;
    }

    private ProtocolProcessorI mockProtocolProcessor(String metadataCountryCode) {
        ProtocolProcessorI mockProtocolProcessor = Mockito.mock(ProtocolProcessorI.class);

        Mockito.when(mockProtocolProcessor.getMetadataNodeCountryCode(ISSUER))
            .thenReturn(Optional.ofNullable(metadataCountryCode));

        return mockProtocolProcessor;
    }


    private void mockResponseUnmarshalling(ProtocolEngineI mockedProtocolEngine, IAuthenticationResponse authenticationResponse) {
        Correlated mockCorrelated = Mockito.mock(Correlated.class);
        Mockito.when(mockCorrelated.getInResponseToId()).thenReturn(IN_RESPONSE_TO_ID);
        try {
            Mockito.when(mockedProtocolEngine.unmarshallResponse(SAML_RESPONSE.getBytes()))
                    .thenReturn(mockCorrelated);
            Mockito.when(mockedProtocolEngine.validateUnmarshalledResponse(any(), anyString(), anyLong(), anyLong(), isNull()))
                    .thenReturn(authenticationResponse);
        } catch (Exception e) {
            Assert.fail("mock failed, test invalid");
        }
    }

    private EidasMetadataParametersI mockMetadataParameters() throws EIDASMetadataException {
        CachingMetadataFetcher mockCachingMetadataFetcher = mock(CachingMetadataFetcher.class);
        EidasMetadataParametersI mockEidasMetadataParameters = Mockito.mock(EidasMetadataParametersI.class);
        auConnectorSaml.setMetadataFetcher(mockCachingMetadataFetcher);
        Mockito.when(mockCachingMetadataFetcher.getEidasMetadata(anyString(), any(), any())).thenReturn(mockEidasMetadataParameters);

        EidasMetadataRoleParametersI mockEidasMetadataRoleParameters = Mockito.mock(EidasMetadataRoleParametersI.class);
        Mockito.when(mockEidasMetadataParameters.getRoleDescriptors()).thenReturn(Collections.singleton(mockEidasMetadataRoleParameters));
        Mockito.when(mockEidasMetadataRoleParameters.getRole()).thenReturn(MetadataRole.IDP);
        Mockito.when(mockEidasMetadataRoleParameters.getNameIDFormats()).thenReturn(idpNameIDFormats);

        Map<String, String> map = new LinkedHashMap<>();
        map.put("key", "serviceUrl");
        Mockito.when(mockEidasMetadataRoleParameters.getProtocolBindingLocations()).thenReturn(map);

        return mockEidasMetadataParameters;
    }

    private void mockILightRequest(ILightRequest mockLightRequest) {
        Mockito.when(mockLightRequest.getCitizenCountryCode()).thenReturn("EU");
        Mockito.when(mockLightRequest.getNameIdFormat()).thenReturn(SUBJECT_FORMAT_ID);
        Mockito.when(mockLightRequest.getId()).thenReturn("f5e7e0f5-b9b8-4256-a7d0-4090141b326d");
        Mockito.when(mockLightRequest.getIssuer()).thenReturn("http://localhost:0000/SP/metadata");

        ImmutableAttributeMap mockImmutableAttributeMap = Mockito.mock(ImmutableAttributeMap.class);
        Mockito.when(mockLightRequest.getRequestedAttributes()).thenReturn(mockImmutableAttributeMap);
        Mockito.when(mockLightRequest.getProviderName()).thenReturn("SP");
    }

    private StoredLightRequest mockStoredLightRequest(ILightRequest lightRequest) {
        StoredLightRequest storedLightRequest = Mockito.mock(StoredLightRequest.class);

        Mockito.when(storedLightRequest.getRequest()).thenReturn(lightRequest);

        return storedLightRequest;
    }


    private LightRequest.Builder buildLightRequest(List<String> loa) {
        List<LevelOfAssurance> levelOfAssuranceList = loa.stream().map(LevelOfAssurance::build).collect(Collectors.toList());
        return new LightRequest.Builder()
                .id("f5e7e0f5-b9b8-4256-a7d0-4090141b326d")
                .providerName("SP")
                .issuer("http://localhost:0000/SP/metadata")
                .citizenCountryCode("EU")
                .levelsOfAssurance(levelOfAssuranceList)
                .requestedAttributes(Mockito.mock(ImmutableAttributeMap.class));
    }

    private StoredAuthenticationRequest mockStoredAuthenticationRequest(IAuthenticationRequest authenticationRequest) {
        StoredAuthenticationRequest storedAuthenticationRequest = Mockito.mock(StoredAuthenticationRequest.class);

        Mockito.when(storedAuthenticationRequest.getRemoteIpAddress()).thenReturn("127.0.0.1");
        Mockito.when(storedAuthenticationRequest.getRequest()).thenReturn(authenticationRequest);

        return storedAuthenticationRequest;
    }

    private IAuthenticationRequest mockAuthenticationRequest(ILightRequest lightRequest, String citizenCountryCode, String nameIdFormat) {
        IEidasAuthenticationRequest authenticationRequest = EidasAuthenticationRequest.builder()
                .lightRequest(lightRequest)
                .destination(DESTINATION)
                .citizenCountryCode(citizenCountryCode)
                .nameIdFormat(nameIdFormat)
                .build();

        return authenticationRequest;
    }

    private void mockLevelsOfAssurance(ILightRequest lightRequest, List<String> levelsOfAssurance) {
        List<ILevelOfAssurance> levelsOfAssuranceList = new ArrayList<>();
        for (String loa : levelsOfAssurance) {
            levelsOfAssuranceList.add(eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(loa));
        }
        Mockito.when(lightRequest.getLevelsOfAssurance()).thenReturn(levelsOfAssuranceList);
    }

    private NotifiedLevelOfAssurance getNotifiedLoA(List<String> levelsOfAssurance) {
        NotifiedLevelOfAssurance notifiedLoA = levelsOfAssurance.stream()
                .map((loaValue) -> eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(loaValue))
                .filter((loa) -> loa.getType().equals(LevelOfAssuranceType.NOTIFIED.stringValue()))
                .map((loa) -> NotifiedLevelOfAssurance.getLevel(loa.getValue()))
                .findFirst()
                .orElse(null);
        return notifiedLoA;
    }

    private List<String> getNotNotifiedLoAs(List<String> levelsOfAssurance) {
        List<String> notNotifiedLoAs = levelsOfAssurance.stream()
                .map((loaValue) -> eu.eidas.auth.commons.light.impl.LevelOfAssurance.build(loaValue))
                .filter((loa) -> loa.getType().equals(LevelOfAssuranceType.NON_NOTIFIED.stringValue()))
                .map((loa) -> loa.getValue())
                .collect(Collectors.toList());
        return notNotifiedLoAs;
    }

    private IAuthenticationResponse mockAuthenticationResponse(String citizenCountryCode, String levelOfAssurance,
            ImmutableAttributeMap attributes) {
        IAuthenticationResponse authenticationResponse = Mockito.mock(IAuthenticationResponse.class);

        IResponseStatus responseStatus = Mockito.mock(IResponseStatus.class);
        Mockito.when(responseStatus.getStatusCode()).thenReturn(EIDASStatusCode.SUCCESS_URI.toString());

        Mockito.when(authenticationResponse.getId()).thenReturn(RESPONSE_ID);
        Mockito.when(authenticationResponse.getIssuer()).thenReturn(ISSUER);
        Mockito.when(authenticationResponse.getSubject()).thenReturn(SUBJECT);
        Mockito.when(authenticationResponse.getSubjectNameIdFormat()).thenReturn(SUBJECT_FORMAT_ID);
        Mockito.when(authenticationResponse.getStatus()).thenReturn(responseStatus);
        Mockito.when(authenticationResponse.getCountry()).thenReturn(citizenCountryCode);
        Mockito.when(authenticationResponse.getLevelOfAssurance()).thenReturn(levelOfAssurance);
        Mockito.when(authenticationResponse.getAttributes()).thenReturn(attributes);
        Mockito.when(authenticationResponse.getInResponseToId()).thenReturn(REQUEST_ID);

        return authenticationResponse;
    }

    private ImmutableAttributeMap mockAttributeMap() {
        ImmutableAttributeMap attributesMap = ImmutableAttributeMap.of();
        return attributesMap;
    }

    private ImmutableAttributeMap personIdentifierAttributeMap(String identifier) throws AttributeValueMarshallingException {
        final AttributeDefinition<String> personIdentifierAttributeDefinition = EidasSpec.Definitions.PERSON_IDENTIFIER;
        final AttributeValue<String> personIdentifier = personIdentifierAttributeDefinition.getAttributeValueMarshaller().unmarshal(identifier, false);
        return ImmutableAttributeMap.of(personIdentifierAttributeDefinition, personIdentifier);
    }

    private AttributeValue setAttributeDefinitionStringValue(AttributeDefinition<String> definition, String value) throws AttributeValueMarshallingException {
        return definition.getAttributeValueMarshaller().unmarshal(value, false);
    }

    private WebRequest newEmptyWebRequest() {
        ImmutableMap<String, ImmutableList<String>> webRequestParameters = ImmutableMap.of();
        return newWebRequest(webRequestParameters);
    }

    private WebRequest newWebRequest(ImmutableMap<String, ImmutableList<String>> requestParameters) {
        BindingMethod postMethod = BindingMethod.POST;

        WebRequest emptyWebRequest = new IncomingRequest(postMethod, requestParameters, REMOTE_IP, RELAY_STATE);

        return emptyWebRequest;
    }

    private WebRequest newWebRequestWithSAMLResponse() {
        String encodedSAMLResponse = Base64.getEncoder().encodeToString(SAML_RESPONSE.getBytes());
        ImmutableList<String> samlResponseParam = ImmutableList.of(encodedSAMLResponse);
        ImmutableMap<String, ImmutableList<String>> webRequestParameters = ImmutableMap
                .of(EidasParameterKeys.SAML_RESPONSE.toString(), samlResponseParam);

        WebRequest dummyWebRequest = newWebRequest(webRequestParameters);

        return dummyWebRequest;
    }

    private <T> Cache<String, T> mockCache(T mockedItem) {
        Cache<String, T> mockedCache = Mockito.mock(Cache.class);

        Mockito.when(mockedCache.get(anyString())).thenReturn(mockedItem);

        return mockedCache;
    }

    private void assertResponseStatus(IResponseStatus expectedResponseStatus, IResponseStatus actualResponseStatus) {
        Assert.assertEquals(expectedResponseStatus.isFailure(), actualResponseStatus.isFailure());
        Assert.assertEquals(expectedResponseStatus.getStatusCode(), actualResponseStatus.getStatusCode());
        Assert.assertEquals(expectedResponseStatus.getSubStatusCode(), actualResponseStatus.getSubStatusCode());
        Assert.assertEquals(expectedResponseStatus.getStatusMessage(), actualResponseStatus.getStatusMessage());
    }

    private void assertRequeststate(RequestState actualRequestState, IAuthenticationRequest expectedRequest) {
        Assert.assertNotNull(actualRequestState);

        List<String> expectedLevelsOfAssurance = convertToLevelsOfAssuranceValues(expectedRequest.getLevelsOfAssurance());
        Assert.assertEquals(expectedLevelsOfAssurance, actualRequestState.getLevelsOfAssurance());

        Assert.assertEquals(expectedRequest.getProviderName(), actualRequestState.getProviderName());

        Assert.assertEquals(expectedRequest.getId(), actualRequestState.getInResponseTo());
        Assert.assertEquals(expectedRequest.getIssuer(), actualRequestState.getIssuer());
        Assert.assertEquals(expectedRequest.getAssertionConsumerServiceURL(), actualRequestState.getServiceUrl());
    }

    private List<String> convertToLevelsOfAssuranceValues(List<ILevelOfAssurance> levelOfAssuranceList) {
        if (levelOfAssuranceList == null || levelOfAssuranceList.isEmpty()) {
            return null;
        }
        List<String> levelsOfAssuranceValues = new ArrayList<>();
        for (ILevelOfAssurance loa : levelOfAssuranceList) {
            levelsOfAssuranceValues.add(loa.getValue());
        }
        return levelsOfAssuranceValues;
    }

}
