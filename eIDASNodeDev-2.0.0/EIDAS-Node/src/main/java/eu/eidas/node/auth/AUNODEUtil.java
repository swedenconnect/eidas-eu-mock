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
package eu.eidas.node.auth;

import eu.eidas.auth.commons.cache.ConcurrentMapService;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract part used for the anti replay cache.
 */
public abstract class AUNODEUtil {

    private ConcurrentMapService concurrentMapService;
    public abstract Properties getConfigs() ;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUNODEUtil.class.getName());

    private ConcurrentMap<String, Boolean> antiReplayCache;

    public void setConcurrentMapService(ConcurrentMapService concurrentMapService) {
        this.concurrentMapService = concurrentMapService;
    }

    public ConcurrentMapService getConcurrentMapService() {
        return concurrentMapService;
    }

    public void setAntiReplayCache(ConcurrentMap<String, Boolean> antiReplayCache) {
        this.antiReplayCache = antiReplayCache;
    }

    public void flushReplayCache(){
        if (antiReplayCache != null){
            antiReplayCache.clear();
        }
    }

    /**
     * Method used to check if the saml request has not already been processed (replay attack)
     * @param samlId the SAMLID (uuid) processed
     * @param citizenCountryCode the citizen country code
     * @return true if the request has not yet been processed by the system
     */
    public Boolean checkNotPresentInCache(final String samlId, final String citizenCountryCode){
        if (antiReplayCache==null) {
            throw new EIDASSAMLEngineRuntimeException("Bad configuration for the distributed cache, method should set the concurrentMap");
        }
        if (null != samlId){
            Boolean replayAttack = antiReplayCache.putIfAbsent(citizenCountryCode + "/" + samlId, Boolean.TRUE);

            if (null != replayAttack) {
                LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "Replay attack : Checking in Eidas Node antiReplayCache for samlId " + samlId + " ! ");
                return Boolean.FALSE;
            }
            LOG.debug("Checking in Eidas Node antiReplayCache for samlId " + samlId + " : ok");
        }
        return Boolean.TRUE;
    }

}
