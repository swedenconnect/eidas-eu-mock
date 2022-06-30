/*
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence. You may
 * obtain a copy of the Licence at:
 *
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * Licence for the specific language governing permissions and limitations under
 * the Licence.
 */

package eu.eidas.auth.engine.core.impl;

import java.util.Map;

import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

import javax.annotation.Nullable;

/**
 * A {@link ProtocolSignerI} implementation which uses the same signature configuration file to load another keyStore
 * used to sign the MetaData content.
 */
public class SignSW extends KeyStoreProtocolSigner {

    @Deprecated // use the the constructor supporting defaultPath instead
    public SignSW(Map<String, String> properties) throws EIDASSAMLEngineException {
        super(properties, null);
    }

    public SignSW(Map<String, String> properties, @Nullable String defaultPath) throws EIDASSAMLEngineException {
        super(properties, defaultPath);
    }
}
