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

import com.google.common.collect.ImmutableSet;

import org.opensaml.common.SAMLObject;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;

/**
 * The Interface SAMLEngineSignI.
 *
 * @deprecated since 1.1, use {@link ProtocolSignerI} instead.
 */
@Deprecated
public interface SamlEngineSignI {

    /**
     * @return the signature used by signer to sign
     */
    Signature createSignature() throws EIDASSAMLEngineException;

    /**
     * @return the credential used to check the signature
     */
    Credential getPublicSigningCredential();

    /**
     * Gets the trusted certificates used when validating signatures.
     *
     * @return the trustStore
     */
    ImmutableSet<X509Certificate> getTrustedCertificates();

    /**
     * Sign.
     *
     * @param signableObject the token SAML
     * @return the sAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    SAMLObject sign(SignableSAMLObject signableObject) throws EIDASSAMLEngineException;

    /**
     * Sign metadata.
     *
     * @param signableObject the token SAML
     * @return the sAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    SAMLObject signMetadata(SignableSAMLObject signableObject) throws EIDASSAMLEngineException;

    /**
     * Validate signature.
     *
     * @param signableObject the token SAML
     * @param messageFormat the message format used by the saml object
     * @return the sAML object
     * @throws EIDASSAMLEngineException the SAML engine exception
     */
    SAMLObject validateSignature(SignableSAMLObject signableObject) throws EIDASSAMLEngineException;
}
