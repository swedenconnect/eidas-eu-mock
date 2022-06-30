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
package eu.eidas.auth.engine.core.validator.eidas;

import java.nio.charset.Charset;

import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.StatusCode;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.validator.ResponseSchemaValidator;
import org.opensaml.xml.util.XMLHelper;
import org.opensaml.xml.validation.ValidationException;


public class EidasResponseValidator extends ResponseSchemaValidator {

    private static final String CONSENT_ALLOWED_VALUE_1 = "urn:oasis:names:tc:SAML:2.0:consent:obtained";
    private static final String CONSENT_ALLOWED_VALUE_2 = "urn:oasis:names:tc:SAML:2.0:consent:prior";
    private static final String CONSENT_ALLOWED_VALUE_3 = "urn:oasis:names:tc:SAML:2.0:consent:curent-implicit";
    private static final String CONSENT_ALLOWED_VALUE_4 = "urn:oasis:names:tc:SAML:2.0:consent:curent-explicit";
    private static final String CONSENT_ALLOWED_VALUE_5 = "urn:oasis:names:tc:SAML:2.0:consent:unspecified";

    private static final int MAX_SIZE = 131072;

    /**
     * Constructor
     */

    public EidasResponseValidator() {

        super();
    }

    /**
     * {@inheritDoc}
     */
    public void validate(Response resp) throws ValidationException {

        if (XMLHelper.prettyPrintXML(resp.getDOM()).getBytes(Charset.forName("UTF-8")).length > MAX_SIZE) {
            throw new ValidationException("SAML Response exceeds max size.");
        }

        super.validate(resp);

        if (resp.getID() == null) {

            throw new ValidationException("ID is required");
        }

        if (resp.getInResponseTo() == null) {

            throw new ValidationException("InResponseTo is required");
        }

        if (resp.getVersion() == null) {
            throw new ValidationException("Version is required.");
        } else if (!resp.getVersion().equals(SAMLVersion.VERSION_20)) {
            throw new ValidationException("Version is invalid.");
        }

        if (resp.getIssueInstant() == null) {
            throw new ValidationException("IssueInstant is required");
        }

        if (resp.getDestination() == null) {

            throw new ValidationException("Destination is required");
        }

        // Consent is optional
        if (resp.getConsent() != null) {

            String consent = resp.getConsent();
            boolean allowedValue=CONSENT_ALLOWED_VALUE_1.equals(consent);
            allowedValue = allowedValue || CONSENT_ALLOWED_VALUE_2.equals(consent);
            allowedValue = allowedValue || CONSENT_ALLOWED_VALUE_3.equals(consent);
            allowedValue = allowedValue || CONSENT_ALLOWED_VALUE_4.equals(consent);
            allowedValue = allowedValue || CONSENT_ALLOWED_VALUE_5.equals(consent);
            if (!allowedValue) {
                throw new ValidationException("Consent is invalid.");
            }
        }


        if (resp.getIssuer() == null) {

            throw new ValidationException("Issuer is required.");
        }

        if (resp.getStatus() == null) {

            throw new ValidationException("Status is required.");
        }


        if (resp.getSignature() == null) {

            throw new ValidationException("Signature is required.");
        }


        if (resp.getStatus().getStatusCode().getValue().equals(StatusCode.SUCCESS.toString()) &&
                (resp.getAssertions() == null || resp.getAssertions().isEmpty())) {
                throw new ValidationException("Assertion is required");
        }

    }

}

