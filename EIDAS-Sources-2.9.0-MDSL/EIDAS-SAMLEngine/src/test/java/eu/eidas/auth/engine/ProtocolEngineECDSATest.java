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

package eu.eidas.auth.engine;

import org.apache.xml.security.signature.XMLSignature;

/**
 * Test class for {@link ProtocolEngine}
 * with {@link XMLSignature#ALGO_ID_SIGNATURE_ECDSA_SHA256} Signature Algorithm configuration
 */
public class ProtocolEngineECDSATest extends ProtocolEngineTest {

    @Override
    protected String getEngineConfigurationInstance() {
        return "ECDSA";
    }
}
