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
package eu.eidas.util;

import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.io.ReloadableProperties;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Node Metadata whitelist related utilities.
 */
public final class WhitelistUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistUtil.class);
    private static final int MAX_URI_LENGTH_FOR_SAML = 1024;

    public static Collection<String> metadataWhitelist(String in) {
        List<String> result = new ArrayList<>();
        if (in == null) {
            return result;
        }
        for (String uriString : in.replaceAll("\t", "").replaceAll("\n", "").split(";")) {
            String trimmedUri = StringUtils.trim(uriString);
            try {
                if (trimmedUri.length() > MAX_URI_LENGTH_FOR_SAML) {
                    throw new IllegalArgumentException(
                            "Non SAML compliant URI. URI is more than 1024 characters in length. See '8.3.6 Entity Identifier' in the SAML2 Core spec");
                }
                URI.create(trimmedUri);
                result.add(trimmedUri);
            } catch (IllegalArgumentException e) {
                //If the given string violates RFC 2396 or is more than 1024 characters.
                LOGGER.warn("Invalid URI in matadata whitelist : " + e.getMessage(), e);
            }
        }
        return result;
    }

    public static boolean isWhitelisted(String issuer, Collection<String> whitelistMetadata) {
        return whitelistMetadata != null &&
                !whitelistMetadata.isEmpty() &&
                whitelistMetadata.contains(issuer);
    }

    public static boolean isUseWhitelist(@Nullable ReloadableProperties whitelistConfigProperties) {
        final String useWhitelist = WhitelistUtil.getKey(whitelistConfigProperties, EidasParameterKeys.METADATA_FETCHER_WHITELIST_FLAG);
        return Boolean.parseBoolean(useWhitelist);
    }

    @Nonnull
    public static Collection<String> metadataWhitelistHashes(@Nullable ReloadableProperties whitelistConfigProperties) {
        return getWhitelistURLs(whitelistConfigProperties).stream()
                .filter(StringUtils::isNotBlank)
                .map(WhitelistUtil::hashUrl)
                .collect(Collectors.toList());
    }

    @Nonnull
    public static Collection<String> getWhitelistURLs(@Nullable ReloadableProperties whitelistConfigProperties) {
        String whitelistUrls = getKey(whitelistConfigProperties, EidasParameterKeys.METADATA_FETCHER_WHITELIST);
        if (whitelistUrls == null) {
            return Collections.emptyList();
        } else {
            return WhitelistUtil.metadataWhitelist(whitelistUrls);
        }
    }

    @Nullable
    private static String getKey(@Nullable ReloadableProperties whitelistConfigProperties, @Nullable EidasParameterKeys key) {
        if (whitelistConfigProperties != null && key != null) {
            try {
                return whitelistConfigProperties.getProperties().getProperty(key.getValue());
            } catch (IOException e) {
                LOGGER.warn("Whitelist property file couldn't be found", e);
            }
        }
        return null;
    }

    @Nonnull
    private static String hashUrl(@Nonnull final String url) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);
        } catch (final NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }
}