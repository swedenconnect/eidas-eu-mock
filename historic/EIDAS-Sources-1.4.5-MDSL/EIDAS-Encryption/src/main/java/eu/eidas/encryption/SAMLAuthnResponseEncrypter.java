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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.opensaml.common.xml.SAMLConstants;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.EncryptedAssertion;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.encryption.Encrypter;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.Namespace;
import org.opensaml.xml.encryption.EncryptionParameters;
import org.opensaml.xml.encryption.KeyEncryptionParameters;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.EncryptionException;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.util.Preconditions;

/**
 * Low-level implementation of the OpenSAML encryption process.
 */
@Immutable
@ThreadSafe
public final class SAMLAuthnResponseEncrypter {

    /**
     * Builder pattern for the {@link SAMLAuthnResponseEncrypter} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        private String dataEncryptionAlgorithm;

        private String jcaProviderName;

        private String keyEncryptionAlgorithm;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            dataEncryptionAlgorithm = copy.dataEncryptionAlgorithm;
            jcaProviderName = copy.jcaProviderName;
            keyEncryptionAlgorithm = copy.keyEncryptionAlgorithm;
        }

        public Builder(@Nonnull SAMLAuthnResponseEncrypter copy) {
            Preconditions.checkNotNull(copy, "copy");
            dataEncryptionAlgorithm = copy.dataEncryptionAlgorithm;
            jcaProviderName = copy.jcaProviderName;
            keyEncryptionAlgorithm = copy.keyEncryptionAlgorithm;
        }

        public SAMLAuthnResponseEncrypter build() {
            validate();
            return new SAMLAuthnResponseEncrypter(this);
        }

        public Builder dataEncryptionAlgorithm(final String dataEncryptionAlgorithm) {
            this.dataEncryptionAlgorithm = dataEncryptionAlgorithm;
            return this;
        }

        public Builder jcaProviderName(final String jcaProviderName) {
            this.jcaProviderName = jcaProviderName;
            return this;
        }

        public Builder keyEncryptionAlgorithm(final String keyEncryptionAlgorithm) {
            this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
            return this;
        }

        private void validate() throws IllegalArgumentException {
            if (StringUtils.isBlank(dataEncryptionAlgorithm)) {
                dataEncryptionAlgorithm = DefaultEncryptionAlgorithm.DEFAULT_DATA_ENCRYPTION_ALGORITHM.getValue();
            }
            if (StringUtils.isBlank(jcaProviderName)) {
                jcaProviderName = null;
            }
            if (StringUtils.isBlank(keyEncryptionAlgorithm)) {
                keyEncryptionAlgorithm = DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM.getValue();
            }
        }
    }

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull SAMLAuthnResponseEncrypter copy) {
        return new Builder(copy);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SAMLAuthnResponseEncrypter.class);

    @Nonnull
    private final String dataEncryptionAlgorithm;

    @Nullable
    private final String jcaProviderName;

    @Nonnull
    private final String keyEncryptionAlgorithm;

    private SAMLAuthnResponseEncrypter(@Nonnull Builder builder) {
        dataEncryptionAlgorithm = builder.dataEncryptionAlgorithm;
        jcaProviderName = builder.jcaProviderName;
        keyEncryptionAlgorithm = builder.keyEncryptionAlgorithm;
    }

    public Response encryptSAMLResponse(final Response samlResponse, final Credential credential)
            throws EncryptionException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SAML Response encrypting with data encryption algorithm: '" + getDataEncAlgorithm() + "'");
            LOGGER.debug("SAML Response encrypting with key encryption algorithm: '" + getKeyEncAlgorithm() + "'");
        }
        try {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("SAML Response XMLObject to encrypt: " + EidasStringUtil.toString(
                        OpenSamlHelper.marshall(samlResponse)));
            }
            Response encryptedResponse = performEncryption(samlResponse, credential);

            if (LOGGER.isTraceEnabled()) {
                byte[] samlResponseEncrypted = OpenSamlHelper.marshall(encryptedResponse);
                LOGGER.trace("SAML Response XMLObject encrypted: " + EidasStringUtil.toString(samlResponseEncrypted));
            }

            return encryptedResponse;
        } catch (MarshallException e) {
            throw new EncryptionException(e);
        }
    }

    public byte[] encryptSAMLResponseAndMarshall(final Response samlResponse, final BasicX509Credential credential)
            throws EncryptionException {
        Response samlResponseEncryptee = encryptSAMLResponse(samlResponse, credential);
        byte[] samlResponseEncrypted;
        try {
            samlResponseEncrypted = OpenSamlHelper.marshall(samlResponseEncryptee);
        } catch (MarshallException e) {
            throw new EncryptionException(e);
        }

        return samlResponseEncrypted;
    }

    @Nonnull
    public String getDataEncAlgorithm() {
        return dataEncryptionAlgorithm;
    }

    public String getJcaProviderName() {
        return jcaProviderName;
    }

    public String getKeyEncAlgorithm() {
        return keyEncryptionAlgorithm;
    }

    /**
     * Manage specific namespace (e.g.saml2:)
     *
     * @param assertion
     */
    private void manageNamespaces(Assertion assertion) {
        if (assertion.getDOM().getAttributeNode("xmlns:saml2") == null) {
            Namespace saml2NS = new Namespace(SAMLConstants.SAML20_NS, SAMLConstants.SAML20_PREFIX);
            assertion.getNamespaceManager().registerNamespaceDeclaration(saml2NS);
            assertion.getDOM().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:saml2", SAMLConstants.SAML20_NS);
        }

        Namespace storkNS = new Namespace("urn:eu:stork:names:tc:STORK:1.0:assertion", "stork");
        Set<Namespace> namespaces = assertion.getNamespaceManager().getAllNamespacesInSubtreeScope();
        for (Namespace namespace : namespaces) {
            if (namespace.getNamespaceURI().equals(storkNS.getNamespaceURI())
                    && assertion.getDOM().getAttributeNode("xmlns:stork") == null) {
                assertion.getNamespaceManager().registerNamespaceDeclaration(storkNS);
                assertion.getDOM()
                        .setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:stork",
                                        "urn:eu:stork:names:tc:STORK:1.0:assertion");
                break;
            }
        }

        // TODO: what about eIDAS?
    }

    @Nonnull
    private Response performEncryption(@Nonnull Response samlResponseEncryptee, @Nonnull Credential credential)
            throws EncryptionException {
        try {
            // Set Data Encryption parameters
            EncryptionParameters encParams = new EncryptionParameters();
            encParams.setAlgorithm(getDataEncAlgorithm());
            // Set Key Encryption parameters
            KeyEncryptionParameters kekParams = new KeyEncryptionParameters();
            kekParams.setEncryptionCredential(credential);
            kekParams.setAlgorithm(getKeyEncAlgorithm());
            KeyInfoGeneratorFactory kigf = Configuration.getGlobalSecurityConfiguration()
                    .getKeyInfoGeneratorManager()
                    .getDefaultManager()
                    .getFactory(credential);
            kekParams.setKeyInfoGenerator(kigf.newInstance());
            // Setup Open SAML Encrypter
            Encrypter encrypter = new Encrypter(encParams, kekParams);
            encrypter.setKeyPlacement(Encrypter.KeyPlacement.INLINE);
            if (getJcaProviderName() != null) {
                encrypter.setJCAProviderName(getJcaProviderName());
            }

            for (Assertion assertion : samlResponseEncryptee.getAssertions()) {
                if (assertion.getDOM() == null) {
                    OpenSamlHelper.marshallToDom(assertion);
                }
                manageNamespaces(assertion);
            }
            List<EncryptedAssertion> encryptedAssertions = new ArrayList<>();
            for (Assertion assertion : samlResponseEncryptee.getAssertions()) {
                EncryptedAssertion encryptedAssertion = encrypter.encrypt(assertion);
                encryptedAssertions.add(encryptedAssertion);
            }

            Element previousDom = samlResponseEncryptee.getDOM();
            if (null == previousDom) {
                previousDom = OpenSamlHelper.marshallToDom(samlResponseEncryptee);
            }
            Document ownerDocument = previousDom.getOwnerDocument();

            // Deep copy the previous DOM into a new one using importNode()
            Document newDocument = DocumentBuilderFactoryUtil.newDocument();
            Node copiedRoot = newDocument.importNode(ownerDocument.getDocumentElement(), true);
            newDocument.appendChild(copiedRoot);

            Element newRootElement = newDocument.getDocumentElement();
            NodeList assertionList =
                    newRootElement.getElementsByTagNameNS(Assertion.DEFAULT_ELEMENT_NAME.getNamespaceURI(),
                                                          Assertion.DEFAULT_ELEMENT_NAME.getLocalPart());

            // Replace the encrypted assertions by the decrypted assertions in the new DOM tree:
            for (int i = 0, n = assertionList.getLength(); i < n; i++) {
                Node assertion = assertionList.item(i);
                EncryptedAssertion encryptedAssertion = encryptedAssertions.get(i);
                Element encryptedAssertionDOM = encryptedAssertion.getDOM();
                Node copiedEncryptedAssertion;
                if (null == encryptedAssertionDOM) {
                    encryptedAssertionDOM = OpenSamlHelper.marshallToDom(encryptedAssertion);
                }
                // we may use adoptNode() instead of importNode() because the unmarshaller rectifies the ID-ness:
                copiedEncryptedAssertion = newDocument.adoptNode(encryptedAssertionDOM);
                newRootElement.replaceChild(copiedEncryptedAssertion, assertion);
            }

            // Finally unmarshall the updated DOM into a new XMLObject graph:
            // The unmarshaller rectifies the ID-ness:
            // See org.opensaml.saml1.core.impl.AssertionUnmarshaller.unmarshall()
            // See org.opensaml.saml2.core.impl.AssertionUnmarshaller.processAttribute()
            // And org.opensaml.saml1.core.impl.ResponseAbstractTypeUnmarshaller.unmarshall()
            // And org.opensaml.saml2.core.impl.StatusResponseTypeUnmarshaller.processAttribute()
            Response encryptedResponse = (Response) OpenSamlHelper.unmarshallFromDom(newDocument);

            if (LOGGER.isTraceEnabled()) {
                try {
                    LOGGER.trace("SAML Response XMLObject encrypted: " + EidasStringUtil.toString(
                            DocumentBuilderFactoryUtil.marshall(newDocument, true)));
                } catch (TransformerException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }

            return encryptedResponse;

        } catch (org.opensaml.xml.encryption.EncryptionException | ParserConfigurationException | MarshallException | UnmarshallException e) {
            throw new EncryptionException(e);
        }
    }
}
