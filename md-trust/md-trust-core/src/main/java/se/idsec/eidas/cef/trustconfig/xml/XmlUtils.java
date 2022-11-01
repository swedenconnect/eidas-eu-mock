/*
 * Copyright 2012 Swedish Agency for Economic and Regional Growth - Tillväxtverket
 *
 * Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the
 * European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package se.idsec.eidas.cef.trustconfig.xml;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for XML processing
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */

public class XmlUtils {

    private static final Logger LOG = Logger.getLogger(XmlUtils.class.getName());
    private static Transformer trans;
    private static Transformer transformer;
    private static DocumentBuilderFactory safeDocBuilderFactory;
    public static final XmlOptions canonical;

    static {
        /**
         * This document builder factory is created in line with recommendations by OWASP
         * <p>https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet#JAXP_DocumentBuilderFactory.2C_SAXParserFactory_and_DOM4J</p>
         * This Document builder disables the use of DTD and mitigates XEE attack threats
         */
        safeDocBuilderFactory = DocumentBuilderFactory.newInstance();
        safeDocBuilderFactory.setNamespaceAware(true);
        try {
            safeDocBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            safeDocBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            safeDocBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            safeDocBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            safeDocBuilderFactory.setXIncludeAware(false);
            safeDocBuilderFactory.setExpandEntityReferences(false);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            trans = tf.newTransformer();
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            //transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        canonical = new XmlOptions().setSavePrettyPrintIndent(0).setSaveNoXmlDecl();
        canonical.setSaveCDataLengthThreshold(10000);
        canonical.setSaveCDataEntityCountThreshold(50);
    }


    /**
     * Generates a pretty XML print of an XML document based on java.xml
     * functions.
     *
     * @param doc The doc being processed
     * @return Test representation of the XML document
     */
    public static String getDocText(Document doc) {
        if (doc == null) {
            return "";
        }

        DOMSource domSource = new DOMSource(doc);
        try {
            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(domSource, sr);
            String xml = sw.toString();
            return xml;
        } catch (Exception ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return "";
    }

    /**
     * Provides a canonical print of the XML document. The purpose of this print
     * is to try to preserve integrity of an existing signature.
     *
     * @param doc The XML document being processed.
     * @return XML document bytes
     */
    public static byte[] getCanonicalBytes(Document doc) {
        try {
            // Output the resulting document.
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            trans.transform(new DOMSource(doc), new StreamResult(os));
            byte[] xmlData = os.toByteArray();
            return xmlData;
        } catch (TransformerException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Provides a canonical print of the XML document. The purpose of this print
     * is to try to preserve integrity of an existing signature.
     *
     * @param xo XMLBeans {@link XmlObject}
     * @return XML document bytes
     */
    public static byte[] getCanonicalBytes(XmlObject xo) {
        byte[] result = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            xo.save(bos, new XmlOptions().setSaveNoXmlDecl());
            result = bos.toByteArray();
            bos.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static Document getDocument(byte[] xmlData) throws IOException, SAXException, ParserConfigurationException {
        Document doc = safeDocBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(xmlData));
        return doc;
    }

    /**
     * Gets a secure document builder factory
     * @return {@link DocumentBuilderFactory}
     */
    public static DocumentBuilderFactory getDbFactory() {
        return safeDocBuilderFactory;
    }


}
