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
package eu.eidas.auth.engine.metadata.impl;


import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.MetadataClockI;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.util.Preconditions;
import eu.eidas.util.WhitelistUtil;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.impl.IssuerBuilder;
import org.opensaml.xmlsec.signature.KeyInfo;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Managed MetadataFetcher with whitelisting functionality
 */

public abstract class WhitelistingMetadataFetcher implements MetadataFetcherI {

    private Collection<String> whitelistURL;
    private boolean useWhitelist;

    public WhitelistingMetadataFetcher(
            String whitelistURL,
            Boolean useWhitelist) {
        this.setWhitelistURL(whitelistURL);
        this.setUseWhitelist(useWhitelist);
    }

    public WhitelistingMetadataFetcher() {
    }

    protected final Collection<String> getWhitelistURL() {
        return whitelistURL;
    }

    protected final void setWhitelistURL(String whitelist) {
        this.whitelistURL = WhitelistUtil.metadataWhitelist(whitelist);
    }

    protected final boolean mustUseWhitelist() {
        return useWhitelist;
    }

    protected final void setUseWhitelist(boolean useWhitelist) {
        this.useWhitelist = useWhitelist;
    }

    protected final void checkWhitelisted(String url) throws EIDASMetadataException {
        if (mustUseWhitelist() && !WhitelistUtil.isWhitelisted(url, getWhitelistURL())) {
            throw new EIDASMetadataException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()),
                    "URL: " + url + " , metadata not in whitelist: ");
        }
    }

    private final void checkValidUri(String url) throws EIDASMetadataException {
        try {
            Preconditions.checkURISyntax(url, "Metadata URL");
        } catch (IllegalArgumentException e) {
            throw new EIDASMetadataException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_NO_METADATA.errorMessage()),
                    "Metadata URL format is invalid");
        }
    }

    protected abstract MetadataFetcherI getMetadataFetcher();

    @Nonnull
    @Override
    public EidasMetadataParametersI getEidasMetadata(@Nonnull String metadataUrl, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        Issuer metadataIssuer = new IssuerBuilder().buildObject();
        metadataIssuer.setValue(metadataUrl);
        return this.getEidasMetadata(metadataIssuer,null, metadataSigner, metadataClock);
    }

    @Nonnull
    @Override
    public final EidasMetadataParametersI getEidasMetadata(@Nonnull Issuer metadataIssuer, KeyInfo certificate, @Nonnull MetadataSignerI metadataSigner, MetadataClockI metadataClock)
            throws EIDASMetadataException {
        final String url = metadataIssuer.getValue();
        checkValidUri(url);
        checkWhitelisted(url);
        return getMetadataFetcher().getEidasMetadata(metadataIssuer, certificate, metadataSigner, metadataClock);
    }

}
