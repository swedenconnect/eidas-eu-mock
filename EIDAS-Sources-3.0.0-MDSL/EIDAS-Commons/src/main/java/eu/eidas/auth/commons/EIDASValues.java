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
package eu.eidas.auth.commons;

import javax.annotation.Nonnull;

/**
 * This enum class contains all the value constants.
 */
public enum EIDASValues {

    /**
     * Represents the 'all' constant value.
     */
    ALL("all"),
    /**
     * Represents the 'none' constant value.
     */
    NONE("none"),
    /**
     * Represents the 'true' constant value.
     */
    TRUE("true"),
    /**
     * Represents the 'false' constant value.
     */
    FALSE("false"),

    /**
     * Represents the ';' separator constant value.
     */
    ATTRIBUTE_SEP(";"),

    /**
     * Represents the 'EIDASSERVICE' constant value.
     */
    EIDAS_SERVICE_PREFIX("service"),
    /**
     * Represents the '-EIDASNODE' constant value.
     */
    EIDAS_SERVICE_SUFFIX("-EIDASNODE"),

    /**
     * Represents the 'eu.eidas.communication.requests' constant value.
     */
    EIDAS_PACKAGE_REQUEST_LOGGER_VALUE("eu.eidas.communication.requests"),

    /**
     * Represents the 'eu.eidas.communication.responses' constant value.
     */
    EIDAS_PACKAGE_RESPONSE_LOGGER_VALUE("eu.eidas.communication.responses"),

    /**
     *  Represents the 'eu.eidas.communication.logging.full' package name
     */
    EIDAS_PACKAGE_LOGGING_FULL("eu.eidas.logging.full"),

    /**
     * Represents the 'Connector receives request from SP' constant value.
     */
    SP_REQUEST("eIDAS Connector receives request from SP"),
    /**
     * Represents the 'eIDAS Connector receives request from Specific Connector' constant value.
     */
    SPECIFIC_EIDAS_CONNECTOR_REQUEST("eIDAS Connector receives request from Specific Connector"),
    /**
     * Represents the 'eIDAS Connector sends request to eIDAS Proxy Service' constant value.
     */
    CONNECTOR_SERVICE_REQUEST("eIDAS Connector sends request to eIDAS Proxy Service"),
    /**
     * Represents the 'eIDAS Proxy Service sends request to Specific Proxy Service' constant value.
     */
    EIDAS_SERVICE_SPECIFIC_REQUEST("eIDAS Proxy Service sends request to Specific Proxy Service"),
    /**
     * Represents the 'eIDAS Proxy Service receives request from eIDAS Connector' constant value.
     */
    EIDAS_SERVICE_REQUEST("eIDAS Proxy Service receives request from eIDAS Connector"),
    /**
     * Represents the 'eIDAS Proxy Service receives response from Specific Proxy Service' constant value.
     */
    EIDAS_SERVICE_SPECIFIC_RESPONSE("eIDAS Proxy Service receives response from Specific Proxy Service"),
    /**
     * Represents the 'eIDAS Service generates response to eIDAS Connector' constant value.
     */
    EIDAS_SERVICE_RESPONSE("eIDAS Service generates response to eIDAS Connector"),
    /**
     * Represents the 'eIDAS Connector generates request to eIDAS Service' constant value.
     */
    EIDAS_CONNECTOR_REQUEST("eIDAS Connector generates request to eIDAS Service"),
    /**
     * Represents the 'eIDAS Proxy Service sends response to eIDAS Connector' constant value.
     */
    EIDAS_SERVICE_CONNECTOR_RESPONSE("eIDAS Proxy Service sends response to eIDAS Connector"),
    /**
     * Represents the 'eIDAS Connector receives response from eIDAS Proxy Service' constant value.
     */
    EIDAS_CONNECTOR_RESPONSE("eIDAS Connector receives response from eIDAS Proxy Service"),
    /**
     * Represents the 'eIDAS Connector sends response to Specific Connector' constant value.
     */
    EIDAS_CONNECTOR_CONNECTOR_RESPONSE("eIDAS Connector sends response to Specific Connector"),
    /**
     * Represents the December's month number constant value.
     */
    LAST_MONTH("12"),

    /**
     * Represents the 'attrValue' constant value.
     */
    ATTRIBUTE("attrValue"),

    /**
     * Represents the 'referer' constant value.
     */
    REFERER("referer"),
    /**
     * Represents the ';' constant value.
     */
    SEMICOLON(";"),
    EIDAS_SERVICE_LOA("service.LoA"),
    EIDAS_SERVICE_REDIRECT_URIDEST("ssos.serviceMetadataGeneratorIDP.redirect.location"),
    EIDAS_SERVICE_POST_URIDEST("ssos.serviceMetadataGeneratorIDP.post.location"),
    EIDAS_SPTYPE("metadata.sector"),
    EIDAS_NODE_COUNTRY("metadata.node.country"),

    EIDAS_PROTOCOL_VERSION("eidas.protocol.version"),
    EIDAS_APPLICATION_IDENTIFIER("eidas.application.identifier"),

    REQUESTER_ID_FLAG("requester.id.flag"),

    INTERCONNECTION_GRAPH_ENABLED("interconnection.graph.enabled"),

    METADATA_CHECK_SIGNATURE("metadata.check.signature"),

    CONNECTOR_NAMEID_FORMATS("connector.nameid.formats"),

    SERVICE_NAMEID_FORMATS("service.nameid.formats"),

    // put the ; on a separate line to make merges easier
    ;

    /**
     * Represents the constant's value.
     */
    @Nonnull
    private final transient String value;

    /**
     * Solo Constructor.
     *
     * @param val The Constant value.
     */
    EIDASValues(@Nonnull String val) {
        value = val;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    @Nonnull
    @Override
    public String toString() {
        return value;
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".id".
     *
     * @param index the number.
     * @return The concatenated String value.
     */
    public String index(final int index) {

        return value + index + ".id";
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".value".
     *
     * @param index the number.
     * @return The concatenated string value.
     */
    public String value(final int index) {

        return value + index + ".value";
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".name".
     *
     * @param index the number.
     * @return The concatenated String value.
     */
    public String name(final int index) {

        return attribute("name", index);
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".url".
     *
     * @param index the number.
     * @return The concatenated String value.
     */
    public String url(final int index) {

        return attribute("url", index);
    }

    public String attribute(final String attribName, final int index) {

        return value + index + "." + attribName;
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".skew".
     *
     * @param index the number.
     * @return The concatenated String value.
     */
    public String beforeSkew(final int index) {

        return value + index + ".skew.notbefore";
    }

    /**
     * Construct the return value with the following structure CONSTANT_VALUE+index+".skew".
     *
     * @param index the number.
     * @return The concatenated String value.
     */
    public String afterSkew(final int index) {

        return value + index + ".skew.notonorafter";
    }

}
