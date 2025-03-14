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
package eu.eidas.logging;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.engine.DefaultProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineFactory;
import eu.eidas.auth.engine.ProtocolEngineI;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.impl.CachingMetadataFetcher;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;

import static eu.eidas.logging.utils.ReadFileUtils.readFileAsByteArray;
import static eu.eidas.logging.utils.TestingConstants.CITIZEN_COUNTRY_CODE_CONS;
import static eu.eidas.logging.utils.TestingConstants.REQUEST_DESTINATION_CONS;
import static eu.eidas.logging.utils.TestingConstants.REQUEST_ID_CONS;
import static eu.eidas.logging.utils.TestingConstants.SAML_INSTANCE_CONS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link MessageLoggerUtils}.
 *
 * @since 2.3
 */
public class MessageLoggerUtilsTest {



    /**
     * Test method for {@link MessageLoggerUtils#isLogMessages()}. Must succeed.
     */
    @Test
    public void isMessageLogged() {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        messageLoggerUtils.setLogMessage(true);
        boolean actual = messageLoggerUtils.isLogMessages();
        assertThat(actual, is(true));
    }


    /**
     * Test method for {@link MessageLoggerUtils#getIAuthenticationProxyRequest(byte[])}. Must succeed.
     */
    @Test
    public void getIAuthenticationProxyRequest() throws EIDASSAMLEngineException {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();

        final byte[] samlObj = readFileAsByteArray("logging/testLoggingSamlRequest.xml");
        IAuthenticationRequest authenticationRequest = messageLoggerUtils.getIAuthenticationProxyRequest(samlObj);

        assertThat(authenticationRequest.getId(), is("_d048cb317813845b32a7a2b75f3bad59"));
        assertThat(authenticationRequest.getCitizenCountryCode(), is("N/A"));
        assertThat(authenticationRequest.getIssuer(), is("http://S-PEPS.gov.xx"));
        assertThat(authenticationRequest.getDestination(), is("http://peps.local:9090/PEPS/ServiceProvider"));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getIAuthenticationResponse(byte[])}.
     *
     * Must succeed.
     */
    @Test
    public void getIAuthenticationResponse() throws EIDASSAMLEngineException {
        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();

        final byte[] samlObj = readFileAsByteArray("logging/testLoggingSamlResponse.xml");
        IAuthenticationResponse iAuthenticationResponse = messageLoggerUtils.getIAuthenticationResponse(samlObj);

        assertThat(iAuthenticationResponse.getId(), is("_7POlm0XG6RUlrv67ArCVMe4cjc4IjIO5S2fe74-0yYbPRqbjJYl-v2hE4l6sqA2"));
        assertThat(iAuthenticationResponse.getInResponseToId(), is("_BWujh-SYHdY7qT35CXZ8y.yk5tSK793zQoiavTqXe.i_AuVw0f9RTOEQOihenSv"));
        assertThat(iAuthenticationResponse.getStatusCode(), is("urn:oasis:names:tc:SAML:2.0:status:Success"));
        assertThat(iAuthenticationResponse.getIssuer(), is("http://cef-eid-build-1:8080/EidasNode/ServiceMetadata"));
    }

    /**
     * Test method for {@link MessageLoggerUtils#getEntityId(String)}. Must succeed.
     */
    @Test
    public void getProxyServiceEntityId() throws EIDASMetadataException {
        String expectedEntityId = "proxyServiceEntityId";

        EidasMetadataParametersI mockEidasMetadataParameters = mock(EidasMetadataParametersI.class);
        when(mockEidasMetadataParameters.getEntityID()).thenReturn(expectedEntityId);

        ProtocolEngineFactory  mockProtocolEngineFactory = mock(ProtocolEngineFactory.class);
        ProtocolEngineI protocolEngine = DefaultProtocolEngineFactory.getInstance().getProtocolEngine(SAML_INSTANCE_CONS.toString());
        when(mockProtocolEngineFactory.getProtocolEngine(SAML_INSTANCE_CONS.toString())).thenReturn(protocolEngine);

        WebApplicationContext mockApplicationContext = mock(WebApplicationContext.class);
        try(MockedStatic<ContextLoader> mockContextLoader = Mockito.mockStatic(ContextLoader.class)) {

            mockContextLoader.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockApplicationContext);

            MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
            messageLoggerUtils.setProtocolEngine(protocolEngine);

            String actualEntityId = messageLoggerUtils.getEntityId("nonBlankMetaDataUrl");
            assertThat(actualEntityId, is(expectedEntityId));
        }
    }

    /**
     * Test method for {@link MessageLoggerUtils#getIssuer(String, HttpServletRequest)}. Must succeed.
     */
    @Test
    public void getNonBlankServletRequestIssuer() {
        String nonBlankIssuer = "nonBlankIssuer";
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString())).thenReturn(nonBlankIssuer);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        String actualIssuer = messageLoggerUtils.getIssuer("aRequestId", mockHttpServletRequest);
        assertThat(actualIssuer, is(nonBlankIssuer));

    }

    /**
     * Test method for {@link MessageLoggerUtils#getIssuer(String, HttpServletRequest)}. Must succeed.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void getBlankServletRequestIssuer() {
        String expectedIssuer = "expeectedIssuer";
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getAttribute(EidasParameterKeys.ISSUER.toString())).thenReturn(StringUtils.EMPTY);

        StoredAuthenticationRequest storedAuthenticationRequest = buildStoredAuthenticationRequest(expectedIssuer);
        Cache mockCache = mock(Cache.class);
        when(mockCache.getAndRemove(anyString())).thenReturn(storedAuthenticationRequest);

        MessageLoggerUtils messageLoggerUtils = new MessageLoggerUtils();
        messageLoggerUtils.setProxyServiceRequestCorrelationCache(mockCache);
        String actualIssuer = messageLoggerUtils.getIssuer("aRequestId", mockHttpServletRequest);
        assertThat(actualIssuer, is(expectedIssuer));

    }

    private StoredAuthenticationRequest buildStoredAuthenticationRequest (String expectedIssuer) {

        final ImmutableAttributeMap.Builder attributeMapBuilder  = ImmutableAttributeMap.builder();
        ImmutableAttributeMap attributeMap = attributeMapBuilder
                           .put(NaturalPersonSpec.Definitions.BIRTH_NAME, "PersonName")
                           .build();

        EidasAuthenticationRequest.Builder eidasAuthenticationRequestBuilder = EidasAuthenticationRequest.builder();
        EidasAuthenticationRequest eidasAuthenticationRequest = eidasAuthenticationRequestBuilder
                .requestedAttributes(attributeMap)
                .destination(REQUEST_DESTINATION_CONS.toString())
                .issuer(expectedIssuer)
                .citizenCountryCode(CITIZEN_COUNTRY_CODE_CONS.toString())
                .id(REQUEST_ID_CONS.toString())
                .levelOfAssurance(NotifiedLevelOfAssurance.HIGH)
                .build();

        final StoredAuthenticationRequest.Builder storedAuthenticationRequestBuilder = StoredAuthenticationRequest.builder();
        return storedAuthenticationRequestBuilder
                .request(eidasAuthenticationRequest)
                .remoteIpAddress("127.0.0.1")
                .build();

    }

}