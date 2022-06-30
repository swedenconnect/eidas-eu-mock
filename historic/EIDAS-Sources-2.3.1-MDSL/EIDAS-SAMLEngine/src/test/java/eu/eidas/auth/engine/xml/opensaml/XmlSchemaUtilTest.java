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
 * limitations under the Licence
 */

package eu.eidas.auth.engine.xml.opensaml;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
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
import java.io.IOException;
import java.io.StringReader;

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

    /**
     * Expected exception.
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

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
        exception.expect(SAXParseException.class);
        exception.expectMessage("cvc-elt.1: Cannot find the declaration of element 'foo'.");

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
        exception.expect(SAXParseException.class);
        exception.expectMessage("cvc-elt.1: Cannot find the declaration of element 'foo'.");

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
     * @throws IOException                  If any IO errors occur
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     * @throws SAXException                 If any parse errors occur.
     * @throws EIDASSAMLEngineException     If a validatin error occurs
     *
     * Must fail and throw {@link SAXParseException}.
     */
    @Test
    public void validateSchema() throws ParserConfigurationException, SAXException, IOException, EIDASSAMLEngineException {
        exception.expect(SAXParseException.class);
        exception.expectMessage("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");

        Document document = DocumentBuilderFactoryUtil.parse(maliciousXMLSample);
        Schema schema = OpenSamlHelper.getSchema();
        XmlSchemaUtil.validateSchema(schema, document);
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
        DocumentBuilderFactory documentBuilderFactory = new DocumentBuilderFactoryImpl();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final InputSource inputSource = new InputSource(new StringReader(maliciousXMLSample));
        Document document = documentBuilder.parse(inputSource);
        Element element = document.getDocumentElement();
        return new DOMSource(element);
    }

}