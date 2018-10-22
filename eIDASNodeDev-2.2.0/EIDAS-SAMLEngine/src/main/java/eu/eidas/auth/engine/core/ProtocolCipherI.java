/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.core;

/**
 * Marker interface for the encrypt and decrypt interfaces.
 *
 * @since 1.1
 */
public interface ProtocolCipherI {

    /**
     * Returns {@code true} when the validity period of an X.509 certificate must be verified when performing
     * cryptographic operations, returns {@code false} otherwise.
     *
     * @return {@code true} when the validity period of an X.509 certificate must be verified when performing
     * cryptographic operations, returns {@code false} otherwise.
     */
    boolean isCheckedValidityPeriod();

    /**
     * Returns {@code true} when using self-signed X.509 certificate is not allowed, returns {@code false} when it is
     * allowed.
     *
     * @return {@code true} when using self-signed X.509 certificate is not allowed, returns {@code false} when it is
     * allowed.
     */
    boolean isDisallowedSelfSignedCertificate();

    /**
     * Returns whether encryption is mandatory regardless of the country.
     * <p>
     * When this flag is {@code true}, the {@link #isEncryptionEnabled(String)} method must always return {@code true}.
     *
     * @return whether encryption is mandatory regardless of the country.
     */
    boolean isResponseEncryptionMandatory();

	boolean isAssertionEncryptWithKey();
}
