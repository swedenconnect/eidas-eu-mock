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

import eu.eidas.engine.exceptions.ValidationException;
import net.shibboleth.utilities.java.support.xml.SerializeSupport;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.w3c.dom.Element;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static eu.eidas.auth.engine.core.validator.eidas.EidasValidator.validateNotNull;
import static eu.eidas.auth.engine.core.validator.eidas.EidasValidator.validateOK;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * eIDAS validator for {@link Response}.
 */
public class EidasResponseValidator extends ResponseSchemaValidator implements EidasValidator {

    static final int MAX_SIZE = 131072;

    static final String[] CONSENT_ALLOWED_VALUES = {
            "urn:oasis:names:tc:SAML:2.0:consent:obtained",
            "urn:oasis:names:tc:SAML:2.0:consent:prior",
            "urn:oasis:names:tc:SAML:2.0:consent:current-implicit",
            "urn:oasis:names:tc:SAML:2.0:consent:current-explicit",
            "urn:oasis:names:tc:SAML:2.0:consent:unspecified",
            "urn:oasis:names:tc:SAML:2.0:consent:unavailable",
            "urn:oasis:names:tc:SAML:2.0:consent:inapplicable"
    };

    public EidasResponseValidator() {
        super();
    }

    /**
     * Validates a single {@link Response} and throws a {@link ValidationException} if the validation fails.
     *
     * @param response the {@link Response} to validate
     * @throws ValidationException when an invalid value was found
     */
    @Override
    public void validate(Response response) throws ValidationException {

        Element node = Objects.requireNonNull(response.getDOM());
        int responseSize = SerializeSupport.prettyPrintXML(node).getBytes(UTF_8).length;
        validateOK(responseSize <= MAX_SIZE, "SAML Response exceeds max size.");

        super.validate(response);

        validateNotNull(response.getID(), "ID is required");
        validateNotNull(response.getInResponseTo(), "InResponseTo is required");
        validateNotNull(response.getVersion(), "Version is required.");
        validateOK(SAMLVersion.VERSION_20.equals(response.getVersion()), "Version is invalid.");
        validateNotNull(response.getIssueInstant(), "IssueInstant is required");
        validateNotNull(response.getDestination(), "Destination is required");

        validateConsent(response.getConsent());

        validateNotNull(response.getIssuer(), "Issuer is required.");
        validateNotNull(response.getStatus(), "Status is required.");
        validateNotNull(response.getSignature(), "Signature is required.");
        validateOK((!StatusCode.SUCCESS.equals(response.getStatus().getStatusCode().getValue())
                        || !(response.getAssertions() == null || response.getAssertions().isEmpty())),
                "Assertion is required");
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

}