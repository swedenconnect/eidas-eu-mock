/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.engine.metadata;

import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.xmlsec.signature.SignableXMLObject;

import javax.annotation.Nonnull;

/**
 * Retrieves SAML2 metadata {code EntityDescriptor}s associated with a SAML Service Provider (SP) and/or Identity
 * Provider (IDP).
 *
 * @since 1.1
 */
public interface MetadataFetcherI {

    /**
     * Returns the metadata EntityDescriptor instance fetched from the given URL.
     *
     * @param url the url of the metadata file
     * @param metadataSigner the metadataSigner which can be used to verify the digital signature of the retrieved
     * EntityDescriptor via the ({@link MetadataSignerI#validateMetadataSignature(SignableXMLObject)} method.
     * @param metadataClock used to validate
     * @return the entity descriptor associated with the given url.
     * @throws EIDASMetadataException in case of errors
     */
    @Nonnull
    EidasMetadataParametersI getEidasMetadata(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException;
}
