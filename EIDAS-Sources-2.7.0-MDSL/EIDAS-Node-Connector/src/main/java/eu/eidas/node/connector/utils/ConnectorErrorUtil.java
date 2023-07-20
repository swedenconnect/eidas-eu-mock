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
package eu.eidas.node.connector.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.ConnectorBeanNames;
import eu.eidas.node.connector.exceptions.ConnectorError;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * Utility class for handling errors in Connector
 */
public class ConnectorErrorUtil {

    /**
     * Method called for processing the SAML error message and specific error behaviour related
     *
     * @param exception     the exception triggered
     * @param redirectError the redirected error
     */
    public static void processSAMLEngineException(Exception exception, EidasErrorKey redirectError) {
        String errorCode = null;
        if (exception instanceof EIDASSAMLEngineException) {
            errorCode = ((EIDASSAMLEngineException) exception).getErrorCode();
            if (EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()).equals(errorCode)) {
                errorCode = EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode();
            }
            if (EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()).equals(errorCode)) {
                throw new ConnectorError(
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorCode()),
                        EidasErrors.get(EidasErrorKey.COLLEAGUE_REQ_INVALID_SAML.errorMessage()));
            }
        }
        if (errorCode == null) {
            return;
        }
        if (EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE.toString().equals(errorCode)) {
            throw new ConnectorError(
                    EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED.errorCode()),
                    EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED.errorMessage()),
                    "Error processing XML : XML entities processing DOCType not allowed, possible XXE attack " + exception);
        } else if (EidasErrorKey.isErrorCode(errorCode)) {
            EidasErrorKey err = EidasErrorKey.fromCode(errorCode);
            String message = EidasErrors.get(err.errorMessage());
            String beanName = ConnectorBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString();
            ResourceBundleMessageSource msgResource = getBean(ResourceBundleMessageSource.class, beanName);
            final String errorMessage = msgResource.getMessage(message, new Object[]{
                    EidasErrors.get(err.errorCode())}, Locale.getDefault());
            throw new ConnectorError(
                    EidasErrors.get(redirectError.errorCode()),
                    EidasErrors.get(redirectError.errorMessage()),
                    errorMessage);
        }
    }

    /**
     * Method to localize the error message
     *
     * @param abstractEIDASException the exception triggered
     * @param locale                 the locale object
     */
    public static String getLocalizedErrorMessage(AbstractEIDASException abstractEIDASException, Locale locale) {
        final String beanName = ConnectorBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString();
        ResourceBundleMessageSource msgResource = getBean(ResourceBundleMessageSource.class, beanName);
        String errorKey = abstractEIDASException.getErrorMessage();
        String errorMessage;
        if (null == locale) locale = Locale.getDefault();
        if (null != errorKey) {
            errorMessage = msgResource.getMessage(errorKey, new Object[]{abstractEIDASException.getErrorCode()}, locale);
        } else {
            errorMessage = "Generic Error";
        }
        return errorMessage;
    }
}
