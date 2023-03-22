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

import java.util.regex.Pattern;

/**
 * Pattern validator implementation.
 */
public final class PatternValidator extends BaseValueValidator<String> {

    private Pattern pattern;

    private PatternValidator() {}

    @Override
    protected boolean isValidValue(String value) {
        return pattern.matcher(value).matches();
    }

    /**
     * Method to get a PatternParameterValidator builder.
     * @return a new PatternParameterValidator builder.
     */
    public static Builder Builder() {
        return new Builder();
    }

    /**
     * Builder class for a Pattern based validator.
     */
    public static class Builder extends BaseValueValidator.Builder<PatternValidator> {

        private Pattern pattern;

        public Builder() {
            super(PatternValidator::new);
        }

        /**
         * Set the validation pattern to be used
         * @param regex the validation regex
         * @return this pattern validator builder.
         */
        public Builder pattern(String regex) {
            Pattern compiledPattern = Pattern.compile(regex);
            return pattern(compiledPattern);
        }

        /**
         * Set the validation pattern to be used
         * @param pattern the validation pattern
         * @return this pattern validator builder
         */
        public Builder pattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        /**
         * Build a pattern based validator with the given pattern and paramName.
         * @return the newly construct pattern parameter validator.
         */
        public PatternValidator build() {
            assert this.pattern != null : "Pattern cannot be null";
            PatternValidator validator = super.build();
            validator.pattern = this.pattern;
            return validator;
        }

    }
}
