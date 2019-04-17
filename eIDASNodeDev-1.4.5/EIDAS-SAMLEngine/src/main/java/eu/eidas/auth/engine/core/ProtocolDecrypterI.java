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
package eu.eidas.auth.engine.core;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import org.opensaml.saml2.core.Response;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * Interface responsible for decrypting.
 * <p>
 * Typically this interface decrypts responses sent to one node with the local decryption certificate of that node.
 *
 * @since 1.1
 */
public interface ProtocolDecrypterI extends ProtocolCipherI {

    /**
     * Decrypts the given response.
     *
     * @param authResponse the response to decrypt
     * @return the decrypted response
     * @throws EIDASSAMLEngineException if any error occurs
     */
    @Nonnull
    Response decryptSamlResponse(@Nonnull Response authResponse) throws EIDASSAMLEngineException;

    /**
     * Returns the X.509 certificate used to decrypt encrypted responses.
     *
     * @return the X.509 certificate used to decrypt encrypted responses.
     * @throws EIDASSAMLEngineException if any error occurs
     */
    @Nonnull
    X509Certificate getDecryptionCertificate() throws EIDASSAMLEngineException;
}
