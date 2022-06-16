/*
 * Copyright (c) 2021 by European Commission
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
import org.apache.xml.security.encryption.XMLCipher;

public interface EidasEncryptionConstants {

    ImmutableSet<String> DEFAULT_ENCRYPTION_ALGORITHM_WHITE_LIST = ImmutableSet.of(
            XMLCipher.AES_128_GCM,
            XMLCipher.AES_192_GCM,
            XMLCipher.AES_256_GCM
    );
}
