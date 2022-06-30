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

package eu.eidas.auth.commons.xml;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Test class for {@link DocumentBuilderFactoryUtil}.
 */
public class DocumentBuilderFactoryUtilTest {

    private final static String XXE_PATH = "src/test/resources/auth/commons/xml/xxeattack.txt";

    /**
     * Expected exception.
     */
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static String maliciousXMLSample = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "  <!ELEMENT foo ANY >\n" +
            "  <!ENTITY xxe SYSTEM \"" + XXE_PATH + "\" >]><foo>&xxe;</foo>";

    /**
     * Test method to show it is not possible a XXE parsing attack using the protected DocumentBuilder
     * returned by method {@link DocumentBuilderFactoryUtil#newSecureDocumentBuilderFactory()}.
     *
     * Check {@link XmlExternalEntityTest#testParseShowXXE}
     * for an example of successful  XXE parsing attack with and unsecure {@link DocumentBuilderFactory}
     *
     * @throws ParserConfigurationException if a DocumentBuilder cannot be created.
     * @throws IOException                  If any IO errors occur
     * @throws SAXException                 If any parse errors occur.
     *
     * Must fail and throw exception.
     */
    @Test
    public void newSecureDocumentBuilderFactory() throws IOException, SAXException, ParserConfigurationException {
        exception.expect(SAXParseException.class);
        exception.expectMessage("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");

        DocumentBuilderFactory securedDocumentBuilderFactory = DocumentBuilderFactoryUtil.newSecureDocumentBuilderFactory();
        DocumentBuilder securedDocumentBuilder = securedDocumentBuilderFactory.newDocumentBuilder();
        final InputSource inputSource = new InputSource(new StringReader(maliciousXMLSample));
        securedDocumentBuilder.parse(inputSource);
    }

}