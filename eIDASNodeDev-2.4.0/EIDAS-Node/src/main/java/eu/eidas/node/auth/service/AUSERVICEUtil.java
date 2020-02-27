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
package eu.eidas.node.auth.service;

import java.util.Properties;

import eu.eidas.auth.commons.cache.ConcurrentCacheService;
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

    public AUSERVICEUtil(final ConcurrentCacheService concurrentCacheService) {
        // Obtaining the anti-replay cache service provider defined in configuration and call it for setting up cache
        setAntiReplayCache(concurrentCacheService.getConfiguredCache());
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
