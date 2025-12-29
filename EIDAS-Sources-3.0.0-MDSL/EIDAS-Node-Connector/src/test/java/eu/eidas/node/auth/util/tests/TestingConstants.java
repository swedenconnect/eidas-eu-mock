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

package eu.eidas.node.auth.util.tests;

import eu.eidas.auth.commons.EIDASStatusCode;
import eu.eidas.auth.commons.protocol.eidas.NotifiedLevelOfAssurance;

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
     * Represents the 'CONNECTOR-METADATA-URL' constant.
     */
    CONNECTOR_METADATA_URL_CONS("CONNECTOR-METADATA-URL"),
    /**
     * Represents the 'DESTINATION_CONS' constant.
     */
    DESTINATION_CONS("SP-URL"),
    /**
     * Represents the 'ERROR_CODE' constant.
     */
    ERROR_CODE_CONS("ERROR_CODE"),
    /**
     * Represents the 'ERROR_MESSAGE' constant.
     */
    ERROR_MESSAGE_CONS("ERROR_MESSAGE"),
    /**
     * Represents the '127.0.0.1' constant.
     */
    IP_ADDRESS("127.0.0.1"),
    /**
     * Represents the 'LOCAL-Connector' constant.
     */
    ISSUER_CONS("LOCAL-Connector"),
    /**
     * Represents the 'LOCAL' constant.
     */
    LOCAL_CONS("LO"),
    /**
     * Represents the LevelOfAssurance.LOW stringValue.
     */
    LEVEL_OF_ASSURANCE_LOW_CONS(NotifiedLevelOfAssurance.LOW.stringValue()),
    /**
     * Represents the LevelOfAssurance.HIGH stringValue.
     */
    LEVEL_OF_ASSURANCE_HIGH_CONS(NotifiedLevelOfAssurance.HIGH.stringValue()),
    /**
     * Represents the 'LOCAL_DUMMY_URL' constant.
     */
    LOCAL_URL_CONS("LOCAL_DUMMY_URL"),
    /**
     * Represents the '1' constant.
     */
    ONE_CONS("1"),
    /**
     * Represents the 'PROVIDERNAME_CERT' constant.
     */
    PROVIDERNAME_CERT_CONS("PROVIDERNAME_CERT"),
    /**
     * Represents the 'REQUEST_CITIZEN_COUNTRY_CODE' constant.
     */
    REQUEST_CITIZEN_COUNTRY_CODE_CONS("REQUEST_CITIZEN_COUNTRY_CODE"),
    /**
     * Represents the 'f5e7e0f5-b9b8-4256-a7d0-4090141b326d' constant.
     */
    REQUEST_ID_CONS("f5e7e0f5-b9b8-4256-a7d0-4090141b326d"),
    /**
     * Represents the 'REQUEST_DESTINATION' constant.
     */
    REQUEST_DESTINATION_CONS("REQUEST_DESTINATION"),
    /**
     * Represents the '_fHhEts9JPpseTQXGCaSwyqNX-waO6lnphoG7xTOe6c0Tw10oDGIlsPLkh97Uiq' const.
     */
    RESPONSE_ID_CONS("_fHhEts9JPpseTQXGCaSwyqNX-waO6lnphoG7xTOe6c0Tw10oDGIlsPLkh97Uiq"),
    /**
     * Represents the 'REQUEST_ISSUER' constant.
     */
    REQUEST_ISSUER_CONS("REQUEST_ISSUER"),
    /**
     * Represents the 'LevelOfAssurance.LOW.stringValue()' value.
     */
    REQUEST_LEVEL_OF_ASSURANCE_LOW_CONS(NotifiedLevelOfAssurance.LOW.stringValue()),
    /**
     * Represents the 'EIDASStatusCode.SUCCESS_URI.toString()' value.
     */
    RESPONSE_STATUS_CODE_SUCCESS_CONS(EIDASStatusCode.SUCCESS_URI.toString()),
    /**
     * Represents the response subject value.
     */
    RESPONSE_SUBJECT_CONS("RESPONSE_SUBJECT"),
    /**
     * Represents the 'samlId' constant.
     */
    SAML_ID_CONS("_12341234123412341234123412341234"),
    /**
     * Represents the 'SAML_ISSUER_CONS' constant.
     */
    SAML_ISSUER_CONS("http://ConnectorMetadata"),
    /**
     * Represents the 'samlInstance' constant.
     */
    SAML_INSTANCE_CONS("Service"),
    /**
     * Represents the 'SAML_TOKEN_CONS' constant.
     */
    SAML_TOKEN_CONS("<saml>...</saml>"),
    /**
     * Represents the 'spid' constant.
     */
    SPID_CONS("SP"),
    /**
     * Represents the 'SP-REQUEST-DESTINATION-URL' constant.
     */
    SP_REQUEST_DESTINATION_CONS("SP-REQUEST-DESTINATION-URL"),
    /**
     * Represents the 'SP-REQUEST-ISSUER' constant.
     */
    SP_REQUEST_ISSUER_CONS("SP-REQUEST-ISSUER"),
    /**
     * Represents the 'true' constant.
     */
    TRUE_CONS("true"),
    /**
     * Represents a skew time of 0
     */
    SKEW_ZERO_CONS("0"),
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

    /**
     * Return the Constant integer Value.
     *
     * @return The constant int value.
     */
    public long longValue() {
        return Long.valueOf(value).longValue();
    }
}
