/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.auth.engine.core;

import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import org.opensaml.saml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;

/**
 * Interface responsible for encrypting.
 * <p>
 * Typically this interface encrypts responses sent to several nodes with their public certificates (e.g. found in their
 * metadata).
 *
 * @since 1.1
 */
public interface ProtocolEncrypterI extends ProtocolCipherI {

    /**
     * Encrypts the given response with the given destination certificate.
     *
     * @param authResponse the response to encrypt
     * @param destinationCertificate the certificate to encrypt with
     * @return the encrypted response
     * @throws EIDASSAMLEngineException if any error occurs
     */
    @Nonnull
    Response encryptSamlResponse(@Nonnull Response authResponse, @Nonnull X509Certificate destinationCertificate)
            throws EIDASSAMLEngineException;

    /**
     * Returns the encryption certificate to be used to encrypt a response for the given country
     *
     * @return the encryption certificate to be used to encrypt a response for the given country
     */
    @Nullable
    X509Certificate getEncryptionCertificate(@Nullable String destinationCountryCode) throws EIDASSAMLEngineException;

    /**
     * Returns whether encryption is enabled for the given country.
     *
     * @param countryCode the 2-letter country code as defined in ISO 3166 of the country the response is being sent
     * to.
     * @return whether encryption is enabled for the given country.
     * @see ProtocolEncrypterI#isResponseEncryptionMandatory()
     */
    boolean isEncryptionEnabled(@Nonnull String countryCode);
}
