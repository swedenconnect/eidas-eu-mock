/*
 * Licensed under the EUPL, Version 1.1 or â€“ as soon they will be approved by
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

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.NameIDType;
import org.opensaml.saml2.core.RequestAbstractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DOMConfigurationParser;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;

/**
 * Saml Engine Core Properties.
 */
@ThreadSafe
public final class DefaultCoreProperties implements SamlEngineCoreProperties {

    /**
     * Builder pattern for the {@link DefaultCoreProperties} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    static final class Builder {

        /**
         * The consent authentication request.
         */
        private String consentAuthnReq;

        /**
         * The consent authentication response.
         */
        private String consentAuthnResp;

        /**
         * The id cross border share.
         */
        private String eidCrossBordShare;

        /**
         * The e id cross sect share.
         */
        private String eidCrossSectShare;

        /**
         * The e id sector share.
         */
        private String eidSectorShare;

        /**
         * The format entity.
         */
        private String formatEntity;

        /**
         * The IP validation.
         */
        private boolean ipValidation;

        /**
         * The one time use.
         */
        private boolean oneTimeUse = false;

        /**
         * The protocol binding.
         */
        private String protocolBinding;

        /**
         * The requester.
         */
        private String requester;

        /**
         * The responder.
         */
        private String responder;

        private boolean validateSignature = true;

        private Set<String> supportedMessageFormatNames = new HashSet<String>();

        /**
         * The time not on or after.
         */
        private Integer timeNotOnOrAfter;

        TypedState build() {
            validate();
            return new TypedState(this);
        }

        Builder consentAuthnReq(final String consentAuthnReq) {
            this.consentAuthnReq = consentAuthnReq;
            return this;
        }

        Builder consentAuthnResp(final String consentAuthnResp) {
            this.consentAuthnResp = consentAuthnResp;
            return this;
        }

        Builder eidCrossBordShare(final String eidCrossBordShare) {
            this.eidCrossBordShare = eidCrossBordShare;
            return this;
        }

        Builder eidCrossSectShare(final String eidCrossSectShare) {
            this.eidCrossSectShare = eidCrossSectShare;
            return this;
        }

        Builder eidSectorShare(final String eidSectorShare) {
            this.eidSectorShare = eidSectorShare;
            return this;
        }

        Builder formatEntity(final String formatEntity) {
            this.formatEntity = formatEntity;
            return this;
        }

        Builder ipValidation(final boolean ipValidation) {
            this.ipValidation = ipValidation;
            return this;
        }

        Builder oneTimeUse(final boolean oneTimeUse) {
            this.oneTimeUse = oneTimeUse;
            return this;
        }

        Builder protocolBinding(final String protocolBinding) {
            this.protocolBinding = protocolBinding;
            return this;
        }

        Builder requester(final String requester) {
            this.requester = requester;
            return this;
        }

        Builder responder(final String responder) {
            this.responder = responder;
            return this;
        }

        Builder supportedMessageFormatNames(final Set<String> supportedMessageFormatNames) {
            this.supportedMessageFormatNames = supportedMessageFormatNames;
            return this;
        }

        Builder timeNotOnOrAfter(final Integer timeNotOnOrAfter) {
            this.timeNotOnOrAfter = timeNotOnOrAfter;
            return this;
        }

        public Builder validateSignature(final boolean validateSignature) {
            this.validateSignature = validateSignature;
            return this;
        }

        private void validate() throws IllegalArgumentException {
            // validation logic
        }
    }

    @Immutable
    @ThreadSafe
    static final class TypedState {

        /**
         * The consent authentication request.
         */
        private final String consentAuthnReq;

        /**
         * The consent authentication response.
         */
        private final String consentAuthnResp;

        /**
         * The id cross border share.
         */
        private final String eidCrossBordShare;

        /**
         * The e id cross sect share.
         */
        private final String eidCrossSectShare;

        /**
         * The e id sector share.
         */
        private final String eidSectorShare;

        /**
         * The format entity.
         */
        private final String formatEntity;

        /**
         * The IP validation.
         */
        private final boolean ipValidation;

        /**
         * The one time use.
         */
        private final boolean oneTimeUse;

        /**
         * The protocol binding.
         */
        private final String protocolBinding;

        /**
         * The requester.
         */
        private final String requester;

        /**
         * The responder.
         */
        private final String responder;

        /**
         * The time not on or after.
         */
        private final Integer timeNotOnOrAfter;

        private final ImmutableSet<String> supportedMessageFormatNames;

        private final boolean validateSignature;

        private TypedState(@Nonnull Builder builder) {
            consentAuthnReq = builder.consentAuthnReq;
            consentAuthnResp = builder.consentAuthnResp;
            eidCrossBordShare = builder.eidCrossBordShare;
            eidCrossSectShare = builder.eidCrossSectShare;
            eidSectorShare = builder.eidSectorShare;
            formatEntity = builder.formatEntity;
            ipValidation = builder.ipValidation;
            oneTimeUse = builder.oneTimeUse;
            protocolBinding = builder.protocolBinding;
            requester = builder.requester;
            responder = builder.responder;
            timeNotOnOrAfter = builder.timeNotOnOrAfter;
            supportedMessageFormatNames = ImmutableSet.copyOf(builder.supportedMessageFormatNames);
            validateSignature = builder.validateSignature;
        }
    }

    /**
     * The Constant LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCoreProperties.class.getName());

    private static final String[] AVAILABLE_FORMATS =
            {SAMLExtensionFormat.EIDAS_FORMAT_NAME, SAMLExtensionFormat.STORK1_FORMAT_NAME};

    /**
     * The SAML core properties.
     */
    @Nonnull
    private final ImmutableMap<String, String> samlCoreProp;

    @Nonnull
    private final TypedState state;

    public DefaultCoreProperties(@Nonnull Map<String, String> instance) {
        Preconditions.checkNotNull(instance, "instance");
        samlCoreProp = ImmutableMap.copyOf(instance);
        state = loadConfiguration();
    }

    /**
     * Instantiates a new sAML core.
     *
     * @param instance the instance
     */
    public DefaultCoreProperties(@Nonnull Properties instance) {
        Preconditions.checkNotNull(instance, "instance");
        samlCoreProp = Maps.fromProperties(instance);
        state = loadConfiguration();
    }

    /**
     * Gets the consent.
     *
     * @return the consent
     */
    @Override
    public String getConsentAuthnRequest() {
        return state.consentAuthnReq;
    }

    /**
     * Gets the consent authentication response.
     *
     * @return the consent authentication response.
     */
    @Override
    public String getConsentAuthnResp() {
        return state.consentAuthnResp;
    }

    /**
     * Gets the consent authentication response.
     *
     * @return the consent authentication response
     */
    @Override
    public String getConsentAuthnResponse() {
        return state.consentAuthnResp;
    }

    /**
     * Gets the format entity.
     *
     * @return the format entity
     */
    @Override
    public String getFormatEntity() {
        return state.formatEntity;
    }

    /**
     * Gets the property.
     *
     * @param key the key
     * @return the property
     */
    @Override
    public String getProperty(final String key) {
        return samlCoreProp.get(key);
    }

    /**
     * Gets the protocol binding.
     *
     * @return the protocol binding
     */
    @Override
    public String getProtocolBinding() {
        return state.protocolBinding;
    }

    /**
     * Gets the requester.
     *
     * @return the requester
     */
    @Override
    public String getRequester() {
        return state.requester;
    }

    /**
     * Gets the responder.
     *
     * @return the responder
     */
    @Override
    public String getResponder() {
        return state.responder;
    }

    @Override
    public Set<String> getSupportedMessageFormatNames() {
        return state.supportedMessageFormatNames;
    }

    /**
     * Gets the time not on or after.
     *
     * @return the time not on or after
     */
    @Override
    public Integer getTimeNotOnOrAfter() {
        return state.timeNotOnOrAfter;
    }

    /**
     * Checks if is IP validation.
     *
     * @return true, if is IP validation
     */
    @Override
    public boolean isIpValidation() {
        return state.ipValidation;
    }

    /**
     * Checks if is one time use.
     *
     * @return true, if is one time use
     */
    @Override
    public boolean isOneTimeUse() {
        return state.oneTimeUse;
    }

    /**
     * Checks if is e id cross border share.
     *
     * @return true, if is e id cross border share
     */
    @Override
    public String isEidCrossBordShare() {
        return state.eidCrossBordShare;
    }

    /**
     * Checks if is e id cross border share.
     *
     * @return true, if is e id cross border share
     */
    @Override
    public String isEidCrossBorderShare() {
        return state.eidCrossBordShare;
    }

    /**
     * Checks if is e id cross sect share.
     *
     * @return true, if is e id cross sect share
     */
    @Override
    public String isEidCrossSectShare() {
        return state.eidCrossSectShare;
    }

    /**
     * Checks if is e id cross sector share.
     *
     * @return true, if is e id cross sector share
     */
    @Override
    public String isEidCrossSectorShare() {
        return state.eidCrossSectShare;
    }

    /**
     * Checks if is e id sector share.
     *
     * @return true, if is e id sector share
     */
    @Override
    public String isEidSectorShare() {
        return state.eidSectorShare;
    }

    @Override
    public boolean isValidateSignature() {
        return state.validateSignature;
    }

    /**
     * Method that loads the configuration file for the SAML Engine.
     *
     * @param instance the instance of the Engine properties.
     */
    @Nonnull
    private TypedState loadConfiguration() {
        try {
            LOGGER.trace("SAMLCore: Loading SAMLEngine properties.");

            Builder builder = new Builder();

            String parameter = samlCoreProp.get(SAMLCore.FORMAT_ENTITY.getValue());

            if ("entity".equalsIgnoreCase(parameter)) {
                builder.formatEntity(NameIDType.ENTITY);
            }

            builder.eidSectorShare(samlCoreProp.get("eIDSectorShare"));
            builder.eidCrossSectShare(samlCoreProp.get("eIDCrossSectorShare"));
            builder.eidCrossBordShare(samlCoreProp.get("eIDCrossBorderShare"));

            String ipAddrValidation = samlCoreProp.get("ipAddrValidation");
            if (StringUtils.isNotBlank(ipAddrValidation)) {
                builder.ipValidation(Boolean.valueOf(ipAddrValidation).booleanValue());
            }

            String oneTimeUseProp = samlCoreProp.get(SAMLCore.ONE_TIME_USE.getValue());

            if (StringUtils.isNotBlank(oneTimeUseProp)) {
                builder.oneTimeUse(Boolean.valueOf(oneTimeUseProp).booleanValue());
            }

            // Protocol Binding
            builder.protocolBinding(loadProtocolBiding());

            // Consent Authentication Request
            String consentAuthnReq = samlCoreProp.get(SAMLCore.CONSENT_AUTHN_REQ.getValue());
            if ("unspecified".equalsIgnoreCase(consentAuthnReq)) {
                builder.consentAuthnReq(RequestAbstractType.UNSPECIFIED_CONSENT);
            } else {
                builder.consentAuthnReq(consentAuthnReq);
            }

            builder.consentAuthnResp(loadConsentAuthResp());

            Integer timeNotOnOrAfter = Integer.valueOf(samlCoreProp.get("timeNotOnOrAfter"));
            if (timeNotOnOrAfter.intValue() < 0) {
                LOGGER.error("{} - timeNotOnOrAfter cannot be negative.",
                             DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE);
                throw new SamlEngineConfigurationException(
                        DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - timeNotOnOrAfter cannot be negative.");
            }
            builder.timeNotOnOrAfter(timeNotOnOrAfter);

            builder.requester(samlCoreProp.get(SAMLCore.REQUESTER_TAG.getValue()));
            builder.responder(samlCoreProp.get(SAMLCore.RESPONDER_TAG.getValue()));

            String validateSignature = samlCoreProp.get(SAMLCore.VALIDATE_SIGNATURE_TAG.getValue());
            if (StringUtils.isNotBlank(validateSignature)) {
                builder.validateSignature(Boolean.valueOf(validateSignature).booleanValue());
            }

            builder.supportedMessageFormatNames(loadSupportedFormats());

            return builder.build();
        } catch (SamlEngineConfigurationException e) {
            LOGGER.error("SAMLCore: error loadConfiguration. ", e);
            throw new EIDASSAMLEngineRuntimeException(e);
        } catch (RuntimeException e) {
            LOGGER.error("SAMLCore: error loadConfiguration. ", e);
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }

    /**
     * Load consent authentication response.
     */
    private String loadConsentAuthResp() throws SamlEngineConfigurationException {
        // Consent Authentication Response
        String consentAuthnResp = samlCoreProp.get(SAMLCore.CONSENT_AUTHN_RES.getValue());

        if ("obtained".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = RequestAbstractType.OBTAINED_CONSENT;
        } else if ("prior".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = RequestAbstractType.PRIOR_CONSENT;
        } else if ("curent-implicit".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = "urn:oasis:names:tc:SAML:2.0:consent:current-implicit";
        } else if ("curent-explicit".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = "urn:oasis:names:tc:SAML:2.0:consent:current-explicit";
        } else if ("unspecified".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = RequestAbstractType.UNSPECIFIED_CONSENT;
        } else {
            LOGGER.info("ERROR : " + DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - "
                                + SAMLCore.CONSENT_AUTHN_RES.getValue() + " is not supported (" + consentAuthnResp
                                + ").");
            throw new SamlEngineConfigurationException(
                    DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.CONSENT_AUTHN_RES.getValue()
                            + " is not supported (" + consentAuthnResp + ").");
        }
        return consentAuthnResp;
    }

    /**
     * Load protocol biding.
     *
     * @throws SamlEngineConfigurationException the SAML engine exception
     *
     * // TODO check this, as it means that the SAML Engine can only be used with HTTP-POST
     */
    @Nonnull
    private String loadProtocolBiding() throws SamlEngineConfigurationException {
        // Protocol Binding
        String protocolBinding = samlCoreProp.get(SAMLCore.PROT_BINDING_TAG.getValue());

        if (StringUtils.isBlank(protocolBinding)) {
            LOGGER.info(
                    "ERROR : " + DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                            + " is mandatory.");
            throw new SamlEngineConfigurationException(
                    DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                            + " is mandatory.");
        } else if ("HTTP-POST".equalsIgnoreCase(protocolBinding)) {
            return SAMLConstants.SAML2_POST_BINDING_URI;
        } else {
            LOGGER.info(
                    "ERROR : " + DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                            + " is not supported (" + protocolBinding + ").");
            throw new SamlEngineConfigurationException(
                    DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                            + " is not supported (" + protocolBinding + ").");
        }
    }

    private Set<String> loadSupportedFormats() {
        Set<String> supportedFormats = new HashSet<String>();
        for (String format : AVAILABLE_FORMATS) {
            String formatKeyName = "messageFormat." + format;
            if (samlCoreProp.containsKey(formatKeyName) && !Boolean.parseBoolean(samlCoreProp.get(formatKeyName))) {
                continue;
            }
            supportedFormats.add(format);
        }
        return supportedFormats;
    }
}
