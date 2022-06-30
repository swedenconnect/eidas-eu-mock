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
package eu.eidas.auth.commons.xml;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.*;
import org.junit.rules.ExpectedException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Test class for addressing XXE vulnerability/exploitation.
 */
public class XmlExternalEntityTest {

    private final static String XXE_PATH = "src/test/resources/auth/commons/xml/xxeattack.txt";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private final static String XML = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
            " <!DOCTYPE foo [  \n" +
            "  <!ELEMENT foo ANY >\n" +
            "  <!ENTITY xxe SYSTEM \"" + XXE_PATH + "\" >]><foo>&xxe;</foo>";


    /**
     * This test shows how XXE parsing attack is possible when using a non protected DocumentBuilder.
     */
    @Test
    public void testParseShowXXE() throws Exception {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = df.newDocumentBuilder();
        final InputSource is = new InputSource(new StringReader(XML));
        Document newDocument = builder.parse(is);

        final byte[] marshall = DocumentBuilderFactoryUtil.marshall(newDocument, true);
        final String xmlOut = new String(marshall);
        assertThat(xmlOut, containsString("Important info"));
    }

    /**
     * This test shows that XXE parsing attack is blocked when using the secured parse method
     * in DocumentBuilderFactoryUtil
     */
    @Test
    public void testParsePreventXXE() throws Exception {
        exception.expect(SAXParseException.class);
        exception.expectMessage("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.");
        DocumentBuilderFactoryUtil.parse(XML);
    }

    /**
     * This test shows how XXE transform XSLT attack is possible when using a non protected Transformer
     */
    @Test
    public void testTransformShowXXE() throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter buff = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new StreamSource(new StringReader(XML)), new StreamResult(buff));
        assertThat(buff.toString(), containsString("Important info"));
    }

    /**
     * This test shows how XXE transform XSLT attack is blocked when using a secured TransformerFactory from
     * DocumentBuilderFactoryUtil.
     */
    @Test
    public void testTransformPreventXXE() throws Exception {
        exception.expect(TransformerException.class);
        exception.expectMessage("access is not allowed due to restriction set by the accessExternalDTD property");
        TransformerFactory tf = DocumentBuilderFactoryUtil.newSecureTransformerFactory();
        Transformer transformer = tf.newTransformer();
        StringWriter buff = new StringWriter();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(new StreamSource(new StringReader(XML)), new StreamResult(buff));
    }
}