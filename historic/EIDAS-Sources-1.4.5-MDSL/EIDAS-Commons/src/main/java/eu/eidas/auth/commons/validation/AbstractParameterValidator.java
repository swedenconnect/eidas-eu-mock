package eu.eidas.auth.commons.validation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;

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
     * <p/>
     * Checks that the given value is not empty and is not greater than the configured maximum length.
     *
     * @param paramName The name of the parameter to validate.
     * @param paramValue The value of the parameter to validate.
     * @return true if the parameter is valid.
     */
    private static boolean isValidParameter(@Nonnull String paramName, @Nullable String paramValue) {
        String validationParam = EidasErrors.get(EidasParameterKeys.VALIDATION_ACTIVE.toString());
        if (!EIDASValues.TRUE.toString().equals(validationParam)) {
            return true;
        }
        if (StringUtils.isBlank(paramValue)) {
            LOG.info("ERROR : Invalid parameter [" + paramName + "] is blank");
            return false;
        }
        String paramConf = MAX_PARAM_PREFIX + paramName + MAX_PARAM_SUFFIX;
        final String paramSizeStr = EidasParameters.get(paramConf);
        // Checking if the parameter size exists and if it's numeric
        if (StringUtils.isNumeric(paramSizeStr)) {
            final int maxParamSize = Integer.parseInt(paramSizeStr);
            if (paramValue == null || StringUtils.isEmpty(paramValue) || (paramValue.length() > maxParamSize)) {
                LOG.info("ERROR : Invalid parameter [" + paramName + "] value " + paramValue);
                return false;
            }
        } else {
            LOG.info("ERROR : Missing \""
                    + paramConf
                    + "\" configuration in the "
                    + EidasParameters.getPropertiesFilename()
                    + " configuration file");
            return false;
        }
        return true;
    }

    @Nonnull
    private String paramName;

    @Nullable
    private String paramValue;

    @Nonnull
    private String errorCode;

    @Nonnull
    private String errorMessage;

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
            LOG.warn("Invalid parameter [" + paramName + "] value: \"" + paramValue + "\"");
            if (StringUtils.isNotBlank(paramName)) {
                if (StringUtils.isBlank(errorCode)) {
                    errorCode = getErrorCode(paramName);
                }
                if (StringUtils.isBlank(errorMessage)) {
                    errorMessage = getErrorMessage(paramName);
                }
            }
            throw newInvalidParameterException(errorCode, errorMessage);
        }
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
        return isValidParameter(paramName, paramValue);
    }

    @Nonnull
    protected abstract AbstractEIDASException newInvalidParameterException(@Nonnull String errorCode,
                                                                           @Nonnull String errorMessage);

    @Nonnull
    public final V paramValue(final String paramValue) {
        this.paramValue = paramValue;
        return (V) this;
    }
}
