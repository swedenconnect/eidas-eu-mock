/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.node.connector;

import eu.eidas.auth.commons.tx.CorrelationMap;
import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.auth.commons.tx.StoredLightRequest;
import eu.eidas.node.auth.connector.ICONNECTORService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConnectorControllerService {

    private String assertionConsUrl;

    /**
     * Connector service.
     */
    private ICONNECTORService connectorService;

    private CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap;

    private CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap;

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


    public void setSpecificSpRequestCorrelationMap(CorrelationMap<StoredLightRequest> specificSpRequestCorrelationMap) {
        this.specificSpRequestCorrelationMap = specificSpRequestCorrelationMap;
    }

    public void setConnectorRequestCorrelationMap(CorrelationMap<StoredAuthenticationRequest> connectorRequestCorrelationMap) {
        this.connectorRequestCorrelationMap = connectorRequestCorrelationMap;
    }

    @Override
    public String toString() {
        return "ConnectorControllerService{" +
                ", assertionConsUrl='" + assertionConsUrl + '\'' +
                ", connectorService=" + connectorService +
                ", specificSpRequestCorrelationMap=" + specificSpRequestCorrelationMap +
                ", connectorRequestCorrelationMap=" + connectorRequestCorrelationMap +
                '}';
    }
}
