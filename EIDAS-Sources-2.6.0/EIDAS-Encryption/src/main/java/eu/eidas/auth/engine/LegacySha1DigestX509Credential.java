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

package eu.eidas.auth.engine;

import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.security.x509.X509Credential;

/**
 * Class {@link LegacySha1DigestX509Credential} to provide legacy support for
 * the SHA-1 OAEP Encryption Digest Algorithm with eIDAS 1.x nodes
 */
public class LegacySha1DigestX509Credential extends BasicX509Credential {

    public LegacySha1DigestX509Credential(X509Credential credential) {
        super(credential.getEntityCertificate());
    }
}
