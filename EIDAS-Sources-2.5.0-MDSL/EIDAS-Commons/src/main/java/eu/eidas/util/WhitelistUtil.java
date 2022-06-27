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
package eu.eidas.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
}