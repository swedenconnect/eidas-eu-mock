/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
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

    /**
     * Represents the 'authenticationFailed' constant error identifier.
     */
    AUTHENTICATION_FAILED_ERROR("authenticationFailed"),
    /**
     * Represents the 'spCountrySelector.errorCreatingSAML' constant error identifier.
     */
    SP_COUNTRY_SELECTOR_ERROR_CREATE_SAML("spCountrySelector.errorCreatingSAML"),
    /**
     * Represents the 'spCountrySelector.destNull' constant error identifier.
     */
    SP_COUNTRY_SELECTOR_DESTNULL("spCountrySelector.destNull"),
    /**
     * Represents the 'spCountrySelector.invalidAttr' constant error identifier.
     */
    SP_COUNTRY_SELECTOR_INVALID_ATTR("spCountrySelector.invalidAttr"),
    /**
     * Represents the 'spCountrySelector.invalidProviderName' constant error identifier.
     */
    //SP_COUNTRY_SELECTOR_INVALID_PROVIDER_NAME("spCountrySelector.invalidProviderName"),
    /**
     * Represents the 'spCountrySelector.invalidQaaSPid' constant error identifier.
     */
    //SP_COUNTRY_SELECTOR_INVALID_QAASPID("spCountrySelector.invalidQaaSPid"),
    /**
     * Represents the 'spCountrySelector.invalidSpId' constant error identifier.
     */
    //SP_COUNTRY_SELECTOR_INVALID_SPID("spCountrySelector.invalidSpId"),
    /**
     * Represents the 'spCountrySelector.invalidSPQAA' constant error identifier.
     */
    //SP_COUNTRY_SELECTOR_INVALID_SPQAA("spCountrySelector.invalidSPQAA"),
    /**
     * Represents the 'spCountrySelector.invalidSpURL' constant error identifier.
     */
    //SP_COUNTRY_SELECTOR_INVALID_SPURL("spCountrySelector.invalidSpURL"),

    /**
     * Represents the 'spCountrySelector.invalidCountry' constant error identifier.
     */
    SP_COUNTRY_SELECTOR_INVALID("spCountrySelector.invalidCountry"),

    /**
     * Represents the 'spCountrySelector.spNotAllowed' constant error identifier.
     */
    SP_COUNTRY_SELECTOR_SPNOTALLOWED("spCountrySelector.spNotAllowed"),

    /**
     * Represents the 'sProviderAction.errorCreatingSAML' constant error identifier.
     */
    SPROVIDER_SELECTOR_ERROR_CREATE_SAML("sProviderAction.errorCreatingSAML"),
    /**
     * Represents the 'sProviderAction.attr' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_ATTR("sProviderAction.invalidAttr"),
    /**
     * Represents the 'sProviderAction.country' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_COUNTRY("sProviderAction.invalidCountry"),
    /**
     * Represents the 'sProviderAction.relayState' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_RELAY_STATE("sProviderAction.invalidRelayState"),
    /**
     * Represents the 'sProviderAction.saml' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SAML("sProviderAction.invalidSaml"),
    /**
     * Represents the 'sProviderAction.spAlias' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SPALIAS("sProviderAction.invalidSPAlias"),
    /**
     * Represents the 'sProviderAction.spDomain' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SPDOMAIN("sProviderAction.invalidSPDomain"),
    /**
     * Represents the 'sProviderAction.spId' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SPID("sProviderAction.invalidSPId"),
    /**
     * Represents the 'sProviderAction.spQAAId' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SPQAAID("sProviderAction.invalidSPQAAId"),
    /**
     * Represents the 'sProviderAction.spRedirect' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SPREDIRECT("sProviderAction.invalidSPRedirect"),
    /**
     * Represents the 'sProviderAction.invalidSPProviderName' constant error identifier.
     */
    SPROVIDER_SELECTOR_INVALID_SP_PROVIDERNAME("sProviderAction.invalidSPProviderName"),
    /**
     * Represents the 'sProviderAction.spNotAllowed' constant error identifier.
     */
    SPROVIDER_SELECTOR_SPNOTALLOWED("sProviderAction.spNotAllowed"), // not used

    /**
     * Represents the 'internalError' constant error identifier.
     */
    INTERNAL_ERROR("internalError"),

    /**
     * Represents the 'colleagueRequest.attrNull' constant error identifier.
     */
    COLLEAGUE_REQ_ATTR_NULL("colleagueRequest.attrNull"),
    /**
     * Represents the 'colleagueRequest.errorCreatingSAML' constant error identifier.
     */
    COLLEAGUE_REQ_ERROR_CREATE_SAML("colleagueRequest.errorCreatingSAML"),
    /**
     * Represents the 'colleagueRequest.invalidCountryCode' constant error identifier.
     */
    COLLEAGUE_REQ_INVALID_COUNTRYCODE("colleagueRequest.invalidCountryCode"),
    /**
     * Represents the 'colleagueRequest.invalidDestUrl' constant error identifier.
     */
    COLLEAGUE_REQ_INVALID_DEST_URL("colleagueRequest.invalidDestUrl"),
    /**
     * Represents the 'colleagueRequest.invalidQaa' constant error identifier.
     */
    COLLEAGUE_REQ_INVALID_QAA("colleagueRequest.invalidQaa"),
    /**
     * Represents the 'colleagueRequest.invalidRedirect' constant error identifier.
     */
    COLLEAGUE_REQ_INVALID_REDIRECT("colleagueRequest.invalidRedirect"),
    /**
     * Represents the 'colleagueRequest.invalidSAML' constant error identifier.
     */
    COLLEAGUE_REQ_INVALID_SAML("colleagueRequest.invalidSAML"),

    /**
     * Represents the 'serviceRedirectUrl' constant error identifier.
     */
    SERVICE_REDIRECT_URL("serviceRedirectUrl"),
    /**
     * Represents the 'connectorRedirectUrl' constant error identifier.
     */
    CONNECTOR_REDIRECT_URL("connectorRedirectUrl"),
    /**
     * Represents the 'sProviderAction.invCountry' constant error identifier.
     */
    SP_ACTION_INV_COUNTRY("sProviderAction.invCountry"),

    /**
     * Represents the 'providernameAlias.invalid' constant error identifier.
     */
    PROVIDER_ALIAS_INVALID("providernameAlias.invalid"),

    /**
     * Represents the 'service.attrNull' constant error identifier.
     */
    SERVICE_ATTR_NULL("service.attrNull"),

    /**
     * Represents the 'colleagueResponse.invalidSAML' constant error identifier.
     */
    COLLEAGUE_RESP_INVALID_SAML("colleagueResponse.invalidSAML"),

    /**
     * Represents the 'citizenNoConsent.mandatory' constant error identifier.
     */
    CITIZEN_NO_CONSENT_MANDATORY("citizenNoConsent.mandatory"),
    /**
     * Represents the 'citizenResponse.mandatory' constant error identifier.
     */
    CITIZEN_RESPONSE_MANDATORY("citizenResponse.mandatory"),
    /**
     * Represents the 'attVerification.mandatory' constant error identifier.
     */
    ATT_VERIFICATION_MANDATORY("attVerification.mandatory"),
    /**
     * Represents the 'attrValue.verification' constant error identifier.
     */
    ATTR_VALUE_VERIFICATION("attrValue.verification"),

    /**
     * Represents the 'audienceRestrictionError' constant error identifier.
     */
    AUDIENCE_RESTRICTION("audienceRestrictionError"),
    /**
     * Represents the 'auRequestIdError' constant error identifier.
     */
    AU_REQUEST_ID("auRequestIdError"),
    /**
     * Represents the 'domain' constant error identifier.
     */
    DOMAIN("domain"),
    /**
     * Represents the 'hash.error' constant error identifier.
     */
    HASH_ERROR("hash.error"),
    /**
     * Represents the 'invalidAttributeList' constant error identifier.
     */
    INVALID_ATTRIBUTE_LIST("invalidAttributeList"),
    /**
     * Represents the 'invalidAttributeValue' constant error identifier.
     */
    INVALID_ATTRIBUTE_VALUE("invalidAttributeValue"),
    /**
     * Represents the 'qaaLevel' constant error identifier.
     */
    QAALEVEL("qaaLevel"),
    /**
     * Represents the 'requests' constant error identifier.
     */
    REQUESTS("requests"),
    /**
     * Represents the 'SPSAMLRequest' constant error identifier.
     */
    SP_SAML_REQUEST("SPSAMLRequest"),
    /**
     * Represents the 'connectorSAMLRequest' constant error identifier.
     */
    CONNECTOR_SAML_REQUEST("connectorSAMLRequest"),
    /**
     * Represents the 'IdPSAMLResponse' constant error identifier.
     */
    IDP_SAML_RESPONSE("IdPSAMLResponse"),
    /**
     * Represents the 'serviceSAMLResponse' constant error identifier.
     */
    SERVICE_SAML_RESPONSE("serviceSAMLResponse"),
    /**
     * Represents the 'connectorSAMLResponse' constant error identifier.
     */
    //CONNECTOR_SAML_RESPONSE("connectorSAMLResponse"),
    /**
     * Represents the 'session' constant error identifier.
     */
    SESSION("session"),
    /**
     * Represents the 'invalid.session' constant error identifier.
     */
    INVALID_SESSION("invalid.session"),
    /**
     * Represents the 'invalid.sessionId' constant error identifier.
     */
    INVALID_SESSION_ID("invalid.sessionId"),
    /**
     * Represents the 'sessionError' constant error identifier.
     */
    MISSING_SESSION_ID("sessionError"),

    /**
     * Plugin config has errors
     */
    SPWARE_CONFIG_ERROR("spWare.config.error"),

    /**
     * Error for propagating the SAML XEE attack error
     */
    DOC_TYPE_NOT_ALLOWED("docTypeNotPermited"),
    DOC_TYPE_NOT_ALLOWED_CODE("203013"),
    //an invalid certificate used for generating the signature
    INVALID_CERTIFICATE_SIGN("invalidCertificateSign.error"),
    //an invalid certificate was used for the signature of the received signed object
    INVALID_SIGNATURE_ALGORITHM("invalidReceivedSignAlgo.error"),
    INVALID_PROTOCOL_BINDING("invalidProtocolBinding.error"),
    INVALID_ASSERTION_SIGNATURE("invalidSamlAssertionSignature.error"),
    INVALID_ENCRYPTION_ALGORITHM("invalidEncryptionAlgorithm.error", false),
    SAML_ENGINE_CONFIGURATION_ERROR("samlEngine.configuration.error"),
    MESSAGE_VALIDATION_ERROR("message.validation.error"),
    SAML_ENGINE_INVALID_KEYSTORE("samlengine.invalid.keystore", false),
    SAML_ENGINE_INVALID_CERTIFICATE("samlengine.invalid.certificate", false),
    SAML_ENGINE_UNTRUSTED_CERTIFICATE("samlengine.untrusted.certificate", false),
    //SAML_ENGINE_LOAD_PROVIDER("samlengine.load.provider", false),
    SAML_ENGINE_INVALID_METADATA("samlengine.invalid.metadata.error", false),
    CONSOLE_METADATA_ISSUER_ALREADY_EXISTS("err.metadata.already.exists"),
    CONSOLE_METADATA_FILE_ALREADY_EXISTS("err.metadata.file.already.exists"),
    CONSOLE_METADATA_FILE_PARSING("err.metadata.file.invalid.format"),
    SAML_ENGINE_UNENCRYPTED_RESPONSE("samlengine.unencrypted.response"),
    SAML_ENGINE_DECRYPTING_RESPONSE("samlengine.decrypting.response"),
    EIDAS_MANDATORY_ATTRIBUTES("missing.mandatory.attribute"),
    EIDAS_REPRESENTATIVE_ATTRIBUTES("request.representative.attribute"),
    SAML_ENGINE_INVALID_METADATA_SOURCE("samlengine.invalid.metadata.source.error", false),
    SAML_ENGINE_NO_METADATA("samlengine.metadata.retrieval.error", true),
    /**
     * Represents the 'colleagueRequest.invalidLoA' constant error identifier.
     */
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
    MESSAGE_FORMAT_UNSUPPORTED("samlengine.message.format.unsupported"),
    INVALID_RESPONSE_COUNTRY_ISOCODE("invalid.response.country.isocode"),

    ILLEGAL_ARGUMENTS_IN_BUILDER("illegal.arguments.in.builder"),

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
     * Construct the errorCode Constant value with the given code text.
     *
     * @param text the code text to append to the constant.
     * @return The errorCode Constant for the given code text.
     */
    public String errorCode(final String text) {
        return id + DOT_SEPARATOR + text + CODE_CONSTANT;
    }

    /**
     * Construct the errorMessage constant value.
     *
     * @return The errorMessage constant.
     */
    public String errorMessage() {
        return id + MESSAGE_CONSTANT;
    }

    /**
     * Construct the errorMessage Constant value with the given message text.
     *
     * @param text the message text to append to the constant.
     * @return The errorMessage Constant for the given text.
     */
    public String errorMessage(final String text) {
        return id + DOT_SEPARATOR + text + MESSAGE_CONSTANT;
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
