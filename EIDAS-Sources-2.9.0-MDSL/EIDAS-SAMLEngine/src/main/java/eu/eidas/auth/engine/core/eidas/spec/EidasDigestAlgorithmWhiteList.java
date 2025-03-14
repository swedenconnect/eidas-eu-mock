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

package eu.eidas.auth.engine.core.eidas.spec;

import org.opensaml.xmlsec.signature.support.SignatureConstants;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class EidasDigestAlgorithmWhiteList {

    private static final Set<String> DEFAULT_DIGEST_ALGORITHM_WHITE_LIST =
            new LinkedHashSet<>(List.of(
                    SignatureConstants.ALGO_ID_DIGEST_SHA256,
                    SignatureConstants.ALGO_ID_DIGEST_SHA384,
                    SignatureConstants.ALGO_ID_DIGEST_SHA512
            ));

    public static Set<String> getDefaultDigestAlgorithmWhiteList() {
        return DEFAULT_DIGEST_ALGORITHM_WHITE_LIST;
    }
}
