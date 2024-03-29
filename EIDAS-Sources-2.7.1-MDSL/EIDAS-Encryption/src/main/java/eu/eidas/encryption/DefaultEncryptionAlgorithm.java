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
package eu.eidas.encryption;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

import org.opensaml.xmlsec.encryption.support.EncryptionConstants;
import org.opensaml.xmlsec.signature.support.SignatureConstants;

/**
 * DefaultEncryptionAlgorithm
 *
 * @since 1.1
 */
public enum DefaultEncryptionAlgorithm {

    DEFAULT_DATA_ENCRYPTION_ALGORITHM(EncryptionConstants.ALGO_ID_BLOCKCIPHER_AES256_GCM),

    DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_TRANSPORT(EncryptionConstants.ALGO_ID_KEYTRANSPORT_RSAOAEP11),

    DEFAULT_DIGEST_ALGORITHM_FOR_KEY_TRANSPORT(SignatureConstants.ALGO_ID_DIGEST_SHA256),

    DEFAULT_MGF_ALGORITHM_FOR_KEY_TRANSPORT(EncryptionConstants.ALGO_ID_MGF1_SHA256),

    DEFAULT_KEY_ENCRYPTION_ALGORITHM_FOR_KEY_AGREEMENT(EncryptionConstants.ALGO_ID_KEYWRAP_AES256),

    DEFAULT_KEY_ENCRYPTION_AGREEMENT_METHOD_ALGORITHM(EncryptionConstants.ALGO_ID_KEYAGREEMENT_ECDH_ES),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, DefaultEncryptionAlgorithm> MAPPER =
            new EnumMapper<String, DefaultEncryptionAlgorithm>(new KeyAccessor<String, DefaultEncryptionAlgorithm>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull DefaultEncryptionAlgorithm defaultEncryptionAlgorithm) {
                    return defaultEncryptionAlgorithm.getValue();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static DefaultEncryptionAlgorithm fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, DefaultEncryptionAlgorithm> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String value;

    DefaultEncryptionAlgorithm(@Nonnull String val) {
        value = val;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
