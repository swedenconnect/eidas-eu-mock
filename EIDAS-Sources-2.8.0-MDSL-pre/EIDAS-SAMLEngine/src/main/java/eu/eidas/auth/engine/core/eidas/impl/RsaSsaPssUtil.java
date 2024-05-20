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

package eu.eidas.auth.engine.core.eidas.impl;

import org.apache.xml.security.signature.XMLSignature;

import java.security.spec.PSSParameterSpec;
import java.util.HashMap;
import java.util.Map;

import static java.security.spec.MGF1ParameterSpec.SHA256;
import static java.security.spec.MGF1ParameterSpec.SHA384;
import static java.security.spec.MGF1ParameterSpec.SHA512;
import static java.security.spec.PSSParameterSpec.TRAILER_FIELD_BC;

/**
 * Class to create PSSParameterSpec for RSASSA-PSS (PKCS1#2.1)
 */
public class RsaSsaPssUtil {

    private static final Map<String, PSSParameterSpec> specMap = new HashMap<>();
    static {
        specMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
                new PSSParameterSpec("SHA-256", "MGF1", SHA256, 32, TRAILER_FIELD_BC));
        specMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
                new PSSParameterSpec("SHA-384", "MGF1", SHA384, 48, TRAILER_FIELD_BC));
        specMap.put(XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1,
                new PSSParameterSpec("SHA-512", "MGF1", SHA512, 64, TRAILER_FIELD_BC));
    }

    /**
     * Although [RFC3447] allows for parameterization,
     * the default is to use the same hash function as the digest method function.
     * derived from
     * @param signingAlgorithm RSASSA-PSS without Parameters URI
     */
    public static PSSParameterSpec getRsaSsaPssParameters(String signingAlgorithm) {
        return specMap.get(signingAlgorithm);
    }
}
