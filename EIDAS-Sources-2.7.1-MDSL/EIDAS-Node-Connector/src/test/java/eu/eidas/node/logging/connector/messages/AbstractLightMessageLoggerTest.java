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

package eu.eidas.node.logging.connector.messages;

import eu.eidas.auth.commons.BindingMethod;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.logging.MessageLoggerUtils;
import eu.eidas.node.utils.ReflectionUtils;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractLightMessageLoggerTest {

    protected ApplicationContext oldContext = null;

    protected HttpServletRequest mockHttpServletRequest(BindingMethod method, String destination, String origin, String... tokenBase64Array) {
        HttpServletRequest mockHttpServletRequest = mock(HttpServletRequest.class);
        when(mockHttpServletRequest.getRequestURL()).thenReturn(new StringBuffer(destination));
        when(mockHttpServletRequest.getHeader(EIDASValues.REFERER.toString())).thenReturn(origin);

        HashMap<String, String[]> httpParametersMap = createHttpParameterMap(tokenBase64Array);
        when(mockHttpServletRequest.getParameterMap()).thenReturn(httpParametersMap);
        when(mockHttpServletRequest.getMethod()).thenReturn(method.getValue());
        when(mockHttpServletRequest.getAttribute(EidasParameterKeys.TOKEN.toString())).thenReturn(tokenBase64Array[0]);

        return mockHttpServletRequest;
    }

    protected HashMap<String, String[]> createHttpParameterMap(String[] tokenBase64Array) {
        String servletParameter = EidasParameterKeys.TOKEN.toString();
        HashMap<String, String[]> httpParametersMap = new HashMap<>();
        httpParametersMap.put(servletParameter, tokenBase64Array);
        return httpParametersMap;
    }

    protected void setUpBeanProvider (Object object) throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ApplicationContext mockApplicationContext = mock(ApplicationContext.class);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        Mockito.when(mockApplicationContext.getBean(anyString())).thenReturn(object);
    }

    protected void tearDownBeanProvider () {
        if (oldContext!=null){
            ReflectionUtils.setStaticField(BeanProvider.class,"CONTEXT",oldContext);
            oldContext = null;
        }
    }

    protected class MsgUtilsTestBuilder {

        private boolean logMessages = false;
        private boolean logCompleteMessage = false;

        public AbstractLightMessageLoggerTest.MsgUtilsTestBuilder logMsg(boolean logMessages) {
            this.logMessages = logMessages;
            return this;
        }

        public AbstractLightMessageLoggerTest.MsgUtilsTestBuilder logCompleteMsg(boolean logCompleteMessage) {
            this.logCompleteMessage = logCompleteMessage;
            return this;
        }

        public MessageLoggerUtils build() {
            MessageLoggerUtils loggerUtils = mock(MessageLoggerUtils.class);
            when(loggerUtils.isLogMessages()).thenReturn(logMessages);
            when(loggerUtils.isLogCompleteMessage()).thenReturn(logCompleteMessage);
            return loggerUtils;
        }
    }
}
