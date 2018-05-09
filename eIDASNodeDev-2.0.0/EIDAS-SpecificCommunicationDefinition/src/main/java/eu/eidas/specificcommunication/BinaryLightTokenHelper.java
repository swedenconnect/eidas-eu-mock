/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.specificcommunication;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.impl.LightToken;
import eu.eidas.auth.commons.tx.BinaryLightToken;
import eu.eidas.auth.commons.tx.LightTokenEncoder;
import eu.eidas.specificcommunication.exception.SpecificCommunicationException;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.UUID;

/**
 * Helper class for BinaryLightToken.
 *
 * @since 2.0
 */
public class BinaryLightTokenHelper {

    private BinaryLightTokenHelper(){}

    /**
     * Get the {@link BinaryLightToken} id from a {@link BinaryLightToken} Base64 encoded string.
     *
     * @param binaryLightTokenBase64 the {@link BinaryLightToken} Base64 encoded
     * @param secret secret for creating the digest
     * @param algorithm digest algorithm
     * @return the {@link BinaryLightToken} id
     * @throws SpecificCommunicationException if the {@code algorithm} is an invalid one
     */
    public static String getBinaryLightTokenId(final @Nonnull String binaryLightTokenBase64, final String secret, final String algorithm) throws SpecificCommunicationException {
        final String binaryLightTokenString = EidasStringUtil.decodeStringFromBase64(binaryLightTokenBase64);

        final BinaryLightToken binaryLightToken;
        try {
            binaryLightToken = LightTokenEncoder.decode(binaryLightTokenString.getBytes(), secret, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new SpecificCommunicationException(e);
        }

        return binaryLightToken.getToken().getId();
    }

    /**
     * Gets the {@link BinaryLightToken} Base64 encoded.
     *
     * @param httpServletRequest the http servlet request that contains the {@link BinaryLightToken} Base64 encoded.
     * @param parameterKey the parameter key of the {@link BinaryLightToken} Base64 encoded
     * @return
     */
    public static String getBinaryToken(final @Nonnull HttpServletRequest httpServletRequest, String parameterKey) {
        String binaryLightTokenBase64 = httpServletRequest.getParameter(parameterKey);
        if (null == binaryLightTokenBase64) {
            binaryLightTokenBase64 = (String) httpServletRequest.getAttribute(parameterKey);
        }
        return binaryLightTokenBase64;
    }

    /**
     * Encodes the {@code binaryLightToken} into Base64.
     *
     * @param binaryLightToken the {@link BinaryLightToken} to be created
     * @return the {@link BinaryLightToken} Base64 encoded
     */
    public static String encodeBinaryLightTokenBase64(final BinaryLightToken binaryLightToken) {
        final byte[] binaryLightTokenBytes = binaryLightToken.getTokenBytes();
        return EidasStringUtil.encodeToBase64(binaryLightTokenBytes);
    }

    /**
     * Creates an instance of {@link BinaryLightToken}.
     *
     * @param issuerName the issuer name
     * @param secret secret for creating the digest
     * @param algorithm digest algorithm
     * @return {@link BinaryLightToken}
     * @throws SpecificCommunicationException
     */
    public static BinaryLightToken createBinaryLightToken(String issuerName, String secret, String algorithm) throws SpecificCommunicationException {
        final LightToken lightToken = BinaryLightTokenHelper.createLightToken(issuerName);
        try {
            return LightTokenEncoder.encode(lightToken, secret, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new SpecificCommunicationException(e);
        }
    }

    /**
     * Creates an instance of {@link LightToken}
     *
     * @param issuerName the issuer name of the {@link LightToken}
     * @return an instance of {@link LightToken}
     */
    private static LightToken createLightToken(final String issuerName) {
        final String lightTokenId = UUID.randomUUID().toString();
        return new LightToken.Builder().id(lightTokenId)
                .issuer(issuerName)
                .createdOn(new DateTime(Calendar.getInstance().getTimeInMillis()))
                .build();
    }

}
