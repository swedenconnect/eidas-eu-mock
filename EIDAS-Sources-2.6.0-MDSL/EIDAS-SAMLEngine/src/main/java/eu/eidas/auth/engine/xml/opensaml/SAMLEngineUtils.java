/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.auth.engine.xml.opensaml;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.impl.EidasSamlBinding;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.apache.xml.security.algorithms.MessageDigestAlgorithm;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.SignedInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.impl.SignatureImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Optional;

/**
 * The Class SAMLEngineUtils.
 */
public final class SAMLEngineUtils {

    /**
     * The Constant SHA_512.
     */
    public static final String SHA_512 = "SHA-512";

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SAMLEngineUtils.class);

    /**
     * Encode value with an specific algorithm.
     *
     * @param value the value
     * @param alg the algorithm
     * @return the string
     * @throws EIDASSAMLEngineException the EIDASSAML engine exception
     * @deprecated use {@link eu.eidas.auth.commons.EidasDigestUtil} instead.
     */
    @Deprecated
    // TODO rename this method as this performs a hash not an encoding
    public static String encode(final String value, final String alg) throws EIDASSAMLEngineException {
        LOG.debug("Encode value with  " + alg + " algorithm.");

        final StringBuilder hash = new StringBuilder("");
        try {
            MessageDigest msgDig;
            msgDig = MessageDigest.getInstance(alg);

            msgDig.update(EidasStringUtil.getBytes(value));
            final byte[] digest = msgDig.digest();

            final int signedByte = 0xff;
            for (byte aux : digest) {
                final int byt = aux & signedByte;
                if (Integer.toHexString(byt).length() == 1) {
                    hash.append('0');
                }
                hash.append(Integer.toHexString(byt));
            }

        } catch (NoSuchAlgorithmException e) {
            LOG.info("ERROR : NoSuchAlgorithmException: " + alg);
            throw new EIDASSAMLEngineException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                               EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }

        return hash.toString();
    }

    /**
     * Generate NCName.
     *
     * @return the string
     */
    public static String generateNCName() {
        return SecureRandomXmlIdGenerator.INSTANCE.generateIdentifier();
    }

    /**
     * Method that returns the current time.
     *
     * @deprecated since 1.4.
     * The current time should be obtained as a parameter originated from {@link eu.eidas.auth.engine.ProtocolEngine}
     * and not instantiating it directly.
     *
     * @return the current time
     */
    @Deprecated
    public static DateTime getCurrentTime() {
        return new DateTime();
    }

    /**
     * Instantiates a new SAML engine utilities.
     */
    private SAMLEngineUtils() {
    }

    private static final String ALLOWED_METADATA_SCHEMES[] = {"https://", "http://"};

    /**
     * validates the issuer to be an url of a known scheme
     *
     * @param value the issuer
     * @return the validated value
     * @throws EIDASSAMLEngineException if the issuer can not be retrieved
     */
    public static String getValidIssuerValue(String value) throws EIDASSAMLEngineException {
        if (value != null) {
            for (String scheme : ALLOWED_METADATA_SCHEMES) {
                if (value.toLowerCase(Locale.ENGLISH).startsWith(scheme)) {
                    return value;
                }
            }
        }
        LOG.error("CONFIGURATION ERROR - Issuer error, configuration entry " + value
                          + " is not valid (HTTP and HTTPS are the only metadata scheme are supported)");
        throw new EIDASSAMLEngineException(EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorCode(),
                                           EidasErrorKey.SAML_ENGINE_INVALID_METADATA.errorMessage());
    }

    /**
     * @param bindingUri SAML binding
     * @return http method(either POST or GET)
     */
    public static String getBindingMethod(String bindingUri) {
        return EidasSamlBinding.toNameNotEmpty(bindingUri);
    }

    public static boolean isErrorSamlResponse(Response response) {
        return response != null && !StatusCode.SUCCESS.equals(response.getStatus().getStatusCode().getValue());
    }

    public static String getConsentValue(String responseConsent) {
        SAMLConsent consent = SAMLConsent.fromValue(responseConsent);
        if (consent != null) {
            return consent.getValue();
        }
        return SAMLConsent.UNSPECIFIED.getValue();
    }

    /**
     * Get the digest method algorithm URI used for the given Signature
     * @param signature the signature
     * @return the digest method algorithm URI
     * @throws EIDASSAMLEngineException if no digest method found or if an error occurs.
     */
    public static String getDigestMethodAlgorithm(final Signature signature) throws EIDASSAMLEngineException {
        XMLSignature xmlSignature = ((SignatureImpl) signature).getXMLSignature();
        if (xmlSignature != null) {
            SignedInfo signedInfo = xmlSignature.getSignedInfo();
            Optional<MessageDigestAlgorithm> messageDigestAlgorithm = getMessageDigestAlgorithm(signedInfo);
            if (messageDigestAlgorithm.isPresent()) {
                return messageDigestAlgorithm.get().getAlgorithmURI();
            }
        }
        LOG.error("ERROR : Invalid signature, digest method is missing");
        throw new EIDASSAMLEngineException(EidasErrorKey.INVALID_SIGNATURE_ALGORITHM.errorCode());
    }

    private static Optional<MessageDigestAlgorithm> getMessageDigestAlgorithm(SignedInfo signedInfo) {
        try {
            Optional<Reference> reference = getSignedInfoReference(signedInfo);
            if (reference.isPresent()) {
                return Optional.ofNullable(reference.get().getMessageDigestAlgorithm());
            }
        } catch (XMLSecurityException e) {
            LOG.error("ERROR : Invalid signature, malformed Reference");
        }
        return Optional.empty();
    }

    private static Optional<Reference> getSignedInfoReference(SignedInfo signedInfo) {
        if (signedInfo != null) {
            try {
                if (signedInfo != null && signedInfo.getLength() != 0) {
                    return Optional.ofNullable(signedInfo.item(0));
                }
            } catch (XMLSecurityException e) {
                LOG.error("ERROR : Invalid signature, malformed SignedInfo");
            }
        }
        return Optional.empty();
    }
}
