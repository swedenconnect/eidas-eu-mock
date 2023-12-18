/*
 * Copyright (c) 2023 by European Commission
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

package eu.eidas.encryption.support;

import com.google.common.base.Strings;
import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.encryption.EncryptionMethod;
import org.apache.xml.security.encryption.XMLCipher;
import org.apache.xml.security.encryption.XMLCipherInput;
import org.apache.xml.security.encryption.XMLEncryptionException;
import org.apache.xml.security.utils.EncryptionConstants;
import org.opensaml.saml.saml2.encryption.Decrypter;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.DecryptionParameters;
import org.opensaml.xmlsec.encryption.EncryptedKey;
import org.opensaml.xmlsec.encryption.support.DecryptionException;
import org.opensaml.xmlsec.encryption.support.EncryptedKeyResolver;
import org.opensaml.xmlsec.keyinfo.KeyInfoCredentialResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import sun.security.rsa.RSAPadding;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.util.Collection;

/**
 * Override of the opensaml Decrypter
 * to be able to use different JCA Providers for keyDecryption and DataDecryption.
 */
public class Pkcs11Decrypter extends Decrypter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Pkcs11Decrypter.class);

    public Pkcs11Decrypter(DecryptionParameters params) {
        super(params);
    }

    public Pkcs11Decrypter(KeyInfoCredentialResolver newResolver, KeyInfoCredentialResolver newKEKResolver, EncryptedKeyResolver newEncKeyResolver) {
        super(newResolver, newKEKResolver, newEncKeyResolver);
    }

    public Pkcs11Decrypter(KeyInfoCredentialResolver newResolver, KeyInfoCredentialResolver newKEKResolver, EncryptedKeyResolver newEncKeyResolver, Collection<String> whitelistAlgos, Collection<String> blacklistAlgos) {
        super(newResolver, newKEKResolver, newEncKeyResolver, whitelistAlgos, blacklistAlgos);
    }

    @Nonnull
    @SuppressWarnings("squid:S1872")
    public Key decryptKey(@Nonnull EncryptedKey encryptedKey, @Nonnull String algorithm, @Nonnull Key kek) throws DecryptionException {
        if (kek == null) {
            LOGGER.error("Data encryption key was null");
            throw new IllegalArgumentException("Data encryption key cannot be null");
        } else if (Strings.isNullOrEmpty(algorithm)) {
            LOGGER.error("Algorithm of encrypted key not supplied, key decryption cannot proceed");
            throw new DecryptionException("Algorithm of encrypted key not supplied, key decryption cannot proceed");
        } else {
            if (!"sun.security.pkcs11.P11Key$P11PrivateKey".equals(kek.getClass().getName())) {
                return super.decryptKey(encryptedKey, algorithm, kek);
            }
            return decryptP11Key(encryptedKey, algorithm, kek);
        }
    }

    protected Key decryptP11Key(@Nonnull EncryptedKey encryptedKey, @Nonnull String algorithm, @Nonnull Key kek)
            throws DecryptionException {
        if (JCAConstants.KEY_ALGO_RSA.equalsIgnoreCase(kek.getAlgorithm())) {
            return this.decryptRSAKey(encryptedKey, algorithm, kek);
        } else if (JCAConstants.KEY_ALGO_EC.equalsIgnoreCase(kek.getAlgorithm())) {
            return this.decryptECKey(encryptedKey, algorithm, kek);
        }
        return super.decryptKey(encryptedKey, algorithm, kek);
    }

    @Nonnull
    protected Key decryptECKey(@Nonnull EncryptedKey encryptedKey, @Nonnull String algorithm, @Nonnull Key kek) throws DecryptionException {
        return super.decryptKey(encryptedKey, algorithm, kek);
    }

    @Nonnull
    protected Key decryptRSAKey(@Nonnull EncryptedKey encryptedKey, @Nonnull String algorithm, @Nonnull Key kek) throws DecryptionException {
        if (Strings.isNullOrEmpty(algorithm)) {
            LOGGER.error("Algorithm of encrypted key not supplied, key decryption cannot proceed.");
            throw new DecryptionException("Algorithm of encrypted key not supplied, key decryption cannot proceed.");
        } else {
            this.validateAlgorithms(encryptedKey);

            try {
                this.checkAndMarshall(encryptedKey);
            } catch (DecryptionException var12) {
                LOGGER.error("Error marshalling EncryptedKey for decryption", var12);
                throw var12;
            }

            this.preProcessEncryptedKey(encryptedKey, algorithm, kek);

            XMLCipher xmlCipher;
            try {
                if (this.getJCAProviderName() != null) {
                    xmlCipher = XMLCipher.getProviderInstance(this.getJCAProviderName());
                } else {
                    xmlCipher = XMLCipher.getInstance();
                }

                xmlCipher.init(Cipher.UNWRAP_MODE, kek);
            } catch (XMLEncryptionException var11) {
                LOGGER.error("Error initialzing cipher instance on key decryption", var11);
                throw new DecryptionException("Error initialzing cipher instance on key decryption", var11);
            }

            org.apache.xml.security.encryption.EncryptedKey encKey;
            try {
                Element targetElement = encryptedKey.getDOM();
                encKey = xmlCipher.loadEncryptedKey(targetElement.getOwnerDocument(), targetElement);
            } catch (XMLEncryptionException var10) {
                LOGGER.error("Error when loading library native encrypted key representation", var10);
                throw new DecryptionException("Error when loading library native encrypted key representation", var10);
            }

            try {
                Key key = xmlCipher.decryptKey(encKey, algorithm);
                if (key == null) {
                    throw new DecryptionException("Key could not be decrypted");
                } else {
                    return key;
                }
            } catch (XMLEncryptionException e) {
                LOGGER.debug("Failed to decrypt transport key with RSA/ECB/OAEPPadding", e);
                LOGGER.debug("Trying to decrypt transport key with RSA/ECB/NoPadding + software unpadding");
                try {
                    Key key = this.decryptRsaOaepDecryptKey(encKey, algorithm, kek);
                    if (key == null) {
                        throw new DecryptionException("Key could not be decrypted");
                    }
                    return key;
                } catch (XMLEncryptionException ex) {
                    LOGGER.warn("Error with classic decryption of encrypted key", e);
                    LOGGER.warn("Error with custom decryption of encrypted key", ex);
                    throw new DecryptionException("Error decrypting encrypted key", ex);
                }
            } catch (Exception e) {
                throw new DecryptionException("Probable runtime exception on decryption:" + e.getMessage(), e);
            }
        }
    }

    /**
     * Performs the actual key decryption of an RSA ECB OAEPPadded key.
     *
     * @param encryptedKey the encrypted key
     * @param algorithm the algorithm
     * @param kek the private key
     * @return the decrypted key inside an instance of {@link SecretKeySpec}
     * @throws XMLEncryptionException when decryption of the key could not be done
     */
    protected Key decryptRsaOaepDecryptKey(org.apache.xml.security.encryption.EncryptedKey encryptedKey, String algorithm, Key kek)
            throws XMLEncryptionException {

        byte[] encryptedKeyBytes = (new XMLCipherInput(encryptedKey)).getBytes();

        try {
            byte[] dataDecryptionKeyOaepMGF1PaddedBytes = decryptsWithRsaEcbNoPadding(kek, encryptedKeyBytes);

            dataDecryptionKeyOaepMGF1PaddedBytes = prefixZerosIfNeeded(dataDecryptionKeyOaepMGF1PaddedBytes);

            final EncryptionMethod encryptionMethod = encryptedKey.getEncryptionMethod();
            byte[] dataDecryptionKey = unpadWithOaepMgf1(encryptionMethod, dataDecryptionKeyOaepMGF1PaddedBytes);

            final String dataDecryptionJceKeyAlgorithm = JCEMapper.getJCEKeyAlgorithmFromURI(algorithm);

            return new SecretKeySpec(dataDecryptionKey, dataDecryptionJceKeyAlgorithm);
        }
        catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | InvalidAlgorithmParameterException
                | BadPaddingException  e) {
            throw new XMLEncryptionException(e);
        }
    }

    private byte[] decryptsWithRsaEcbNoPadding(Key kek, byte[] encryptedKeyBytes) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, kek);
        byte[] dataDecryptionKeyOaepMGF1PaddedBytes = cipher.doFinal(encryptedKeyBytes);
        return dataDecryptionKeyOaepMGF1PaddedBytes;
    }

    private byte[] unpadWithOaepMgf1(EncryptionMethod encMethod, byte[] dataDecryptionKeyOaepMGF1PaddedBytes) throws InvalidKeyException, InvalidAlgorithmParameterException, BadPaddingException {
        OAEPParameterSpec parameterSpec = constructOAEPParameters(
                encMethod.getAlgorithm(),
                encMethod.getDigestAlgorithm(),
                encMethod.getMGFAlgorithm(),
                encMethod.getOAEPparams());
        RSAPadding padding = RSAPadding.getInstance(RSAPadding.PAD_OAEP_MGF1, dataDecryptionKeyOaepMGF1PaddedBytes.length, new SecureRandom(), parameterSpec);
        byte[] dataDecryptionKey = padding.unpad(dataDecryptionKeyOaepMGF1PaddedBytes);
        return dataDecryptionKey;
    }

    /**
     * Prefixes with the right amount of zeros to fill up 8 bytes.
     *
     * @param dataDecryptionKeyOaepMGF1PaddedBytes the bytes that might be prefixed
     * @return the dataDecryptionKeyOaepMGF1PaddedBytes with the prefixed zeros
     */
    private byte[] prefixZerosIfNeeded(byte[] dataDecryptionKeyOaepMGF1PaddedBytes) {
        int strippedAmount = dataDecryptionKeyOaepMGF1PaddedBytes.length % 8;
        int roundUpPaddedSize = (dataDecryptionKeyOaepMGF1PaddedBytes.length + (8 - strippedAmount));
        if (strippedAmount != 0) {
            byte[] prefixedZerosDataDecryptionKeyOaepMGF1PaddedBytes = new byte[roundUpPaddedSize];
            System.arraycopy(dataDecryptionKeyOaepMGF1PaddedBytes, 0, prefixedZerosDataDecryptionKeyOaepMGF1PaddedBytes, prefixedZerosDataDecryptionKeyOaepMGF1PaddedBytes.length - dataDecryptionKeyOaepMGF1PaddedBytes.length, dataDecryptionKeyOaepMGF1PaddedBytes.length);
            return prefixedZerosDataDecryptionKeyOaepMGF1PaddedBytes;
        } else {
            return dataDecryptionKeyOaepMGF1PaddedBytes;
        }
    }

    /**
     * Construct an OAEPParameterSpec object from the given parameters
     * @param encryptionAlgorithm the encryption algorithm
     * @param digestAlgorithm the digest algorithm
     * @param mgfAlgorithm the mask generation function algorithm
     * @param oaepParams the oaep parameters
     * @return the instance of {@link OAEPParameterSpec}
     */
    private OAEPParameterSpec constructOAEPParameters(
            String encryptionAlgorithm,
            String digestAlgorithm,
            String mgfAlgorithm,
            byte[] oaepParams) {

        String jceDigestAlgorithm = "SHA-1";
        if (digestAlgorithm != null) {
            jceDigestAlgorithm = JCEMapper.translateURItoJCEID(digestAlgorithm);
        }

        PSource.PSpecified pSource = PSource.PSpecified.DEFAULT;
        if (oaepParams != null) {
            pSource = new PSource.PSpecified(oaepParams);
        }

        MGF1ParameterSpec mgfParameterSpec = new MGF1ParameterSpec("SHA-1");
        if (XMLCipher.RSA_OAEP_11.equals(encryptionAlgorithm)) {
            if (EncryptionConstants.MGF1_SHA224.equals(mgfAlgorithm)) {
                mgfParameterSpec = new MGF1ParameterSpec("SHA-224");
            }
            else if (EncryptionConstants.MGF1_SHA256.equals(mgfAlgorithm)) {
                mgfParameterSpec = new MGF1ParameterSpec("SHA-256");
            }
            else if (EncryptionConstants.MGF1_SHA384.equals(mgfAlgorithm)) {
                mgfParameterSpec = new MGF1ParameterSpec("SHA-384");
            }
            else if (EncryptionConstants.MGF1_SHA512.equals(mgfAlgorithm)) {
                mgfParameterSpec = new MGF1ParameterSpec("SHA-512");
            }
        }
        return new OAEPParameterSpec(jceDigestAlgorithm, "MGF1", mgfParameterSpec, pSource);
    }

}
