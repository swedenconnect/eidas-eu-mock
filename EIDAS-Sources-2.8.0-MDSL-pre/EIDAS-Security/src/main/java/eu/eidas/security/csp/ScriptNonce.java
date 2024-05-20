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

package eu.eidas.security.csp;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class ScriptNonce {

    private static final char[] HEX_DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static final int HEX_CONVERT_SHIFT = 4;
    public static final int INT_OXFO = 0xF0;
    public static final int INT_OXF = 0x0F;

    public static String getNonce() throws NoSuchAlgorithmException {
        final SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
        final String randomNum = Integer.toString(secureRandom.nextInt());
        final MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] digest = sha.digest(randomNum.getBytes(Charset.forName("UTF-8")));
        return encodeHexString(digest, HEX_DIGITS_LOWER);
    }

    protected static String encodeHexString(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++, j += 2) {
            out[j] = toDigits[(0xF0 & data[i]) >>> HEX_CONVERT_SHIFT];
            out[j + 1] = toDigits[0x0F & data[i]];
        }
        return new String(out);
    }
}
