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

    /** The STOR k10_ ns. */
    STORK10_NS("urn:eu:stork:names:tc:STORK:1.0:assertion"),

    /** The STOR k10 p_ ns. */
    STORK10P_NS("urn:eu:stork:names:tc:STORK:1.0:protocol"),

    /** The STOR k10_ prefix. */
    STORK10_PREFIX("stork"),

    /** The STOR k10 p_ prefix. */
    STORK10P_PREFIX("storkp"),

    /** The STOR k10_ bas e_ uri. */
    STORK10_BASE_URI("http://www.stork.gov.eu/1.0/"),

    /** The EIDAS10 saml extension ns. */
    EIDAS10_SAML_NS("http://eidas.europa.eu/saml-extensions"),
    /** The EIDAS10_ ns. */
    EIDAS10_NS(EIDAS10_SAML_NS.getValue()),

    /**
     * The EIDAS10 p_ ns.
     * TODO: This is wrong!
     */
    @Deprecated
    EIDAS10P_NS(STORK10P_NS.getValue()),

    /** The EIDAS10_ prefix. */
    EIDAS10_PREFIX("eidas"),

    /**
     * The EIDAS10 p_ prefix.
     * TODO: This is wrong!
     */
    @Deprecated
    EIDAS10P_PREFIX("storkp"),

    /** The EIDAS10 saml extension ns.
     *
     * TODO: wrong! An attribute can be from http://eidas.europa.eu/attributes/legalperson/
     */
    @Deprecated
    EIDAS10_RESPONSESAML_NS("http://eidas.europa.eu/attributes/naturalperson"),
    /** The EIDAS10 saml extension prefix */
    EIDAS10_SAML_PREFIX("eidas"),
    /**
     * The EIDAS10_ base attribute uri. this should be in sync with the configuration of saml engine
     * @deprecated use {@link eu.eidas.auth.engine.core.eidas.spec.EidasSpec} instead.
     */
    @Deprecated
    EIDAS10_BASE_URI("eidas/attributes/"),

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
