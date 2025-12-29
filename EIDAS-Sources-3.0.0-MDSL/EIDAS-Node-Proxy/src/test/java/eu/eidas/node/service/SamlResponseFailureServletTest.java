/*
 * Copyright (c) 2023 by European Commission
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
package eu.eidas.node.service;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;

public class SamlResponseFailureServletTest {

    ApplicationContext mockApplicationContext = Mockito.mock(ApplicationContext.class);
    ApplicationContext oldContext;
    ResourceBundleMessageSource mockMessageSource = Mockito.mock(ResourceBundleMessageSource.class);

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);

        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString()))
                .thenReturn(mockMessageSource);
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    @Test
    public void testLocalizeErrorMessage() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method localizeErrorMessageMethod = SamlResponseFailureServlet.class.getDeclaredMethod(
                "localizeErrorMessage", AbstractEIDASException.class,Locale.class);
        localizeErrorMessageMethod.setAccessible(true);

        SamlResponseFailureServlet testServlet = new SamlResponseFailureServlet();
        AbstractEIDASException exception = new ProxyServiceError(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                EidasErrorKey.INTERNAL_ERROR.errorMessage());
        Locale germanLocale = Locale.GERMAN;

        Mockito.when(mockMessageSource.getMessage(any(),any(),any())).thenReturn("localizedString");

        localizeErrorMessageMethod.invoke(testServlet,exception,germanLocale);

        Assert.assertEquals(exception.getErrorMessage(),"localizedString");
    }

    @Test
    public void testLocalizeErrorMessageWhenMessageBlank() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method localizeErrorMessageMethod = SamlResponseFailureServlet.class.getDeclaredMethod(
                "localizeErrorMessage", AbstractEIDASException.class,Locale.class);
        localizeErrorMessageMethod.setAccessible(true);

        SamlResponseFailureServlet testServlet = new SamlResponseFailureServlet();
        AbstractEIDASException exception = new ProxyServiceError(EidasErrorKey.INTERNAL_ERROR.errorCode(), (String) null);
        Locale germanLocale = Locale.GERMAN;

        localizeErrorMessageMethod.invoke(testServlet,exception,germanLocale);

        Assert.assertEquals(exception.getErrorMessage(),null);
    }

}