/*
 * Copyright (c) 2017 by European Commission
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

import java.nio.charset.Charset;

/**
 * The Class ExtensionsSchemaValidator for eIDAS request format.
 */
public class EidasAuthnRequestValidator extends AuthnRequestSchemaValidator {
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasAuthnRequestValidator.class);
    private static final int MAX_SIZE = 131072;
    private static final String ALLOWED_CONSENT = "urn:oasis:names:tc:SAML:2.0:consent:unspecified";
    private static final String ALLOWED_PROTOCOL_BINDING_HTTP_POST = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST";
    private static final String ALLOWED_PROTOCOL_BINDING_HTTP_REDIRECT = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect";
    private static final String ALLOWED_PROTOCOL_BINDING_SOAP = "urn:oasis:names:tc:SAML:2.0:bindings:SOAP";


    /**
     * Constructor
     */
    public EidasAuthnRequestValidator() {

        super();
    }

    @Override
    public void validate(AuthnRequest request) throws ValidationException {

        if (SerializeSupport.prettyPrintXML(request.getDOM()).getBytes(Charset.forName("UTF-8")).length > MAX_SIZE) {
            throw new ValidationException("SAML AuthnRequest exceeds max size.");
        }

        super.validate(request);

        if (request.getID() == null) {

            throw new ValidationException("ID is required.");
        }

        if (request.getVersion() == null) {

            throw new ValidationException("Version is required.");
        } else {

            if (!request.getVersion().equals(SAMLVersion.VERSION_20)) {

                throw new ValidationException("Version is invalid.");
            }
        }

        if (request.getIssueInstant() == null) {

            throw new ValidationException("IssueInstant is required.");
        }

        if (request.getConsent() != null && !request.getConsent().equals(ALLOWED_CONSENT)) {
                throw new ValidationException("Consent is invalid.");
        }

        if (request.isForceAuthn() == null) {

            throw new ValidationException("ForceAuthn is required.");
        } else if (!request.isForceAuthn()) {

            throw new ValidationException("ForceAuthn is invalid.");
        }

        if (request.isPassive() == null) {

            throw new ValidationException("IsPassive is required.");
        } else if (request.isPassive()) {

            throw new ValidationException("IsPassive is invalid.");
        }

        if (request.getProtocolBinding() != null && !isValidProtocolBinding(request)) {
                throw new ValidationException("ProtocolBinding is invalid.");
        }
        if (request.getProtocolBinding()!=null && (request.getProtocolBinding().equals(ALLOWED_PROTOCOL_BINDING_HTTP_POST) ||
                request.getProtocolBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI)) &&
                request.getDestination() == null) {
                throw new ValidationException("Destination is required.");
        }

        //TODO move destination validation against local URI here from AISERVICESAML.processAuthenticationRequest after Metadata has been made accessible

        if (request.getProviderName() == null) {

            throw new ValidationException("ProviderName is required.");
        }
        if (request.getIssuer() == null) {

            throw new ValidationException("Issuer is required.");
        }
        if (request.getExtensions() == null) {

            throw new ValidationException("Extensions is required.");
        }
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

    private boolean isValidProtocolBinding(AuthnRequest request){
        return request.getProtocolBinding().equals(ALLOWED_PROTOCOL_BINDING_HTTP_POST)
                || request.getProtocolBinding().equals(ALLOWED_PROTOCOL_BINDING_HTTP_REDIRECT)
                || request.getProtocolBinding().equals(ALLOWED_PROTOCOL_BINDING_SOAP)
                || request.getProtocolBinding().equals(SAMLConstants.SAML2_REDIRECT_BINDING_URI);
    }
}
