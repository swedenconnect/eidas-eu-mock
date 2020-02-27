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
package eu.eidas.node.logging.service.messages;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import eu.eidas.auth.cache.ConcurrentMapJcacheServiceDefaultImpl;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.utils.ReflectionUtils;
import eu.eidas.node.utils.logger.LevelFilter;
import eu.eidas.node.utils.logger.LoggerTestData;
import eu.eidas.specificcommunication.CommunicationCache;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import eu.eidas.specificcommunication.protocol.SpecificCommunicationService;
import eu.eidas.specificcommunication.protocol.impl.SpecificProxyserviceCommunicationServiceExtensionImpl;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static eu.eidas.auth.commons.EidasParameterKeys.TOKEN;
import static eu.eidas.node.logging.AbstractLogger.END_TAG;
import static eu.eidas.node.logging.AbstractLogger.NOT_APPLICABLE;
import static eu.eidas.node.logging.MessageLoggerTag.BLT_HASH;
import static eu.eidas.node.logging.MessageLoggerTag.DESTINATION;
import static eu.eidas.node.logging.MessageLoggerTag.FLOW_ID;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_HASH;
import static eu.eidas.node.logging.MessageLoggerTag.MSG_ID;
import static eu.eidas.node.logging.MessageLoggerTag.NODE_ID;
import static eu.eidas.node.logging.MessageLoggerTag.OP_TYPE;
import static eu.eidas.node.logging.MessageLoggerTag.ORIGIN;
import static eu.eidas.node.logging.MessageLoggerTag.TIMESTAMP;
import static eu.eidas.node.utils.logger.LoggerTestUtils.createStartListAppender;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

/**
 * Tests for the {@link ProxyServiceOutgoingLightRequestLogger}.
 *
 * @since 2.3
 */
public class ProxyServiceOutgoingLightRequestLoggerTest {

    private final Logger logger = (Logger) LoggerFactory.getLogger(ProxyServiceOutgoingLightRequestLogger.class.getName());
    private ListAppender<ILoggingEvent> infoAppender;

    @Before
    public void setup() {
        infoAppender = createStartListAppender(new LevelFilter(Level.INFO));
        logger.addAppender(infoAppender);
    }

    @After
    public void teardown() {
        logger.detachAndStopAllAppenders();
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check no logging occurs when logging is deactivated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageInactive () throws Exception {
        LoggerTestData testData = new LoggerTestData().
                setLogMessage(false).
                setDestination(StringUtils.EMPTY);

        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        setUpBeanProvider(proxyServiceOutgoingLightRequestLogger);
        proxyServiceOutgoingLightRequestLogger.logMessage(logger, mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(0));
    }

    /**
     * Test method for {@link ProxyServiceOutgoingLightRequestLogger#logMessage(org.slf4j.Logger, HttpServletRequest)}.
     * Check logging occurs when logging is activated for the logger
     * Must succeed.
     */
    @Test
    public void logMessageActive () throws Exception {
        String msgId = String.format("msgID_%s", RandomStringUtils.randomAlphabetic(10));
        String flowId = String.format("flowId_%s", RandomStringUtils.randomAlphabetic(10));
        Cache<String, String> flowIdCache = new CommunicationCache(new ConcurrentMapJcacheServiceDefaultImpl());
        flowIdCache.put(msgId, flowId);
        String tokenBase64 = EidasStringUtil.encodeToBase64(String.format("token_%s", randomAlphabetic(10)) .getBytes());

        LoggerTestData testData = new LoggerTestData().
                setLogMessage(true).
                setTokenBase64(tokenBase64).
                setFlowIdCache(flowIdCache).
                setOpType(EIDASValues.EIDAS_SERVICE_SPECIFIC_REQUEST.toString()).
                setNodeId("specificProxyService").
                setOrigin(NOT_APPLICABLE).
                setDestination("/SpecificProxyService/ProxyServiceRequest").
                setMsgId(msgId);

        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = proxyServiceOutgoingLightRequestLogger(testData);
        setUpBeanProvider(proxyServiceOutgoingLightRequestLogger);
        proxyServiceOutgoingLightRequestLogger.logMessage(logger, mockHttpServletRequest(testData));

        assertThat(infoAppender.list.size(), is(1));
        List<String> expectedStrings = Arrays.asList(
                TIMESTAMP.getValue(), END_TAG,
                OP_TYPE.getValue(), testData.opType, END_TAG,
                NODE_ID.getValue(), testData.nodeId, END_TAG,
                ORIGIN.getValue(), testData.origin, END_TAG,
                DESTINATION.getValue(), testData.destination, END_TAG,
                FLOW_ID.getValue(), testData.flowIdCache.get(testData.msgId), END_TAG,
                MSG_ID.getValue(), testData.msgId, END_TAG,
                MSG_HASH.getValue(), END_TAG,
                BLT_HASH.getValue()
        );
        String loggedMessage = infoAppender.list.get(0).getMessage();
        assertThat(loggedMessage, stringContainsInOrder(expectedStrings));
    }

    private void setUpBeanProvider (ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger) {
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(proxyServiceOutgoingLightRequestLogger);
    }

    private HttpServletRequest mockHttpServletRequest(LoggerTestData testData) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        Mockito.when(mockHttpServletRequest.getAttribute(TOKEN.toString())).thenReturn(testData.tokenBase64);
        return mockHttpServletRequest;
    }

    private ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger (LoggerTestData testData) throws SpecificCommunicationException {
        ProxyServiceOutgoingLightRequestLogger proxyServiceOutgoingLightRequestLogger = new ProxyServiceOutgoingLightRequestLogger();

        SpecificProxyserviceCommunicationServiceExtensionImpl specificProxyserviceCommunicationServiceExtension = mockSpecificProxyserviceCommunicationServiceExtension(testData);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificProxyserviceCommunicationServiceExtension(specificProxyserviceCommunicationServiceExtension);

        SpecificCommunicationService mockSpecificCommunicationService = mockSpecificCommunicationService(testData);
        proxyServiceOutgoingLightRequestLogger.setSpringManagedSpecificProxyserviceCommunicationService(mockSpecificCommunicationService);

        proxyServiceOutgoingLightRequestLogger.setMessageLoggerUtils(mockMessageLoggerUtils(testData));
        proxyServiceOutgoingLightRequestLogger.setFlowIdCache(testData.flowIdCache);
        return proxyServiceOutgoingLightRequestLogger;
    }

    private SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension(LoggerTestData testData) {
        SpecificProxyserviceCommunicationServiceExtensionImpl mockSpecificProxyserviceCommunicationServiceExtension = mock(SpecificProxyserviceCommunicationServiceExtensionImpl.class);
        Mockito.when(mockSpecificProxyserviceCommunicationServiceExtension.getLightTokenRequestNodeId()).thenReturn(testData.nodeId);
        return mockSpecificProxyserviceCommunicationServiceExtension;
    }

    private SpecificCommunicationService mockSpecificCommunicationService(LoggerTestData testData) throws SpecificCommunicationException {
        SpecificCommunicationService mockSpecificCommunicationService = mock(SpecificCommunicationService.class);
        ILightRequest mockLightRequest = mockLightRequest(testData);
        Mockito.when(mockSpecificCommunicationService.getAndRemoveRequest(anyString(), any())).thenReturn(mockLightRequest);
        return mockSpecificCommunicationService;
    }

    private ILightRequest mockLightRequest (LoggerTestData  testData) {
        ILightRequest mockLightRequest = mock(ILightRequest.class);
        Mockito.when(mockLightRequest.getId()).thenReturn(testData.msgId);
        return mockLightRequest;
    }

    private MessageLoggerUtils mockMessageLoggerUtils(LoggerTestData testData) {
        MessageLoggerUtils mockMessageLoggerUtils = mock(MessageLoggerUtils.class);
        Mockito.when(mockMessageLoggerUtils.isLogMessages()).thenReturn(testData.logMessage);
        Mockito.when(mockMessageLoggerUtils.getProxyServiceRedirectUrl()).thenReturn(testData.destination);
        return mockMessageLoggerUtils;
    }
}