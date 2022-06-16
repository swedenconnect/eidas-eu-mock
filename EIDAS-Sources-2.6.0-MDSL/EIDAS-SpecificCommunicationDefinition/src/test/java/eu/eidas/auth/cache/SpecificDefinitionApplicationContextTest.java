/*
 * Copyright (c) 2020 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.auth.cache;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test class for cache names defined by specificCommunicationDefinitionApplicationContext.xml.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:specificCommunicationDefinitionApplicationContext.xml"})
public class SpecificDefinitionApplicationContextTest {

    @Value("${specific.node.connector.request.cache.name}")
    String specificNodeConnectorRequestCache;

    @Value("${node.specific.connector.response.cache.name}")
    String nodeSpecificConnectorResponseCache;

    @Value("${node.specific.proxyservice.request.cache.name}")
    String nodeSpecificProxyserviceRequestCache;

    @Value("${specific.node.proxyservice.response.cache.name}")
    String specificNodeProxyserviceResponseCache;

    /**
     * Test method for
     * testing cache name for {@link SpecificDefinitionApplicationContextTest#specificNodeConnectorRequestCache}
     * when it was set in external specificCommunicationDefinitionConnector.xml
     * <p/>
     * Must succeed.
     */
    @Test
    public void testSpecificNodeConnectorRequestCacheName() {
        Assert.assertEquals("specificNodeConnectorRequestCacheExternal" , specificNodeConnectorRequestCache);
    }

    /**
     * Test method for
     * testing cache name for {@link SpecificDefinitionApplicationContextTest#nodeSpecificConnectorResponseCache}
     * when only set in default specificCommunicationDefinitionConnector.xml
     * <p/>
     * Must succeed.
     */
    @Test
    public void testNodeSpecificConnectorResponseCacheName() {
        Assert.assertEquals("nodeSpecificConnectorResponseCache", nodeSpecificConnectorResponseCache);
    }

    /**
     * Test method for
     * testing cache name for {@link SpecificDefinitionApplicationContextTest#nodeSpecificProxyserviceRequestCache}
     * when only set in default specificCommunicationDefinitionProxyservice.xml
     * <p/>
     * Must succeed.
     */
    @Test
    public void testNodeSpecificProxyserviceRequestCacheName() {
        Assert.assertEquals("nodeSpecificProxyserviceRequestCache", nodeSpecificProxyserviceRequestCache);
    }

    /**
     * Test method for
     * testing cache name for {@link SpecificDefinitionApplicationContextTest#specificNodeProxyserviceResponseCache}
     * when it was set in external specificCommunicationDefinitionProxyservice.xml
     * <p/>
     * Must succeed.
     */
    @Test
    public void testSpecificNodeProxyserviceResponseCacheName() {
        Assert.assertEquals("specificNodeProxyserviceResponseCacheExternal", specificNodeProxyserviceResponseCache);
    }

}