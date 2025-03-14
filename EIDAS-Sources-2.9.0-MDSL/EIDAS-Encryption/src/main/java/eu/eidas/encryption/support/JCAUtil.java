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


import java.security.SecureRandom;

/**
 * Collection of static utility methods used by the security framework.
 *
 * @author  Andreas Sterbenz
 * @since   1.5
 */
public final class JCAUtil {

    private JCAUtil() {
        // no instantiation
    }

    // size of the temporary arrays we use. Should fit into the CPU's 1st
    // level cache and could be adjusted based on the platform
    private static final int ARRAY_SIZE = 4096;

    /**
     * Get the size of a temporary buffer array to use in order to be
     * cache efficient. totalSize indicates the total amount of data to
     * be buffered. Used by the engineUpdate(ByteBuffer) methods.
     */
    public static int getTempArraySize(int totalSize) {
        return Math.min(ARRAY_SIZE, totalSize);
    }

    // cached SecureRandom instance
    private static class CachedSecureRandomHolder {
        public static final SecureRandom instance = new SecureRandom();
    }

    @SuppressWarnings("squid:S3077")
    private static volatile SecureRandom def = null;

    /**
     * Get a SecureRandom instance. This method should be used by JDK
     * internal code in favor of calling "new SecureRandom()". That needs to
     * iterate through the provider table to find the default SecureRandom
     * implementation, which is fairly inefficient.
     */
    public static SecureRandom getSecureRandom() {
        return CachedSecureRandomHolder.instance;
    }

    // called by sun.security.jca.Providers class when provider list is changed
    static void clearDefSecureRandom() {
        def = null;
    }

    /**
     * Get the default SecureRandom instance. This method is the
     * optimized version of "new SecureRandom()" which re-uses the default
     * SecureRandom impl if the provider table is the same.
     */
    public static SecureRandom getDefSecureRandom() {
        SecureRandom result = def;
        if (result == null) {
            synchronized (JCAUtil.class) {
                result = def;
                if (result == null) {
                    def = result = new SecureRandom();
                }
            }
        }
        return result;
    }
}
