package eu.eidas.node.auth;

import eu.eidas.auth.commons.EIDASValues;
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

    private static final String FALSE=Boolean.FALSE.toString();

    public Boolean isEidasMessageSupportedOnly(){
        final String eidasOnlyValue = getConfigs().getProperty(EIDASValues.NODE_SUPPORT_EIDAS_MESSAGE_FORMAT_ONLY.toString());
        return !(FALSE.equalsIgnoreCase(eidasOnlyValue));
    }

}
