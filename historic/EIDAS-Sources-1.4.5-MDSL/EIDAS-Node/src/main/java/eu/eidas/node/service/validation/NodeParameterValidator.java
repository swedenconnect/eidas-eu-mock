package eu.eidas.node.service.validation;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASServiceException;
import eu.eidas.auth.commons.validation.AbstractParameterValidator;
import eu.eidas.node.NodeParameterNames;

/**
 * Fluent API to validate parameter in node service classes.
 * <p/>
 * Usage:
 * <pre>
 *     NodeParameterValidator.paramName("name").paramValue("value").eidasError(error).validate();
 * </pre>
 *
 * @since 1.1
 */
public final class NodeParameterValidator extends AbstractParameterValidator<NodeParameterValidator> {

    @Nonnull
    public static NodeParameterValidator paramName(@Nonnull NodeParameterNames paramName) {
        return new NodeParameterValidator().baseParamName(paramName.toString());
    }

    @Nonnull
    public static NodeParameterValidator paramName(@Nonnull EidasParameterKeys paramName) {
        return new NodeParameterValidator().baseParamName(paramName);
    }

    @Nonnull
    public static NodeParameterValidator paramName(@Nonnull String paramName) {
        return new NodeParameterValidator().baseParamName(paramName);
    }

    private NodeParameterValidator() {
    }

    @Nonnull
    @Override
    protected AbstractEIDASException newInvalidParameterException(@Nonnull String errorCode,
                                                                  @Nonnull String errorMessage) {
        return new InvalidParameterEIDASServiceException(errorCode, errorMessage);
    }

    public void validate() throws InvalidParameterEIDASServiceException {
        baseValidate();
    }
}
