/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.auth.commons.validation;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static eu.eidas.auth.commons.EIDASValues.EIDAS_PACKAGE_LOGGING_FULL;

/**
 * Fluent API to validate parameter.
 *
 * @since 1.1
 */
@SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
public abstract class AbstractParameterValidator<V extends AbstractParameterValidator<V>> {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractParameterValidator.class);

    private static final Logger fullLogger = LoggerFactory.getLogger(String.format("%s.%s", EIDAS_PACKAGE_LOGGING_FULL, AbstractParameterValidator.class.getSimpleName()));

    /**
     * Max prefix.
     */
    private static final String MAX_PARAM_PREFIX = "max.";

    /**
     * Code prefix to get error code.
     */
    private static final String CODE_PARAM_SUFFIX = ".code";

    /**
     * param's size prefix to get max param size.
     */
    private static final String MAX_PARAM_SUFFIX = ".size";

    /**
     * Message prefix to get error message.
     */
    private static final String MSG_PARAM_SUFFIX = ".message";

    /**
     * Getter for the error code of some given error related to the input param.
     *
     * @param paramName The name of the parameter associated with the error.
     * @return The code of the error.
     */
    private static String getErrorCode(final String paramName) {
        return EidasErrors.get(paramName + CODE_PARAM_SUFFIX);
    }

    /**
     * Getter for the error message of some given error related to the input parameter.
     *
     * @param paramName The name of the parameter associated with the message.
     * @return The message for the error.
     */
    private static String getErrorMessage(final String paramName) {
        return EidasErrors.get(paramName + MSG_PARAM_SUFFIX);
    }

    /**
     * Validates the input paramValue identified by the paramName.
     * <p>
     * Checks that the given value is not empty and is not greater than the configured maximum length.
     *
     * @param paramName  The name of the parameter to validate.
     * @param paramValue The value of the parameter to validate.
     * @return true if the parameter is valid.
     */
    private static boolean isValidParameter(@Nonnull String paramName, @Nullable String paramValue, @Nullable Marker marker) {
        final String validationParam = EidasParameters.get(EidasParameterKeys.VALIDATION_ACTIVE.toString());
        if (!EIDASValues.TRUE.toString().equals(validationParam)) {
            return true;
        }
        if (StringUtils.isBlank(paramValue)) {
            LOG.error("ERROR : Invalid parameter [" + paramName + "] is blank");
            return false;
        }
        final String paramConf = getParameterConfiguration(paramName);
        final String paramSizeStr = EidasParameters.get(paramConf);
        String message;
        // Checking if the parameter size exists and if it's numeric
        if (StringUtils.isNumeric(paramSizeStr)) {
            final int maxParamSize = Integer.parseInt(paramSizeStr);

            if (paramValue == null || StringUtils.isEmpty(paramValue) || (paramValue.length() > maxParamSize)) {
                paramValue = truncateToMaxSize(paramValue, maxParamSize);
                if (null == marker) {
                    paramValue = setEmptyParamValueForSamlMessage(paramName, paramValue);
                }
                message = ("ERROR : Invalid parameter [" + paramName + "] value " + paramValue);
                logErrorMessage(marker, message);
                return false;
            }
        } else {
            message = ("ERROR : Missing \""
                    + paramConf
                    + "\" configuration in the "
                    + EidasParameters.getPropertiesFilename()
                    + " configuration file");
            logErrorMessage(marker, message);
            return false;
        }
        return true;
    }

    private static String getParameterConfiguration(@Nonnull String paramName) {
        String paramConfString;
        if (paramName.equalsIgnoreCase(EidasParameterKeys.SAML_REQUEST.toString())
                || paramName.equalsIgnoreCase(EidasParameterKeys.LIGHT_REQUEST.toString())) {
            paramConfString = "SAMLRequest";
        } else if (paramName.equalsIgnoreCase(EidasParameterKeys.SAML_RESPONSE.toString())
                || paramName.equalsIgnoreCase(EidasParameterKeys.LIGHT_RESPONSE.toString())) {
            paramConfString = "SAMLResponse";
        } else {
            paramConfString = paramName;
        }
        return MAX_PARAM_PREFIX + paramConfString + MAX_PARAM_SUFFIX;
    }

    private static void logErrorMessage(@Nullable Marker marker, String message) {
        if (null != marker) {
            fullLogger.error(marker, message);
        } else {
            LOG.error(message);
        }
    }

    @Nonnull
    private String paramName;

    @Nullable
    private String paramValue;

    @Nonnull
    private String errorCode;

    @Nonnull
    private String errorMessage;

    @Nullable
    public Marker marker;

    protected AbstractParameterValidator() {
    }

    @Nonnull
    protected final V baseParamName(@Nonnull EidasParameterKeys paramName) {
        this.paramName = paramName.getValue();
        return (V) this;
    }

    @Nonnull
    protected final V baseParamName(final String paramName) {
        this.paramName = paramName;
        return (V) this;
    }

    protected final void baseValidate() {
        if (!isValid()) {
            if (StringUtils.isNotBlank(paramName)) {
                if (StringUtils.isBlank(errorCode)) {
                    errorCode = getErrorCode(paramName);
                }
                if (StringUtils.isBlank(errorMessage)) {
                    errorMessage = getErrorMessage(paramName);
                }
            }
            throw newAbstractEIDASException(errorCode, errorMessage);
        }
    }

    private static String truncateToMaxSize(String paramValue, int maxSize) {
        if (paramValue != null && paramValue.length() > maxSize) {
            paramValue = paramValue.substring(0, maxSize) + "... (Logged message truncated due to message size restrictions)";
        }
        return paramValue;
    }

    /**
     * Method to set empty String for Saml Response parameter
     *
     * @param paramName  The name of the parameter.
     * @param paramValue The value of the parameter.
     * @return empty String
     */
    private static String setEmptyParamValueForSamlMessage(@Nonnull String paramName, @Nullable String paramValue) {
        if (paramName.equalsIgnoreCase(EidasParameterKeys.SAML_REQUEST.toString())
                || paramName.equalsIgnoreCase(EidasParameterKeys.SAML_RESPONSE.toString())) {
            paramValue = StringUtils.EMPTY;
        }
        return paramValue;
    }

    @Nonnull
    public final V eidasError(final EidasErrorKey eidasError) {
        this.errorCode = EidasErrors.get(eidasError.errorCode());
        this.errorMessage = EidasErrors.get(eidasError.errorMessage());
        return (V) this;
    }

    @Nonnull
    public final V errorCode(final String errorCode) {
        this.errorCode = errorCode;
        return (V) this;
    }

    @Nonnull
    public final V errorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
        return (V) this;
    }

    public final boolean isValid() {
        return isValidParameter(paramName, paramValue, marker);
    }

    @Nonnull
    protected abstract AbstractEIDASException newAbstractEIDASException(@Nonnull String errorCode,
                                                                        @Nonnull String errorMessage);

    @Nonnull
    public final V paramValue(final String paramValue) {
        this.paramValue = paramValue;
        return (V) this;
    }

    @Nonnull
    public final V marker(Marker marker) {
        this.marker = marker;
        return (V) this;
    }
}
