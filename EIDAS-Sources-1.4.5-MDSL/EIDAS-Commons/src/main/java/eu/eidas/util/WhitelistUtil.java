 package eu.eidas.util;
 
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  * Node Metadata whitelist related utilities.
  */
public final class WhitelistUtil    {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhitelistUtil.class);
    private static final int MAX_URI_LENGTH_FOR_SAML = 1024;

    public static Collection<String> metadataWhitelist(String in){
    	List<String> result = new ArrayList<>();
    	if (in == null){
    		return result;
    	}
    	for(String uriString : in.replaceAll("\t", "").replaceAll("\n", "").split(";")){
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
