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

import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.node.auth.connector.ICONNECTORService;

import javax.cache.Cache;

public final class ConnectorControllerService {

    private String assertionConsUrl;

    /**
     * Connector service.
     */
    private ICONNECTORService connectorService;

    private Cache<String, StoredLightRequest> specificSpRequestCorrelationCache;

    private Cache<String, StoredAuthenticationRequest> connectorRequestCorrelationCache;

    public String getAssertionConsUrl() {
        return assertionConsUrl;
    }

    public void setAssertionConsUrl(String assertionConsUrl) {
        this.assertionConsUrl = assertionConsUrl;
    }

    /**
     * Setter for connectorService.
     *
     * @param connectorService The new connectorService value.
     * @see ICONNECTORService
     */
    public void setConnectorService(ICONNECTORService connectorService) {
        this.connectorService = connectorService;
    }

    /**
     * Getter for connectorService.
     *
     * @return The connectorService value.
     * @see ICONNECTORService
     */
    public ICONNECTORService getConnectorService() {
        return connectorService;
    }

    /**
     * Setter for {@link ConnectorControllerService#specificSpRequestCorrelationCache}.
     *
     * @param specificSpRequestCorrelationCache to be set to {@link ConnectorControllerService#specificSpRequestCorrelationCache}
     */
    public void setSpecificSpRequestCorrelationCache(Cache<String, StoredLightRequest> specificSpRequestCorrelationCache) {
        this.specificSpRequestCorrelationCache = specificSpRequestCorrelationCache;
    }

    /**
     * Setter for {@link ConnectorControllerService#connectorRequestCorrelationCache}.
     *
     * @param connectorRequestCorrelationCache to be set to {@link ConnectorControllerService#connectorRequestCorrelationCache}
     */
    public void setConnectorRequestCorrelationCache(Cache<String, StoredAuthenticationRequest> connectorRequestCorrelationCache) {
        this.connectorRequestCorrelationCache = connectorRequestCorrelationCache;
    }

    @Override
    public String toString() {
        return "ConnectorControllerService{" +
                ", assertionConsUrl='" + assertionConsUrl + '\'' +
                ", connectorService=" + connectorService +
                ", specificSpRequestCorrelationCache=" + specificSpRequestCorrelationCache +
                ", connectorRequestCorrelationMap=" + connectorRequestCorrelationCache +
                '}';
    }
}
