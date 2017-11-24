/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.utils;

import java.util.Arrays;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.EIDASSubStatusCode;
import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASServiceException;
import eu.eidas.auth.commons.protocol.eidas.impl.EidasAuthenticationRequest;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.exceptions.SAMLEngineException;
import eu.eidas.node.ApplicationContextProvider;
import eu.eidas.node.NodeBeanNames;
import eu.eidas.node.auth.connector.ICONNECTORSAMLService;
import eu.eidas.node.auth.service.ISERVICESAMLService;
import eu.eidas.node.logging.LoggingMarkerMDC;

/**
 * Utility class for preparing error saml response
 */
public class EidasNodeErrorUtil {

    public enum ErrorSource {
        CONNECTOR,
        PROXYSERVICE;
    }

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasNodeErrorUtil.class.getName());

    /**
     * returned substatuscode
     */
    private static final EIDASSubStatusCode EIDAS_SUB_STATUS_CODES[] = {
            EIDASSubStatusCode.QAA_NOT_SUPPORTED,
            EIDASSubStatusCode.REQUEST_DENIED_URI,
            EIDASSubStatusCode.INVALID_ATTR_NAME_VALUE_URI,
            EIDASSubStatusCode.AUTHN_FAILED_URI,
            };
    /**
     * EidasErrorKey mapped to substatuscodes
     */
    private static final EidasErrorKey EIDAS_ERRORS_WITH_SAML_GENERATION[][] = {
            {EidasErrorKey.SPROVIDER_SELECTOR_INVALID_SPQAAID},
            {EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID, EidasErrorKey.SPWARE_CONFIG_ERROR, EidasErrorKey.IDP_SAML_RESPONSE, EidasErrorKey.COLLEAGUE_RESP_INVALID_SAML,},
            {EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID, EidasErrorKey.SPWARE_CONFIG_ERROR},
            {EidasErrorKey.AUTHENTICATION_FAILED_ERROR}
    };
    /**
     * EidasErrorKey codes, mapped to substatuscodes
     */
    private static final String EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[][] = new String[EIDAS_ERRORS_WITH_SAML_GENERATION.length][];

    /**
     * errorSAMLResponse 'blacklist' - for these codes there will be no SAML reponse generated.
     */
    private static final String EIDAS_ERRORS_NO_SAML_GENERATION[] = {
            EidasErrors.get(EidasErrorKey.SERVICE_REDIRECT_URL.errorCode())
    };


    private EidasNodeErrorUtil() {
    }

    /**
     * Method called by the EidasNode exception handler to manage properly the exception occured
     *
     * @param request - the current http request
     * @param exc     - the exception for which the saml response is to be prepared
     *                <p/>
     *                side effect: exc's samlTokenFail is set to the saml response to return
     * @param source  Enum values defining ProxyService/Connector
     */
    public static void prepareSamlResponseFail(final HttpServletRequest request, AbstractEIDASException exc, ErrorSource source) {

        try {
            if (source == ErrorSource.CONNECTOR) {
                prepareSamlResponseFailConnector(request, exc);
                return;
            }
            prepareSamlResponseFailService(request, exc);

        } catch (final Exception e) {
            LOG.info("ERROR : Error while trying to generate error SAMLToken", e.getMessage());
            LOG.debug("ERROR : Error while trying to generate error SAMLToken", e);
        }

    }

    /**
     * Method called for processing the SAML error message and specific error behaviour related
     * @param e the exception triggered
     * @param destLog the specific logger
     * @param redirectError the redirected error
     */
    public static void processSAMLEngineException(Exception e, Logger destLog, EidasErrorKey redirectError) {
        // Special case for propagating the error in case of xxe
        String errorCode=null;
        if(e instanceof EIDASSAMLEngineException){
            errorCode = ((EIDASSAMLEngineException)e).getErrorCode();
        }else if(e instanceof SAMLEngineException){
            errorCode = ((SAMLEngineException)e).getErrorCode();
        }
        if(errorCode==null) {
            return;
        }
        if (EidasErrorKey.DOC_TYPE_NOT_ALLOWED_CODE.toString().equals(errorCode)) {
            destLog.error(LoggingMarkerMDC.SECURITY_WARNING, "Error processing XML : XML entities processing DOCType not allowed, possible XXE attack ");
            throw new InternalErrorEIDASException(
                    EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED.errorCode()),
                    EidasErrors.get(EidasErrorKey.DOC_TYPE_NOT_ALLOWED.errorMessage()), e);
        } else if (EidasErrorKey.isErrorCode(errorCode)) {
            EidasErrorKey err = EidasErrorKey.fromCode(errorCode);
            String message = EidasErrors.get(err.errorMessage());
            if (ApplicationContextProvider.getApplicationContext() != null) {
                ResourceBundleMessageSource msgResource = (ResourceBundleMessageSource) ApplicationContextProvider.getApplicationContext().
                        getBean(NodeBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString());
                final String errorMessage = msgResource.getMessage(message, new Object[]{
                        EidasErrors.get(err.errorCode())}, Locale.getDefault());
                destLog.info(errorMessage);
            }
            throw new EidasNodeException(
                    EidasErrors.get(redirectError.errorCode()),
                    EidasErrors.get(redirectError.errorMessage()), e);
        }
    }

    static Class[] samlEngineException={EIDASSAMLEngineException.class, SAMLEngineException.class};
    private static boolean isSAMLEngineException(Throwable e){
        for(Class t:samlEngineException){
            if (t.isInstance(e)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param e
     * @return the base SAML engine exception
     */
    public static Exception getBaseSamlException(EIDASSAMLEngineException e){
        Exception baseExc=e;
        Throwable currentException=e;
        while(true){
            if(currentException!=null && currentException.getCause()!=null && currentException!=currentException.getCause()){
                currentException=currentException.getCause();
                if(isSAMLEngineException(currentException)){
                    baseExc=(Exception)currentException;
                }
            }else {
                break;
            }
        }
        return baseExc;
    }

    private static String getErrorReportingUrl(final HttpServletRequest request) {
        Object spUrl = request.getSession().getAttribute(EidasParameterKeys.SP_URL.toString());
        return spUrl == null ? null : spUrl.toString();
    }

    private static void prepareSamlResponseFailConnector(final HttpServletRequest request, AbstractEIDASException exc) {
        String spUrl = getErrorReportingUrl(request);
        LOG.info("ERROR : " + exc.getErrorMessage());
        if (spUrl == null) {
            return;
        }

        ICONNECTORSAMLService connectorSamlService = ApplicationContextProvider.getApplicationContext().getBean(ICONNECTORSAMLService.class);
        if (connectorSamlService == null) {
            return;
        }

        if (!isErrorCodeAllowedForSamlGeneration(exc)) {
            LOG.info("ERROR : " + getEidasErrorMessage(exc, null));
            return;
        }
        byte[] samlToken = connectorSamlService.generateErrorAuthenticationResponse(request, spUrl.toString(),
                                                                                    getSamlStatusCode(request),
                                                                                    getSamlSubStatusCode(exc), exc.getErrorMessage());
        exc.setSamlTokenFail(EidasStringUtil.encodeToBase64(samlToken));
    }

    private static void prepareSamlResponseFailService(final HttpServletRequest request, AbstractEIDASException exc) {
        String spUrl = getErrorReportingUrl(request);
        LOG.info("ERROR : " + exc.getErrorMessage());
        if (spUrl == null ) {
            return;
        }
        generateSamlResponse(request, exc, spUrl);
    }

    private static void generateSamlResponse(final HttpServletRequest request, AbstractEIDASException exc, String spUrl){
        ISERVICESAMLService serviceSamlService = ApplicationContextProvider.getApplicationContext().getBean(ISERVICESAMLService.class);
        if (serviceSamlService == null) {
            return;
        }
        if(exc.getUserErrorCode()!=null){
            exc.setErrorMessage("");
        }
        String samlSubStatusCode = getSamlSubStatusCode(exc);
        String errorMessage=exc.getErrorMessage();
        if(!isErrorCodeAllowedForSamlGeneration(exc)){
            if(exc.getUserErrorCode()!=null && isErrorCodeAllowedForSamlGeneration(exc.getUserErrorCode())){
                errorMessage = resolveMessage(exc.getUserErrorMessage(), exc.getUserErrorCode(), request.getLocale());
                samlSubStatusCode=getSamlSubStatusCode(exc.getUserErrorCode());
            }else {
                return;
            }
        }

        EidasAuthenticationRequest.Builder dummyAuthData = new EidasAuthenticationRequest.Builder();
        dummyAuthData.id(getInResponseTo(request));
        dummyAuthData.issuer(getIssuer(request));
        dummyAuthData.destination(spUrl);
        dummyAuthData.citizenCountryCode(getCitizenCountryCode(request));
        dummyAuthData.assertionConsumerServiceURL(spUrl);

        byte[] samlToken = serviceSamlService.generateErrorAuthenticationResponse(dummyAuthData.build(),
                                                                                  getSamlStatusCode(request), null, samlSubStatusCode,
                                                                                  errorMessage, request.getRemoteAddr(), true);
        exc.setSamlTokenFail(EidasStringUtil.encodeToBase64(samlToken));
    }

    public static String getInResponseTo(final HttpServletRequest req) {
        Object inResponseTo = req.getSession().getAttribute(EidasParameterKeys.SAML_IN_RESPONSE_TO.toString());
        return inResponseTo == null ? "error" : inResponseTo.toString();
    }

    public static String getIssuer(final HttpServletRequest req) {
        Object issuer = req.getSession().getAttribute(EidasParameterKeys.ISSUER.toString());
        return issuer == null ? "ConnectorExceptionHandlerServlet" : issuer.toString();
    }

    public static String getCitizenCountryCode(final HttpServletRequest req) {
        Object issuer = req.getSession().getAttribute(EidasParameterKeys.CITIZEN_COUNTRY_CODE.toString());
        return issuer == null ? "ConnectorExceptionHandlerServlet" : issuer.toString();
    }

    private static String getSamlStatusCode(final HttpServletRequest req) {
        Object phase = req.getSession().getAttribute(EidasParameterKeys.SAML_PHASE.toString());
        return phase == EIDASValues.SP_REQUEST ? EIDASStatusCode.REQUESTER_URI.toString() : EIDASStatusCode.RESPONDER_URI.toString();
    }


    private static String getSamlSubStatusCode(final AbstractEIDASException exc) {
        loadErrorCodesArrays();
        String subStatusCode = getSamlSubStatusCode(exc.getErrorCode());
        if(subStatusCode != null){
            return subStatusCode;
        }
        if (exc instanceof InvalidParameterEIDASException) {
            return EIDASSubStatusCode.INVALID_ATTR_NAME_VALUE_URI.toString();
        }
        return EIDASSubStatusCode.REQUEST_DENIED_URI.toString();// default?
    }

    private static String getSamlSubStatusCode(final String errorCode){
        for (int i = 0; i < EIDAS_SUB_STATUS_CODES.length; i++) {
            if (EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i] != null && EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i].length > 0 && Arrays.binarySearch(EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i], errorCode) >= 0) {
                return EIDAS_SUB_STATUS_CODES[i].toString();
            }
        }
        return null;
    }

    /**
     *
     * @param exc
     * @return true if the exception are allowed to generate a saml message to be shown to the user
     */
    private static boolean isErrorCodeAllowedForSamlGeneration(final AbstractEIDASException exc) {
        loadErrorCodesArrays();
        String errorCode = exc.getErrorCode();
        if(isErrorCodeDisabledForSamlGeneration(errorCode)){
            return false;
        }
        if(isErrorCodeAllowedForSamlGeneration(errorCode)){
            return true;
        }
        if (exc instanceof InvalidParameterEIDASException || exc instanceof InvalidParameterEIDASServiceException) {
            return true;
        }
        return false;

    }

    private static boolean isErrorCodeAllowedForSamlGeneration(final String errorCode){
        for (int i = 0; i < EIDAS_SUB_STATUS_CODES.length; i++) {
            if (EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i] != null && EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i].length > 0 && Arrays.binarySearch(EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i], errorCode) >= 0) {
                return true;
            }
        }
        return false;
    }
    private static boolean isErrorCodeDisabledForSamlGeneration(final String errorCode){
        if (Arrays.binarySearch(EIDAS_ERRORS_NO_SAML_GENERATION, errorCode) >= 0 ) {
            return true;
        }
        return false;
    }


    /**
     * loads error codes if needed
     */
    private static void loadErrorCodesArrays() {
        if (EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[0] == null) {
            for (int i = 0; i < EIDAS_ERRORS_WITH_SAML_GENERATION.length; i++) {
                EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i] = new String[EIDAS_ERRORS_WITH_SAML_GENERATION[i].length];
                for (int j = 0; j < EIDAS_ERRORS_WITH_SAML_GENERATION[i].length; j++) {
                    EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i][j] = EidasErrors.get(EIDAS_ERRORS_WITH_SAML_GENERATION[i][j].errorCode());
                }
                Arrays.sort(EIDAS_ERRORS_CODES_WITH_SAML_GENERATION[i]);
                Arrays.sort(EIDAS_ERRORS_NO_SAML_GENERATION);
            }
        }
    }

    /**
     *
     * @param exceptionMessage
     * @param exceptionCode
     * @param locale
     * @return the message associated with the given error (identified by exceptionMessage and exceptionCode), retrieved from sysadmin properties
     */
    public static String resolveMessage(String exceptionMessage, String exceptionCode, Locale locale){
        return prepareErrorMessage(exceptionMessage, new Object[]{exceptionCode}, locale);
    }

    private static String prepareErrorMessage(String message, Object[] parameters, Locale locale){
        try {
            ResourceBundleMessageSource msgResource = (ResourceBundleMessageSource) ApplicationContextProvider.getApplicationContext().
                    getBean(NodeBeanNames.SYSADMIN_MESSAGE_RESOURCES.toString());
            final String errorMessage = msgResource.getMessage(message, parameters, locale);
            return errorMessage;
        }catch(NoSuchMessageException e){
            LOG.warn("ERROR : message not found {} - {}", message, e);
        }
        return null;

    }

    /**
     * @param exc               the code of the message
     * @param messageParameters
     * @return the text of an error message
     */
    private static String getEidasErrorMessage(AbstractEIDASException exc, Object[] messageParameters) {
        String errorText = "";
        Throwable cause = exc.getCause();
        String code = cause == null ? exc.getMessage() : cause.getMessage();
        if (cause instanceof EIDASSAMLEngineException) {
            code = ((EIDASSAMLEngineException) cause).getErrorCode();
        }
        EidasErrorKey err = EidasErrorKey.fromID(code);
        if (EidasErrorKey.isErrorCode(code) || err != null) {
            if (err == null) {
                err = EidasErrorKey.fromCode(code);
            }
            String message = EidasErrors.get(err.errorMessage());

            errorText = prepareErrorMessage(message, prepareParameters(err, messageParameters), Locale.getDefault());
            if(!err.isShowToUser()){
                exc.setErrorMessage("");
            }
        }
        return errorText;
    }

    private static Object[] prepareParameters(EidasErrorKey err, Object[] otherMessageParameters) {
        Object[] parameters = new Object[1 + (otherMessageParameters == null ? 0 : otherMessageParameters.length)];
        parameters[0] = EidasErrors.get(err.errorCode());
        if (otherMessageParameters != null && otherMessageParameters.length > 0) {
            System.arraycopy(otherMessageParameters, 0, parameters, 1, otherMessageParameters.length);
        }
        return parameters;
    }

}
