/*
 * Copyright (c) 2020 by European Commission
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
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.EidasParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

/**
 * A base implementation of the ParameterValidator interface
 */
public abstract class BaseValueValidator<T> implements ValueValidator<T> {

    protected BaseValueValidator() {}

    /**
     * Check if the validation is active, if it is active it validates that the given value
     * is not blank and that it is valid.
     * @param value the value to validate
     * @return true if validation is not active or if value is valid, false otherwise.
     */
    @Override
    public final boolean isValid(T value) {
        if (isValidationActive()) {
            return isValidValue(value);
        } else {
            return true;
        }
    }

    /**
     * Technical or business validation of the value
     * @param value the value to validate
     * @return true if the value is valid, false otherwise
     */
    protected abstract boolean isValidValue(T value);

    protected boolean isValidationActive() {
        String validationParam = EidasParameters.get(EidasParameterKeys.VALIDATION_ACTIVE.toString());
        if (EIDASValues.TRUE.toString().equalsIgnoreCase(validationParam)) {
            return true;
        }
        return false;
    }

    /**
     * Base implementation of ParameterValidator builders
     * @param <T> a BaseParameterValidator implementation
     */
    public static abstract class Builder<T extends BaseValueValidator> {

        private Supplier<T> validatorSupplier;

        /**
         * The builder
         * @param supplier a supplier of BaseParameterValidator instances
         */
        public Builder(Supplier<T> supplier) {
            this.validatorSupplier = supplier;
        }

        /**
         * Constructs a ParameterValidator.
         * @return the build ParameterValidator
         */
        public T build() {
            T validator = validatorSupplier.get();
            return validator;
        }
    }
}
