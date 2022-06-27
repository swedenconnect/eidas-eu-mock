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

import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import eu.eidas.auth.commons.protocol.eidas.impl.Gender;

/**
 * Validation of the Gender in regards with the Protocol version.
 */
public class GenderProtocolVersionValidator extends ProtocolVersionValidator<Gender> {

    /**
     * Gender value is validated  with regards to the given eIDAS protocol versions.
     * If no protocol version given gender is checked against technical specs 1.2
     *
     * @param gender  the gender to validate
     * @return true if the gender is valid for the protocol version, false otherwise
     */
    @Override
    protected boolean isValidValue(Gender gender) {
        boolean isValidGender = false;
        if (getProtocolVersions() == null || getProtocolVersions().isEmpty()) {
            return isValidGenderForSpecs1_2(gender);
        }
        for (EidasProtocolVersion protocolVersion : getProtocolVersions()) {
            if (EidasProtocolVersion.PROTOCOL_VERSION_1_2.equals(protocolVersion)) {
                isValidGender |= isValidGenderForSpecs1_2(gender);
            }
            if (EidasProtocolVersion.PROTOCOL_VERSION_1_1.equals(protocolVersion)) {
                isValidGender |= isValidGenderForSpecs1_1(gender);
            }
        }
        return isValidGender;
    }

    private boolean isValidGenderForSpecs1_1(Gender gender) {
        return Gender.MALE.equals(gender)
                || Gender.FEMALE.equals(gender)
                || Gender.NOT_SPECIFIED.equals(gender);
    }

    private boolean  isValidGenderForSpecs1_2(Gender gender) {
        return Gender.MALE.equals(gender)
                || Gender.FEMALE.equals(gender)
                || Gender.UNSPECIFIED.equals(gender);
    }

    /**
     * Method to get a GenderProtocolVersionValidator builder.
     * @return a new GenderProtocolVersionValidator builder.
     */
    public static GenderProtocolVersionValidator.Builder Builder() {
        return new GenderProtocolVersionValidator.Builder();
    }

    /**
     * Method to get a GenderProtocolVersionValidator builder for the given protocol version.
     * @param protocolVersion the eidas protocol version the validator need to validate the gender with.
     * @return the GenderProtocolVersionValidator.
     */
    public static GenderProtocolVersionValidator forProtocolVersion(EidasProtocolVersion protocolVersion) {
        return Builder().protocolVersion(protocolVersion).build();
    }

    /**
     * Builder class for a Gender ProtocolVersion based validator.
     */
    public static class Builder extends ProtocolVersionValidator.Builder<Builder, GenderProtocolVersionValidator> {

        public Builder() {
            super(GenderProtocolVersionValidator::new);
        }

    }
}
