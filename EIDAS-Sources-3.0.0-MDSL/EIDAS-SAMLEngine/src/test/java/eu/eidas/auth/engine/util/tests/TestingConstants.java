/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.auth.engine.util.tests;

/**
 * This enum class contains all the EidasNode testing constants.
 */
public enum TestingConstants {
    /**
     * Represents the 'ASSERTION_URL' constant.
     */
    ASSERTION_URL_CONS("ASSERTION_URL"),
    /**
     * Represents the 'ASSERTION_URL' constant.
     */
    CITIZEN_COUNTRY_CODE_CONS("PT"),
    /**
     * Represents the 'DESTINATION_CONS' constant.
     */
    DESTINATION_CONS("SP-URL"),
    /**
     * Represents the 'LOCAL-Connector' constant.
     */
    ISSUER_CONS("LOCAL-Connector"),
    /**
     * Represents the 'f5e7e0f5-b9b8-4256-a7d0-4090141b326d' constant.
     */
    REQUEST_ID_CONS("f5e7e0f5-b9b8-4256-a7d0-4090141b326d"),
    /**
     * Represents the 'REQUEST_DESTINATION' constant.
     */
    REQUEST_DESTINATION_CONS("REQUEST_DESTINATION"),
    /**
     * Represents the 'REQUEST_ISSUER' constant.
     */
    REQUEST_ISSUER_CONS("REQUEST_ISSUER"),
    /**
     * Represents the 'SERVICE-METADATA-URL' constant.
     */
    SERVICE_METADATA_URL_CONS("SERVICE-METADATA-URL"),
    /**
     * Represents the 'samlId' constant.
     */
    SAML_ID_CONS("_12341234123412341234123412341234"),
    ;

    /**
     * Represents the constant's value.
     */
    private final transient String value;

    /**
     * Solo Constructor.
     *
     * @param nValue The Constant value.
     */
    private TestingConstants(final String nValue) {
        this.value = nValue;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    @Override
    public String toString() {
        return value;
    }

}
