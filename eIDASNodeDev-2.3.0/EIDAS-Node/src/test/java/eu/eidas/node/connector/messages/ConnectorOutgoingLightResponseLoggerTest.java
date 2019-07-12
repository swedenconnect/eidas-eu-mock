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
import com.google.common.collect.ImmutableSortedSet;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;
import eu.eidas.auth.commons.attribute.impl.StringAttributeValueMarshaller;
import eu.eidas.auth.commons.light.ILightResponse;
import eu.eidas.auth.commons.light.impl.LightResponse;
import eu.eidas.auth.commons.light.impl.ResponseStatus;
import eu.eidas.auth.commons.protocol.eidas.spec.NaturalPersonSpec;
import eu.eidas.auth.commons.tx.AbstractCache;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.impl.SpecificConnectorCommunicationServiceExtensionImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ConnectorOutgoingLightResponseLogger}.
 *
 * @since 2.3
 */
public class ConnectorOutgoingLightResponseLoggerTest {

    Logger initLogger = (Logger) LoggerFactory.getLogger(ConnectorOutgoingLightResponseLogger.class.getName());

    Logger packageLogger = (Logger) LoggerFactory.getLogger(EIDASValues.EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE.toString() + "_" + ConnectorOutgoingLightResponseLogger.class.getSimpleName());

    private final String VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64 = "c3BlY2lmaWNDb21tdW5pY2F0aW9uRGVmaW5pdGlvbkNvbm5lY3RvclJlcXVlc3R8ODUyYTY0YzAtOGFjMS00NDVmLWIwZTEtOTkyYWRhNDkzMDMzfDIwMTctMTItMTEgMTQ6MTI6MDUgMTQ4fDdNOHArdVA4Q0tYdU1pMklxU2RhMXRnNDUyV2xSdmNPU3d1MGRjaXNTWUU9";

    /**
     * the destination Url of the component this message is targeted to, for testing proposes.
     */
    private final String destinationUrl = "http://localhost:8080/SpecificConnector/ConnectorResponse";

    /**
     * Specific Connector, for testing proposes.
     */
    private final String lightTokenResponseNodeId = "SpecificConnector";

    /**
     * Connector Light Response Identifier, for testing proposes.
     */
    private final String msgId = "2222";

    /**
     * Connector flow Identifier, for testing proposes.
     */
    private final String flowId = "1234";

    private static AttributeDefinition<String> newAttributeDefinition(String fullName,
                                                                      String friendlyName,
                                                                      boolean required) {
        final AttributeDefinition attributeDefinition = new AttributeDefinition.Builder<String>()
                .nameUri(NaturalPersonSpec.Namespace.URI + "/" + fullName)
                .friendlyName(friendlyName)
                .personType(PersonType.NATURAL_PERSON)
                .xmlType("http://eidas.europa.eu/attributes/naturalperson", fullName + "Type", "eidas-natural")
                .required(required)
                .uniqueIdentifier(false)
                .transliterationMandatory(false)
                .attributeValueMarshaller(new StringAttributeValueMarshaller())
                .build();

        return attributeDefinition;
    }

    private static ImmutableAttributeMap getListOfAttributes() {
        final AttributeDefinition dateOfBirth = newAttributeDefinition("DateOfBirth", "DateOfBirth", true);
        final AttributeDefinition eIDNumber = newAttributeDefinition("PersonIdentifier", "PersonIdentifier", true);

        ImmutableAttributeMap immutableAttributeMap = new ImmutableAttributeMap.Builder()
                .put(dateOfBirth)
                .put(eIDNumber).build();

        return immutableAttributeMap;
    }

    private static ILightResponse createLightResponse() {
        ImmutableAttributeMap immutableAttributeMap = getListOfAttributes();

        final LightResponse.Builder builder = LightResponse.builder()
                .id(UUID.randomUUID().toString())
                .subjectNameIdFormat("SUBJECT_NAME_ID_FORMAT")
                .subject("SUBJECT")
                .attributes(immutableAttributeMap)
                .issuer("issuerName")
                .relayState("relayState")
                .levelOfAssurance("loa")
                .inResponseToId("1")
                .ipAddress("192.172.0.0")
                .status(ResponseStatus.builder().statusCode("urn:oasis:names:tc:SAML:2.0:status:Success").build());

        return builder.build();
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
     * Test method for {@link ConnectorOutgoingLightResponseLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}. Must succeed.
     */
    @Test
    public void logMessage() throws SpecificCommunicationException {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        SpecificConnectorCommunicationServiceExtensionImpl mockSpecificConnectorCommunicationServiceExtensionImpl = mock(SpecificConnectorCommunicationServiceExtensionImpl.class);
        SpecificCommunicationService mockSpecificCommunicationService = mock(SpecificCommunicationService.class);
        ImmutableSortedSet<AttributeDefinition<?>> mockImmutableSortedSet = mock(ImmutableSortedSet.class);
        AbstractCache mockAbstractCache = mock(AbstractCache.class);
        mockAbstractCache.put(msgId, flowId);

        ConnectorOutgoingLightResponseLogger connectorOutgoingLightResponseLogger = new ConnectorOutgoingLightResponseLogger();

        connectorOutgoingLightResponseLogger.setMessageLoggerUtils(mockMessageLoggerUtils);
        connectorOutgoingLightResponseLogger.setFlowIdCache(mockAbstractCache);
        connectorOutgoingLightResponseLogger.setSpringManagedSpecificConnectorCommunicationService(mockSpecificCommunicationService);
        connectorOutgoingLightResponseLogger.setSpringManagedSpecificConnectorCommunicationServiceExtension(mockSpecificConnectorCommunicationServiceExtensionImpl);

        Mockito.when(mockHttpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString())).thenReturn(VALID_BINARY_LIGHT_TOKEN_REQUEST_BASE64);
        Mockito.when(mockMessageLoggerUtils.getConnectorRedirectUrl()).thenReturn(destinationUrl);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(true);
        Mockito.when(mockMessageLoggerUtils.retrieveConnectorAttributes()).thenReturn(mockImmutableSortedSet);
        Mockito.when(mockSpecificCommunicationService.getAndRemoveResponse(anyString(), any())).thenReturn(createLightResponse());
        Mockito.when(mockSpecificConnectorCommunicationServiceExtensionImpl.getLightTokenResponseNodeId()).thenReturn(lightTokenResponseNodeId);

        connectorOutgoingLightResponseLogger.logMessage(packageLogger, mockHttpServletRequest);

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