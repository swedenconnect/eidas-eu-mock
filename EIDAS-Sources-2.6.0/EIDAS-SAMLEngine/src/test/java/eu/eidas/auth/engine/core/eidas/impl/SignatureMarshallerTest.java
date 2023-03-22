/*
 * Copyright (c) 2021 by European Commission
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
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.junit.Before;
import org.junit.Test;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.xmlsec.signature.impl.SignatureBuilder;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Test class for {@link SignatureMarshaller}
 */
public class SignatureMarshallerTest {

    private SignatureMarshaller signatureMarshaller;

    @Before
    public void setUp() throws Exception {
        signatureMarshaller = new SignatureMarshaller();
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject, Document)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshallWithDocument() throws MarshallingException, ParserConfigurationException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        Document document = DocumentBuilderFactoryUtil.newDocument();

        signatureMarshaller.marshall(signature, document);
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject)}
     * <p>
     * Must succeed.
     */
    @Test
    public void marshall() throws MarshallingException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        signatureMarshaller.marshall(signature);
    }

    /**
     * Test method for
     * {@link SignatureMarshaller#marshall(XMLObject, Element)}
     * <p>
     * Must succeed.
     */
    @Test
    public void testMarshall() throws ParserConfigurationException, MarshallingException, XMLSecurityException {
        SignatureImpl signature = new SignatureBuilder().buildObject();
        signature.setSignatureAlgorithm(XMLSignature.ALGO_ID_SIGNATURE_ECDSA_SHA512);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
        Document document = DocumentBuilderFactoryUtil.newDocument();

        XMLSignature xmlSignature = new XMLSignature(document, "",
                signature.getSignatureAlgorithm(),
                signature.getCanonicalizationAlgorithm());

        signatureMarshaller.marshall(signature, xmlSignature.getElement());
    }
}