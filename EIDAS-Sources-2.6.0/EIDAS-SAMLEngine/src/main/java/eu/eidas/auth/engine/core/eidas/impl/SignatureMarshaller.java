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
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.algorithm.AlgorithmSupport;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.opensaml.xmlsec.signature.support.ContentReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;

/**
 * Override of the OpenSaml SignatureMarshaller to provide a way to configure the signature provider.
 */
public class SignatureMarshaller implements Marshaller {
    private final Logger log = LoggerFactory.getLogger(SignatureMarshaller.class);

    public SignatureMarshaller() {
        if (!Init.isInitialized()) {
            this.log.debug("Initializing XML security library");
            Init.init();
        }

    }
    
    public Element marshall(XMLObject xmlObject) throws MarshallingException {
        try {
            Document document = DocumentBuilderFactoryUtil.newDocument();
            return this.marshall(xmlObject, document);
        } catch (ParserConfigurationException exception) {
            throw new MarshallingException("Unable to create Document to place marshalled elements in", exception);
        }
    }

    public Element marshall(XMLObject xmlObject, Element parentElement) throws MarshallingException {
        Element signatureElement = this.createSignatureElement((SignatureImpl)xmlObject, parentElement.getOwnerDocument());
        ElementSupport.appendChildElement(parentElement, signatureElement);
        return signatureElement;
    }

    public Element marshall(XMLObject xmlObject, Document document) throws MarshallingException {
        Element signatureElement = this.createSignatureElement((SignatureImpl)xmlObject, document);
        Element documentRoot = document.getDocumentElement();
        if (documentRoot != null) {
            document.replaceChild(signatureElement, documentRoot);
        } else {
            document.appendChild(signatureElement);
        }

        return signatureElement;
    }

    private Element createSignatureElement(Signature signature, Document document) throws MarshallingException {
        this.log.debug("Starting to marshall {}", signature.getElementQName());

        try {
            this.log.debug("Creating XMLSignature object");
            XMLSignature dsig = createXMLSignature(document, signature);

            this.log.debug("Adding content to XMLSignature.");
            Iterator iterator = signature.getContentReferences().iterator();

            while(iterator.hasNext()) {
                ContentReference contentReference = (ContentReference)iterator.next();
                contentReference.createReference(dsig);
            }

            this.log.debug("Creating Signature DOM element");
            Element signatureElement = dsig.getElement();
            if (signature.getKeyInfo() != null) {
                Marshaller keyInfoMarshaller = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(KeyInfo.DEFAULT_ELEMENT_NAME);
                keyInfoMarshaller.marshall(signature.getKeyInfo(), signatureElement);
            }

            ((SignatureImpl)signature).setXMLSignature(dsig);
            signature.setDOM(signatureElement);
            signature.releaseParentDOM(true);
            return signatureElement;
        } catch (XMLSecurityException exception) {
            String msg = "Unable to construct signature Element " + signature.getElementQName();
            this.log.error(msg, exception);
            throw new MarshallingException(msg, exception);
        }
    }

    /**
     * Static method to create the XMLSignature to ensure XMLSignature creation is thread safe.
     * Because of the possible override of the JCEMapper which is static.
     * @param document document
     * @param signature signature
     * @return the XMLSignature
     */
    private static XMLSignature createXMLSignature(final Document document, final Signature signature) throws XMLSecurityException {
        XMLSignature xmlSignature;
        if (signature.getHMACOutputLength() != null && AlgorithmSupport.isHMAC(signature.getSignatureAlgorithm())) {
            xmlSignature = new XMLSignature(document, "", signature.getSignatureAlgorithm(), signature.getHMACOutputLength(), signature.getCanonicalizationAlgorithm());
        } else {
            xmlSignature = new XMLSignature(document, "", signature.getSignatureAlgorithm(), signature.getCanonicalizationAlgorithm());
        }
        return xmlSignature;
    }

    private static boolean isPKCS11Credential(Credential credential) {
        return credential != null && credential.getPrivateKey() != null
                && "sun.security.pkcs11.P11Key$P11PrivateKey".equals(credential.getPrivateKey().getClass().getName());
    }

    private static boolean isRSASigningCredential(Credential credential) {
        return credential != null && credential.getPublicKey() != null
                && JCAConstants.KEY_ALGO_RSA.equalsIgnoreCase(credential.getPublicKey().getAlgorithm());
    }

}