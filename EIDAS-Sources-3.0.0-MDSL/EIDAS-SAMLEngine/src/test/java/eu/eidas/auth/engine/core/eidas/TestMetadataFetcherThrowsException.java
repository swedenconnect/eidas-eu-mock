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

package eu.eidas.auth.engine.core.eidas;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;

import javax.annotation.Nonnull;

/**
 * Test MetadataFetcher for tests that need
 * that {@link MetadataFetcherI#getEidasMetadata(String, MetadataSignerI, MetadataClockI)}.
 * throws an {@link EIDASMetadataException}
 */
public final class TestMetadataFetcherThrowsException implements MetadataFetcherI {

    public TestMetadataFetcherThrowsException() {
    }

    public TestMetadataFetcherThrowsException(String string, Boolean bolean) {
    }

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(@Nonnull Issuer metadataIssuer, KeyInfo requiredMsgSigningCert, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock) throws EIDASMetadataException {
        throw new EIDASMetadataException("demo exception");
    }

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String metadataUrl, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock) throws EIDASMetadataException {
        Issuer issuer = new IssuerBuilder().buildObject();
        issuer.setValue(metadataUrl);
        return getEidasMetadata(issuer, null, metadataSigner, metadataClock);
    }
}
