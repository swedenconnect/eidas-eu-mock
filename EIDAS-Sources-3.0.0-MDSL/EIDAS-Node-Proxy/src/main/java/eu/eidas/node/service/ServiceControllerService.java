/*
 * Copyright (c) 2018 by European Commission
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
package eu.eidas.node.service;

import eu.eidas.auth.commons.tx.StoredAuthenticationRequest;
import eu.eidas.node.auth.service.ISERVICEService;

import javax.cache.Cache;

public class ServiceControllerService {

    private Cache<String, StoredAuthenticationRequest> proxyServiceRequestCorrelationMap;

    public Cache<String, StoredAuthenticationRequest> getProxyServiceRequestCorrelationCache() {
        return proxyServiceRequestCorrelationMap;
    }

    public void setProxyServiceRequestCorrelationCache(Cache<String, StoredAuthenticationRequest> proxyServiceRequestCorrelationCache) {
        this.proxyServiceRequestCorrelationMap = proxyServiceRequestCorrelationCache;
    }

    /**
     * ProxyService service.
     */
    private transient ISERVICEService proxyService;

    public ISERVICEService getProxyService() {
        return proxyService;
    }

    public void setProxyService(ISERVICEService proxyService) {
        this.proxyService = proxyService;
    }

    @Override
    public String toString() {
        return "ServiceControllerService{" +
                ", proxyService=" + proxyService +
                ", proxyServiceRequestCorrelationMap=" + proxyServiceRequestCorrelationMap +
                '}';
    }
}
