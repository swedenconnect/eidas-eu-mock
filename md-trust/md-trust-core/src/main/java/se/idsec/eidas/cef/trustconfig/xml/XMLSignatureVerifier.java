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

import org.apache.xml.security.keys.KeyInfo;
import org.w3.x2000.x09.xmldsig.ReferenceType;
import org.w3.x2000.x09.xmldsig.SignatureDocument;
import org.w3.x2000.x09.xmldsig.SignatureType;
import org.w3c.dom.*;

import javax.xml.crypto.dsig.XMLSignature;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Basic XML Signature verifier. Providing functionality to verify signatures on an XML document.
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class XMLSignatureVerifier {

    private static final Logger LOG = Logger.getLogger(XMLSignatureVerifier.class.getName());

    static {
        // Initialize the Apache Santuario XML sec library
        // This should have been done by the CEF code, but there is protection against multiple activation, so calling this function is never harmful.
        org.apache.xml.security.Init.init();
    }

    /**
     * Prevents this class from being instantiated as it contains only static functions.
     */
    private XMLSignatureVerifier() {
    }

    /**
     * Verifies the signatures on a signed XML document
     * @param signedXml signed XML document bytes
     * @return the result of signature validation
     */
    public static SigVerifyResult verifySignature(byte[] signedXml) {
        try {
            Document doc = XmlUtils.getDocument(signedXml);
            return verifySignature(doc);
        } catch (Exception ex) {
            LOG.warning(ex.getMessage());
            return new SigVerifyResult("Unable to parse document");
        }

    }

    /**
     * Verifies the signatures on a signed XML document
     * @param doc the signed XML document
     * @return the resutl of signature validation
     */
    public static SigVerifyResult verifySignature(Document doc) {

        SigVerifyResult result = new SigVerifyResult();
        // Get signature nodes;
        NodeList signatureNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (signatureNodes.getLength() == 0) {
            return new SigVerifyResult("No Signature");
        }
        //Get document ID attribute
        Node idAttrNode = registerIdAttributes(doc);

        boolean hasId = false;
        String docID = null;
        if (idAttrNode != null) {
            try {
                docID = idAttrNode.getTextContent();
                hasId = docID != null;
            } catch (Exception ex) {
            }
        }

        //Verify all signatures
        for (int i = 0; i < signatureNodes.getLength(); i++) {
            //Check if this signature covers the document
            boolean coversDoc = false;
            try {
                Node sigNode = signatureNodes.item(i);
                SignatureType sigType = SignatureDocument.Factory.parse(sigNode).getSignature();
                ReferenceType[] referenceArray = sigType.getSignedInfo().getReferenceArray();
                for (ReferenceType ref : referenceArray) {
                    if (ref.getURI().equals("")) {
                        coversDoc = true;
                    }
                    if (hasId) {
                        if (ref.getURI().equals("#" + docID)) {
                            coversDoc = true;
                        }
                    }
                }
                //Verify the signature if it covers the doc
                if (coversDoc) {
                    SigVerifyResult.IndivdualSignatureResult newResult = result.addNewIndividualSignatureResult();
                    newResult.thisSignatureNode = sigNode;
                    verifySignatureElement(doc, (Element) sigNode, newResult);
                }

            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "unable to verify signature on XML document", ex);
            }
        }
        result.consolidateResults();

        return result;
    }

    /**
     * Verifies the signature associated with a signature element
     * @param doc the complete xml document being validated
     * @param sigElement the signature element being validated
     * @param result result of signature validation
     * @throws Exception exception caused by an error during signature validation
     */
    public static void verifySignatureElement(Document doc, Element sigElement, SigVerifyResult.IndivdualSignatureResult result) throws Exception {
        try {
            org.apache.xml.security.signature.XMLSignature signature = new org.apache.xml.security.signature.XMLSignature(sigElement, "");
            KeyInfo ki = signature.getKeyInfo();

            if (ki == null) {
                result.thisStatus = "No Key Info";
                return;
            }
            X509Certificate cert = signature.getKeyInfo().getX509Certificate();

            if (cert == null) {
                result.thisStatus = "No Certificate in signature";
                return;
            }
            result.thisValid = signature.checkSignatureValue(cert);
            result.thisStatus = result.thisValid ? "Signature valid" : "Signature validation failed";
            result.thisSignatureNode = sigElement;
            result.thisCert = cert;
            return;
        } catch (Exception ex) {
            result.thisStatus = "Signature parsing error";
            throw ex;
        }
    }

    /**
     * Locates the main ID node of the document, if any, and register the ID attribute
     * @param doc the document to process
     * @return the ID node
     */
    public static Node registerIdAttributes(Document doc){
        Element rootElm = getFirstElementNode(doc);
        Node idAttrNode = null;
        try {
            NamedNodeMap rootAttr = rootElm.getAttributes();
            idAttrNode = rootAttr.getNamedItem("ID");
            if (idAttrNode != null) {
                rootElm.setIdAttribute("ID", true);
            }
        } catch (Exception ex) {
        }
        if (idAttrNode == null) {
            try {
                NamedNodeMap rootAttr = rootElm.getAttributes();
                idAttrNode = rootAttr.getNamedItem("Id");
                if (idAttrNode != null) {
                    rootElm.setIdAttribute("Id", true);
                }
            } catch (Exception ex) {
            }
        }
        return idAttrNode;
    }

    /**
     * Parse an X.509 certificate byte array
     * @param encoded encoded bytes of the certificate
     * @return the encoded certificate or null if certificate parsing failed
     */
    public static X509Certificate getCertificate(byte[] encoded) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            InputStream is = new ByteArrayInputStream(encoded);
            X509Certificate generateCertificate = (X509Certificate) fact.generateCertificate(is);
            is.close();
            return generateCertificate;
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (CertificateException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Return the first element node of a document
     * @param doc document
     * @return first element node or null if not found
     */
    private static Element getFirstElementNode(Document doc) {
        NodeList childNodes = doc.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }
}
