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

package eu.eidas.encryption.config;

import org.apache.xml.security.algorithms.JCEMapper;
import org.apache.xml.security.signature.XMLSignature;

/**
 * This class sets up the JCEName names used by OpenSaml
 * and allows for other providers than the default BouncyCastle
 */
public class EidasJCENameConfiguration {

    /**
     * JCEName configuration for OpenSaml in eIDAS
     */
    public static void setJCENameConfiguration() {
        setJCENamesForRsaSsaPss();
    }

    /**
     * The signature algorithms that use the RSASSA-PSS signature scheme as defined in [PKCS #1 v2.2] (https://tools.ietf.org/html/rfc8017).
     * Note that this signature algorithm needs parameters to be supplied before performing the RSA operation.
     * Parameters such as a digesting algorithm, salt length and MGF1 algorithm.
     */
    private static void setJCENamesForRsaSsaPss() {
        final String jceName = "RSASSA-PSS";
        JCEMapper.register(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1, new JCEMapper.Algorithm("RSA", jceName, "Signature"));
        JCEMapper.register(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1, new JCEMapper.Algorithm("RSA", jceName, "Signature"));
        JCEMapper.register(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1, new JCEMapper.Algorithm("RSA", jceName, "Signature"));
    }
}
