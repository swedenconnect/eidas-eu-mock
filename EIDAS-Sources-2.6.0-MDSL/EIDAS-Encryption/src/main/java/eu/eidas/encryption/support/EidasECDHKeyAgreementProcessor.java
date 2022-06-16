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

package eu.eidas.encryption.support;

import net.shibboleth.utilities.java.support.logic.Constraint;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.crypto.JCAConstants;
import org.opensaml.xmlsec.agreement.KeyAgreementException;
import org.opensaml.xmlsec.agreement.KeyAgreementParameters;
import org.opensaml.xmlsec.agreement.impl.ECDHKeyAgreementProcessor;

import javax.annotation.Nonnull;
import javax.crypto.KeyAgreement;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * Alternative implementation of {@link org.opensaml.xmlsec.agreement.impl.ECDHKeyAgreementProcessor}
 * which performs Elliptic Curve Diffie-Hellman (ECDH)
 * Ephemeral-Static Mode key agreement as defined in XML Encryption 1.1.
 *
 * And which doesn't discriminate on key type to allow PKCS11 keys.
 */
public class EidasECDHKeyAgreementProcessor extends ECDHKeyAgreementProcessor {

    /** {@inheritDoc} */
    protected byte[] generateAgreementSecret(@Nonnull final Credential publicCredential,
            @Nonnull final Credential privateCredential, @Nonnull final KeyAgreementParameters parameters)
            throws KeyAgreementException {

        Key publicKey = publicCredential.getPublicKey();
        if (publicKey == null || !JCAConstants.KEY_ALGO_EC.equalsIgnoreCase(publicKey.getAlgorithm())) {
            throw new KeyAgreementException("Public credential's public key is not an EC key");
        }
        Key privateKey = privateCredential.getPrivateKey();
        if (privateKey == null || !JCAConstants.KEY_ALGO_EC.equalsIgnoreCase(privateKey.getAlgorithm())) {
            throw new KeyAgreementException("Private credential's private key is not an EC key");
        }

        try {
            return performKeyAgreement(publicKey, privateKey);
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            throw new KeyAgreementException("Error generating secret from public and private EC keys", e);
        }
    }

    @Nonnull
    private static byte[] performKeyAgreement(@Nonnull Key publicKey, @Nonnull Key privateKey)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Constraint.isNotNull(publicKey, "publicKey was null");
        Constraint.isNotNull(privateKey, "privateKey was null");
        KeyAgreement keyAgreement = KeyAgreement.getInstance(JCAConstants.KEY_AGREEMENT_ECDH);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(publicKey, true);
        return keyAgreement.generateSecret();
    }

}
