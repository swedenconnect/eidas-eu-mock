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
package eu.eidas.node;

import javax.annotation.Nonnull;

/**
 * This enum class contains parameter names.
 */
public enum NodeParameterNames {

    AP_RESPONSE("springManagedAPResponse"),

    ATTR_LIST("attrList"),

    CALLBACK_URL("callbackURL"),

    CITIZEN_COUNTRY_CODE("citizenCountryCode"),

    EIDAS_ATTRIBUTES_PARAM("eidasAttributes"),

    EIDAS_SERVICE_URL("serviceUrl"),

    EXCEPTION("exception"),

    ERROR_MESSAGE("errorMessage"),

    LOA_VALUE("LoA"),

    REDIRECT_URL("redirectUrl"),

    RELAY_STATE("RelayState"),

    REQUEST_ID("requestId"),

    SPECIFIC_RESPONSE("specificResponse"),

    SAML_TOKEN("samlToken"),

    SAML_TOKEN_FAIL("samlTokenFail"),

    STR_ATTR_LIST("strAttrList"),

    LIGHT_RESPONSE("lightResponse"),

    CONTACT_SUPPORT_EMAIL("contactSupportEmail"),

    ERROR_ID("errorId"),

    // put the ; on a separate line to make merges easier
    ;

    /**
     * constant name.
     */
    @Nonnull
    private final transient String name;

    /**
     * Constructor
     *
     * @param name name of the attribute
     */
    NodeParameterNames(@Nonnull String name){
        this.name = name;
    }

    @Nonnull
    @Override
    public String toString() {
        return name;
    }
}