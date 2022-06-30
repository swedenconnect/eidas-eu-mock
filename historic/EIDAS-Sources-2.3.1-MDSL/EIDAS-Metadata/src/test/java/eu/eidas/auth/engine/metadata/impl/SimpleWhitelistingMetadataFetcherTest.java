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
package eu.eidas.auth.engine.metadata.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.engine.exceptions.EIDASMetadataRuntimeException;

public class SimpleWhitelistingMetadataFetcherTest {

	static final boolean FETCHER_USE_WHITELIST = true;
	static final boolean FETCHER_DONT_USE_WHITELIST = false;

	static final String FETCHER_ALLOWED_URL_1 = "http://google.com/metadata";
	static final String FETCHER_ALLOWED_URL_2 = "http://europa.eu/metadata";

	static final String FETCHER_ALLOWED = FETCHER_ALLOWED_URL_1 + ";" + FETCHER_ALLOWED_URL_2;
	static final String FETCHER_DENIED = "http://disney.com/metadata";

	TestWhitelistingMetadataFetcher underTest = new TestWhitelistingMetadataFetcher(){

		@Override
		protected MetadataFetcherI getMetadataFetcher() {
			return null;
		}
		
	};
	
	@Before
	public void setup(){
		underTest.setUseWhitelist(FETCHER_USE_WHITELIST);
		underTest.setWhitelistURL(FETCHER_ALLOWED);
	}
	
	@Test
	public void testWhitelistURL() {
		Collection<String> whitelistURL = underTest.getWhitelistURL();
		assertTrue(whitelistURL.contains(FETCHER_ALLOWED_URL_1));
		assertTrue(whitelistURL.contains(FETCHER_ALLOWED_URL_2));
		assertFalse(whitelistURL.contains(FETCHER_DENIED));
		assertTrue(whitelistURL.size()==2);
	}

	@Test
	public void testMustUseWhitelist() {
		assertTrue(underTest.mustUseWhitelist());
		
		underTest.setUseWhitelist(FETCHER_DONT_USE_WHITELIST);
		assertFalse(underTest.mustUseWhitelist());
	}

	@Test
	public void testCheckWhitelistedSuccess() {
		underTest.checkWhitelisted(FETCHER_ALLOWED_URL_1);
	}

	@Test(expected=EIDASMetadataRuntimeException.class)
	public void testCheckWhitelistedFail() {
		underTest.checkWhitelisted(FETCHER_DENIED);
		fail("Expecting EIDASMetadataRuntimeException");
	}

}
