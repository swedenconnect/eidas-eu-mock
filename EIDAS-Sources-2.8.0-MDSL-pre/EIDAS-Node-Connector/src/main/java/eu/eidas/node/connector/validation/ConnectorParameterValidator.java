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
package eu.eidas.node.connector.validation;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.auth.commons.validation.AbstractParameterValidator;
import eu.eidas.node.connector.exceptions.ConnectorError;

import javax.annotation.Nonnull;

public final class ConnectorParameterValidator extends AbstractParameterValidator<ConnectorParameterValidator> {

    @Nonnull
    public static ConnectorParameterValidator paramName(@Nonnull EidasParameterKeys paramName) {
        return new ConnectorParameterValidator().baseParamName(paramName);
    }

    private ConnectorParameterValidator() {}

    @Nonnull
    @Override
    protected AbstractEIDASException newAbstractEIDASException(@Nonnull String errorCode, @Nonnull String errorMessage) {
        return new ConnectorError(errorCode, errorMessage);
    }

    public void validate() throws AbstractEIDASException {
        baseValidate();
    }
}
