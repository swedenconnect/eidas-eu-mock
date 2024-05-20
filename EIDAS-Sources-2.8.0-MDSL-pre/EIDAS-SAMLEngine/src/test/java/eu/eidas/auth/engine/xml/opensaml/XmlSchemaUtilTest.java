/*
 * Copyright (c) 2022 by European Commission
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
package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.engine.syntax.SyntaxTestUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.security.Provider;
import java.security.Security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link XmlSchemaUtil}
 */
public class XmlSchemaUtilTest {

    /**
     * path to file with xxe content
     */
    private final static String XXE_PATH = "src/test/resources/auth/commons/xml/xxeattack.txt";

    /**
     * Xml sample of a XXE.
     */
    private final static String maliciousXMLSample = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "  <!ELEMENT foo ANY >\n" +
            "  <!ENTITY xxe SYSTEM \"" + XXE_PATH + "\" >]><foo>&xxe;</foo>";

    @BeforeClass
    public static void setupClass() {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    @AfterClass
    public static void tearDownClass() {
        Provider bouncyCastleProvider = Security.getProvider(BouncyCastleProvider.PROVIDER_NAME);
        if (bouncyCastleProvider != null) {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    /**
     * Expected exception.
     */
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     * Test method for {@link XmlSchemaUtil#newSecureValidator(Schema)}
     * to show the secured validation returned can prevent XXE from occurring
     * throwing a {@link SAXParseException} but with a different message
     * "cvc-elt.1: Cannot find the declaration of element 'foo'.".
     * that should be more appropriated:
     * "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true."
     *
     * @throws IOException                  If any IO errors occur
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     * @throws SAXException                 If any parse errors occur.
     *
     * Must fail and throw {@link SAXParseException}.
     *
     */
    @Test
    public void newSecureValidator() throws SAXException, IOException, ParserConfigurationException {
        expectedException.expect(SAXParseException.class);
        expectedException.expectMessage(matchesRegex("cvc-elt\\.1(\\.a)?: Cannot find the declaration of element 'foo'\\."));

        Schema schema = OpenSamlHelper.getSchema();
        DOMSource maliciousDomSource = createMaliciousDomSource();
        Validator validator = XmlSchemaUtil.newSecureValidator(schema);
        validator.validate(maliciousDomSource);
    }

    /**
     * Test method for {@link XmlSchemaUtil#newSecureValidator(Schema)}
     * when an unsecured validator is created by {@link Schema#newValidator()}
     * to show that with unsecured validator e.g. {@link XMLConstants#FEATURE_SECURE_PROCESSING}
     * feature set to false the XXE is possible
     *
     * @throws SAXException                 If any parse errors occur.
     * @throws EIDASSAMLEngineException     If the validator is not secured
     *
     * Must fail and throw {@link EIDASSAMLEngineException}.
     *
     */
    @Test
    public void newSecureValidatorThrowsExceptionWhenNotSecured() throws SAXException, IOException, ParserConfigurationException {
        Schema mockedSchema = mock(Schema.class);
        Validator mockedValidator = mock(Validator.class);
        when(mockedValidator.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING)).thenReturn(Boolean.FALSE);
        when(mockedSchema.newValidator()).thenReturn(mockedValidator);
        Validator validator = XmlSchemaUtil.newSecureValidator(mockedSchema);

        DOMSource maliciousDomSource = createMaliciousDomSource();
        validator.validate(maliciousDomSource);
    }

    /**
     * Test method for showing that {@link OpenSamlHelper#getSchema()}
     * is already secured and e.g. instances created with {@link Schema#newValidator()}
     * are already secured against XXE
     * throwing a {@link SAXParseException} but with a different message
     * "cvc-elt.1: Cannot find the declaration of element 'foo'.".
     * that should be more appropriated:
     * "DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true."

     *
     * @throws IOException                  If any IO errors occur
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     * @throws SAXException                 If any parse errors occur.
     *
     * Must fail and throw {@link SAXParseException}.
     *
     */
    @Test
    public void validatorFromOpenSamlHelperIsSecured() throws ParserConfigurationException, SAXException, IOException {
        expectedException.expect(SAXParseException.class);
        expectedException.expectMessage(matchesRegex("cvc-elt\\.1(\\.a)?: Cannot find the declaration of element 'foo'\\."));

        DOMSource domSrc = createMaliciousDomSource();
        Schema schema = OpenSamlHelper.getSchema();
        Validator validator = schema.newValidator();
        validator.validate(domSrc);
    }

    /**
     * Test method for {@link XmlSchemaUtil#validateSchema(Schema, Document)}
     * which should be the case in runtime
     * to show XXE cannot occur when the method is called.
     *
     * Must fail.
     */
    @Test
    public void testValidateSchemaDocumentMalicious() throws ParserConfigurationException, SAXException, IOException, EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new ByteArrayInputStream(EidasStringUtil.getBytes(maliciousXMLSample)));

        Schema schema = OpenSamlHelper.getSchema();
        XmlSchemaUtil.validateSchema(schema, document);
    }

    /**
     * Test method for
     * {@link XmlSchemaUtil#validateSchema(Schema, String)}
     * when validated String is valid
     *
     * Must succeed.
     */
    @Test
    public void testValidateSchemaString() throws EIDASSAMLEngineException {
        final byte[] samlToken = SyntaxTestUtil.createSAMLRequestToken();
        final String oSIndependetNewlineDelimeter = System.lineSeparator();
        final String samlTokenString = (EidasStringUtil.toString(samlToken)).replaceAll(oSIndependetNewlineDelimeter, " ");

        Schema schema = OpenSamlHelper.getSchema();
        XmlSchemaUtil.validateSchema(schema, samlTokenString);
    }

    /**
     * Test method for
     * {@link XmlSchemaUtil#validateSchema(Schema, String)}
     * when validated String is malicious
     *
     * Must fail.
     */
    @Test
    public void testValidateSchemaStringMalicious() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        Schema schema = OpenSamlHelper.getSchema();
        XmlSchemaUtil.validateSchema(schema, maliciousXMLSample);
    }

    /**
     * Test method for
     * {@link XmlSchemaUtil#validateSamlSchema(String)}
     * when validated String is malicious
     *
     * Must fail.
     */
    @Test
    public void testValidateSamlSchemaString() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);
        expectedException.expectMessage(EidasErrorKey.MESSAGE_VALIDATION_ERROR.errorMessage());

        XmlSchemaUtil.validateSamlSchema(maliciousXMLSample);
    }

    /**
     * Auxiliary method that creates a malicious {@link DOMSource}
     *
     * @return the instance of {@link DOMSource}
     * @throws IOException  If any IO errors occur
     * @throws SAXException If any parse errors occur.
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     */
    private DOMSource createMaliciousDomSource() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final InputSource inputSource = new InputSource(new StringReader(maliciousXMLSample));
        Document document = documentBuilder.parse(inputSource);
        Element element = document.getDocumentElement();
        return new DOMSource(element);
    }

    private Matcher<String> matchesRegex(final String regex) {
        return new TypeSafeMatcher<String>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Validated against regex: ").appendValue(regex);
            }

            @Override
            protected boolean matchesSafely(final String item) {
                return item.matches(regex);
            }
        };
    }
}