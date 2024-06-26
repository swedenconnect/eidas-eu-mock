/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * This enum class contains all the eIDAS Node, Commons and Specific Parameters.
 */
public enum EidasParameterKeys {

    /**
     * Represents the 'apId' parameter constant.
     */
    AP_ID("apId"),
    /**
     * Represents the 'apUrl' parameter constant.
     */
    AP_URL("apUrl"),
    /**
     * Represents the 'ap.number' parameter constant.
     */
    AP_NUMBER("ap.number"),

    /**
     * Represents the 'assertionConsumerServiceURL' parameter constant.
     */
    ASSERTION_CONSUMER_S_URL("assertionConsumerServiceURL"),

    /**
     * Represents the 'attr' parameter constant.
     */
    ATTRIBUTE("attr"),
    /**
     * Represents the 'attrName' parameter constant.
     */
    ATTRIBUTE_NAME("attrName"),
    /**
     * Represents the 'attrStatus' parameter constant.
     */
    ATTRIBUTE_STATUS("attrStatus"),
    /**
     * Represents the 'attrType' parameter constant.
     */
    ATTRIBUTE_TYPE("attrType"),
    /**
     * Represents the 'attrValue' parameter constant.
     */
    ATTRIBUTE_VALUE("attrValue"),
    /**
     * Represents the 'attrList' parameter constant.
     */
    ATTRIBUTE_LIST("attrList"),
    /**
     * Represents the 'attrTuple' parameter constant.
     */
    ATTRIBUTE_TUPLE("attrTuple"),
    /**
     * Represents the 'attribute-missing' parameter constant.
     */
    ATTRIBUTE_MISSING("attribute-missing"),
    /**
     * Represents the 'attributesNotAllowed' parameter constant.
     */
    ATTRIBUTES_NOT_ALLOWED("attributesNotAllowed"),
    /**
     * Represents the 'authnRequest' parameter constant.
     */
    AUTH_REQUEST("authnRequest"),

    /**
     * Represents the 'attrValue.number' parameter constant.
     */
    ATTR_VALUE_NUMBER("attrValue.number"),

    /**
     * Represents the 'derivation.date.format' parameter constant.
     */
    DERIVATION_DATE_FORMAT("derivation.date.format"),
    /**
     * Represents the 'deriveAttr.number' parameter constant.
     */
    DERIVE_ATTRIBUTE_NUMBER("deriveAttr.number"),

    /**
     * Represents the 'canonicalResidenceAddress' parameter constant.
     */
    COMPLEX_ADDRESS_VALUE("canonicalResidenceAddress"),
    /**
     * Represents the 'country' parameter constant.
     */
    COUNTRY("country"),
    /**
     * Represents the 'countryOrigin' parameter constant.
     */
    COUNTRY_ORIGIN("countryOrigin"),

    /**
     * Represents the 'eidasserviceURL' parameter constant.
     */
    EIDAS_SERVICE_URL("eidasserviceURL"),
    /**
     * Represents the 'callback' parameter constant.
     */
    EIDAS_SERVICE_CALLBACK("callback"),
    /**
     * Represents the 'connector.contact.support.email' parameter constant.
     */
    CONNECTOR_CONTACT_SUPPORT("connector.contact.support.email"),
    /**
     * Represents the 'service.contact.support.email' parameter constant.
     */
    PROXYSERVICE_CONTACT_SUPPORT("service.contact.support.email"),
    /**
     * Represents the 'errorCode' parameter constant.
     */
    ERROR_CODE("errorCode"),
    /**
     * Represents the 'subCode' parameter constant.
     */
    ERROR_SUBCODE("subCode"),
    /**
     * Represents the 'errorMessage' parameter constant.
     */
    ERROR_MESSAGE("errorMessage"),
    /**
     * Represents the 'errorRedirectUrl' parameter constant.
     */
    ERROR_REDIRECT_URL("errorRedirectUrl"),

    /**
     * errorRedirectUrl Represents the 'external-authentication' parameter constant.
     */
    EXTERNAL_AUTH("external-authentication"),
    /**
     * Represents the 'external-ap' parameter constant.
     */
    EXTERNAL_AP("external-ap"),
    /**
     * Represents the 'external-sig-module' parameter constant.
     */
    EXT_SIG_CREATOR_MOD("external-sig-module"),

    /**
     * Represents the 'RelayState' parameter constant.
     */
    RELAY_STATE("RelayState"),

    /**
     * Represents the 'idp.url' parameter constant.
     */
    IDP_URL("idp.url"),
    /**
     * Represents the 'internal-authentication' parameter constant.
     */
    INTERNAL_AUTH("internal-authentication"),
    /**
     * Represents the 'internal-ap' parameter constant.
     */
    INTERNAL_AP("internal-ap"),

    /**
     * Represents the 'samlIssuer' parameter constant.
     */
    ISSUER("samlIssuer"),

    /**
     * Represents the 'provider.name' parameter constant.
     */
    PROVIDER_NAME_VALUE("providerName"),
    /**
     * Represents the 'service.number' parameter constant.
     */
    EIDAS_NUMBER("service.number"),
    /**
     * Represents the 'eidas.connector.redirectUrl' parameter constant.
     */
    EIDAS_CONNECTOR_REDIRECT_URL("connector.redirectUrl"),

    /**
     * Represents the 'sp.redirectUrl' parameter constant.
     */
    SP_REDIRECT_URL("sp.redirectUrl"),
    /**
     * Represents the 'eidas.service.redirectUrl' parameter constant.
     */
    EIDAS_SERVICE_REDIRECT_URL("service.redirectUrl"),
    /**
     * Represents the 'lightRequest' parameter constant.
     */
    LIGHT_REQUEST("lightRequest"),
    /**
     * Represents the 'lightResponse' parameter constant.
     */
    LIGHT_RESPONSE("lightResponse"),

    /**
     * Represents the 'remoteAddr' parameter constant.
     */
    REMOTE_ADDR("remoteAddr"),
    /**
     * Represents the 'remoteUser' parameter constant.
     */
    REMOTE_USER("remoteUser"),
    /**
     * Represents the 'requesterId' parameter constant
     */
    REQUESTER_ID("requesterId"),

    /**
     * Represents the 'SAMLRequest' parameter constant.
     */
    SAML_REQUEST("SAMLRequest"),
    /**
     * Represents the 'SAMLResponse' parameter constant.
     */
    SAML_RESPONSE("SAMLResponse"),
    /**
     * Represents the 'SMSSPRequest' parameter constant.
     */
    SMSSP_REQUEST("SMSSPRequest"),
    /**
     * Represents the 'SMSSPResponse' parameter constant.
     */
    SMSSP_RESPONSE("SMSSPResponse"),
    /**
     * Represents the 'SAMLResponse' parameter constant.
     */
    SAML_ARTIFACT("SAMLArtifact"),
    /**
     * Represents the 'TokenId' parameter constant.
     */
    SAML_TOKEN_ID("TokenId"),
    /**
     * Represents the 'inResponseTo' parameter constant.
     */
    SAML_IN_RESPONSE_TO("inResponseTo"),

    /**
     * Represents the 'SignatureResponse' parameter constant.
     */
    SIGNATURE_RESPONSE("SignatureResponse"),

    /**
     * Represents the 'spId' parameter constant.
     */
    SP_ID("spId"),
    /**
     * Represents the 'spUrl' parameter constant.
     */
    SP_URL("spUrl"),

    /**
     * Represents the 'allow.derivation.all' parameter constant.
     */
    SPECIFIC_ALLOW_DERIVATION_ALL("allow.derivation.all"),
    /**
     * Represents the ''allow.unknowns parameter constant.
     */
    SPECIFIC_ALLOW_UNKNOWNS("allow.unknowns"),
    /**
     * Represents the 'specific.proxyservice.request.receiver' parameter constant.
     */
    SPECIFIC_CONNECTOR_RESPONSE_RECEIVER("specific.connector.response.receiver"),
    /**
     * Represents the 'derivation.date.separator' parameter constant.
     */
    SPECIFIC_DERIVATION_DATE_SEP("derivation.date.separator"),
    /**
     * Represents the 'derivation.month.position' parameter constant.
     */
    SPECIFIC_DERIVATION_MONTH_POS("derivation.month.position"),
    /**
     * Represents the 'derivation.day.position' parameter constant.
     */
    SPECIFIC_DERIVATION_DAY_POS("derivation.day.position"),
    /**
     * Represents the 'derivation.year.position' parameter constant.
     */
    SPECIFIC_DERIVATION_YEAR_POS("derivation.year.position"),
    /**
     * Represents the 'specific.proxyservice.request.receiver' parameter constant.
     */
    SPECIFIC_PROXYSERVICE_REQUEST_RECEIVER("specific.proxyservice.request.receiver"),
    /**
     * Represents the 'spSector' constant value.
     */
    SPSECTOR("spSector"),
    /**
     * Represents the 'serviceProviderCountryCode' constant value.
     */
    SPCOUNTRY("serviceProviderCountryCode"),
    /**
     * Represents the 'spInstitution' constant value.
     */
    SPINSTITUTION("spInstitution"),
    /**
     * Represents the 'Attribute.number' parameter constant.
     */
    SPECIFIC_ATTRIBUTE_NUMBER("Attribute.number"),
    /**
     * Represents the "unsupported.attributes" parameter constant.
     */
    UNSUPPORTED_ATTRIBUTES("unsupported.attributes"),
    /**
     * Represents the 'eidasAttributeValue.number' parameter constant.
     */
    EIDAS_ATTRIBUTE_VALUE_NUMBER("eidasAttributeValue.number"),

    /**
     * Represents the 'tooManyParameters' parameter constant.
     */
    TOO_MANY_PARAMETERS("tooManyParameters"),
    /**
     * Represents the 'validation.active' parameter constant.
     */
    VALIDATION_ACTIVE("validation.active"),

    /**
     * Represents the 'x-forwarded-for' parameter constant.
     */
    X_FORWARDED_FOR("x-forwarded-for"),
    /**
     * Represents the 'x-forwarded-host' parameter constant.
     */
    X_FORWARDED_HOST("x-forwarded-host"),
    /**
     * Represents the 'XMLResponse' parameter constant.
     */
    XML_RESPONSE("XMLResponse"),

    /**
     * the phase of saml (request or response)
     */
    SAML_PHASE("samlphase"),

    /**
     * http binding to use (either GET or POST)
     */
    BINDING("binding"),
    /**
     * name of parameter which activates binding validation
     */
    VALIDATE_BINDING("validate.binding"),
    ALLOW_REDIRECT_BINDING("allow.redirect.binding"),
    /**
     * name of the session parameter holding the url of an error interceptor (used in Validation Node)
     */
    ERROR_INTERCEPTOR_URL("error.interceptor.url"),

    /**
     * set to true when the http session is created on the eidas connector
     */
    EIDAS_CONNECTOR_SESSION("eidas.connector.session"),

    EIDAS_CONNECTOR_COUNTRY("EIDASConnectorCountry"),

    METADATA_ACTIVE("metadata.activate"),
    METADATA_TIMEOUT("metadata.request.timeout"),
    METADATA_FETCHER_WHITELIST("metadata.location.whitelist"),
    METADATA_FETCHER_WHITELIST_FLAG("metadata.location.whitelist.use"),

    EIDAS_CONNECTOR_TRUSTED_SP("trusted.sp.domains"),
    EIDAS_SERVICE_LOA("saml.loa"),

    /**
     * Parameter key for configuring the algorithm to be use to hash the logging token message.
     */
    LOGGING_HASH_DIGEST_ALGORITHM ("logging.hash.digest.algorithm"),
    /**
     * Parameter key for configuring the provider to be use to apply hash on logging token message.
     */
    LOGGING_HASH_DIGEST_PROVIDER ("logging.hash.digest.provider"),

    /**
     * Parameter key for configuring a list of additional optional nameid formats for the connector.
     */
    EIDAS_CONNECTOR_NAMEID_FORMATS("connector.optional.nameid.formats"),
    /**
     * Parameter key for configuring a list of additional optional nameid formats for the proxyservice.
     */
    EIDAS_SERVICE_NAMEID_FORMATS("service.optional.nameid.formats"),
    /**
     * whether to sign the assertion in Idp
     */
    RESPONSE_ENCRYPT_ASSERTION("response.encryption.mandatory"),
    /**
     * the current http method
     */
    HTTP_METHOD("request.http.method"),
    /**
     * request format name(eidas)
     */
    REQUEST_FORMAT("request.format"),

    CITIZEN_COUNTRY_CODE("citizen.countryCode"),

    CITIZEN_IP_ADDRESS("citizen.ipAddress"),

    SERVICE_PROVIDER_NAME("serviceProvider.name"),

    SERVICE_PROVIDER_TYPE("serviceProvider.type"),

    TOKEN("token"),

    CSP_SCRIPT_NONCE("cspScriptNonce"),
    // put the ; on a separate line to make merges easier
    ;

    /**
     * Represents the constant's value.
     */
    @Nonnull
    private final transient String value;

    private static final EnumMapper<String, EidasParameterKeys> MAPPER =
            new EnumMapper<String, EidasParameterKeys>(new KeyAccessor<String, EidasParameterKeys>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EidasParameterKeys eidasParameter) {
                    return eidasParameter.getValue();
                }
            }, Canonicalizers.trim(), values());

    private static final Set<String> ALL_NAMES = MAPPER.unmodifiableKeySet(values());

    /**
     * Solo Constructor.
     *
     * @param nValue The Constant value.
     */
    EidasParameterKeys(@Nonnull String nValue) {
        value = nValue;
    }

    @Nullable
    public static EidasParameterKeys fromString(@Nonnull String val) {
        return MAPPER.fromKey(val);
    }

    /**
     * @return all registered names
     */
    public static Set<String> getNames() {
        return ALL_NAMES;
    }

    public static EnumMapper<String, EidasParameterKeys> mapper() {
        return MAPPER;
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
}
