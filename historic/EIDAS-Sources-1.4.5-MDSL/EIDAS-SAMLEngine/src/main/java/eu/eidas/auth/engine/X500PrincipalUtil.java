/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.auth.engine;

import org.bouncycastle.asn1.x500.X500Name;

/**
 * Utility class used to decrease complexity of comparison of 2 X509principal
 *
 * @version $Revision: 1.00 $, $Date: 2013-05-24 20:53:51 $ $Revision: 1.1 $, $Date: 2013-05-24 20:53:51 $
 * @deprecated do not use, use {@link javax.security.auth.x500.X500Principal} instead.
 */
@Deprecated
public final class X500PrincipalUtil {

    /**
     * Compares 2 X500Principals to detect if they principalEquals
     *
     * @param principal1
     * @param principal2
     * @return true if arguments are not null and principalEquals
     */
    public static boolean principalNotNullEquals(X500Name principal1, X500Name principal2) {
        if (principal1 == null || principal2 == null) {
            return false;
        }

        return principal1.equals(principal2);
    }

    private X500PrincipalUtil() {
        throw new AssertionError();
    }
}
