/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.node.service.validation;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASServiceException;
import eu.eidas.auth.commons.validation.AbstractParameterValidator;
import eu.eidas.node.NodeParameterNames;

/**
 * Fluent API to validate parameter in node service classes.
 * <p>
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
