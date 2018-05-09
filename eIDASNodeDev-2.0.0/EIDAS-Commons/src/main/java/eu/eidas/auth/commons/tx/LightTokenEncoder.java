/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.exceptions.SecurityEIDASException;
import eu.eidas.auth.commons.light.ILightToken;
import eu.eidas.auth.commons.light.impl.AbstractLightToken;
import eu.eidas.auth.commons.light.impl.LightToken;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;

/**
 * LightTokenEncoder class is responsible to encode/decode LightTokens.
 *
 * @since 2.0.0
 */
public final class LightTokenEncoder {

    /** Logger. */
    private static final Logger log = LoggerFactory.getLogger(LightTokenEncoder.class);

    /** Maximum size for BASE64 encoded token, but applies for the unencoded one also */
    public static final int MAX_TOKEN_SIZE = 1024;

    /** Maximum number of parts for the tokenizer. */
    public static final int MAX_PARTS = 4;

    /**
     * This method created a BinaryLightToken from the supplied LightToken, with the supplied parameters.
     * The resulting BinaryLightToken will contain a bytearray to be used for reference in HTTP.
     * Format of the encoded token is: ISSUER/ID/CREATEDON/DIGEST. DIGEST is in BASE64.
     * @param token the logical to LightToken to be encoded
     * @param secret secret for creating the digest
     * @param algorithm digest algorithm (SHA256 is recommended)
     * @return Object encapsulation both Logical and encoded LightToken
     * @throws NoSuchAlgorithmException
     */
    @Nonnull
    public static BinaryLightToken encode(ILightToken token, String secret, String algorithm) throws NoSuchAlgorithmException {
        StringBuilder tokenString = new StringBuilder();
        byte[] digestBytes = calculateDigest(token, secret, algorithm);
        tokenString.append(token.getIssuer());
        tokenString.append(AbstractLightToken.SEPARATOR);
        tokenString.append(token.getId());
        tokenString.append(AbstractLightToken.SEPARATOR);
        tokenString.append(token.getFormattedCreatedOn());
        tokenString.append(AbstractLightToken.SEPARATOR);
        tokenString.append(EidasStringUtil.encodeToBase64(digestBytes));
        return new BinaryLightToken(token, tokenString.toString().getBytes());
    }

    /**
     * Same as decode, but it performs a safe BASE64 deocding as well
     * @see decode(@Nonnull byte[] bytes , @Nonnull String secret, @Nonnull String algorithm)
     */
    @Nonnull
    public static BinaryLightToken decodeBase64(@Nonnull byte[] bytes , @Nonnull String secret, @Nonnull String algorithm) throws NoSuchAlgorithmException, SecurityEIDASException {
        ILightToken retLightToken = null;
        if (bytes.length > MAX_TOKEN_SIZE) {
            log.error("Error parsing LightToken, size exceeds " + MAX_TOKEN_SIZE);
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                    "Error parsing LightToken, size exceeds " + MAX_TOKEN_SIZE);
        }
        String stringTokenEncoded = new String(bytes);
        return decode(EidasStringUtil.decodeBytesFromBase64(stringTokenEncoded), secret, algorithm);
    }


    /**
     * Decodes an array to a BinaryLightToken encapsulating the logical LightToken. This method is safe to process tokens coming in from the Web.
     * @param bytes array to be decoded
     * @param secret secret for checking the digest
     * @param algorithm digest algorithm (SHA256 is recommended)
     * @return
     * @throws NoSuchAlgorithmException
     * @throws SecurityEIDASException when token cannot be parsed
     */
    @Nonnull
    public static BinaryLightToken decode(@Nonnull byte[] bytes , @Nonnull String secret, @Nonnull String algorithm) throws NoSuchAlgorithmException, SecurityEIDASException {
        ILightToken retLightToken = null;
        if (bytes.length > MAX_TOKEN_SIZE) {
            log.error("Error parsing LightToken, size exceeds " + MAX_TOKEN_SIZE);
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                    "Error parsing LightToken, size exceeds " + MAX_TOKEN_SIZE);
        }
        String stringToken = new String(bytes);
        if (StringUtils.countMatches(stringToken, AbstractLightToken.SEPARATOR) == 3) {
            String[] parts = StringUtils.split(stringToken, AbstractLightToken.SEPARATOR, MAX_PARTS);
            DateTime createdOn = null;
            try {
                createdOn = AbstractLightToken.LIGHTTOKEN_DATE_FORMAT.parseDateTime(parts[2]);
            } catch (Exception e) {
                log.error("Invalid LightToken - createdOn timestamp parse failure");
                throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                        "LightToken createdOn timestamp parse failure");
            }
            // build token
            ILightToken lightToken;
            try {
                lightToken = new LightToken.Builder()
                        .issuer(parts[0])
                        .id(parts[1])
                        .createdOn(createdOn).build();
            } catch (Exception e) {
                log.error("Invalid LightToken - failure in builder : "+e.getMessage());
                throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                        "LightToken parse error");
            }
            byte[] digestBytes = calculateDigest(lightToken, secret, algorithm);
            byte[] incomingDigest = EidasStringUtil.decodeBytesFromBase64(parts[3]);
            if (Arrays.equals(digestBytes, incomingDigest)) {
                retLightToken = lightToken;
            } else {
                log.error("Invalid LightToken - digest failure");
                throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                        "LightToken digest failure");
            }
        } else {
            log.error("Invalid LightToken");
            throw new SecurityEIDASException(EidasErrors.get(EidasErrorKey.INVALID_LIGHT_TOKEN.errorCode()),
                    "LightToken parse error");
        }
        return new BinaryLightToken(retLightToken, bytes.clone());
    }

    /**
     * Calclates a Digest for the supplied LightToken
     * @param lightToken logical LightToken
     * @param secret secret for the digest
     * @param algorithm algorithm for the digest
     * @return
     * @throws NoSuchAlgorithmException
     */
    protected static byte[] calculateDigest(@Nonnull ILightToken lightToken, @Nonnull String secret, @Nonnull String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        md.reset();
        md.update(lightToken.getId().getBytes());
        md.update(AbstractLightToken.SEPARATOR.getBytes());
        md.update(lightToken.getIssuer().getBytes());
        md.update(AbstractLightToken.SEPARATOR.getBytes());
        md.update(lightToken.getFormattedCreatedOn().getBytes());
        md.update(AbstractLightToken.SEPARATOR.getBytes());
        md.update(secret.getBytes() );
        return md.digest();
    }

}
