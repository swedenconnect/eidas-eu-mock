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
package eu.eidas.node.service.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.BeanProvider;
import eu.eidas.node.ProxyBeanNames;
import eu.eidas.node.service.exceptions.ProxyServiceError;
import eu.eidas.node.utils.ReflectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.junit.Assert;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;

/**
 * Test class for {@link ProxyServiceErrorUtil}
 */
@RunWith(MockitoJUnitRunner.class)
public class ProxyServiceErrorUtilTest {

    private ApplicationContext oldContext = null;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private Logger mockLogger;
    @Mock
    private ApplicationContext mockApplicationContext;

    @Before
    public void setUp() throws Exception {
        Field contextField = BeanProvider.class.getDeclaredField("CONTEXT");
        contextField.setAccessible(true);
        oldContext = (ApplicationContext) contextField.get(null);
        ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", mockApplicationContext);
        mockResourceBundleMessageSource();
    }

    @After
    public void tearDown() {
        if (oldContext != null) {
            ReflectionUtils.setStaticField(BeanProvider.class, "CONTEXT", oldContext);
            oldContext = null;
        }
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#processSAMLEngineException(Exception, Logger, EidasErrorKey)}
     * When {@link EidasErrorKey} is {@link EidasErrorKey.MESSAGE_VALIDATION_ERROR}
     * <p>
     * Must fail
     */
    @Test
    public void testProcessSAMLEngineExceptionWithMessageValidationError() {
        expectedException.expect(ProxyServiceError.class);

        final String errorCode = EidasErrors.get((EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()));
        final String errorMessage = EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());
        final EIDASSAMLEngineException eidassamlEngineException = new EIDASSAMLEngineException(errorCode, errorMessage);

        ProxyServiceErrorUtil.processSAMLEngineException(eidassamlEngineException, mockLogger, EidasErrorKey.MESSAGE_VALIDATION_ERROR);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#processSAMLEngineException(Exception, Logger, EidasErrorKey)}
     * When {@link EidasErrorKey} is {@link EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML}
     * <p>
     * Must fail
     */
    @Test
    public void testProcessSAMLEngineExceptionWithInvalidSAML() {
        expectedException.expect(ProxyServiceError.class);

        final String errorCode = EidasErrors.get((EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()));
        final String errorMessage = EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage());
        final EIDASSAMLEngineException eidassamlEngineException = new EIDASSAMLEngineException(errorCode, errorMessage);

        ProxyServiceErrorUtil.processSAMLEngineException(eidassamlEngineException, mockLogger, EidasErrorKey.MESSAGE_VALIDATION_ERROR);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#processSAMLEngineException(Exception, Logger, EidasErrorKey)}
     * when exception is instance of {@link EIDASSAMLEngineException}
     * Must succeed
     */
    @Test
    public void testProcessSAMLEngineExceptionWithSAMLEngineException() {
        final EIDASSAMLEngineException eidassamlEngineException = new EIDASSAMLEngineException("error");

        ProxyServiceErrorUtil.processSAMLEngineException(eidassamlEngineException, mockLogger, EidasErrorKey.MESSAGE_VALIDATION_ERROR);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#processSAMLEngineException(Exception, Logger, EidasErrorKey)}
     * When {@link EidasErrorKey} is {@link EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE}
     * <p>
     * Must fail
     */
    @Test
    public void testProcessSAMLEngineExceptionWithDocTypeNotAllowedCode() {
        expectedException.expect(ProxyServiceError.class);

        final String errorCode = (EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE.toString());
        final String errorMessage = EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE.errorMessage());
        final EIDASSAMLEngineException eidassamlEngineException = new EIDASSAMLEngineException(errorCode, errorMessage);

        ProxyServiceErrorUtil.processSAMLEngineException(eidassamlEngineException, mockLogger, EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#processSAMLEngineException(Exception, Logger, EidasErrorKey)}
     * Must succeed
     */
    @Test
    public void testProcessSAMLEngineException() {
        final String errorCode = "errorCode";
        final String errorMessage = EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE.errorMessage());
        final EIDASSAMLEngineException eidassamlEngineException = new EIDASSAMLEngineException(errorCode, errorMessage);

        ProxyServiceErrorUtil.processSAMLEngineException(eidassamlEngineException, mockLogger, null);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#getLocalizedErrorMessage(AbstractEIDASException, Locale)}
     * Must succeed
     */
    @Test
    public void testGetLocalizedErrorMessage() {
        final String errorCode = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorCode());
        final String errorMessage = EidasErrors.get(EidasErrorKey.EIDAS_MANDATORY_ATTRIBUTES.errorMessage());
        final ProxyServiceError proxyServiceError = new ProxyServiceError(errorCode, errorMessage);
        final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);

        String result = ProxyServiceErrorUtil.getLocalizedErrorMessage(proxyServiceError, mockHttpServletRequest.getLocale());
        Assert.assertNotNull(result);
    }

    /**
     * Test method for {@link ProxyServiceErrorUtil#getLocalizedErrorMessage(AbstractEIDASException, Locale)}
     * when errorKey is null
     * Must succeed
     */
    @Test
    public void testGetLocalizedErrorMessageWithNullErrorKey() {
        final ProxyServiceError proxyServiceError = new ProxyServiceError("errorCode", new Exception());
        final HttpServletRequest mockHttpServletRequest = Mockito.mock(HttpServletRequest.class);

        String result = ProxyServiceErrorUtil.getLocalizedErrorMessage(proxyServiceError, mockHttpServletRequest.getLocale());
        Assert.assertNotNull(result);
    }

    private void mockResourceBundleMessageSource() {
        final ResourceBundleMessageSource mockResourceBundleMessageSource = Mockito.mock(ResourceBundleMessageSource.class);
        Mockito.when(mockApplicationContext.getBean(ProxyBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString())).thenReturn(mockResourceBundleMessageSource);
        Mockito.when(mockResourceBundleMessageSource.getMessage(any(), any(), any())).thenReturn("203021 - incomplete attribute set");
    }

}