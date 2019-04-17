/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.encryption;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.encryption.Decrypter;
import org.opensaml.xml.encryption.EncryptedKey;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.StaticKeyInfoCredentialResolver;
import org.opensaml.xml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.DecryptionException;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;

/**
 * Low-level implementation of the OpenSAML decryption process.
 */
public final class SAMLAuthnResponseDecrypter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAuthnResponseDecrypter.class);

    @Nullable
    private final String jcaProviderName;

    public SAMLAuthnResponseDecrypter(@Nullable String jcaProviderName) {
        this.jcaProviderName = jcaProviderName;
    }

    @Nonnull
    private Response performDecryption(@Nonnull Response samlResponseDecryptee, @Nonnull X509Credential credential)
            throws DecryptionException, MarshallException {

        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("SAML Response XMLObject to decrypt: " + EidasStringUtil.toString(
                        OpenSamlHelper.marshall(samlResponseDecryptee)));
            }
            List<DocumentFragment> decryptedAssertionFragments = new ArrayList<>();
            for (EncryptedAssertion encAssertion : samlResponseDecryptee.getEncryptedAssertions()) {
                EncryptedKey encryptedSymmetricKey =
                        encAssertion.getEncryptedData().getKeyInfo().getEncryptedKeys().get(0);

                //KEY DECRYPTER
                Decrypter keyDecrypter = new Decrypter(null, new StaticKeyInfoCredentialResolver(credential), null);
                SecretKey dataDecKey = (SecretKey) keyDecrypter.decryptKey(encryptedSymmetricKey,
                                                                           encAssertion.getEncryptedData()
                                                                                   .getEncryptionMethod()
                                                                                   .getAlgorithm());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("SAML Response decrypting with data encryption algorithm: '"
                                         + encAssertion.getEncryptedData().getEncryptionMethod().getAlgorithm() + "'");
                }

                //DATA DECRYPTER
                Credential dataDecCredential = SecurityHelper.getSimpleCredential(dataDecKey);
                Decrypter dataDecrypter =
                        new Decrypter(new StaticKeyInfoCredentialResolver(dataDecCredential), null, null);
                dataDecrypter.setRootInNewDocument(false);
                if (getJcaProviderName() != null) {
                    dataDecrypter.setJCAProviderName(getJcaProviderName());
                }
                //https://jira.spring.io/browse/SES-148
                //http://digitaliser.dk/forum/2621692
                DocumentFragment decryptedAssertionFragment =
                        dataDecrypter.decryptDataToDOM(encAssertion.getEncryptedData());

                decryptedAssertionFragments.add(decryptedAssertionFragment);

                // We only want to work on the DOM tree because:
                //
                // When you call add() on an OpenSAML list: see org.opensaml.xml.util.XMLObjectChildrenList.add()
                // it calls org.opensaml.xml.util.XMLObjectChildrenList.setParent()
                // which invokes: element.releaseParentDOM(true);
                // therefore after this call, the DOM is null
            }

            // Then we will unmarshall the decrypted fragments into a cloned DOM tree
            /*
                In the @eu.eidas.encryption.SAMLAuthnResponseDecrypter.decryptSAMLResponse method when inserting
                the decrypted Assertions the DOM resets to null.
                Marsahlling it again resolves it but this loses the ID-ness of attributes.
                Which means that signatures cannot be found anymore in the DOM and signature verification
                fails (because it used Document.getElementById("assertionID") to find the signed assertion).

                See http://svn.shibboleth.net/view/java-xmltooling/branches/REL_1/src/main/java/org/opensaml/xml/encryption/Decrypter.java?view=markup
                    http://shibboleth.net/pipermail/dev/2012-April/000624.html

                And https://issues.apache.org/jira/browse/XERCESJ-1022

                More info in the links belows
                https://jira.spring.io/browse/SES-148
                http://digitaliser.dk/forum/2621692
            */
            Element previousDom = samlResponseDecryptee.getDOM();
            if (null == previousDom) {
                previousDom = OpenSamlHelper.marshallToDom(samlResponseDecryptee);
            }
            Document ownerDocument = previousDom.getOwnerDocument();

            // Deep copy the previous DOM into a new one using importNode()
            Document newDocument = DocumentBuilderFactoryUtil.newDocument();
            Node copiedRoot = newDocument.importNode(ownerDocument.getDocumentElement(), true);
            newDocument.appendChild(copiedRoot);

            Element newRootElement = newDocument.getDocumentElement();
            NodeList encryptedAssertionList =
                    newRootElement.getElementsByTagNameNS(EncryptedAssertion.DEFAULT_ELEMENT_NAME.getNamespaceURI(),
                                                          EncryptedAssertion.DEFAULT_ELEMENT_NAME.getLocalPart());

            // Replace the encrypted assertions by the decrypted assertions in the new DOM tree:
            for (int i = 0, n = encryptedAssertionList.getLength(); i < n; i++) {
                Node encryptedAssertion = encryptedAssertionList.item(i);
                DocumentFragment decryptedAssertionFragment = decryptedAssertionFragments.get(i);
                // we may use adoptNode() instead of importNode() because the unmarshaller rectifies the ID-ness:
                // See org.opensaml.saml1.core.impl.AssertionUnmarshaller.unmarshall()
                // See org.opensaml.saml2.core.impl.AssertionUnmarshaller.processAttribute()
                // And org.opensaml.saml1.core.impl.ResponseAbstractTypeUnmarshaller.unmarshall()
                // And org.opensaml.saml2.core.impl.StatusResponseTypeUnmarshaller.processAttribute()
                Node copiedFragment = newDocument.adoptNode(decryptedAssertionFragment);
                newRootElement.replaceChild(copiedFragment, encryptedAssertion);
            }

            // Finally unmarshall the updated DOM into a new XMLObject graph:
            // The unmarshaller rectifies the ID-ness:
            // See org.opensaml.saml1.core.impl.AssertionUnmarshaller.unmarshall()
            // See org.opensaml.saml2.core.impl.AssertionUnmarshaller.processAttribute()
            // And org.opensaml.saml1.core.impl.ResponseAbstractTypeUnmarshaller.unmarshall()
            // And org.opensaml.saml2.core.impl.StatusResponseTypeUnmarshaller.processAttribute()
            Response decryptedResponse = (Response) OpenSamlHelper.unmarshallFromDom(newDocument);

            if (LOGGER.isTraceEnabled()) {
                try {
                    LOGGER.trace("SAML Response XMLObject decrypted: " + EidasStringUtil.toString(
                            DocumentBuilderFactoryUtil.marshall(newDocument, true)));
                } catch (TransformerException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            return decryptedResponse;

        } catch (org.opensaml.xml.encryption.DecryptionException | ParserConfigurationException | UnmarshallException e) {
            throw new DecryptionException(e);
        }
    }

    public Response decryptSAMLResponse(Response samlResponseEncrypted, X509Credential credential)
            throws DecryptionException {
        try {
            return performDecryption(samlResponseEncrypted, credential);
        } catch (MarshallException e) {
            throw new DecryptionException(e);
        }
    }

    public byte[] decryptSAMLResponseAndMarshall(Response samlResponse, X509Credential credential)
            throws DecryptionException {

        Response samlResponseDecryptee = this.decryptSAMLResponse(samlResponse, credential);

        byte[] samlResponseDecrypted;
        try {
            samlResponseDecrypted = OpenSamlHelper.marshall(samlResponseDecryptee);
        } catch (MarshallException e) {
            throw new DecryptionException(e);
        }

        return samlResponseDecrypted;
    }

    @Nullable
    public String getJcaProviderName() {
        return jcaProviderName;
    }
}
