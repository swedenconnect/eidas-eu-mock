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

package eu.eidas.engine.test.simple;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.opensaml.Configuration;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.MarshallingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.eidas.auth.commons.Constants;
import eu.eidas.auth.commons.EidasStringUtil;

/**
 * The Class SSETestUtils.
 */
public final class SSETestUtils {


    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory
	    .getLogger(SSETestUtils.class.getName());

    /**
     * Instantiates a new sSE test utils.
     */
    private SSETestUtils() {
    }

    /**
     * Prints the tree DOM.
     *
     * @param samlToken the SAML token
     * @param isIndent the is indent
     *
     * @return the string
     * @throws TransformerException the exception
     */
    public static String printTreeDOM(final Element samlToken,
	    final boolean isIndent) throws TransformerException {
	// set up a transformer
	final TransformerFactory transfac = TransformerFactory.newInstance();
	final Transformer trans = transfac.newTransformer();
	trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	trans.setOutputProperty(OutputKeys.INDENT, String.valueOf(isIndent));

	// create string from XML tree
	final StringWriter stringWriter = new StringWriter();
	final StreamResult result = new StreamResult(stringWriter);
	final DOMSource source = new DOMSource(samlToken);
	trans.transform(source, result);
	final String xmlString = stringWriter.toString();

	return xmlString;
    }

    /**
     * Marshall.
     *
     * @param samlToken the SAML token
     *
     * @return the byte[]
     *
     * @throws MarshallingException the marshalling exception
     * @throws ParserConfigurationException the parser configuration exception
     * @throws TransformerException the transformer exception
     */
    public static byte[] marshall(final XMLObject samlToken)
	    throws MarshallingException, ParserConfigurationException,
	    TransformerException {

	final javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory
		.newInstance();
	dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
	dbf.setNamespaceAware(true);
	dbf.setIgnoringComments(true);
	final javax.xml.parsers.DocumentBuilder docBuild = dbf
		.newDocumentBuilder();

	// Get the marshaller factory
	final MarshallerFactory marshallerFactory = Configuration
		.getMarshallerFactory();

	// Get the Subject marshaller
	final Marshaller marshaller = marshallerFactory
		.getMarshaller(samlToken);

	final Document doc = docBuild.newDocument();

	// Marshall the SAML token
	marshaller.marshall(samlToken, doc);

	// Obtain a byte array representation of the marshalled SAML object
	final DOMSource domSource = new DOMSource(doc);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
	final StreamResult result = new StreamResult(new OutputStreamWriter(baos, Constants.UTF8));
	final TransformerFactory transFact = TransformerFactory.newInstance();
	final Transformer transformer = transFact.newTransformer();
	transformer.transform(domSource, result);

	return baos.toByteArray();
    }

    /**
     * Encode SAML token.
     *
     * @param samlToken the SAML token
     *
     * @return the string
     */
    public static String encodeSAMLToken(final byte[] samlToken) {
	return EidasStringUtil.encodeToBase64(samlToken);
    }

    /**
     * Read SAML from file.
     *
     * @param resource the resource
     *
     * @return the byte[]
     * @throws IOException the exception
     *
     */
    public static byte[] readSamlFromFile(final String resource)
	    throws IOException {
	InputStream inputStream = null;
	byte[] bytes;

	try {
	    inputStream = AuthRequestTest.class
		.getResourceAsStream(resource);

	    // Create the byte array to hold the data
	    bytes = new byte[(int) inputStream.available()];
	    inputStream.read(bytes);
	} catch (IOException e) {
	    LOG.error("Error read from file: " + resource);
	    throw e;
	} finally {
	    IOUtils.closeQuietly(inputStream);
	}
	return bytes;

    }
}
