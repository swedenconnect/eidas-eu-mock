package eu.eidas.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

public class WhitelistUtilTest {

	@Test
	public void testWhitelistUtils() {
		Collection<String> result = WhitelistUtil.metadataWhitelist("http://Europa.eu;mickey://mouse.com");
		assertTrue(result.contains("http://Europa.eu"));
		assertFalse(result.contains("http://europa.eu"));
		assertFalse(result.contains("mickey://mouse.com"));
		
		assertTrue(WhitelistUtil.isWhitelisted("http://Europa.eu", result));
		assertFalse(WhitelistUtil.isWhitelisted("http://europa.eu", result));
		assertFalse(WhitelistUtil.isWhitelisted("http://arctic.pole", result));
		assertFalse(WhitelistUtil.isWhitelisted("http://Europa.eu", null));
		assertFalse(WhitelistUtil.isWhitelisted("http://Europa.eu", Collections.EMPTY_LIST));
		
		assertNotNull(WhitelistUtil.metadataWhitelist(null));
		assertTrue(WhitelistUtil.metadataWhitelist(null).isEmpty());
		
	}

}
