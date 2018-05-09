package eu.eidas.node.auth.service;

import java.util.Properties;

import eu.eidas.auth.commons.cache.ConcurrentMapService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.protocol.impl.AuthenticationResponse;
import eu.eidas.node.auth.AUNODEUtil;

public class AUSERVICEUtil extends AUNODEUtil {
    /**
     * Logger object.
     */
    protected static final Logger LOGGER = LoggerFactory.getLogger(AUSERVICEUtil.class.getName());
    /**
     * Configuration file.
     */
    private Properties configs;

    public AUSERVICEUtil() {
        // default constructor for use without concurrentMapService
    }

    public AUSERVICEUtil(final ConcurrentMapService concurrentMapService) {
        // Obtaining the anti-replay cache service provider defined in configuration and call it for setting up cache
        setAntiReplayCache(concurrentMapService.getConfiguredMapCache());
    }

    /**
     * Setter for configs.
     * @param confs The configs to set.
     * @see Properties
     */
    public void setConfigs(final Properties confs) {
        this.configs = confs;
    }

    /**
     * Getter for configs.
     * @return configs The configs value.
     * @see Properties
     */
    public Properties getConfigs() {
        return configs;
    }

    /**
     * Obtains the key property value from property file
     * @param key the key
     * @return the value
     * TODO : refactor this
     */
    public String getProperty(String key){
        if (StringUtils.isEmpty(key) || configs==null) {
            LOGGER.error("BUSINESS EXCEPTION : Config file is null {} or key to retrieve is null {}", configs, key);
            return null;
        }
        return configs.getProperty(key);
    }
    public void setMetadatUrlToAuthnResponse(final String metadataUrl, AuthenticationResponse.Builder authnResponseBuilder){
        if(metadataUrl!=null && !metadataUrl.isEmpty()) {
            authnResponseBuilder.issuer(metadataUrl);
        }
    }
}
