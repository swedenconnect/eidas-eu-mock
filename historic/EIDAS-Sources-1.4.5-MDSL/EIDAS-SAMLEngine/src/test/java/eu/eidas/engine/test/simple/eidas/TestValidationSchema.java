package eu.eidas.engine.test.simple.eidas;

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;

import static org.junit.Assert.fail;


public class TestValidationSchema {
    private static final Logger LOG = LoggerFactory.getLogger(TestValidationSchema.class.getName());
    @Test
    public void testDummy(){

    }
    //@Test
    private void testSchemaValidation() {
        try {
        // parse an XML document into a DOM tree
        DocumentBuilder parser = DocumentBuilderFactoryUtil.newSecureDocumentBuilderFactory().newDocumentBuilder();
            File f=new File("EncryptModule_Metadata.xml");
            if(!f.exists()){
                throw new IOException();
            }
        Document document = parser.parse(f);

        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        // load a WXS schema, represented by a Schema instance
        Source schemaFile = new StreamSource(new File("./target/classes/test/saml_eidas_natural_person.xsd"));
        Schema schema = factory.newSchema(schemaFile);

        // create a Validator instance, which can be used to validate an instance document
        Validator validator = schema.newValidator();

        // validate the DOM tree
            validator.validate(new DOMSource(document));
        } catch (ParserConfigurationException e) {
            LOG.error("ParserConfigurationException {}", e);
            fail("ParserConfigurationException");
        }catch (SAXException e) {
            LOG.error("SAXException {}", e);
            fail("SAXException");
        }catch (IOException e) {
            LOG.error("IoException {}", e);
            fail("IoException");
        }
    }
}
