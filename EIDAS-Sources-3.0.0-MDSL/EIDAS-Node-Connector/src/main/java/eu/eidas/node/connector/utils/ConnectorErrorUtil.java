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
package eu.eidas.node.connector.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.engine.exceptions.EIDASMetadataException;
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
     * @param eidasSamlEngineException     the exception triggered
     * @param redirectErrorKey the redirected error
     */
    public static void processSAMLEngineException(EIDASSAMLEngineException eidasSamlEngineException, EidasErrorKey redirectErrorKey) {
        final ResourceBundleMessageSource msgResource = getBean(ResourceBundleMessageSource.class, ConnectorBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString());
        final EidasErrorKey eidasErrorKey = eidasSamlEngineException.getEidasErrorKey();

        final String logMessageSamlException = msgResource.getMessage(
                EidasErrors.get(eidasErrorKey.errorMessage()),
                new Object[]{EidasErrors.get(eidasErrorKey.errorCode())},
                Locale.getDefault());

        throw new ConnectorError(
                EidasErrors.get(redirectErrorKey.errorCode()),
                EidasErrors.get(redirectErrorKey.errorMessage()),
                logMessageSamlException,
                eidasSamlEngineException);
    }

    public static void processMetadataException(EIDASMetadataException metadataException, EidasErrorKey redirectError) {
        return; // basically what the original code does
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
