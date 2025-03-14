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

package eu.eidas.auth.engine.core.impl;

import eu.eidas.auth.engine.configuration.dom.EncryptionConfiguration;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Set;


public class AbstractProtocolCipherTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    /**
     Test method for
     {@link AbstractProtocolDecrypter#validateEncryptionAlgorithm(String)}
     when the encryption algorithm string is not in the whitelist
     should throw EIDASSAMLEngineException
     <p>
     Must fail.
     */
    @Test
    public void testValidateEncryptionAlgorithmFail() throws EIDASSAMLEngineException {
        expectedException.expect(EIDASSAMLEngineException.class);

        Set<String> encryptionAlgorithms = Set.of("sha512-rsa-MGF1");
        EncryptionConfiguration encryptionConfiguration = new EncryptionConfiguration.Builder()
                .setCheckedValidityPeriod(false)
                .setDisallowedSelfSignedCertificate(false)
                .setResponseEncryptionMandatory(true)
                .setAssertionEncryptWithKey(true)
                .setEncryptionAlgorithmWhitelist(encryptionAlgorithms)
                .build();
        final AbstractProtocolCipher cipher = new AbstractProtocolCipher(encryptionConfiguration) {
        };

        cipher.validateEncryptionAlgorithm("Disallowed");
    }
}