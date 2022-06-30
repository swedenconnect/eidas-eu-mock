package eu.eidas.auth.commons.validation;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;

/**
 * Fluent API to validate parameter in non-service classes.
 * <p/>
 * Usage:
 * <pre>
 *     NormalParameterValidator.paramName("name").paramValue("value").eidasError(error).validate();
 * </pre>
 *
 * @since 1.1
 */
public final class NormalParameterValidator extends AbstractParameterValidator<NormalParameterValidator> {

    @Nonnull
    public static NormalParameterValidator paramName(@Nonnull EidasParameterKeys paramName) {
        return new NormalParameterValidator().baseParamName(paramName);
    }

    @Nonnull
    public static NormalParameterValidator paramName(@Nonnull String paramName) {
        return new NormalParameterValidator().baseParamName(paramName);
    }

    private NormalParameterValidator() {
    }

    @Nonnull
    @Override
    protected AbstractEIDASException newInvalidParameterException(@Nonnull String errorCode,
                                                                  @Nonnull String errorMessage) {
        return new InvalidParameterEIDASException(errorCode, errorMessage);
    }

    public void validate() throws InvalidParameterEIDASException {
        baseValidate();
    }
}
