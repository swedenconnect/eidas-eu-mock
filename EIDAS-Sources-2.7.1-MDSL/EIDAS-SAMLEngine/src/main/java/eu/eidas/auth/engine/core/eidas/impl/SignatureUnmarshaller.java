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

import net.shibboleth.utilities.java.support.primitive.StringSupport;
import net.shibboleth.utilities.java.support.xml.ElementSupport;
import org.apache.xml.security.Init;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Unmarshaller;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import java.security.spec.PSSParameterSpec;
import java.util.List;

/**
 * Class to prepare the signature to be verified
 * forked from {@link org.opensaml.xmlsec.signature.impl.SignatureUnmarshaller}
 */
public class SignatureUnmarshaller implements Unmarshaller {
    private final Logger log = LoggerFactory.getLogger(org.opensaml.xmlsec.signature.impl.SignatureUnmarshaller.class);

    public SignatureUnmarshaller() {
        if (!Init.isInitialized()) {
            this.log.debug("Initializing XML security library");
            Init.init();
        }

    }

    class ConcreteSignature extends SignatureImpl {

        protected ConcreteSignature(String namespaceURI, String elementLocalName, String namespacePrefix) {
            super(namespaceURI, elementLocalName, namespacePrefix);
        }
    }

    public Signature unmarshall(Element signatureElement) throws UnmarshallingException {
        this.log.debug("Starting to unmarshall Apache XML-Security-based SignatureImpl element");
        SignatureImpl signature = new ConcreteSignature(signatureElement.getNamespaceURI(), signatureElement.getLocalName(), signatureElement.getPrefix());

        try {
            this.log.debug("Constructing Apache XMLSignature object");

            final XMLSignature xmlSignature = new XMLSignature(signatureElement, "");
            final SignedInfo signedInfo = xmlSignature.getSignedInfo();
            final String signatureMethodURI = signedInfo.getSignatureMethodURI();
            if ("RSASSA-PSS".equalsIgnoreCase(signedInfo.getSignatureAlgorithm().getJCEAlgorithmString())) {
                final PSSParameterSpec algorithmParameterSpec = RsaSsaPssUtil.getRsaSsaPssParameters(signatureMethodURI);
                xmlSignature.getSignedInfo().getSignatureAlgorithm().setParameter(algorithmParameterSpec);
            }

            this.log.debug("Adding canonicalization and signing algorithms, and HMAC output length to Signature");
            signature.setCanonicalizationAlgorithm(signedInfo.getCanonicalizationMethodURI());
            signature.setSignatureAlgorithm(signedInfo.getSignatureMethodURI());
            signature.setHMACOutputLength(this.getHMACOutputLengthValue(signedInfo.getSignatureMethodElement()));
            KeyInfo xmlSecKeyInfo = xmlSignature.getKeyInfo();
            if (xmlSecKeyInfo != null) {
                this.log.debug("Adding KeyInfo to Signature");
                Unmarshaller unmarshaller = XMLObjectProviderRegistrySupport.getUnmarshallerFactory().getUnmarshaller(xmlSecKeyInfo.getElement());
                org.opensaml.xmlsec.signature.KeyInfo keyInfo = (org.opensaml.xmlsec.signature.KeyInfo) unmarshaller.unmarshall(xmlSecKeyInfo.getElement());
                signature.setKeyInfo(keyInfo);
            }

            signature.setXMLSignature(xmlSignature);
            signature.setDOM(signatureElement);
            return signature;
        } catch (XMLSecurityException var8) {
            this.log.error("Error constructing Apache XMLSignature instance from Signature element: {}", var8.getMessage());
            throw new UnmarshallingException("Unable to unmarshall Signature with Apache XMLSignature", var8);
        }
    }

    private Integer getHMACOutputLengthValue(Element signatureMethodElement) {
        if (signatureMethodElement == null) {
            return null;
        } else {
            List<Element> children = ElementSupport.getChildElementsByTagNameNS(signatureMethodElement, "http://www.w3.org/2000/09/xmldsig#", "HMACOutputLength");
            if (!children.isEmpty()) {
                Element hmacElement = children.get(0);
                String value = StringSupport.trimOrNull(hmacElement.getTextContent());
                if (value != null) {
                    return Integer.valueOf(value);
                }
            }

            return null;
        }
    }
}