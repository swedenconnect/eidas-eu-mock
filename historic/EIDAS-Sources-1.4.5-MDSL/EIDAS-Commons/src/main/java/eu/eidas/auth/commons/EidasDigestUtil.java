package eu.eidas.auth.commons;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.crypto.Digest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;

/**
 * Message Digest Utility class (hashing).
 *
 * @since 1.1
 */
public final class EidasDigestUtil {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EidasDigestUtil.class);

    private static final String DEFAULT_HASH_DIGEST_CLASS = "org.bouncycastle.crypto.digests.SHA512Digest";

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-512";

    private EidasDigestUtil() {
    }

    /**
     * Performs the given hash using the given {@link MessageDigest} algorithm name and provider name (optional).
     * <p/>
     * The default algorithm {@code "SHA-512"} is used.
     * <p/>
     * The default JVM security providers are queried to perform the requested algorithm.
     *
     * @param bytes the bytes to digest (hash)
     * @param algorithm the algorithm name e.g. {@code "SHA-512"}. If {@code null}, the default value {@code "SHA-512"}
     * is used.
     * @param provider the provider name (can be {@code null}).
     * @return the result of the hashing computation
     */
    @Nonnull
    public static byte[] hash(@Nonnull byte[] bytes) {
        return hash(bytes, null, null);
    }

    /**
     * Performs the given hash using the given {@link MessageDigest} algorithm name and provider name (optional).
     * <p/>
     * If a {@code null} algorithm name is provided, the default value {@code "SHA-512"} is used.
     * <p/>
     * If a {@code null} provider name is given, the default JVM security providers are queried to perform the requested
     * algorithm.
     *
     * @param bytes the bytes to digest (hash)
     * @param algorithm the algorithm name e.g. {@code "SHA-512"}. If {@code null}, the default value {@code "SHA-512"}
     * is used.
     * @param provider the provider name (can be {@code null}).
     * @return the result of the hashing computation
     */
    @Nonnull
    public static byte[] hash(@Nonnull byte[] bytes, @Nullable String algorithm, @Nullable String provider) {

        String algorithmName;
        if (StringUtils.isEmpty(algorithm)) {
            algorithmName = DEFAULT_DIGEST_ALGORITHM;
        } else {
            algorithmName = algorithm;
        }

        try {
            MessageDigest messageDigest;
            if (null == provider) {
                messageDigest = MessageDigest.getInstance(algorithmName);
            } else {
                messageDigest = MessageDigest.getInstance(algorithmName, provider);
            }
            return messageDigest.digest(bytes);
        } catch (NoSuchAlgorithmException nsae) {
            // For all those exceptions that could be thrown, we always log it and
            // thrown an InternalErrorEIDASException.
            LOG.info(EidasErrorKey.HASH_ERROR.errorMessage() + ": " + nsae, nsae);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.HASH_ERROR.errorCode()),
                                                  EidasErrors.get(EidasErrorKey.HASH_ERROR.errorMessage()), nsae);
        } catch (NoSuchProviderException nspe) {
            // For all those exceptions that could be thrown, we always log it and
            // thrown an InternalErrorEIDASException.
            LOG.info(EidasErrorKey.HASH_ERROR.errorMessage() + ": " + nspe, nspe);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.HASH_ERROR.errorCode()),
                                                  EidasErrors.get(EidasErrorKey.HASH_ERROR.errorMessage()), nspe);
        }
    }

    /**
     * Hashes a SAML token. Throws an InternalErrorEIDASException runtime exception if the Cryptographic Engine fails.
     *
     * @param samlToken the SAML Token to be hashed.
     * @return byte[] with the hashed SAML Token.
     */
    @Nonnull
    public static byte[] hashPersonalToken(@Nonnull byte[] samlToken) {
        String className = EidasErrors.get(EIDASValues.HASH_DIGEST_CLASS.toString());
        return hashPersonalToken(samlToken, className);
    }

    /**
     * @deprecated This implementation is bound to a concrete BouncyCastle implementation instead of using the standard
     * {@link java.security.MessageDigest} and the standardized algorithm names. Use {@link #hash(byte[], String,
     * String)} instead.
     */
    @Nonnull
    @Deprecated
    public static byte[] hashPersonalToken(@Nonnull byte[] samlToken, String className) {
        try {
            String hashClassName = className;
            if (null == hashClassName || hashClassName.isEmpty()) {
                hashClassName = DEFAULT_HASH_DIGEST_CLASS;
            }
            final Digest digest = (Digest) Class.forName(hashClassName).getConstructor().newInstance();
            digest.update(samlToken, 0, samlToken.length);

            final int retLength = digest.getDigestSize();
            final byte[] ret = new byte[retLength];

            digest.doFinal(ret, 0);
            return ret;
        } catch (final Exception e) {
            // For all those exceptions that could be thrown, we always log it and
            // thrown an InternalErrorEIDASException.
            LOG.info(EidasErrorKey.HASH_ERROR.errorMessage(), e);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.HASH_ERROR.errorCode()),
                                                  EidasErrors.get(EidasErrorKey.HASH_ERROR.errorMessage()), e);
        }
    }
}
