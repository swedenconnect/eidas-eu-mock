/*
 * Copyright (c) 2023 by European Commission
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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.io.ReloadableProperties;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static eu.eidas.util.WhitelistUtil.isUseWhitelist;
import static eu.eidas.util.WhitelistUtil.isWhitelisted;
import static eu.eidas.util.WhitelistUtil.metadataWhitelist;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WhitelistUtilTest {

    private final static LevelFilter filter = new LevelFilter(Level.WARN);

    private Logger testLogger = (Logger) LoggerFactory.getLogger(WhitelistUtil.class);
    private ListAppender<ILoggingEvent> listAppender;

    @Before
    public void setup() {
        listAppender = new ListAppender<>();
        listAppender.addFilter(filter);
        listAppender.start();
        testLogger.addAppender(listAppender);
    }

    @After
    public void teardown() {
        testLogger.detachAndStopAllAppenders();
    }

    @Test
    public void testValidURISyntax() {
        Collection<String> result = metadataWhitelist("http://Europa.eu;mickey://mouse.com");
        assertThat(result, containsInAnyOrder("http://Europa.eu", "mickey://mouse.com"));

        //All URIS are OK => No warning message generated
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    public void testInvalidURISyntax() {
        String noScheme = "://Europa.eu";      // missing scheme
        String wrongUrn = "urn_o:mouse.com";   // urn must begin with urn:
        String invalidChar = "urn:^mouse.com"; // ^ is invalid
        Collection<String> result = metadataWhitelist(noScheme + ";" + wrongUrn + ";" + invalidChar);

        assertThat(result, emptyCollectionOf(String.class));

        //All URIS are wrong => One warning message generated for erach URI
        assertThat(listAppender.list.size(), is(3));
        assertTrue(logContains("Illegal character in scheme name"));
        assertTrue(logContains("Illegal character in opaque part"));
        assertTrue(logContains("Expected scheme name"));
    }

    @Test
    public void testCaseSensitivity() {
        Collection<String> result = metadataWhitelist("http://EURopa.eu;mickey://mouse.com");
        assertThat(result, containsInAnyOrder("http://EURopa.eu", "mickey://mouse.com"));
        assertThat(result, not(contains("http://europa.eu")));
        assertThat(result, not(contains("MICKEY://mouse.com")));

        //All URIS are OK => No warning message generated
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    public void testInWhiteList() {
        Collection<String> whiteList = metadataWhitelist("http://Europa.eu;mickey://mouse.com");
        assertTrue(isWhitelisted("http://Europa.eu", whiteList));

        //All URIS are OK => No warning message generated
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    public void testNotInWhiteList() {
        Collection<String> whiteList = metadataWhitelist("http://Europa.eu;mickey://mouse.com");
        assertFalse(isWhitelisted("http://notWhitelisted", whiteList));

        //All URIS are OK => No warning message generated
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    public void testNullWhiteList() {
        Collection<String> whiteList = null;
        assertFalse(isWhitelisted("http://notWhitelisted", whiteList));
    }

    @Test
    public void testEmptyWhiteList() {
        Collection<String> whiteList = Collections.EMPTY_LIST;
        assertFalse(isWhitelisted("http://notWhitelisted", whiteList));
    }

    @Test
    public void maxLenghtURIforSAML() {
        String maxLenghtURI = RandomStringUtils.randomAlphanumeric(1024);
        Collection<String> result = metadataWhitelist(maxLenghtURI + ";" + "donald:duck.com");
        assertThat(result, containsInAnyOrder(maxLenghtURI, "donald:duck.com"));

        //All URIS are OK => No warning message generated
        assertTrue(listAppender.list.isEmpty());
    }

    @Test
    public void tooLongURIforSAML() {
        String tooLongURI = RandomStringUtils.randomAlphanumeric(1025);
        Collection<String> result = metadataWhitelist(tooLongURI + ";" + "donald:duck.com");
        assertThat(result, not(contains(tooLongURI)));
        assertThat(result, contains("donald:duck.com"));
        assertThat(listAppender.list.size(), is(1));

        assertTrue(logContains("Non SAML compliant URI"));
    }

    @Test
    public void testUseWhitelistTrue() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST_FLAG.getValue(), "true");
        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        assertTrue(isUseWhitelist(mockedProperties));
    }

    @Test
    public void testUseWhitelistFalse() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST_FLAG.getValue(), "false");
        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        assertFalse(isUseWhitelist(mockedProperties));
    }

    @Test
    public void testUseWhitelistNull() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        assertFalse(isUseWhitelist(mockedProperties));
    }

    @Test
    public void testGetWhitelistURLsSingleUrl() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue(), "metadataWhiteListProp");
        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        final Collection<String> whitelistURLs = WhitelistUtil.getWhitelistURLs(mockedProperties);
        assertThat(whitelistURLs, hasSize(1));
        assertThat(whitelistURLs, contains("metadataWhiteListProp"));
    }

    @Test
    public void testGetWhitelistURLsMultipleUrl() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue(), "url1;url2;url3");
        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        final Collection<String> whitelistURLs = WhitelistUtil.getWhitelistURLs(mockedProperties);
        assertThat(whitelistURLs, hasSize(3));
        assertThat(whitelistURLs, contains("url1", "url2", "url3"));
    }

    @Test
    public void testGetWhitelistURLsPropNotFound() throws Exception {
        final Properties mockedData = new Properties();

        mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue() + "q", "metadataWhiteListProp");
        mockedData.setProperty("anotherKey", "otherKey");

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        final Collection<String> whitelistURLs = WhitelistUtil.getWhitelistURLs(mockedProperties);
        assertThat(whitelistURLs, hasSize(0));
    }

    @Test
    public void testMultipleMetadataWhitelistHashes() throws Exception {
        final ReloadableProperties reloadableProperties = this.createTestWhitelist(
                "http://metadata.eu/ProxyService/Metadata",
                "http://metadata.eu/ConnectorService/Metadata",
                "http://test.be/ConnectorMetadata",
                "http://test.be/ConnectorMetadata",
                "http://TEST.be/ConnectorMetadata"
        );

        final Collection<String> hashes = WhitelistUtil.metadataWhitelistHashes(reloadableProperties);

        assertThat(hashes, hasSize(5));
        assertThat(hashes, containsInAnyOrder(
                "LTWlTYv532XTLNroWvdwRg1NAuNqFssNco5Tl2TJWv0=",
                "4s3vl3jwb67aWE2JeW+regF6kPPmr95SqmhFSQY997Q=",
                "V6fZWd0LUcyzkN/tr85UiMxhhCUrJfUtfVzqtp631vs=",
                "V6fZWd0LUcyzkN/tr85UiMxhhCUrJfUtfVzqtp631vs=",
                "ouZL6GEaZc5KzQEMBchVTnDT8phhYHgIhobqEl+p2M4="
        ));
    }

    @Test
    public void testBlankMetadataWhitelistHashes() throws Exception {
        final ReloadableProperties emptyUrl = this.createTestWhitelist(
                ""
        );

        Collection<String> hashes = WhitelistUtil.metadataWhitelistHashes(emptyUrl);
        assertThat(hashes, hasSize(0));

        final ReloadableProperties blankUrl = this.createTestWhitelist(
                "  "
        );

        hashes = WhitelistUtil.metadataWhitelistHashes(blankUrl);
        assertThat(hashes, hasSize(0));
    }

    @Test
    public void testSingleMetadataWhitelistHashes() throws Exception {
        final ReloadableProperties reloadableProperties = this.createTestWhitelist(
                "http://metadata.eu/ProxyService/Metadata"
        );

        final Collection<String> hashes = WhitelistUtil.metadataWhitelistHashes(reloadableProperties);

        assertThat(hashes, hasSize(1));
        assertThat(hashes, containsInAnyOrder(
                "LTWlTYv532XTLNroWvdwRg1NAuNqFssNco5Tl2TJWv0="
        ));
    }

    private ReloadableProperties createTestWhitelist(final String... urls) throws IOException {
        final Properties mockedData = new Properties();

        if (urls == null) {
            mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue(), null);
        } else if (urls.length <= 0) {
            mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue(), "");
        } else {
            mockedData.setProperty(EidasParameterKeys.METADATA_FETCHER_WHITELIST.getValue(), String.join(";", urls));
        }

        final ReloadableProperties mockedProperties = mock(ReloadableProperties.class);

        when(mockedProperties.getProperties()).thenReturn(mockedData);

        return mockedProperties;
    }

    private boolean logContains(String logOutput) {
        List<ILoggingEvent> list = this.listAppender.list;
        for (ILoggingEvent event : list) {
            if (event.getFormattedMessage().contains(logOutput)) {
                return true;
            }
        }
        return false;
    }

    private final static class LevelFilter extends Filter<ILoggingEvent> {
        private Level level;

        private LevelFilter(Level level) {
            this.level = level;
        }

        @Override
        public FilterReply decide(ILoggingEvent event) {
            if (event.getLevel().equals(level)) {
                return FilterReply.ACCEPT;
            } else {
                return FilterReply.DENY;
            }
        }
    }
}
