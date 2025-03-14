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

package eu.eidas.auth.engine;

import eu.eidas.RecommendedSecurityProviders;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;

/**
 * Test class to test the signature compatibility between 2.6 and 2.7 RSASSA-PSS on BouncyCastle
 * @since 2.7
 */
public class RSASigningProviderTest {

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


    private static final Logger LOGGER = LoggerFactory.getLogger(RSASigningProviderTest.class);
    private static final String BOUNCY_CASTLE_PROVIDER_NAME = "BC";

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setupClass() {
        RecommendedSecurityProviders.setupRecommendedSecurityProviders();
        OpenSamlHelper.initialize();
    }

    /**
     * Test sign and verify a message using the specific BC algorithm name "SHA256withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyBCSha256() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha256withRSAandMGF1 = Signature.getInstance("SHA256withRSAandMGF1");

        byte[] signatureValue = sign(sha256withRSAandMGF1, pair.getPrivate(), message);

        Assert.assertTrue(verify(sha256withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the specific BC algorithm name "SHA384withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyBCSha384() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha384withRSAandMGF1 = Signature.getInstance("SHA384withRSAandMGF1");

        byte[] signatureValue = sign(sha384withRSAandMGF1, pair.getPrivate(), message);

        Assert.assertTrue(verify(sha384withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the specific BC algorithm name "SHA512withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyBCSha512() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha512withRSAandMGF1 = Signature.getInstance("SHA512withRSAandMGF1");

        byte[] signatureValue = sign(sha512withRSAandMGF1, pair.getPrivate(), message);

        Assert.assertTrue(verify(sha512withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the RSASSA-PSS and SHA-256 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyPssSHA256() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-256";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA256,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        byte[] signatureValue = sign(rsaSsaPss, pair.getPrivate(), message);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the RSASSA-PSS and SHA-384 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyPssSHA384() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-384";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA384,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        byte[] signatureValue = sign(rsaSsaPss, pair.getPrivate(), message);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the RSASSA-PSS and SHA-512 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyPssSHA512() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-512";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA512,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        byte[] signatureValue = sign(rsaSsaPss, pair.getPrivate(), message);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign and verify a message using the RSASSA-PSS and SHA-512 Digest and SHA-1 MGF Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyPssSHA512MGFSHA1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-512";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA1,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        byte[] signatureValue = sign(rsaSsaPss, pair.getPrivate(), message);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA1withRSAandMGF1"
     * and verify using the RSASSA-PSS with SHA-1 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyPssSHA1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature Sha1withRSAandMGF1 = Signature.getInstance("SHA1withRSAandMGF1");

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(Sha1withRSAandMGF1, privateKey, message);

        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        String messageDigestJCEName = "SHA-1";
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA1,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA256withRSAandMGF1"
     * and verify using the RSASSA-PSS with SHA-256 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyPssSHA256() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha256withRSAandMGF1 = Signature.getInstance("SHA256withRSAandMGF1");

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(sha256withRSAandMGF1, privateKey, message);

        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        String messageDigestJCEName = "SHA-256";
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA256,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA384withRSAandMGF1"
     * and verify using the RSASSA-PSS with SHA-384 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyPssSHA384() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha384withRSAandMGF1 = Signature.getInstance("SHA384withRSAandMGF1");

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(sha384withRSAandMGF1, privateKey, message);

        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        String messageDigestJCEName = "SHA-384";
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA384,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA512withRSAandMGF1"
     * and verify using the RSASSA-PSS with SHA-512 Algorithm Parameters
     * <p>
     * Must succeed.
     */
    @Test
    public void signBCVerifyPssSHA512() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        Signature sha512withRSAandMGF1 = Signature.getInstance("SHA512withRSAandMGF1");

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(sha512withRSAandMGF1, privateKey, message);

        final String messageDigestJCEName = "SHA-512";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA512,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        Assert.assertTrue(verify(rsaSsaPss, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the RSASSA-PSS with SHA-1 Algorithm Parameters
     * and verify using the specific BC algorithm name "SHA1withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyBCSHA1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-1";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA1,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(rsaSsaPss, privateKey, message);

        Signature sha1withRSAandMGF1 = Signature.getInstance("SHA1withRSAandMGF1");

        Assert.assertTrue(verify(sha1withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the RSASSA-PSS with SHA-256 Algorithm Parameters
     * and verify using the specific BC algorithm name "SHA256withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyBCSHA256() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-256";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA256,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(rsaSsaPss, privateKey, message);

        Signature sha256withRSAandMGF1 = Signature.getInstance("SHA256withRSAandMGF1");

        Assert.assertTrue(verify(sha256withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the RSASSA-PSS with SHA-384 Algorithm Parameters
     * and verify using the specific BC algorithm name "SHA384withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyBCSHA384() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-384";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA384,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(rsaSsaPss, privateKey, message);

        Signature sha384withRSAandMGF1 = Signature.getInstance("SHA384withRSAandMGF1");

        Assert.assertTrue(verify(sha384withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the RSASSA-PSS with SHA-512 Algorithm Parameters
     * and verify using the specific BC algorithm name "SHA512withRSAandMGF1"
     * <p>
     * Must succeed.
     */
    @Test
    public void signPssVerifyBCSHA512() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        byte[] message = "This is a message".getBytes();
        KeyPair pair = generateRSAKeyPair(2048);

        final String messageDigestJCEName = "SHA-512";
        final Signature rsaSsaPss = Signature.getInstance("RSASSA-PSS");
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA512,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);
        rsaSsaPss.setParameter(pssParameterSpec);

        PrivateKey privateKey = pair.getPrivate();
        byte[] signatureValue = sign(rsaSsaPss, privateKey, message);

        Signature sha512withRSAandMGF1 = Signature.getInstance("SHA512withRSAandMGF1");

        Assert.assertTrue(verify(sha512withRSAandMGF1, pair.getPublic(), message, signatureValue));
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA1withRSAandMGF1"
     * with SHA-512 Digest and SHA-1 MGF Algorithm Parameters
     * <p>
     * Must fail.
     */
    @Test
    public void signBCVerifyPssSHA1forceDigest() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        exception.expect(InvalidAlgorithmParameterException.class);
        exception.expectMessage("parameter must be using SHA-1");

        String messageDigestJCEName = "SHA-512";
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA1,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);


        Signature Sha1withRSAandMGF1 = Signature.getInstance("SHA1withRSAandMGF1");
        Sha1withRSAandMGF1.setParameter(pssParameterSpec);
    }

    /**
     * Test sign a message using the specific BC algorithm name "SHA512withRSAandMGF1"
     * with SHA-512 Digest and SHA-1 MGF Algorithm Parameters
     * <p>
     * Must fail.
     */
    @Test
    public void signBCVerifyPssSHA1forceMgf1() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException {
        exception.expect(InvalidAlgorithmParameterException.class);
        exception.expectMessage("digest algorithm for MGF should be the same as for PSS parameters.");

        String messageDigestJCEName = "SHA-512";
        final PSSParameterSpec pssParameterSpec = new PSSParameterSpec(
                messageDigestJCEName,
                "MGF1",
                MGF1ParameterSpec.SHA1,
                MessageDigest.getInstance(messageDigestJCEName).getDigestLength(),
                PSSParameterSpec.TRAILER_FIELD_BC);


        Signature sha512withRSAandMGF1 = Signature.getInstance("SHA512withRSAandMGF1");
        sha512withRSAandMGF1.setParameter(pssParameterSpec);
    }

    private KeyPair generateRSAKeyPair(int keyLength) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(keyLength);
        KeyPair pair = keyPairGen.generateKeyPair();
        return pair;
    }

    private byte[] sign(Signature securityProviderImplementationForSigning, PrivateKey privateKey, byte[] message) throws InvalidKeyException, SignatureException {
        securityProviderImplementationForSigning.initSign(privateKey);
        securityProviderImplementationForSigning.update(message);
        return securityProviderImplementationForSigning.sign();
    }

    private boolean verify(Signature securityProviderImplementationForSigning, PublicKey publicKey, byte[] message, byte[] signatureValue) throws InvalidKeyException, SignatureException {
        securityProviderImplementationForSigning.initVerify(publicKey);
        securityProviderImplementationForSigning.update(message);
        return securityProviderImplementationForSigning.verify(signatureValue);
    }
}
