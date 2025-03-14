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

/**
 * This enum class contains all the eIDAS Nodes, Commons and Specific errors constant identifiers.
 */
public enum EidasErrorKey {

    AUTHENTICATION_FAILED_ERROR("authenticationFailed"),
    SP_COUNTRY_SELECTOR_ERROR_CREATE_SAML("spCountrySelector.errorCreatingSAML"),
    SP_COUNTRY_SELECTOR_INVALID_ATTR("spCountrySelector.invalidAttr"),
    SP_COUNTRY_SELECTOR_INVALID("spCountrySelector.invalidCountry"),
    SP_COUNTRY_SELECTOR_SPNOTALLOWED("spCountrySelector.spNotAllowed"),
    SP_REQUEST_INVALID("spRequest.invalid"),
    SPROVIDER_SELECTOR_ERROR_CREATE_SAML("sProviderAction.errorCreatingSAML"),
    SPROVIDER_SELECTOR_INVALID_ATTR("sProviderAction.invalidAttr"),
    SPROVIDER_SELECTOR_INVALID_COUNTRY("sProviderAction.invalidCountry"),
    SPROVIDER_SELECTOR_INVALID_RELAY_STATE("sProviderAction.invalidRelayState"),
    SPROVIDER_SELECTOR_INVALID_SAML("sProviderAction.invalidSaml"),
    SPROVIDER_SELECTOR_INVALID_SPALIAS("sProviderAction.invalidSPAlias"),//not used
    SPROVIDER_SELECTOR_INVALID_SPID("sProviderAction.invalidSPId"),//not used
    SPROVIDER_SELECTOR_INVALID_SPREDIRECT("sProviderAction.invalidSPRedirect"),
    SPROVIDER_SELECTOR_SPNOTALLOWED("sProviderAction.spNotAllowed"), // not used
    SPROVIDER_INVALID_REQUESTERID("sProviderAction.invalidRequesterId"),
    INTERNAL_ERROR("internalError"),
    COLLEAGUE_REQ_ATTR_NULL("colleagueRequest.attrNull"),
    COLLEAGUE_REQ_ATTR_LIST("colleagueRequest.attrList"),
    COLLEAGUE_REQ_ERROR_CREATE_SAML("colleagueRequest.errorCreatingSAML"),
    COLLEAGUE_REQ_INVALID_COUNTRYCODE("colleagueRequest.invalidCountryCode"),
    COLLEAGUE_REQ_INVALID_NAMEID("colleagueRequest.invalidNameID"),
    COLLEAGUE_REQ_INVALID_DEST_URL("colleagueRequest.invalidDestUrl"),
    COLLEAGUE_REQ_INVALID_REDIRECT("colleagueRequest.invalidRedirect"),//not used
    COLLEAGUE_REQ_INVALID_SAML("colleagueRequest.invalidSAML"),
    COLLEAGUE_REQ_MISSING_REQUESTER_ID("colleagueRequest.missing.requesterID"),
    SERVICE_REDIRECT_URL("serviceRedirectUrl"),
    CONNECTOR_REDIRECT_URL("connectorRedirectUrl"),
    COLLEAGUE_RESP_INVALID_SAML("colleagueResponse.invalidSAML"),
    ATT_VERIFICATION_MANDATORY("attVerification.mandatory"),
    ATTR_VALUE_VERIFICATION("attrValue.verification"),
    AUDIENCE_RESTRICTION("audience.restriction.error"),
    AU_REQUEST_ID("auRequestIdError"),
    HASH_ERROR("hash.error"),
    INVALID_ATTRIBUTE_LIST("invalidAttributeList"),
    INVALID_ATTRIBUTE_VALUE("invalidAttributeValue"),
    /**
     * Connector Interface error code 000004
     */
    REQUESTS_COLLEAGUE_REQUEST ("requests.ColleagueRequest"),
    IDP_SAML_RESPONSE("IdPSAMLResponse"),
    SERVICE_SAML_RESPONSE("serviceSAMLResponse"),

    INVALID_SESSION("invalid.session"),
    INVALID_SESSION_ID("invalid.sessionId"),
    MISSING_SESSION_ID("sessionError"),
    /**
     * Plugin config has errors
     */
    SPWARE_CONFIG_ERROR("spWare.config.error"),
    /**
     * Error for propagating the SAML XEE attack error
     */
    DOC_TYPE_NOT_ALLOWED("docTypeNotPermited"),
    /**
     * an invalid certificate used for generating the signature
     */
    INVALID_CERTIFICATE_SIGN("invalidCertificateSign.error"),
    INVALID_SIGNATURE_ALGORITHM("invalidReceivedSignAlgo.error"),
    INVALID_HASH_ALGORITHM("invalidHashAlgorithm.error"),
    INVALID_MINIMUM_SIGNATURE_HASH_LENGTH("invalidMinimumHashLength.error"),
    INVALID_PROTOCOL_BINDING("invalidProtocolBinding.error"),
    INVALID_ASSERTION_SIGNATURE("invalidSamlAssertionSignature.error"),
    INVALID_ENCRYPTION_ALGORITHM("invalidEncryptionAlgorithm.error", false),
    SAML_ENGINE_CONFIGURATION_ERROR("samlEngine.configuration.error"),
    MESSAGE_VALIDATION_ERROR("message.validation.error"),
    SAML_ENGINE_INVALID_KEYSTORE("samlengine.invalid.keystore", false),
    SAML_ENGINE_INVALID_CERTIFICATE("samlengine.invalid.certificate", false),
    SAML_ENGINE_UNTRUSTED_CERTIFICATE("samlengine.untrusted.certificate", false),
    SAML_ENGINE_INVALID_METADATA("samlengine.invalid.metadata.error", false),
    SAML_ENGINE_UNENCRYPTED_RESPONSE("samlengine.unencrypted.response"),
    SAML_ENGINE_DECRYPTING_RESPONSE("samlengine.decrypting.response"),
    EIDAS_MANDATORY_ATTRIBUTES("missing.mandatory.attribute"),
    EIDAS_REPRESENTATIVE_ATTRIBUTES("request.representative.attribute"),
    SAML_ENGINE_INVALID_METADATA_SOURCE("samlengine.invalid.metadata.source.error", false),
    SAML_ENGINE_NO_METADATA("samlengine.metadata.retrieval.error", true),
    COLLEAGUE_REQ_INVALID_LOA("colleagueRequest.invalidLoA"),
    COLLEAGUE_REQ_INCONSISTENT_SPTYPE("inconsistent.sptype"),
    COLLEAGUE_REQ_MISSING_SPTYPE("missing.sptype"),
    SERVICE_PROVIDER_INVALID_LOA("serviceProviderRequest.invalidLoA"),
    CONNECTOR_INVALID_SPTYPE("connector.invalid.sptype"),
    /**
     * LoA is not not one of http://eidas.europa.eu/LoA/low, http://eidas.europa.eu/LoA/substantial,
     * http://eidas.europa.eu/LoA/high
     */
    INVALID_LOA_VALUE("invalidLoA"),
    INVALID_RESPONSE_LOA_VALUE("idp.incorrect.loa"),
    INVALID_RESPONSE_LOA_VALUE_UNPUBLISHED("idp.incorrect.loa.unpublished"),
    MESSAGE_FORMAT_UNSUPPORTED("samlengine.message.format.unsupported"),
    INVALID_RESPONSE_COUNTRY_ISOCODE("invalid.response.country.isocode"),
    ILLEGAL_ARGUMENTS_IN_BUILDER("illegal.arguments.in.builder"),
    INVALID_LIGHT_TOKEN("invalid.light.token"),
    PROTOCOL_VERSION_UNSUPPORTED("protocol.version.unsupported"),
    /**
     * Generic Error code 003001
     */
    REMOTE_ADDR("remoteAddr"),
    /**
     * Connector Error code 200003
     */
    CONNECTOR_DOMAIN("domain.ServiceProvider"),
    /**
     * Connector Error code 200010
     */
    SAML_RESPONSE("SAMLResponse"),
    /**
     * SAML Messaging and Protocol error 203001
     */
    SAML_REQUEST("SAMLRequest"),

    // put the ; on a separate line to make merges easier
    ;

    public static final String CODE_CONSTANT = ".code";

    public static final String MESSAGE_CONSTANT = ".message";

    public static final String DOT_SEPARATOR = ".";

    private static final EnumMapper<String, EidasErrorKey> ERROR_CODE_MAPPER =
            new EnumMapper<String, EidasErrorKey>(new KeyAccessor<String, EidasErrorKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EidasErrorKey eidasError) {
                    return eidasError.errorCode();
                }
            }, Canonicalizers.trim(), values());

    private static final EnumMapper<String, EidasErrorKey> ID_MAPPER =
            new EnumMapper<String, EidasErrorKey>(new KeyAccessor<String, EidasErrorKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull EidasErrorKey eidasError) {
                    return eidasError.id;
                }
            }, Canonicalizers.trim(), values());

    @Nonnull
    public static EnumMapper<String, EidasErrorKey> errorCodeMapper() {
        return ERROR_CODE_MAPPER;
    }

    @Nonnull
    public static EnumMapper<String, EidasErrorKey> errorIdMapper() {
        return ID_MAPPER;
    }

    @Nullable
    public static EidasErrorKey fromCode(@Nullable String code) {
        if (code != null && code.endsWith(CODE_CONSTANT)) {
            return ERROR_CODE_MAPPER.fromKey(code);
        }
        return null;
    }

    @Nullable
    public static EidasErrorKey fromID(@Nullable String id) {
        if (null != id) {
            return ID_MAPPER.fromKey(id);
        }
        return null;
    }

    public static boolean isErrorCode(@Nullable String code) {
        return ERROR_CODE_MAPPER.containsKey(code);
    }

    /**
     * Represents the constant's value.
     */
    private final transient String id;

    private final transient boolean showToUser;

    /**
     * Solo Constructor.
     *
     * @param nError The Constant error value.
     */
    EidasErrorKey(String nError) {
        this(nError, true);
    }

    EidasErrorKey(String nError, boolean showToUser) {
        id = nError;
        this.showToUser = showToUser;
    }

    /**
     * Construct the errorCode Constant value.
     *
     * @return The errorCode Constant.
     */
    public String errorCode() {
        return id + CODE_CONSTANT;
    }

    /**
     * Construct the errorMessage constant value.
     *
     * @return The errorMessage constant.
     */
    public String errorMessage() {
        return id + MESSAGE_CONSTANT;
    }

    public boolean isShowToUser() {
        return showToUser;
    }

    /**
     * Return the Constant Value.
     *
     * @return The constant value.
     */
    @Override
    public String toString() {
        return id;
    }
}
