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

package eu.eidas.auth.engine.core.eidas.impl;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.encryption.exception.UnmarshallException;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Test class for {@link SignatureUnmarshaller}
 */
public class SignatureUnmarshallerTest {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void beforeClass() {
        new SignatureUnmarshaller(); // Init.isInitialized()
    }

    @Before
    public void setUp() throws Exception {
        new EidasProtocolProcessor(null, null, null);
    }

    /**
     * Test method for
     * {@link SignatureUnmarshaller#unmarshall(Element)}
     * <p>
     * Must succeed.
     */
    @Test
    public void unmarshall() throws IOException, ParserConfigurationException, SAXException, UnmarshallingException {
        final Element signatureRootElement = getSignatureDocument().getDocumentElement();

        final Signature unmarshalledSignature = new SignatureUnmarshaller().unmarshall(signatureRootElement);

        Assert.assertNotNull(unmarshalledSignature);
        Assert.assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1, unmarshalledSignature.getSignatureAlgorithm());
        Assert.assertEquals(CanonicalizationMethod.EXCLUSIVE, unmarshalledSignature.getCanonicalizationAlgorithm());

    }

    /**
     * Test method for
     * {@link SignatureUnmarshaller#unmarshall(Element)}
     * via {@link OpenSamlHelper#unmarshallFromDom(Document)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testUnmarshallerConfigurationFromDocument() throws IOException, ParserConfigurationException, SAXException, UnmarshallException {
        final Document signatureDocument = getSignatureDocument();

        final Signature unmarshalledSignature = (Signature) OpenSamlHelper.unmarshallFromDom(signatureDocument);

        Assert.assertNotNull(unmarshalledSignature);
        Assert.assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1, unmarshalledSignature.getSignatureAlgorithm());
        Assert.assertEquals(CanonicalizationMethod.EXCLUSIVE, unmarshalledSignature.getCanonicalizationAlgorithm());
    }

    /**
     * Test method for
     * {@link SignatureUnmarshaller#unmarshall(Element)} via {@link UnmarshallerFactory#getUnmarshaller(Element)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testUnmarshallerConfigurationFromElement() throws IOException, ParserConfigurationException, SAXException, UnmarshallingException {
        final Element signatureRootElement = getSignatureDocument().getDocumentElement();
        final UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
        final Unmarshaller signatureUnmarshaller = unmarshallerFactory.getUnmarshaller(signatureRootElement);
        org.springframework.util.Assert.isInstanceOf(eu.eidas.auth.engine.core.eidas.impl.SignatureUnmarshaller.class, signatureUnmarshaller);

        final Signature unmarshalledSignature = (Signature) signatureUnmarshaller.unmarshall(signatureRootElement);

        Assert.assertNotNull(unmarshalledSignature);
        Assert.assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1, unmarshalledSignature.getSignatureAlgorithm());
        Assert.assertEquals(CanonicalizationMethod.EXCLUSIVE, unmarshalledSignature.getCanonicalizationAlgorithm());
    }

    /**
     * Test method for
     * {@link SignatureUnmarshaller#unmarshall(Element)} via {@link UnmarshallerFactory#getUnmarshaller(QName)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testUnmarshallerConfigurationFromQName() throws IOException, ParserConfigurationException, SAXException, UnmarshallingException {
        final Element signatureRootElement = getSignatureDocument().getDocumentElement();
        final UnmarshallerFactory unmarshallerFactory = XMLObjectProviderRegistrySupport.getUnmarshallerFactory();
        final Unmarshaller signatureUnmarshaller = unmarshallerFactory.getUnmarshaller(Signature.DEFAULT_ELEMENT_NAME);
        org.springframework.util.Assert.isInstanceOf(eu.eidas.auth.engine.core.eidas.impl.SignatureUnmarshaller.class, signatureUnmarshaller);

        final Signature unmarshalledSignature = (Signature) signatureUnmarshaller.unmarshall(signatureRootElement);

        Assert.assertNotNull(unmarshalledSignature);
        Assert.assertEquals(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1, unmarshalledSignature.getSignatureAlgorithm());
        Assert.assertEquals(CanonicalizationMethod.EXCLUSIVE, unmarshalledSignature.getCanonicalizationAlgorithm());
    }
    
    @Test
    public void unmarshallMalformedDomDOMException() throws UnmarshallingException, ParserConfigurationException, XMLSecurityException {
        expected.expect(DOMException.class);
        expected.expectMessage("Cannot find Reference in Manifest");

        final SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        final Document document = DocumentBuilderFactoryUtil.newDocument();
        final XMLSignature xmlSignature = new XMLSignature(
                document,
                "",
                signature.getSignatureAlgorithm(),
                signature.getCanonicalizationAlgorithm()
        );

        new SignatureUnmarshaller().unmarshall(xmlSignature.getElement());
    }

    private Document getSignatureDocument() throws IOException, SAXException, ParserConfigurationException {
        final String MOCK_RESPONSE_FILE_PATH = "src/test/resources/signature.xml";
        final FileInputStream fileInputStream = new FileInputStream(MOCK_RESPONSE_FILE_PATH);
        final Document signatureDocument = DocumentBuilderFactoryUtil.parse(fileInputStream);
        return signatureDocument;
    }
}