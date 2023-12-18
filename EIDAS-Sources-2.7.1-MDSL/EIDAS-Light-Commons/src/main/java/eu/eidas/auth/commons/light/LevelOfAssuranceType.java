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

package eu.eidas.auth.commons.light;

/**
 * Enum class to describe the possible values of the Level of Assurance types.
 */
public enum LevelOfAssuranceType {

    NOTIFIED ("notified"),
    NON_NOTIFIED ("nonNotified"),
    ;

    private String value;

    LevelOfAssuranceType(String value) {
        this.value = value;
    }

    /**
     * @return return the string value of the Type.
     */
    public String stringValue() {
        return value;
    }

    /**
     * Get the Level of Assurance Type based on a given value.
     * @param value the type value.
     * @return The Type matching the given value or null
     */
    public static LevelOfAssuranceType fromString(String value) {
        if (value == null) {
            return null;
        }
        if (value.equalsIgnoreCase(NON_NOTIFIED.stringValue())) {
            return NON_NOTIFIED;
        } else if (value.equalsIgnoreCase(NOTIFIED.stringValue())) {
            return NOTIFIED;
        }
        return null;
    }

    /**
     * Get the Level of Assurance Type based on the Level of Assurance value.
     * @param loaValue the Level of Assurance value.
     * @return the Type matching with the given Level of Assurance or null if loaValue is null
     */
    public static LevelOfAssuranceType fromLoAValue(String loaValue) {
        if (loaValue == null) {
            return null;
        }
        // Specwise URI's should be case insensitive only in the protocol://host portion however we decided to go for exact string matching of notified LoA's
        if (ILevelOfAssurance.EIDAS_LOA_LOW.equals(loaValue) || ILevelOfAssurance.EIDAS_LOA_SUBSTANTIAL.equals(loaValue) || ILevelOfAssurance.EIDAS_LOA_HIGH.equals(loaValue)) {
            return NOTIFIED;
            // The eidas prefix will be prevented no matter the upper or lower case
        } else if (loaValue.trim().toUpperCase().startsWith(ILevelOfAssurance.EIDAS_LOA_PREFIX.toUpperCase())) {
            throw new IllegalArgumentException("Loa with EIDAS prefix does not match any Notified Values.");
        } else {
            return NON_NOTIFIED;
        }
    }

    @Override
    public String toString() {
        return stringValue();
    }
}
