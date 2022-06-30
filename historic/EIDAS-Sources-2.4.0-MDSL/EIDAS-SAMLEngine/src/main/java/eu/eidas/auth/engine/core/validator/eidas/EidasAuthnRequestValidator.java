/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.engine.core.validator.eidas;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.engine.exceptions.ValidationException;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.apache.commons.lang.StringUtils;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.eidas.auth.engine.core.validator.eidas.EidasValidator.validateNotNull;
import static eu.eidas.auth.engine.core.validator.eidas.EidasValidator.validateOK;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.opensaml.saml.common.xml.SAMLConstants.SAML2_POST_BINDING_URI;
import static org.opensaml.saml.common.xml.SAMLConstants.SAML2_REDIRECT_BINDING_URI;
import static org.opensaml.saml.common.xml.SAMLConstants.SAML2_SOAP11_BINDING_URI;

/**
 * eIDAS validator for {@link AuthnRequest}
 */
public class EidasAuthnRequestValidator extends AuthnRequestSchemaValidator implements EidasValidator {

    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthnRequestValidator.class);

    static final String[] CONSENT_ALLOWED_VALUES = {
            "urn:oasis:names:tc:SAML:2.0:consent:obtained",
            "urn:oasis:names:tc:SAML:2.0:consent:prior",
            "urn:oasis:names:tc:SAML:2.0:consent:current-implicit",
            "urn:oasis:names:tc:SAML:2.0:consent:current-explicit",
            "urn:oasis:names:tc:SAML:2.0:consent:unspecified",
            "urn:oasis:names:tc:SAML:2.0:consent:unavailable",
            "urn:oasis:names:tc:SAML:2.0:consent:inapplicable"
    };

    static final int MAX_SIZE = 131072;

    public EidasAuthnRequestValidator() {
        super();
    }

    /**
     * Validates a single {@link AuthnRequest} and throws a {@link ValidationException} if the validation fails.
     *
     * @param request the {@link AuthnRequest} to validate
     * @throws ValidationException when an invalid value was found
     */
    @Override
    public void validate(AuthnRequest request) throws ValidationException {

        Element node = Objects.requireNonNull(request.getDOM());
        int requestSize = SerializeSupport.prettyPrintXML(node).getBytes(UTF_8).length;
        validateOK(requestSize <= MAX_SIZE, "SAML AuthnRequest exceeds max size.");

        super.validate(request);

        validateNotNull(request.getID(), "ID is required.");
        validateNotNull(request.getVersion(), "Version is required.");
        validateOK(SAMLVersion.VERSION_20.equals(request.getVersion()), "Version is invalid.");
        validateNotNull(request.getIssueInstant(), "IssueInstant is required.");

        validateConsent(request.getConsent());

        validateNotNull(request.isForceAuthn(), "ForceAuthn is required.");
        validateOK(TRUE.equals(request.isForceAuthn()), "ForceAuthn is invalid.");
        validateNotNull(request.isPassive(), "IsPassive is required.");
        validateOK(FALSE.equals(request.isPassive()), "IsPassive is invalid.");

        validateOK(isValidProtocolBinding(request.getProtocolBinding()), "ProtocolBinding is invalid.");

        validateDestination(request);

        validateNotNull(request.getProviderName(), "ProviderName is required.");
        validateNotNull(request.getIssuer(), "Issuer is required.");
        validateNotNull(request.getExtensions(), "Extensions is required.");

        validateNameIdPolicy(request);
    }

    /**
     * Validates the NameIDPolicy; it is mandatory, and it's format should be one of {@link SamlNameIdFormat}
     * Throws a {@link EidasNodeException} when the NameIDPolicy is not valid.
     *
     * @param request an {@link AuthnRequest}
     */
    private void validateNameIdPolicy(AuthnRequest request) {
        if (request.getNameIDPolicy() != null &&
                StringUtils.isNotBlank(request.getNameIDPolicy().getFormat()) &&
                null == SamlNameIdFormat.fromString(request.getNameIDPolicy().getFormat())) {
            String message = "NameIDPolicy format has to be one of the following: " +
                    SamlNameIdFormat.mapper().unmodifiableKeyList(SamlNameIdFormat.values());
            LOG.error(message);
            throw new EidasNodeException(
                    EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage()));
        }
    }

    /**
     * Validates the consent, it is optional, but when not null, it should be one of:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:obtained</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:prior</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-implicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-explicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unspecified</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unavailable</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:inapplicable</li>
     * </ul>
     *
     * @param consent the consent
     * @throws ValidationException when the consent is invalid
     */
    private static void validateConsent(String consent) throws ValidationException {
        // Consent is optional
        if (consent != null) {

            Optional<String> consentOptional = Stream.of(CONSENT_ALLOWED_VALUES)
                    .filter(consent::equals)
                    .findAny();
            validateOK(consentOptional.isPresent(), "Consent is invalid");
        }
    }

    /**
     * Validates the destination, it is required when the protocol binding is either:
     * {@link SAMLConstants#SAML2_POST_BINDING_URI} or {@link SAMLConstants#SAML2_REDIRECT_BINDING_URI}
     *
     * @param request an {@link AuthnRequest}
     * @throws ValidationException when the destination is not valid
     */
    private void validateDestination(AuthnRequest request) throws ValidationException {
        if (request.getProtocolBinding() != null &&
                (SAML2_POST_BINDING_URI.equals(request.getProtocolBinding()) ||
                        SAML2_REDIRECT_BINDING_URI.equals(request.getProtocolBinding())) &&
                request.getDestination() == null) {
            throw new ValidationException("Destination is required.");
        }
    }

    /**
     * Returns true if the protocol binding is either null, or one of;
     * <ul>
     * <li>{@link SAMLConstants#SAML2_POST_BINDING_URI}</li>
     * <li>{@link SAMLConstants#SAML2_SOAP11_BINDING_URI}</li>
     * <li>{@link SAMLConstants#SAML2_REDIRECT_BINDING_URI}</li>
     * </ul>
     *
     * @param protocolBinding the protocol binding string
     * @return true if the protocolbinding is valid
     */
    private boolean isValidProtocolBinding(String protocolBinding) {
        return protocolBinding == null
                || SAML2_POST_BINDING_URI.equals(protocolBinding)
                || SAML2_SOAP11_BINDING_URI.equals(protocolBinding)
                || SAML2_REDIRECT_BINDING_URI.equals(protocolBinding);
    }

}
