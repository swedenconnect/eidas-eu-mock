/*
 * Copyright (c) 2021 by European Commission
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

package eu.eidas.auth.engine.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfigurationException;
import eu.eidas.auth.engine.configuration.dom.DOMConfigurationParser;
import eu.eidas.auth.engine.core.eidas.spec.EidasSAMLFormat;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.saml.saml2.core.StatusResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.lang.Boolean.parseBoolean;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

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

        private Set<String> supportedMessageFormatNames = new HashSet<>();

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

        private void validate() {
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
            {(new EidasSAMLFormat()).getName()};

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
            if (isNotBlank(ipAddrValidation)) {
                builder.ipValidation(parseBoolean(ipAddrValidation));
            }

            String oneTimeUseProp = samlCoreProp.get(SAMLCore.ONE_TIME_USE.getValue());

            if (isNotBlank(oneTimeUseProp)) {
                builder.oneTimeUse(parseBoolean(oneTimeUseProp));
            }

            // Protocol Binding
            builder.protocolBinding(loadProtocolBinding());
            builder.consentAuthnReq(loadConsentAuthReq());
            builder.consentAuthnResp(loadConsentAuthResp());

            Integer timeNotOnOrAfter = Integer.valueOf(samlCoreProp.get("timeNotOnOrAfter"));
            if (timeNotOnOrAfter < 0) {
                LOGGER.error("{} - timeNotOnOrAfter cannot be negative.",
                        DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE);
                throw new ProtocolEngineConfigurationException(
                        DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - timeNotOnOrAfter cannot be negative.");
            }
            builder.timeNotOnOrAfter(timeNotOnOrAfter);

            builder.requester(samlCoreProp.get(SAMLCore.REQUESTER_TAG.getValue()));
            builder.responder(samlCoreProp.get(SAMLCore.RESPONDER_TAG.getValue()));

            String validateSignature = samlCoreProp.get(SAMLCore.VALIDATE_SIGNATURE_TAG.getValue());
            if (isNotBlank(validateSignature)) {
                builder.validateSignature(parseBoolean(validateSignature));
            }

            builder.supportedMessageFormatNames(loadSupportedFormats());

            return builder.build();
        } catch (ProtocolEngineConfigurationException | NumberFormatException e) {
            LOGGER.error("SAMLCore: error loadConfiguration. ", e);
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }

    /**
     * Load consent authentication request.
     *
     * @return the loaded consent authentication request
     */
    private String loadConsentAuthReq() {
        String consentAuthnReq = samlCoreProp.get(SAMLCore.CONSENT_AUTHN_REQ.getValue());

        if ("obtained".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.OBTAINED_CONSENT;
        } else if ("prior".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.PRIOR_CONSENT;
        } else if ("current-implicit".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.IMPLICIT_CONSENT;
        } else if ("current-explicit".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.EXPLICIT_CONSENT;
        } else if ("unspecified".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.UNSPECIFIED_CONSENT;
        } else if ("unavailable".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.UNAVAILABLE_CONSENT;
        } else if ("inapplicable".equalsIgnoreCase(consentAuthnReq)) {
            consentAuthnReq = RequestAbstractType.INAPPLICABLE_CONSENT;
        } else if (isBlank(consentAuthnReq)) {
            consentAuthnReq = null;
        }
        return consentAuthnReq;
    }

    /**
     * Load consent authentication response.
     *
     * @return the loaded consent authentication response
     * @throws ProtocolEngineConfigurationException when no valid consent authentication response could be mapped
     */
    private String loadConsentAuthResp() throws ProtocolEngineConfigurationException {
        // Consent Authentication Response
        String consentAuthnResp = samlCoreProp.get(SAMLCore.CONSENT_AUTHN_RES.getValue());

        if ("obtained".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.OBTAINED_CONSENT;
        } else if ("prior".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.PRIOR_CONSENT;
        } else if ("current-implicit".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.IMPLICIT_CONSENT;
        } else if ("current-explicit".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.EXPLICIT_CONSENT;
        } else if ("unspecified".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.UNSPECIFIED_CONSENT;
        } else if ("unavailable".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.UNAVAILABLE_CONSENT;
        } else if ("inapplicable".equalsIgnoreCase(consentAuthnResp)) {
            consentAuthnResp = StatusResponseType.INAPPLICABLE_CONSENT;
        } else if (isBlank(consentAuthnResp)) {
            consentAuthnResp = null;
        } else {
            String message = DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - "
                    + SAMLCore.CONSENT_AUTHN_RES.getValue() + " is not supported (" + consentAuthnResp
                    + ").";
            LOGGER.info("ERROR : {}", message);
            throw new ProtocolEngineConfigurationException(message);
        }
        return consentAuthnResp;
    }

    /**
     * Load protocol binding.
     *
     * @throws ProtocolEngineConfigurationException the SAML engine exception
     */
    // TODO check this, as it means that the SAML Engine can only be used with HTTP-POST
    @Nonnull
    private String loadProtocolBinding() throws ProtocolEngineConfigurationException {
        // Protocol Binding
        String protocolBinding = samlCoreProp.get(SAMLCore.PROT_BINDING_TAG.getValue());

        if (isBlank(protocolBinding)) {
            String message = DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                    + " is mandatory.";
            LOGGER.info("ERROR : {}", message);
            throw new ProtocolEngineConfigurationException(message);
        } else if ("HTTP-POST".equalsIgnoreCase(protocolBinding)) {
            return SAMLConstants.SAML2_POST_BINDING_URI;
        } else {
            String message = DOMConfigurationParser.DEFAULT_CONFIGURATION_FILE + " - " + SAMLCore.PROT_BINDING_TAG
                    + " is not supported (" + protocolBinding + ").";
            LOGGER.info("ERROR : {}", message);
            throw new ProtocolEngineConfigurationException(message);
        }
    }

    private Set<String> loadSupportedFormats() {
        Set<String> supportedFormats = new HashSet<>();
        for (String format : AVAILABLE_FORMATS) {
            String formatKeyName = "messageFormat." + format;
            if (samlCoreProp.containsKey(formatKeyName) && !parseBoolean(samlCoreProp.get(formatKeyName))) {
                continue;
            }
            supportedFormats.add(format);
        }
        return supportedFormats;
    }
}
