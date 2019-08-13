/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.node.connector.messages;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.protocol.IAuthenticationResponse;
import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.auth.commons.tx.AbstractCache;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.logging.MessageLoggerUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ConnectorIncomingEidasResponseLogger}.
 *
 * @since 2.3
 */
public class ConnectorIncomingEidasResponseLoggerTest {

    Logger initLogger = (Logger) LoggerFactory.getLogger(ConnectorIncomingEidasResponseLogger.class.getName());

    Logger packageLogger = (Logger) LoggerFactory.getLogger(EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "_" + ConnectorIncomingEidasResponseLogger.class.getSimpleName());

    private static String SAML_TOKEN =
            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9Im5vIj8+PHNhbWwycDpBdXRoblJlcXVlc3QgeG1sbnM6c2FtbDJwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgeG1sbnM6c3Rvcms9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6YXNzZXJ0aW9uIiB4bWxuczpzdG9ya3A9InVybjpldTpzdG9yazpuYW1lczp0YzpTVE9SSzoxLjA6cHJvdG9jb2wiIEFzc2VydGlvbkNvbnN1bWVyU2VydmljZVVSTD0iaHR0cDovL3NwLmxvY2FsOjkwOTAvU1AvUmV0dXJuUGFnZSIgQ29uc2VudD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmNvbnNlbnQ6dW5zcGVjaWZpZWQiIERlc3RpbmF0aW9uPSJodHRwOi8vcGVwcy5sb2NhbDo5MDkwL1BFUFMvU2VydmljZVByb3ZpZGVyIiBGb3JjZUF1dGhuPSJ0cnVlIiBJRD0iX2QwNDhjYjMxNzgxMzg0NWIzMmE3YTJiNzVmM2JhZDU5IiBJc1Bhc3NpdmU9ImZhbHNlIiBJc3N1ZUluc3RhbnQ9IjIwMTEtMTEtMzBUMTY6MDk6NTcuODI0WiIgUHJvdG9jb2xCaW5kaW5nPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YmluZGluZ3M6SFRUUC1QT1NUIiBQcm92aWRlck5hbWU9IkRFTU8tU1AiIFZlcnNpb249IjIuMCI+PHNhbWwyOklzc3VlciBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpuYW1laWQtZm9ybWF0OmVudGl0eSI+aHR0cDovL1MtUEVQUy5nb3YueHg8L3NhbWwyOklzc3Vlcj48ZHM6U2lnbmF0dXJlPjxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PGRzOlNpZ25hdHVyZU1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNyc2Etc2hhMSIvPjxkczpSZWZlcmVuY2UgVVJJPSIjX2QwNDhjYjMxNzgxMzg0NWIzMmE3YTJiNzVmM2JhZDU5Ij48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxkczpEaWdlc3RWYWx1ZT5LeGhHTnJtdVN3MGxaVXBiUHpldm9EOUk0V3M9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPkg0Rng0QkRaMXJFQWwvS09YOUpIM1dHcCtheWVGci9GTGtLOE5kU2hycE1JNXJXL2lvTFY4ZlV5UXFnMlY5L0tiQkF5cVhaTDFNM0loWjFCMDNuaXZhR3FXUVdiUklOT2syMStvMzZLcDI4a3JRQ2ZqdkhhVVNsK0FHNXVpZGFJT0FSZUdPQkRuQ1VKRkd6aEUvdzc1ZXNaMm5KcmJrR2NVYklRamlJbHBqMExvbnZyUWp0bzgveWpkMFJNNW01SUpDRCtCb0VUZ2tZNkxmajR0UXgzYjVkRytqb1JyU3g4R2ZiQ1Y1TXZwcTRndHgyd0cxSHVEcllDWUE2NUhxSkhJbE5xTWRMeG1BRXEzdkNhMjdMQU9FRWU0ZkxDc2hyVW0ySUZWSGVhWWVQV2NONUhSRStVRGt5MVllQ1pSbWlsa2graUFwS3UvZExqQzdxcXpLaUt1Zz09PC9kczpTaWduYXR1cmVWYWx1ZT48ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlESnpDQ0FnOENCRXVvbmJJd0RRWUpLb1pJaHZjTkFRRUZCUUF3V0RFTE1Ba0dBMVVFQmhNQ1JWTXhEakFNQmdOVkJBZ01CVk53DQpZV2x1TVE4d0RRWURWUVFIREFaTllXUnlhV1F4RGpBTUJnTlZCQW9NQlVsdVpISmhNUmd3RmdZRFZRUUREQTlzYjJOaGJDMWtaVzF2DQpMV05sY25Rd0hoY05NVEF3TXpJek1UQTFNek00V2hjTk1UQXdOakF4TVRBMU16TTRXakJZTVFzd0NRWURWUVFHRXdKRlV6RU9NQXdHDQpBMVVFQ0F3RlUzQmhhVzR4RHpBTkJnTlZCQWNNQmsxaFpISnBaREVPTUF3R0ExVUVDZ3dGU1c1a2NtRXhHREFXQmdOVkJBTU1EMnh2DQpZMkZzTFdSbGJXOHRZMlZ5ZERDQ0FTSXdEUVlKS29aSWh2Y05BUUVCQlFBRGdnRVBBRENDQVFvQ2dnRUJBSmZkUTEvbWtNOXNxcWFiDQpyaThxcXFYNHMwSjZVRUVYeUYrMEFqTFU4UkM0V01lWUZTWjV0ZndueUxzb1hZMC85YlpMWG5CeFNpYlFZYWY0U25wWkpobFZBNHNVDQowZDhxeUVXQTJPdlhHRENtN3VzcEdIRzNDUlFhN2ZwSHNtSG1mSUFxaG85WERsd3B3SmR4NWdLNEVkYzZhQXJjTVFmanFNaHkxczFuDQo2T2YxaTFsTUdHc0dyRVJJUlk3YmlJUXUvOG5JVGJISDE4c1VBWk1HMXUvUTBBdmk5TzNMV3lzd0hYVW91WjgxOGZXd0c2eHJ1Mk41DQp5NnZ0Vk8vU0wzZG9SU2RxWWtFbXM5M1RneFR0YUtnOFhOZTI0emhOVHRlNm52V0xhS2VzaTJLelpHQzU3SFU0N0hCRkVzOE5Xazd6DQo5QkRmOHVMeVB6OVZEYWh3Vkt6TXRvOENBd0VBQVRBTkJna3Foa2lHOXcwQkFRVUZBQU9DQVFFQWdwdVJlWFE3RHNmZG9lNXp6eTJGDQo2a24xcXF0TWtSR3NCUEtuREZDSS9Ha0lacEJpcWxmd2RoNnNpcG5GS1dmS0VEbFBLTjFrRWhia0RSMkdBMUNwNEY0WlNML0h1bVpNDQpwV2FiUmhEeGhUUHZISUZiYlNoUERKWTkzK2p3L3lRZWFXZ011aHczV3pkSDlUclZvUlpIMFE3c0cxcElKbzUvNklvZ0lad0Z2SkhUDQovTkR1dEttdXJVNkx3OFZuZGU4UGZuUWQrRlRFaHowU0VHeUtrV2pBdWhHYkpmc2VCeS96M0wrTUpxMXJkU1E5UEY3d1hEdldOekpxDQp5YU5CVVdXQlYxVHNrdmtOWlhjYWd1cm9WUHkyWGhBMWFpeGxBYWpXRVhMazZVdWo1VVlxYWxyVi9yZU5ZdkR2WTBCVjJDSW41MXI3DQpQcG04SUZWVGs4bVlmWDgvanc9PTwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1sMnA6RXh0ZW5zaW9ucz48c3Rvcms6UXVhbGl0eUF1dGhlbnRpY2F0aW9uQXNzdXJhbmNlTGV2ZWw+Mzwvc3Rvcms6UXVhbGl0eUF1dGhlbnRpY2F0aW9uQXNzdXJhbmNlTGV2ZWw+PHN0b3JrOnNwU2VjdG9yPkRFTU8tU1AtU0VDVE9SPC9zdG9yazpzcFNlY3Rvcj48c3Rvcms6c3BJbnN0aXR1dGlvbj5ERU1PLVNQPC9zdG9yazpzcEluc3RpdHV0aW9uPjxzdG9yazpzcEFwcGxpY2F0aW9uPkRFTU8tU1AtQVBQTElDQVRJT048L3N0b3JrOnNwQXBwbGljYXRpb24+PHN0b3JrOnNwQ291bnRyeT5QVDwvc3Rvcms6c3BDb3VudHJ5PjxzdG9ya3A6ZUlEU2VjdG9yU2hhcmU+dHJ1ZTwvc3RvcmtwOmVJRFNlY3RvclNoYXJlPjxzdG9ya3A6ZUlEQ3Jvc3NTZWN0b3JTaGFyZT50cnVlPC9zdG9ya3A6ZUlEQ3Jvc3NTZWN0b3JTaGFyZT48c3RvcmtwOmVJRENyb3NzQm9yZGVyU2hhcmU+dHJ1ZTwvc3RvcmtwOmVJRENyb3NzQm9yZGVyU2hhcmU+PHN0b3JrcDpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxzdG9yazpSZXF1ZXN0ZWRBdHRyaWJ1dGUgTmFtZT0iaHR0cDovL3d3dy5zdG9yay5nb3YuZXUvMS4wL2dpdmVuTmFtZSIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48c3Rvcms6UmVxdWVzdGVkQXR0cmlidXRlIE5hbWU9Imh0dHA6Ly93d3cuc3RvcmsuZ292LmV1LzEuMC9lSWRlbnRpZmllciIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiIGlzUmVxdWlyZWQ9InRydWUiLz48L3N0b3JrcDpSZXF1ZXN0ZWRBdHRyaWJ1dGVzPjxzdG9ya3A6QXV0aGVudGljYXRpb25BdHRyaWJ1dGVzPjxzdG9ya3A6VklEUEF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz48c3RvcmtwOkNpdGl6ZW5Db3VudHJ5Q29kZT5MTzwvc3RvcmtwOkNpdGl6ZW5Db3VudHJ5Q29kZT48c3RvcmtwOlNQSW5mb3JtYXRpb24+PHN0b3JrcDpTUElEPkRFTU8tU1A8L3N0b3JrcDpTUElEPjwvc3RvcmtwOlNQSW5mb3JtYXRpb24+PC9zdG9ya3A6VklEUEF1dGhlbnRpY2F0aW9uQXR0cmlidXRlcz48L3N0b3JrcDpBdXRoZW50aWNhdGlvbkF0dHJpYnV0ZXM+PC9zYW1sMnA6RXh0ZW5zaW9ucz48L3NhbWwycDpBdXRoblJlcXVlc3Q+";

    /**
     * Http origin. (i.e., http Request referer header.) for testing proposes.
     */
    private final String origin = "http://localhost:8080/EidasNode/SpecificProxyServiceResponse";

    /**
     * the destination Url of the component this message is targeted to, for testing proposes.
     */
    private final String destinationUrl = "http://localhost:8080/EidasNode/ColleagueResponse";

    /**
     * the EntityId from node's SAML Metadata, for testing proposes.
     */
    private final String nodeId = "http://localhost:8080/EidasNode/ServiceMetadata";

    /**
     * Connector Saml message Identifier, for testing proposes.
     */
    private final String msgId = "2222";

    /**
     * Connector flow Identifier, for testing proposes.
     */
    private final String flowId = "1234";

    /**
     * The remote Ip Address. (i.e., WebRequest RemoteIpAddress.) for testing proposes.
     */
    private String ipAddress = "128.128.128.128";

    /**
     * Http method. (i.e., http Request getMethod.) for testing proposes.
     */
    private String httpMethod = "POST";

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

    private final static LevelFilter filter = new LevelFilter(Level.INFO);

    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setup() {
        // create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.addFilter(filter);
        listAppender.start();
        initLogger.addAppender(listAppender);
        packageLogger.addAppender(listAppender);
    }

    @After
    public void teardown() {
        initLogger.detachAndStopAllAppenders();
        packageLogger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ConnectorIncomingEidasResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}. Must succeed.
     */
    @Test
    public void logMessage() throws EIDASSAMLEngineException {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        AbstractCache mockAbstractCache = mock(AbstractCache.class);
        mockAbstractCache.put(msgId, flowId);

        ConnectorIncomingEidasResponseLogger connectorIncomingEidasResponseLogger = new ConnectorIncomingEidasResponseLogger();

        connectorIncomingEidasResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        connectorIncomingEidasResponseLogger.setFlowIdCache(mockAbstractCache);

        Mockito.when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(origin);
        Mockito.when(mockHttpServletRequest.getRequestURL()).thenReturn((new StringBuffer()).append(destinationUrl));
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockHttpServletRequest.getParameter(EidasParameterKeys.SAML_RESPONSE.toString())).thenReturn(SAML_TOKEN);
        Mockito.when(mockMessageLoggerUtils.getIAuthenticationResponse(any(byte[].class))).thenReturn(buildIAuthenticateResponse());
        Mockito.when(mockMessageLoggerUtils.getConnectorEntityId(anyString())).thenReturn(nodeId);
        Mockito.when(mockHttpServletRequest.getMethod()).thenReturn(httpMethod);

        connectorIncomingEidasResponseLogger.logMessage(packageLogger, mockHttpServletRequest);

        assertThat(listAppender.list.size(), is(1));
    }

    private final static class LevelFilter extends Filter<ILoggingEvent> {
        private Level level;

        private LevelFilter(Level level) {
            this.level = level;
        }

        @Override
        public FilterReply decide(ILoggingEvent event) {
            if (event.getLevel().equals(level)) {
                return FilterReply.ACCEPT;
            } else {
                return FilterReply.DENY;
            }
        }
    }
}