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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.*;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.google.common.collect.Lists.newArrayList;
import static eu.eidas.auth.engine.core.validator.eidas.EidasResponseValidator.CONSENT_ALLOWED_VALUES;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link EidasResponseValidator}
 */
public class EidasResponseValidatorTest {

    private Response response;

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
        response = mock(Response.class);
        Issuer issuer = mock(Issuer.class);
        Status status = mock(Status.class);
        Signature signature = mock(Signature.class);
        StatusCode statusCode = mock(StatusCode.class);
        List<Assertion> assertionList = newArrayList(mock(Assertion.class));

        Element element = createDummySaml(generateSaml(false)).getDocumentElement();
        when(status.getStatusCode()).thenReturn(statusCode);
        when(statusCode.getValue()).thenReturn(StatusCode.SUCCESS);
        when(response.getDOM()).thenReturn(element);
        when(response.getID()).thenReturn("ID");
        when(response.getInResponseTo()).thenReturn("InResponseTo");
        when(response.getVersion()).thenReturn(SAMLVersion.VERSION_20);
        when(response.getIssueInstant()).thenReturn(DateTime.now());
        when(response.getDestination()).thenReturn("destination");
        when(response.getIssuer()).thenReturn(issuer);
        when(response.getStatus()).thenReturn(status);
        when(response.getSignature()).thenReturn(signature);
        when(response.getAssertions()).thenReturn(assertionList);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the response is fully valid, no ValidationException is thrown
     * <p>
     * Must succeed.
     */
    @Test
    public void validateAllOK() throws ValidationException {
        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the SAML response exceeds the maximum size the validator throws a ValidationException.
     * <p>
     * Must fail and throw {@link ValidationException}
     */
    @Test
    public void validateMaxSizeEnforced() throws ValidationException, IOException, SAXException, ParserConfigurationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("SAML Response exceeds max size.");

        Element element = createDummySaml(generateSaml(true)).getDocumentElement();
        when(response.getDOM()).thenReturn(element);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the id is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateIDRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ID is required");

        when(response.getID()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the inResponseTo is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateInResponseToRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("InResponseTo is required");

        when(response.getInResponseTo()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the version is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateVersionRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Version is required");

        when(response.getVersion()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the version is not SAMLVersion.VERSION_20 the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateVersionValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Version is invalid.");

        when(response.getVersion()).thenReturn(SAMLVersion.VERSION_10);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the IssueInstant is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateIssueInstantRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("IssueInstant is required");

        when(response.getIssueInstant()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the destination is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateDestinationRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Destination is required");

        when(response.getDestination()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the consent is null, the validator should accept it.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateConsentValidWhenNull() throws ValidationException {
        when(response.getConsent()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the consent is not null and it is not one of the valid values, the validator throws a ValidationException.
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
    public void validateConsentValid() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Consent is invalid");

        when(response.getConsent()).thenReturn("invalid consent");

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
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
            when(response.getConsent()).thenReturn(allowedValue);

            EidasResponseValidator validator = new EidasResponseValidator();
            validator.validate(response);
        }
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the Issuer is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateIssuerRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Issuer is required");

        when(response.getIssuer()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the Status is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateStatusRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Status is required");

        when(response.getStatus()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the Signature is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateSignatureRequired() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Signature is required");

        when(response.getSignature()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the Assertion is null the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateAssertionRequiredWhenNull() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Assertion is required");

        when(response.getAssertions()).thenReturn(null);

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
    }

    /**
     * Test method for
     * {@link EidasResponseValidator#validate(Response)}
     * when the Assertion is empty the validator throws a ValidationException.
     * <p>
     * Must succeed.
     */
    @Test
    public void validateAssertionRequiredWhenEmpty() throws ValidationException {
        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("Assertion is required");

        when(response.getAssertions()).thenReturn(new ArrayList<>());

        EidasResponseValidator validator = new EidasResponseValidator();
        validator.validate(response);
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
            IntStream.range(0, EidasResponseValidator.MAX_SIZE).forEach((i) -> builder.append('X'));
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