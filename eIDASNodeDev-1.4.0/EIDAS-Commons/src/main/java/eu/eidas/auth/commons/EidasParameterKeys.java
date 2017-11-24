/*
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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

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
     * Represents the 'consent-type' parameter constant.
     */
    CONSENT_TYPE("consent-type"),
    /**
     * Represents the 'consent-value' parameter constant.
     */
    CONSENT_VALUE("consent-value"),
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
     * Represents the 'service.specificidpredirect.url' parameter constant.
     */
    EIDAS_SERVICE_IDP_CALLBACK_VALUE("service.specificidpredirect.url"),

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
     * Represents the 'http-x-forwarded-for' parameter constant.
     */
    HTTP_X_FORWARDED_FOR("http-x-forwarded-for"),

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
     * Represents the 'mandatory' parameter constant.
     */
    MANDATORY("mandatory"),
    /**
     * Represents the 'mandatoryAttributeMissing' parameter constant.
     */
    MANDATORY_ATTR_MISSING("mandatoryAttributeMissing"),
    /**
     * Represents the 'mandatoryConsentAttrMissing' parameter constant.
     */
    MANDATORY_CONSENT_MISSING("mandatoryConsentAttrMissing"),
    /**
     * Represents the 'optional' parameter constant.
     */
    OPTIONAL("optional"),

    /**
     * Represents the 'no-consent-type' parameter constant.
     */
    NO_CONSENT_TYPE("no-consent-type"),
    /**
     * Represents the 'no-consent-value' parameter constant.
     */
    NO_CONSENT_VALUE("no-consent-value"),

    /**
     * Represents the 'provider.name' parameter constant.
     */
    PROVIDER_NAME_VALUE("providerName"),
    /**
     * Represents the 'eidas.service.askconsent' parameter constant.
     */
    EIDAS_ASK_CONSENT("eidas.service.askconsent"),
    /**
     * Represents the 'eidas.service.askconsentvalue' parameter constant.
     */
    EIDAS_ASK_CONSENT_VALUE("eidas.service.askconsentvalue"),
    /**
     * Represents the 'eidasAuth' parameter constant.
     */
    EIDAS_AUTH_CONSENT("eidasAuth"),
    /**
     * Represents the 'validation.bypass' parameter constant.
     */
    EIDAS_BYPASS("validation.bypass"),
    /**
     * Represents the 'service.number' parameter constant.
     */
    EIDAS_NUMBER("service.number"),

    /**
     * Represents the 'qaaLevel' parameter constant.
     */
    QAALEVEL("qaaLevel"),

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
     * Represents the 'remoteAddr' parameter constant.
     */
    REMOTE_ADDR("remoteAddr"),
    /**
     * Represents the 'remoteUser' parameter constant.
     */
    REMOTE_USER("remoteUser"),

    /**
     * Represents the 'SAMLRequest' parameter constant.
     */
    SAML_REQUEST("SAMLRequest"),
    /**
     * Represents the 'SAMLResponse' parameter constant.
     */
    SAML_RESPONSE("SAMLResponse"),
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
     * Represents the 'spQaaLevel' parameter constant.
     */
    SP_QAALEVEL("spQaaLevel"),
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
     * sp.authorized.parameters Represents the '' parameter constant.
     */
    EIDAS_CONNECTOR_AUTHORIZED("sp.authorized.parameters"),

    /**
     * Represents the 'spSector' constant value.
     */
    SPSECTOR("spSector"),
    /**
     * Represents the 'spApplication' constant value.
     */
    SPAPPLICATION("spApplication"),
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
     * whether to sign the assertion in Idp
     */
    RESPONSE_SIGN_ASSERTION("response.sign.assertions"),

    /**
     * name of the session parameter holding the url of an error interceptor (used in Validation Node)
     */
    ERROR_INTERCEPTOR_URL("error.interceptor.url"),

    /**
     * set to true when the http session is created on the eidas connector
     */
    EIDAS_CONNECTOR_SESSION("eidas.connector.session"),

    EIDAS_CONNECTOR_COUNTRY("EIDASConnectorCountry"),

    METADATA_TIMEOUT("metadata.request.timeout"),
    EIDAS_CONNECTOR_ACTIVE("active.module.connector"),
    EIDAS_SERVICE_ACTIVE("active.module.service"),
    EIDAS_CONNECTOR_TRUSTED_SP("trusted.sp.domains"),
    HASH_DIGEST_CLASS("hashDigest.className"),
    SP_METADATA_URL("spmetadataurl"),
    EIDAS_SERVICE_LOA("saml.loa"),
    EIDAS_NAMEID_FORMAT("eidas.nameid"),
    /**
     * whether to sign the assertion in Idp
     */
    RESPONSE_ENCRYPT_ASSERTION("response.encryption.mandatory"),
    /**
     * the current http method
     */
    HTTP_METHOD("request.http.method"),
    /**
     * request format name(eidas, stork1)
     */
    REQUEST_FORMAT("request.format"),

    CITIZEN_COUNTRY_CODE("citizen.countryCode"),

    CITIZEN_IP_ADDRESS("citizen.ipAddress"),

    SERVICE_PROVIDER_NAME("serviceProvider.name"),

    SERVICE_PROVIDER_TYPE("serviceProvider.type"),

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
