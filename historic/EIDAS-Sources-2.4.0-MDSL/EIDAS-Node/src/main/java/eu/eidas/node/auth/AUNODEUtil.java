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
package eu.eidas.node.auth;

import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.node.logging.LoggingMarkerMDC;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import java.util.Properties;

/**
 * Abstract part used for the anti replay cache.
 */
public abstract class AUNODEUtil {

    public abstract Properties getConfigs() ;

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUNODEUtil.class.getName());

    private Cache<String, Boolean> antiReplayCache;

    public void setAntiReplayCache(Cache antiReplayCache) {
        this.antiReplayCache = antiReplayCache;
    }

    public void flushReplayCache(){
        if (antiReplayCache != null){
            antiReplayCache.clear();
        }
    }

    /**
     * Method used to check if the request/response has not already been processed (replay attack).
     * Possible checked request types are {@link eu.eidas.auth.commons.light.impl.LightRequest} and SAML Request
     * Possible checked response types are {@link eu.eidas.auth.commons.light.impl.LightResponse} and SAML Response
     * @param messageId the SAMLID (uuid) processed
     * @param citizenCountryCode the citizen country code
     * @return true if the request/response has not yet been processed by the system
     */
    public Boolean checkNotPresentInCache(final String messageId, final String citizenCountryCode){
        if (antiReplayCache==null) {
            throw new EIDASSAMLEngineRuntimeException("Bad configuration for the distributed cache, method should set the concurrentMap");
        }
        if (null != messageId){
            final boolean wasAbsent = antiReplayCache.putIfAbsent(citizenCountryCode + "/" + messageId, Boolean.TRUE);
            final boolean isReplayAttack = !wasAbsent;

            if (isReplayAttack) {
                LOG.warn(LoggingMarkerMDC.SECURITY_WARNING, "Replay attack : Checking in Eidas Node antiReplayCache for samlId " + messageId + " ! ");
                return Boolean.FALSE;
            }
            LOG.debug("Checking in Eidas Node antiReplayCache for samlId " + messageId + " : ok");
        }
        return Boolean.TRUE;
    }

    /**
     * Method used to check if the request/response has not already been processed (replay attack).
     * Checked response type is {@link eu.eidas.auth.commons.light.impl.LightResponse}
     * @param messageId the SAMLID (uuid) processed
     * @return true if the request/response has not yet been processed by the system
     */
    public Boolean checkNotPresentInCache(final String messageId){
        return checkNotPresentInCache(messageId, StringUtils.EMPTY);
    }
}
