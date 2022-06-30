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

import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.protocol.impl.SamlNameIdFormat;
import eu.eidas.engine.exceptions.ValidationException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.IntStream;

import static eu.eidas.auth.engine.core.validator.eidas.EidasAuthnRequestValidator.CONSENT_ALLOWED_VALUES;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opensaml.saml.common.xml.SAMLConstants.*;

/**
 * Test class for {@link EidasAuthnRequestValidator}
 */
public class EidasAuthnRequestValidatorTest {

    private AuthnRequest request;
    private NameIDPolicy nameIDPolicy;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Initializes mocks and return values to be setup before each test.
     * All setup is done to provide enough data to have a valid validation call, individual tests change return values
     * from the mocks to suit their individual needs.
     *
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException                  If any IO errors occur.
     * @throws SAXException                 If any parse errors occur.
     */
    @Before
    public void setup() throws IOException, SAXException, ParserConfigurationException {

        Issuer issuer = mock(Issuer.class);
        Status status = mock(Status.class);
        Extensions extensions = mock(Extensions.class);
        StatusCode statusCode = mock(StatusCode.class);
        request = mock(AuthnRequest.class);
        nameIDPolicy = mock(NameIDPolicy.class);

        Element element = createDummySaml(generateSaml(false)).getDocumentElement();
        when(status.getStatusCode()).thenReturn(statusCode);
        when(statusCode.getValue()).thenReturn(StatusCode.SUCCESS);
        when(request.getDOM()).thenReturn(element);
        when(request.getID()).thenReturn("ID");
        when(request.getVersion()).thenReturn(SAMLVersion.VERSION_20);
        when(request.getIssueInstant()).thenReturn(DateTime.now());
        when(request.isForceAuthn()).thenReturn(TRUE);
        when(request.isPassive()).thenReturn(FALSE);
        when(request.getProtocolBinding()).thenReturn(SAML2_POST_BINDING_URI);
        when(request.getProviderName()).thenReturn("providerName");
        when(request.getIssuer()).thenReturn(issuer);
        when(request.getExtensions()).thenReturn(extensions);
        when(request.getNameIDPolicy()).thenReturn(nameIDPolicy);
        when(request.getDestination()).thenReturn("destination");
    }

    /**
     * Test method for {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the request is fully valid, no {@link ValidationException} is thrown
     * <p>
     * Must succeed.
     */
    @Test
    public void validateAllOK() throws ValidationException {
        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the request exceeds the maximum size the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateMaxSizeEnforced() throws ValidationException, IOException, SAXException,
            ParserConfigurationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("SAML AuthnRequest exceeds max size.");

        Element element = createDummySaml(generateSaml(true)).getDocumentElement();
        when(request.getDOM()).thenReturn(element);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the id is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateIDRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ID is required");

        when(request.getID()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the version is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateVersionRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Version is required");

        when(request.getVersion()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the version is not SAMLVersion.VERSION_20 the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateVersionValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Version is invalid.");

        when(request.getVersion()).thenReturn(SAMLVersion.VERSION_10);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the IssueInstant is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateIssueInstantRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("IssueInstant is required");

        when(request.getIssueInstant()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the consent is null, the validator should accept it.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateConsentValidWhenNull() throws ValidationException {
        when(request.getConsent()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the consent is not null and it is not one of the valid values, the validator throws a {@link ValidationException}.
     * Valid values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:obtained</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:prior</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-implicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-explicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unspecified</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unavailable</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:inapplicable"</li>
     * </ul>
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateConsentValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Consent is invalid");

        when(request.getConsent()).thenReturn("invalid consent");

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the consent is not null and it is one of the valid values, the validator should accept it.
     * Valid values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:obtained</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:prior</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-implicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:current-explicit</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unspecified</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:unavailable</li>
     * <li>urn:oasis:names:tc:SAML:2.0:consent:inapplicable"</li>
     * </ul>
     * <p>
     * Must succeed.
     */
    @Test
    public void validateConsentValidWhenCorrectValue() throws ValidationException {
        for (String allowedValue : CONSENT_ALLOWED_VALUES) {
            when(request.getConsent()).thenReturn(allowedValue);

            EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
            validator.validate(request);
        }
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the ForceAuthn is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateForceAuthnRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ForceAuthn is required.");

        when(request.isForceAuthn()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the ForceAuthn is not valid (Boolean.TRUE) the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateForceAuthnValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ForceAuthn is invalid.");

        when(request.isForceAuthn()).thenReturn(FALSE);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the isPassive is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateIsPassiveRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("IsPassive is required.");

        when(request.isPassive()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the IsPassive is not valid (Boolean.FALSE) the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateIsPassiveValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("IsPassive is invalid.");

        when(request.isPassive()).thenReturn(TRUE);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the protocol binding is null, the validator should accept it.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateProtocolBindingValidWhenNull() throws ValidationException {
        when(request.getProtocolBinding()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the protocol binding is not null and it is not one of the valid values,
     * the validator throws a {@link ValidationException}.
     * Valid values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST</li>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect</li>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:SOAP</li>
     * </ul>
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateProtocolBindingValid() throws ValidationException {

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ProtocolBinding is invalid.");

        when(request.getProtocolBinding()).thenReturn("invalid protocol");

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the protocol binding is not null and it is one of the valid values, the validator should accept it.
     * Valid values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST</li>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect</li>
     * <li>urn:oasis:names:tc:SAML:2.0:bindings:SOAP</li>
     * </ul>
     * <p>
     * Must succeed.
     */
    @Test
    public void validateProtocolBindingValidWhenCorrectValue() throws ValidationException {
        String[] allowedValues = new String[]{
                SAML2_POST_BINDING_URI,
                SAML2_REDIRECT_BINDING_URI,
                SAML2_SOAP11_BINDING_URI};

        for (String allowedValue : allowedValues) {
            when(request.getProtocolBinding()).thenReturn(allowedValue);

            EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
            validator.validate(request);
        }
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the protocol value is 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST' or
     * 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect' and the destination is null the Validator throws a
     * {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateDestinationValid() throws ValidationException {

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Destination is required.");

        String[] allowedValues = new String[]{SAML2_POST_BINDING_URI, SAML2_REDIRECT_BINDING_URI};

        for (String allowedValue : allowedValues) {
            when(request.getProtocolBinding()).thenReturn(allowedValue);
            when(request.getDestination()).thenReturn(null);

            EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
            validator.validate(request);
        }
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the protocol value is other then 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST' or
     * 'urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect' and the destination is null the Validator should accept it.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateDestinationValidWhenValidBindingAndNoDestination() throws ValidationException {
        when(request.getProtocolBinding()).thenReturn(SAML2_SOAP11_BINDING_URI);
        when(request.getDestination()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the ProviderName is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateProviderNameRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ProviderName is required.");

        when(request.getProviderName()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the Issuer is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateIssuerRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Issuer is required.");

        when(request.getIssuer()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the Extensions is null the validator throws a {@link ValidationException}.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateExtensionsRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Extensions is required.");

        when(request.getExtensions()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the NameIDPolicy is null, the validator should accept it.
     * Must succeed.
     */
    @Test
    public void validateNameIDPolicyValidWhenNull() throws ValidationException {
        when(request.getNameIDPolicy()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the NameIDPolicy format is null, the validator should accept it.
     * Must succeed.
     */
    @Test
    public void validateNameIDPolicyValidWhenFormatNull() throws ValidationException {
        when(request.getNameIDPolicy()).thenReturn(nameIDPolicy);
        when(nameIDPolicy.getFormat()).thenReturn(null);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the NameIDPolicy, or it's format is not a valid value an {@link EidasNodeException} is thrown.
     * Valid NameIDPolicy format values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent/li>
     * <li>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</li>
     * <li>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</li>
     * </ul>
     * <p>
     * Must fail and throw {@link EidasNodeException}
     */
    @Test
    public void validateNameIDPolicyExceptionWhenInvalidFormat() throws ValidationException, EidasNodeException {
        expectedException.expect(EidasNodeException.class);
        expectedException.expectMessage("Security Error (203007) processing request : schema.validation.error");

        when(nameIDPolicy.getFormat()).thenReturn("invalid format");
        when(request.getNameIDPolicy()).thenReturn(nameIDPolicy);

        EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
        validator.validate(request);
    }

    /**
     * Test method for
     * {@link EidasAuthnRequestValidator#validate(AuthnRequest)}
     * when the NameIDPolicy format is a valid value the validator should accept it.
     * Valid NameIDPolicy format values are:
     * <ul>
     * <li>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent/li>
     * <li>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</li>
     * <li>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</li>
     * </ul>
     * <p>
     * Must succeed.
     */
    @Test
    public void validateNameIDPolicy() throws ValidationException {

        for (SamlNameIdFormat validSamlNameIDFormat : SamlNameIdFormat.values()) {
            when(nameIDPolicy.getFormat()).thenReturn(validSamlNameIDFormat.toString());
            when(request.getNameIDPolicy()).thenReturn(nameIDPolicy);

            EidasAuthnRequestValidator validator = new EidasAuthnRequestValidator();
            validator.validate(request);
        }
    }

    /**
     * Generates a SAML message as a String, either with or without filler data to force message size to be over
     * a threshold (at current {@link EidasAuthnRequestValidator#MAX_SIZE}), needed to test maximum size limits.
     *
     * @param addFillerData true to add filler data, false otherwise
     * @return a generated SAML message
     */
    private String generateSaml(boolean addFillerData) {
        StringBuilder builder = new StringBuilder("<samlp:Response xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" " +
                "xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\" " +
                "ID=\"_8e8dc5f69a98cc4c1ff3427e5ce34606fd672f91e6\" " +
                "Version=\"2.0\" IssueInstant=\"2014-07-17T01:01:48Z\" " +
                "Destination=\"http://sp.example.com/demo1/index.php?acs\" " +
                "InResponseTo=\"ONELOGIN_4fee3b046395c4e751011e97f8900b5273d56685\">)");
        if (addFillerData) {
            IntStream.range(0, EidasAuthnRequestValidator.MAX_SIZE).forEach((i) -> builder.append('X'));
        }
        builder.append("</samlp:Response>");
        return builder.toString();
    }

    /**
     * Create a dummy SAML message as a {@link Document} from a given string.
     *
     * @param xml the xml content as {@link String}
     * @return a {@link Document}
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created which satisfies the configuration requested.
     * @throws IOException                  If any IO errors occur.
     * @throws SAXException                 If any parse errors occur.
     */
    private Document createDummySaml(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(new ByteArrayInputStream(xml.getBytes(UTF_8)));
    }

}