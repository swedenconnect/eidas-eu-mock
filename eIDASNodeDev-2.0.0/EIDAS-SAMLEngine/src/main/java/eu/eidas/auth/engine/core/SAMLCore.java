/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.auth.engine.core;

/**
 * The Enumeration SAMLCore.
 *
 * @author fjquevedo
 */

public enum SAMLCore {

    /** The consent authentication request. */
    CONSENT_AUTHN_REQ("consentAuthnRequest"),

    /** The consent authentication response. */
    CONSENT_AUTHN_RES("consentAuthnResponse"),

    /** The FORC e_ auth n_ tag. */
    FORCE_AUTHN_TAG("forceAuthN"),

    /** The I s_ passiv e_ tag. */
    IS_PASSIVE_TAG("isPassive"),

    /** The FORMA t_ entity. */
    FORMAT_ENTITY("formatEntity"),

    /** The PRO t_ bindin g_ tag. */
    PROT_BINDING_TAG("protocolBinding"),

    /** The ASSER t_ con s_ tag. */
    ASSERT_CONS_TAG("assertionConsumerServiceURL"),

    /** The REQUESTE r_ tag. */
    REQUESTER_TAG("requester"),

    /** The RESPONDE r_ tag. */
    RESPONDER_TAG("responder"),

    /** The validateSignature tag. */
    VALIDATE_SIGNATURE_TAG("validateSignature"),

    /** The EIDAS10 saml extension ns. */
    EIDAS10_SAML_NS("http://eidas.europa.eu/saml-extensions"),
    /** The EIDAS10_ ns. */
    EIDAS10_NS(EIDAS10_SAML_NS.getValue()),

    /** The EIDAS10_ prefix. */
    EIDAS10_PREFIX("eidas"),

    /** The EIDAS10 saml extension ns.
     *
     * TODO: wrong! An attribute can be from http://eidas.europa.eu/attributes/legalperson/
     */
    @Deprecated
    EIDAS10_RESPONSESAML_NS("http://eidas.europa.eu/attributes/naturalperson"),
    /** The EIDAS10 saml extension prefix */
    EIDAS10_SAML_PREFIX("eidas"),


    /** The ON e_ tim e_ use. */
    ONE_TIME_USE("oneTimeUse"),
    ;

    /** The value. */
    private String value;

    /**
     * Instantiates a new sAML core.
     *
     * @param fullName the full name
     */
    SAMLCore(final String fullName) {
	this.value = fullName;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
	return value;
    }
}
