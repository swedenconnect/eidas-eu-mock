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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.eidas.engine.exceptions.EIDASMetadataRuntimeException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:test-context.xml"})
@PropertySource("classpath:whitelist-fetcher.xml")
public class XmlConfigurationWhitelistingMetadataFetcherTest {
	
	@Value("${fetcher.use.whitelist}")
	boolean FETCHER_USE_WHITELIST;

	@Value("${fetcher.whitelist}")
	String FETCHER_ALLOWED;
	
	@Autowired 
	ApplicationContext springContext;
	
	TestWhitelistingMetadataFetcher underTest_Whitelisting = null;
	TestWhitelistingMetadataFetcher underTest_Not_Whitelisting = null;
	
	@Before
	public void setup(){
		underTest_Whitelisting = null;
		assertNotNull(springContext);
		
		assertTrue(FETCHER_USE_WHITELIST);
		
		underTest_Whitelisting = springContext.getBean(TestWhitelistingMetadataFetcher.class, FETCHER_ALLOWED, FETCHER_USE_WHITELIST);
		assertNotNull(underTest_Whitelisting);
		assertTrue(underTest_Whitelisting.mustUseWhitelist());

		underTest_Not_Whitelisting = springContext.getBean(TestWhitelistingMetadataFetcher.class, FETCHER_ALLOWED, ! FETCHER_USE_WHITELIST);
		assertNotNull(underTest_Not_Whitelisting);
		assertFalse(underTest_Not_Whitelisting.mustUseWhitelist());

		assertFalse(underTest_Whitelisting.equals(underTest_Not_Whitelisting));
	}
	
	@Test
	public void testWhitelistURL() {
		Collection<String> whitelistURL = underTest_Whitelisting.getWhitelistURL();
		
		assertTrue(whitelistURL.contains("http://google.com/metadata"));
		assertTrue(whitelistURL.contains("http://europa.eu/metadata"));
		assertTrue(whitelistURL.size()==2);

		assertFalse(whitelistURL.contains("http://disney.com/metadata"));
	}

	@Test
	public void testMustUseWhitelist() {
		assertTrue(underTest_Whitelisting.mustUseWhitelist());
		
		underTest_Whitelisting.setUseWhitelist( ! FETCHER_USE_WHITELIST);
		assertFalse(underTest_Whitelisting.mustUseWhitelist());
	}

	@Test
	public void testCheckWhitelistedSuccess() {
		underTest_Whitelisting.checkWhitelisted("http://google.com/metadata");
	}

	@Test(expected=EIDASMetadataRuntimeException.class)
	public void testCheckWhitelistedFail() {
		underTest_Whitelisting.checkWhitelisted("http://disney.com/metadata");
		fail("Expecting EIDASMetadataRuntimeException");
	}

}
