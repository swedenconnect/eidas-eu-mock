/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.encryption;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.encryption.exception.EncryptionException;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.opensaml.core.xml.Namespace;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.encryption.Encrypter;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.SecurityConfigurationSupport;
import org.opensaml.xmlsec.agreement.KeyAgreementException;
import org.opensaml.xmlsec.agreement.KeyAgreementParameter;
import org.opensaml.xmlsec.agreement.KeyAgreementParameters;
import org.opensaml.xmlsec.agreement.KeyAgreementProcessor;
import org.opensaml.xmlsec.agreement.KeyAgreementSupport;
import org.opensaml.xmlsec.encryption.support.DataEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.encryption.support.KeyEncryptionParameters;
import org.opensaml.xmlsec.encryption.support.RSAOAEPParameters;
import org.opensaml.xmlsec.keyinfo.KeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.impl.BasicKeyInfoGeneratorFactory;
import org.opensaml.xmlsec.keyinfo.impl.KeyAgreementKeyInfoGeneratorFactory;
import org.opensaml.xmlsec.signature.support.SignatureConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Low-level implementation of the OpenSAML encryption process.
 */
@Immutable
@ThreadSafe
public final class SAMLAuthnResponseEncrypter {

    /**
     * <p>
     * Builder pattern for the {@link SAMLAuthnResponseEncrypter} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        private String dataEncryptionAlgorithm;

        private String jcaProviderName;

        private String keyEncryptionAlgorithm;

        private String messageDigestKeyTransport;

        private String maskGenerationFunctionKeyTransport;

        private String keyEncryptionAlgorithmForKeyAgreement;

        private String keyEncryptionAgreementMethodAlgorithm;

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            dataEncryptionAlgorithm = copy.dataEncryptionAlgorithm;
            jcaProviderName = copy.jcaProviderName;
            keyEncryptionAlgorithm = copy.keyEncryptionAlgorithm;
            messageDigestKeyTransport = copy.messageDigestKeyTransport;
            maskGenerationFunctionKeyTransport = copy.maskGenerationFunctionKeyTransport;
            keyEncryptionAlgorithmForKeyAgreement = copy.keyEncryptionAlgorithmForKeyAgreement;
            keyEncryptionAgreementMethodAlgorithm = copy.keyEncryptionAgreementMethodAlgorithm;
        }

        public Builder(@Nonnull SAMLAuthnResponseEncrypter copy) {
            Preconditions.checkNotNull(copy, "copy");
            dataEncryptionAlgorithm = copy.dataEncryptionAlgorithm;
            jcaProviderName = copy.jcaProviderName;
            keyEncryptionAlgorithm = copy.keyEncryptionAlgorithm;
            messageDigestKeyTransport = copy.messageDigestKeyTransport;
            maskGenerationFunctionKeyTransport = copy.maskGenerationFunctionKeyTransport;
            keyEncryptionAlgorithmForKeyAgreement = copy.keyEncryptionAlgorithmForKeyAgreement;
            keyEncryptionAgreementMethodAlgorithm = copy.keyEncryptionAgreementMethodAlgorithm;
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

        public Builder messageDigestKeyTransport(final String messageDigestKeyTransport) {
            this.messageDigestKeyTransport = messageDigestKeyTransport;
            return this;
        }

        public Builder maskGenerationFunctionKeyTransport(final String maskGenerationFunctionKeyTransport) {
            this.maskGenerationFunctionKeyTransport = maskGenerationFunctionKeyTransport;
            return this;
        }

        public Builder keyEncryptionAlgorithmForKeyAgreement(final String keyEncAlgorithmForKA) {
            this.keyEncryptionAlgorithmForKeyAgreement = keyEncAlgorithmForKA;
            return this;
        }

        public Builder keyEncryptionAgreementMethodAlgorithm(final String keyEncAgreementMethodAlgorithm) {
            this.keyEncryptionAgreementMethodAlgorithm = keyEncAgreementMethodAlgorithm;
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
                keyEncryptionAlgorithm = null;
            }
            if (StringUtils.isBlank(keyEncryptionAlgorithmForKeyAgreement)) {
                keyEncryptionAlgorithmForKeyAgreement =
                        DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT.getValue();
            }
            if (StringUtils.isBlank(keyEncryptionAgreementMethodAlgorithm)) {
                keyEncryptionAgreementMethodAlgorithm =
                        DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_AGREEMENT_METHOD_ALGORITHM.getValue();

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

    private final String messageDigestKeyTransport;

    private final String maskGenerationFunctionKeyTransport;

    @Nonnull
    private final String keyEncryptionAlgorithmForKeyAgreement;

    @Nonnull
    private final String keyEncryptionAgreementMethodAlgorithm;

    private SAMLAuthnResponseEncrypter(@Nonnull Builder builder) {
        dataEncryptionAlgorithm = builder.dataEncryptionAlgorithm;
        jcaProviderName = builder.jcaProviderName;
        keyEncryptionAlgorithm = builder.keyEncryptionAlgorithm;
        messageDigestKeyTransport = builder.messageDigestKeyTransport;
        maskGenerationFunctionKeyTransport = builder.maskGenerationFunctionKeyTransport;
        keyEncryptionAlgorithmForKeyAgreement = builder.keyEncryptionAlgorithmForKeyAgreement;
        keyEncryptionAgreementMethodAlgorithm = builder.keyEncryptionAgreementMethodAlgorithm;
    }

    public Response encryptSAMLResponse(final Response samlResponse, final Credential credential
    		, boolean isKeyInfoDisplayedAsRSAKey)
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
            Response encryptedResponse = performEncryption(samlResponse, credential, isKeyInfoDisplayedAsRSAKey);

            if (LOGGER.isTraceEnabled()) {
                byte[] samlResponseEncrypted = OpenSamlHelper.marshall(encryptedResponse);
                LOGGER.trace("SAML Response XMLObject encrypted: " + EidasStringUtil.toString(samlResponseEncrypted));
            }

            return encryptedResponse;
        } catch (MarshallException e) {
            throw new EncryptionException(e);
        }
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

    private String getKeyAgreementEncAlgorithm() {
        return keyEncryptionAlgorithmForKeyAgreement;
    }

    private String getKeyEncryptionAgreementMethodAlgorithm() {
        return keyEncryptionAgreementMethodAlgorithm;
    }

    private String getKeyTransportEncAlgorithm() {
    //EIDINT-7030 COMMENTED OUT FOR 1.4 SPECIFICATIONS While only one key encryption is allowed, can be reused when this is no longer the case.
        /*    if (StringUtils.isBlank(getKeyEncAlgorithm())) {
            LOGGER.warn("ENCRYPTION MODULE: Falling back to default: Missing Key Transport URI");
            return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT.getValue();
        }
        return getKeyEncAlgorithm(); */

        return DefaultEncryptionAlgorithm.DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT.getValue();
    }

    /**
     * Eidas only supports SHA-2 with a minimal hash length of 256 bits
     */
    private String getMessageDigestKeyTransport() {
        if (SignatureConstants.ALGO_ID_DIGEST_SHA256.equals(messageDigestKeyTransport) ||
            SignatureConstants.ALGO_ID_DIGEST_SHA384.equals(messageDigestKeyTransport) ||
            SignatureConstants.ALGO_ID_DIGEST_SHA512.equals(messageDigestKeyTransport)) {
            return messageDigestKeyTransport;
        } else {
            LOGGER.warn("ENCRYPTION MODULE: Falling back to default: Invalid Key Transport Digest URI");
            return DefaultEncryptionAlgorithm.DEFAULT_DIGEST_ALGORITHM_FOR_KEY_TRANSPORT.getValue();
        }
    }

    private String getKeyTransportMaskGenerationFunction() {
        if (EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11.equals(getKeyTransportEncAlgorithm()) &&
                StringUtils.isBlank(maskGenerationFunctionKeyTransport)) {
            LOGGER.warn("ENCRYPTION MODULE: Falling back to default: Missing Key Transport MGF URI");
            return DefaultEncryptionAlgorithm.DEFAULT_MGF_ALGORITHM_FOR_KEY_TRANSPORT.getValue();
        }
        return maskGenerationFunctionKeyTransport;
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
    }

    @Nonnull
    private Response performEncryption(@Nonnull Response samlResponseEncryptee, @Nonnull Credential credential,
    		boolean isKeyInfoDisplayedAsKeyValue) throws EncryptionException {
        try {
            // Set Data Encryption parameters
            DataEncryptionParameters encParams = new DataEncryptionParameters();
            encParams.setAlgorithm(getDataEncAlgorithm());
            // Set Key Encryption parameters
            KeyEncryptionParameters kekParams = createKeyEncryptionParameters(credential, isKeyInfoDisplayedAsKeyValue);

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
            // See org.opensaml.saml.saml2.core.impl.AssertionUnmarshaller.processAttribute()
            // And org.opensaml.saml1.core.impl.ResponseAbstractTypeUnmarshaller.unmarshall()
            // And org.opensaml.saml.saml2.core.impl.StatusResponseTypeUnmarshaller.processAttribute()
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

        } catch (ParserConfigurationException | MarshallException | UnmarshallException | KeyAgreementException | org.opensaml.xmlsec.encryption.support.EncryptionException e) {
            throw new EncryptionException(e);
        }
    }

    private KeyEncryptionParameters createKeyEncryptionParameters(@Nonnull Credential credential, boolean isKeyInfoWithPublicKey)
            throws KeyAgreementException {
        final KeyEncryptionParameters keyEncryptionParameters;
        if (isECPublicKey(credential)) {
            keyEncryptionParameters = buildKeyAgreementEncryptionParams(credential);
        } else {
            keyEncryptionParameters = buildKeyTransportEncryptionParams(credential, isKeyInfoWithPublicKey);
        }

        return keyEncryptionParameters;
    }

    private boolean isECPublicKey(@Nonnull Credential credential) {
        return JCAConstants.KEY_ALGO_EC.equalsIgnoreCase(credential.getPublicKey().getAlgorithm());
    }

    private KeyEncryptionParameters buildKeyTransportEncryptionParams(@Nonnull Credential credential, boolean isKeyInfoAsKeyValue) {
        KeyEncryptionParameters keyEncryptionParameters = new KeyEncryptionParameters();
        keyEncryptionParameters.setEncryptionCredential(credential);
        RSAOAEPParameters rsaoaepParameters = new RSAOAEPParameters();
        keyEncryptionParameters.setAlgorithm(getKeyTransportEncAlgorithm());
        rsaoaepParameters.setDigestMethod(getMessageDigestKeyTransport());
        rsaoaepParameters.setMaskGenerationFunction(getKeyTransportMaskGenerationFunction());
        keyEncryptionParameters.setRSAOAEPParameters(rsaoaepParameters);

        final KeyInfoGeneratorFactory kigf = createKeyInfoGeneratorFactory(isKeyInfoAsKeyValue, credential);
        keyEncryptionParameters.setKeyInfoGenerator(kigf.newInstance());

        return keyEncryptionParameters;
    }

    private KeyEncryptionParameters buildKeyAgreementEncryptionParams(@Nonnull Credential credential) throws KeyAgreementException {
        KeyEncryptionParameters keyEncryptionParameters= new KeyEncryptionParameters();
        Credential keyAgreementCredential = getKeyAgreementCredential(credential);
        keyEncryptionParameters.setEncryptionCredential(keyAgreementCredential);
        keyEncryptionParameters.setAlgorithm(getKeyAgreementEncAlgorithm());

        final KeyInfoGeneratorFactory kigf = getDefaultKeyInfoGeneratorFactory(credential);
        keyEncryptionParameters.setKeyInfoGenerator(kigf.newInstance());

        return keyEncryptionParameters;
    }

    private KeyInfoGeneratorFactory createKeyInfoGeneratorFactory(boolean isKeyInfoAsKeyValue, Credential credential) {
        final KeyInfoGeneratorFactory kigf;
        if (isKeyInfoAsKeyValue){
            kigf = createKeyInfoGeneratorWithKeyValueFactory();
        } else {
            kigf = getDefaultKeyInfoGeneratorFactory(credential);
        }
        return kigf;
    }

    private static KeyInfoGeneratorFactory createKeyInfoGeneratorWithKeyValueFactory() {
		KeyInfoGeneratorFactory keyInfoGenFac = new BasicKeyInfoGeneratorFactory();
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitPublicKeyValue(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitEntityIDAsKeyName(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitKeyNames(true);
		((BasicKeyInfoGeneratorFactory)keyInfoGenFac).setEmitPublicDEREncodedKeyValue(true);
		return keyInfoGenFac;
	}

	private KeyInfoGeneratorFactory getDefaultKeyInfoGeneratorFactory(Credential credential) {
        if (isECPublicKey(credential)) {
            return new KeyAgreementKeyInfoGeneratorFactory();
        }
        return SecurityConfigurationSupport.getGlobalEncryptionConfiguration()
                .getDataKeyInfoGeneratorManager()
                .getDefaultManager()
                .getFactory(credential);
    }

    private Credential getKeyAgreementCredential(Credential credential) throws KeyAgreementException {
        KeyAgreementProcessor keyAgreementProcessor = getKeyAgreementProcessor();
        Collection<KeyAgreementParameter> keyAgreementParameterCollection = SecurityConfigurationSupport
                .getGlobalEncryptionConfiguration()
                .getKeyAgreementConfigurations()
                .get(JCAConstants.KEY_ALGO_EC)
                .getParameters();
        KeyAgreementParameters keyAgreementParameters = new KeyAgreementParameters(keyAgreementParameterCollection);
        return keyAgreementProcessor.execute(credential, getKeyAgreementEncAlgorithm(), keyAgreementParameters);
    }

    private KeyAgreementProcessor getKeyAgreementProcessor() throws KeyAgreementException {
        String agreementMethod = getKeyEncryptionAgreementMethodAlgorithm();
        if (agreementMethod == null || (!EncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES.equals(agreementMethod)
                && !EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH.equals(agreementMethod)
                && !EncryptionConstants.ALGO_ID_KEYAGREEMENT_DH_EXPLICIT_KDF.equals(agreementMethod))
        ) {
            agreementMethod = EncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES;
        }
        return KeyAgreementSupport.getProcessor(agreementMethod);
    }
}
