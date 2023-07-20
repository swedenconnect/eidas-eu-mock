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
package eu.eidas.auth.commons;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

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

    private static final String DEFAULT_DIGEST_ALGORITHM = "SHA-512";

    private EidasDigestUtil() {
    }

    /**
     * Performs the given hash using the given {@link MessageDigest} algorithm name and provider name (optional).
     * <p>
     * The default algorithm {@code "SHA-512"} is used.
     * <p>
     * The default JVM security providers are queried to perform the requested algorithm.
     *
     * @param bytes the bytes to digest (hash)
     * @return the result of the hashing computation
     */
    @Nonnull
    public static byte[] hash(@Nonnull byte[] bytes) {
        return hash(bytes, null, null);
    }

    /**
     * Performs the given hash using the given {@link MessageDigest} algorithm name and provider name (optional).
     * <p>
     * If a {@code null} algorithm name is provided, the default value {@code "SHA-512"} is used.
     * <p>
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
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            // For all those exceptions that could be thrown, we always log it and
            // thrown an InternalErrorEIDASException.
            LOG.info(EidasErrorKey.HASH_ERROR.errorMessage() + ": " + e, e);
            throw new InternalErrorEIDASException(EidasErrors.get(EidasErrorKey.HASH_ERROR.errorCode()),
                                                  EidasErrors.get(EidasErrorKey.HASH_ERROR.errorMessage()), e);
        }
    }

    /**
     * Hashes a SAML token with SHA-512. Throws an InternalErrorEIDASException runtime exception if the Cryptographic Engine fails.
     *
     * @param samlToken the SAML Token to be hashed.
     * @return byte[] with the hashed SAML Token.
     */
    @Nonnull
    public static byte[] hashPersonalToken(@Nonnull byte[] samlToken) {
        return hash(samlToken, DEFAULT_DIGEST_ALGORITHM, null);
    }
    
}
