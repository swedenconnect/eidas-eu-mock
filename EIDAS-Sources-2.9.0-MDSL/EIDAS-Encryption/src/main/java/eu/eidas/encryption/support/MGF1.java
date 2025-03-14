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
package eu.eidas.encryption.support;

import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class implements the MGF1 mask generation function defined in PKCS#1
 * v2.2 B.2.1 (https://tools.ietf.org/html/rfc8017#appendix-B.2.1). A mask
 * generation function takes an octet string of variable length and a
 * desired output length as input and outputs an octet string of the
 * desired length. MGF1 is a mask generation function based on a hash
 * function, i.e. message digest algorithm.
 *
 * @since   11
 */
public final class MGF1 {

    private final MessageDigest md;

    /**
     * Construct an instance of MGF1 based on the specified digest algorithm.
     */
    MGF1(String mdAlgo) throws NoSuchAlgorithmException {
        this.md = MessageDigest.getInstance(mdAlgo);
    }

    /**
     * Using the specified seed bytes, generate the mask, xor the mask
     * with the specified output buffer and store the result into the
     * output buffer (essentially replaced in place).
     *
     * @param seed the buffer holding the seed bytes
     * @param seedOfs the index of the seed bytes
     * @param seedLen the length of the seed bytes to be used by MGF1
     * @param maskLen the intended length of the generated mask
     * @param out the output buffer holding the mask
     * @param outOfs the index of the output buffer for the mask
     */
    void generateAndXor(byte[] seed, int seedOfs, int seedLen, int maskLen,
            byte[] out, int outOfs) throws RuntimeException {
        byte[] C = new byte[4]; // 32 bit counter
        byte[] digest = new byte[md.getDigestLength()];
        while (maskLen > 0) {
            md.update(seed, seedOfs, seedLen);
            md.update(C);
            try {
                md.digest(digest, 0, digest.length);
            } catch (DigestException e) {
                // should never happen
                throw new RuntimeException(e.toString());
            }
            for (int i = 0; (i < digest.length) && (maskLen > 0); maskLen--) {
                out[outOfs++] ^= digest[i++];
            }
            if (maskLen > 0) {
                // increment counter
                for (int i = C.length - 1; (++C[i] == 0) && (i > 0); i--) {
                    // empty
                }
            }
        }
    }

    /**
     * Returns the name of this MGF1 instance, i.e. "MGF1" followed by the
     * digest algorithm it based on.
     */
    String getName() {
        return "MGF1" + md.getAlgorithm();
    }
}
