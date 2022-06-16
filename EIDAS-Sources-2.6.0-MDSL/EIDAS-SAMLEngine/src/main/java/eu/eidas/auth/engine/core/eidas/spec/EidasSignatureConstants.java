/*
 * Copyright (c) 2020 by European Commission
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

import com.google.common.collect.ImmutableSet;
import org.apache.xml.security.signature.XMLSignature;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

/**
 * Introduction of default constants (values from the specifications) for the signature
 */
public interface EidasSignatureConstants {

    String DEFAULT_SIGNATURE_ALGORITHM = XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1;

    String DEFAULT_DIGEST_ALGORITHM = SignatureConstants.ALGO_ID_DIGEST_SHA512;

    ImmutableSet<String> DEFAULT_SIGNATURE_ALGORITHM_WHITE_LIST = ImmutableSet.of(
            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256_MGF1,
            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA384_MGF1,
            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA512_MGF1,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA256,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA384,
            SignatureConstants.ALGO_ID_SIGNATURE_ECDSA_SHA512
    );

    ImmutableSet<String> DEFAULT_DIGEST_ALGORITHM_WHITE_LIST =
            ImmutableSet.of(SignatureConstants.ALGO_ID_DIGEST_SHA256,
                    SignatureConstants.ALGO_ID_DIGEST_SHA384,
                    SignatureConstants.ALGO_ID_DIGEST_SHA512);

    int MIN_RSA_KEY_LENGTH = 3072;
    int MIN_EC_KEY_LENGTH = 256;
    int MIN_SIGNATURE_HASH_LENGTH = 256;

}
