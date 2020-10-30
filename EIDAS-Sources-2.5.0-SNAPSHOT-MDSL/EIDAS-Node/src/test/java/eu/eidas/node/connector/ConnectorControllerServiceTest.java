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
 * limitations under the Licence
 */

package eu.eidas.node.connector;

import eu.eidas.node.auth.connector.ICONNECTORService;
import org.junit.Assert;
import org.junit.Test;

import javax.cache.Cache;

import static org.mockito.Mockito.mock;

/**
 * Test class for {@link ConnectorControllerService}
 */
public class ConnectorControllerServiceTest {

    /**
     * Test method for
     * {@link ConnectorControllerService#getAssertionConsUrl()}
     * when ConnectorControllerService#assertionConsUrl has not been set.
     * <p>
     * Must succeed.
     */
    @Test
    public void getAssertionConsUrl() {
        ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        String assertionConsUrl = connectorControllerService.getAssertionConsUrl();

        Assert.assertNull(assertionConsUrl);
    }

    /**
     * Test method for
     * {@link ConnectorControllerService#setAssertionConsUrl(String)}
     * when a valid URL in form of a String is used as parameter
     * <p>
     * Must succeed.
     */
    @Test
    public void setAssertionConsUrl() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        final String expectedAssertionConsUrl = "http://localhost/assertionConsUrl";
        connectorControllerService.setAssertionConsUrl(expectedAssertionConsUrl);
        final String actualAssertionConsUrl = connectorControllerService.getAssertionConsUrl();

        Assert.assertEquals(expectedAssertionConsUrl, actualAssertionConsUrl);
    }

    /**
     * Test method for
     * {@link ConnectorControllerService#setConnectorService(ICONNECTORService)}
     * when an instance of {@link ICONNECTORService} is used as parameter
     * <p>
     * Must succeed.
     */
    @Test
    public void setConnectorService() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        ICONNECTORService expectedMockedICONNECTORService = mock(ICONNECTORService.class);
        connectorControllerService.setConnectorService(expectedMockedICONNECTORService);
        final ICONNECTORService actualMockedICONNECTORService = connectorControllerService.getConnectorService();

        Assert.assertEquals(expectedMockedICONNECTORService, actualMockedICONNECTORService);

    }

    /**
     * Test method for
     * {@link ConnectorControllerService#getConnectorService()}
     * when ConnectorControllerService#connectorService has not been set.
     * <p>
     * Must succeed.
     */
    @Test
    public void getConnectorService() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        final ICONNECTORService actualMockedICONNECTORService = connectorControllerService.getConnectorService();

        Assert.assertNull(actualMockedICONNECTORService);
    }

    /**
     * Test method for
     * {@link ConnectorControllerService#setSpecificSpRequestCorrelationCache(Cache)}
     * when an instance of {@link Cache} is used as parameter
     * <p>
     * Must succeed.
     */
    @Test
    public void setSpecificSpRequestCorrelationCache() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        Cache expectedCache = mock(Cache.class);
        connectorControllerService.setSpecificSpRequestCorrelationCache(expectedCache);

        Assert.assertFalse(connectorControllerService.toString().contains("specificSpRequestCorrelationCache=null"));
    }

    /**
     * Test method for
     * {@link ConnectorControllerService#setConnectorRequestCorrelationCache(Cache)}
     * when an instance of {@link Cache} is used as parameter
     * <p>
     * Must succeed.
     */
    @Test
    public void setConnectorRequestCorrelationCache() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        Cache expectedCache = mock(Cache.class);
        connectorControllerService.setConnectorRequestCorrelationCache(expectedCache);

        Assert.assertFalse(connectorControllerService.toString().contains("connectorRequestCorrelationMap=null"));
    }

    /**
     * Test method for
     * {@link ConnectorControllerService#toString()}
     * when a clean state instance of {@link ConnectorControllerService} is used
     * <p>
     * Must succeed.
     */
    @Test
    public void toStringTest() {
        final ConnectorControllerService connectorControllerService = new ConnectorControllerService();
        final String actualString = connectorControllerService.toString();
        final String expectedString = "ConnectorControllerService{, assertionConsUrl='null', connectorService=null, specificSpRequestCorrelationCache=null, connectorRequestCorrelationMap=null}";

        Assert.assertEquals(expectedString, actualString);
    }
}