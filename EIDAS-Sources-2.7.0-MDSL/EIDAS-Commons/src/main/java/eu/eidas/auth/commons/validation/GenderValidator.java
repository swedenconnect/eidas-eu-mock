/*
 * Copyright (c) 2023 by European Commission
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

import eu.eidas.auth.commons.protocol.eidas.impl.Gender;

/**
 * Validation of the Gender.
 */
public class GenderValidator extends BaseValueValidator<Gender> {

    /**
     * Gender value is validated
     *
     * @param gender  the gender to validate
     * @return true if the gender is valid, false otherwise
     */
    @Override
    protected boolean isValidValue(Gender gender) {
        return isValidGender(gender);
    }

    private boolean isValidGender(Gender gender) {
        return Gender.MALE.equals(gender)
                || Gender.FEMALE.equals(gender)
                || Gender.UNSPECIFIED.equals(gender);
    }

    /**
     * Method to get a GenderBaseValueValidator builder.
     * @return a new GenderBaseValueValidator builder.
     */
    public static GenderValidator.Builder Builder() {
        return new GenderValidator.Builder();
    }

    /**
     * Builder class for a Gender ProtocolVersion based validator.
     */
    public static class Builder extends BaseValueValidator.Builder<GenderValidator> {

        public Builder() {
            super(GenderValidator::new);
        }

    }
}
