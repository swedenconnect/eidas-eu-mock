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

package eu.eidas.auth.engine.metadata;

import eu.eidas.auth.commons.protocol.eidas.EidasProtocolVersion;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.Response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

public interface HighLevelMetadataParamsI extends EidasMetadataParametersI{
    List<EidasProtocolVersion> getMetadataProtocolVersions();

    /**
     * @return the first certificate listed in the metadata (no preference)
     */
    @Nullable
    X509Certificate getEncryptionCertificate();

    /**
     * @deprecated now that we support reading multiple certificates in the metadata,
     * simply getting the first one will not suffice. Pass the message.
     */
    @Deprecated
    @Nullable
    X509Certificate getRequestSignatureCertificate();

    @Nullable
    X509Certificate getRequestSignatureCertificate(AuthnRequest samlMessage);

    /**
     * @deprecated now that we support reading multiple certificates in the metadata,
     * simply getting the first one will not suffice. Pass the message.
     */
    @Deprecated
    @Nullable
    X509Certificate getResponseSignatureCertificate();

    @Nullable
    X509Certificate getResponseSignatureCertificate(Response samlMessage);

    @Nonnull
    @SuppressWarnings("squid:S2583")
    Set<String> getSupportedAttributes();

    boolean isSendRequesterId(String requesterId);

    @Nonnull
    EidasMetadataRoleParametersI getIDPRoleDescriptor();

    @Nonnull
    EidasMetadataRoleParametersI getSPRoleDescriptor();
}
