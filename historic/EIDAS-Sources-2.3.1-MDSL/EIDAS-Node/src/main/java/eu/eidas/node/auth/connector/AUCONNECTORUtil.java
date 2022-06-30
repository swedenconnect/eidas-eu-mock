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
package eu.eidas.node.auth.connector;

import eu.eidas.auth.commons.EIDASValues;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.auth.commons.cache.ConcurrentMapService;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.node.auth.AUNODEUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * This Util class is used by {@link AUCONNECTORSAML} and {@link AUCONNECTORCountrySelector} to get a configuration from
 * a loaded configuration file or to validate the SP.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.7 $, $Date: 2011-02-18 02:02:39 $
 */
public final class AUCONNECTORUtil extends AUNODEUtil {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AUCONNECTORUtil.class.getName());

    /**
     * Configuration file.
     */
    private Properties configs;

    /**
     * Bypass all SP validations?
     */
    private boolean bypassValidation;

    /**
     * Minimum QAA Level Allowed.
     */
    private int minQAA;

    /**
     * Maximum QAA Level Allowed.
     */
    private int maxQAA;

    /**
     * Local Skew constants
     */
    public enum CONSUMER_SKEW_TIME {
        BEFORE, AFTER
    }

    public AUCONNECTORUtil() {
        // default constructor for use without concurrentMapService
    }

    public AUCONNECTORUtil(final ConcurrentCacheService concurrentCacheService) {
        // Obtaining the anti-replay cache service provider defined in configuration and call it for setting up cache
        setAntiReplayCache(concurrentCacheService.getConfiguredCache());
    }

    /**
     * Loads a specific property.
     *
     * @param configKey the key of the property to load.
     * @return String containing the value of the property.
     */
    public String loadConfig(final String configKey) {
        LOG.debug("Loading config file " + configKey);
        return getConfigs().getProperty(configKey);
    }

    /**
     * Loads the URL of a ServiceProxy, with the Id serviceId, from the properties file.
     *
     * @param serviceId the Id of the ServiceProxy.
     * @return String with the URL of the ServiceProxy. null if no URL was found.
     */
    public String loadConfigServiceURL(final String serviceId) {
        return loadServiceAttribute(serviceId, "url");
    }

    public String loadConfigServiceMetadataURL(final String pepId) {
        return loadServiceAttribute(pepId, "metadata.url");
    }

    private String loadServiceAttribute(final String pepId, String paramName) {
        String retVal = null;
        final int nServices = Integer.parseInt(configs.getProperty(EidasParameterKeys.EIDAS_NUMBER.toString()));
        LOG.debug("Number of Service: " + nServices);

        // load URL
        for (int i = 1; i <= nServices && retVal == null; i++) {
            final String serviceCons = EIDASValues.EIDAS_SERVICE_PREFIX.index(i);
            if (configs.containsKey(serviceCons) && configs.getProperty(serviceCons).equals(pepId)) {
                retVal = configs.getProperty(EIDASValues.EIDAS_SERVICE_PREFIX.attribute(paramName, i));
                LOG.debug("Service URL " + retVal);
            }
        }

        return retVal;

    }

    /**
     * Loads the skew time of a ProxyService, with the Id serviceId, from the properties file.
     *
     * @param serviceId the Id of the ProxyService.
     * @return String with the URL of the ProxyService. null if no URL was found.
     */
    public Long loadConfigServiceTimeSkewInMillis(final String serviceId, CONSUMER_SKEW_TIME skewType) {
        LOG.trace("loadConfigServiceTimeSkewInMillis");
        Long retVal = null;
        if (StringUtils.isEmpty(serviceId)) {
            LOG.info("BUSINESS EXCEPTION : the serviceId is empty or null !");
            return Long.valueOf(0);
        }
        final int nServices = Integer.parseInt(configs.getProperty(EidasParameterKeys.EIDAS_NUMBER.toString()));
        LOG.debug("Number of Services: " + nServices);
        for (int i = 1; i <= nServices && retVal == null; i++) {
            final String serviceCons = EIDASValues.EIDAS_SERVICE_PREFIX.index(i);
            if (configs.containsKey(serviceCons) && configs.getProperty(serviceCons).equals(serviceId)) {
                String skew = null;
                if (skewType == CONSUMER_SKEW_TIME.BEFORE) {
                    skew = configs.getProperty(EIDASValues.EIDAS_SERVICE_PREFIX.beforeSkew(i));
                } else {
                    skew = configs.getProperty(EIDASValues.EIDAS_SERVICE_PREFIX.afterSkew(i));
                }
                if (StringUtils.isNotEmpty(skew)) {
                    retVal = Long.parseLong(skew);
                } else {
                    retVal = Long.valueOf(0);
                }
            }
        }
        return retVal;
    }

    /**
     * Checks if a specific Service Provider has the required access level and if it is a known Service Provider.
     *
     * @param webRequest A map of attributes.
     * @return true is SP is valid; false otherwise.
     * @see Map
     * @see ICONNECTORSAMLService
     */
    public boolean validateSP(WebRequest webRequest) {
        String spID = webRequest.getRequestState().getSpId();
        String spQAALevel = webRequest.getRequestState().getQaa();
        String spLoA = webRequest.getRequestState().getLevelOfAssurance();
        String loadedSpQAALevel = this.loadConfig(spID + ".qaalevel");

        if (spLoA == null && (!this.isValidQAALevel(spQAALevel) || (!bypassValidation && !this.isValidSPQAALevel(
                spQAALevel, loadedSpQAALevel)))) {

            LOG.info("BUSINESS EXCEPTION : " + spID + " is untrustable or has an invalid QAALevel: " + spQAALevel);
            return false;
        } else if (spLoA != null && LevelOfAssurance.getLevel(spLoA) == null) {
            LOG.info("BUSINESS EXCEPTION : " + spID + " is untrustable or has an invalid LoA: " + spLoA);
            return false;
        }
        LOG.trace("BUSINESS EXCEPTION : " + spID + " is trustable and has either a valid QAALevel: " + spQAALevel
                          + " or a valid LoA: " + spLoA);
        return true;
    }

    /**
     * Checks if the configured QAALevel is greater than minQAALevel and less than maxQAALevel.
     *
     * @param qaaLevel The QAA Level to validate.
     * @return True if the qaaLevel is valid. False otherwise.
     */
    private boolean isValidQAALevel(final String qaaLevel) {
        return StringUtils.isNumeric(qaaLevel) && Integer.parseInt(qaaLevel) >= this.getMinQAA()
                && Integer.parseInt(qaaLevel) <= this.getMaxQAA();
    }

    /**
     * Checks if the requested SP's QAALevel is less than configured SP's QAALevel.
     *
     * @param spQAALevel The QAA Level of the SP.
     * @param confQAALevel The QAA Level from the configurations.
     * @return True if spQAALevel is valid. False otherwise.
     */
    private boolean isValidSPQAALevel(final String spQAALevel, final String confQAALevel) {

        return StringUtils.isNumeric(spQAALevel) && StringUtils.isNumeric(confQAALevel)
                && Integer.parseInt(confQAALevel) >= Integer.parseInt(spQAALevel);
    }

    /**
     * Setter for bypassValidation.
     *
     * @param byPassValidation The bypassValidation to set.
     */
    public void setBypassValidation(final boolean byPassValidation) {
        this.bypassValidation = byPassValidation;
    }

    /**
     * Getter for bypassValidation.
     *
     * @return The bypassValidation value.
     */
    public boolean isBypassValidation() {
        return bypassValidation;
    }

    /**
     * Setter for configs.
     *
     * @param confs The configs to set.
     * @see Properties
     */
    public void setConfigs(final Properties confs) {
        this.configs = confs;
    }

    /**
     * Getter for configs.
     *
     * @return configs The configs value.
     * @see Properties
     */
    public Properties getConfigs() {
        return configs;
    }

    /**
     * Getter for minQAA.
     *
     * @return The minQAA value.
     */
    public int getMinQAA() {
        return minQAA;
    }

    /**
     * Setter for minQAA.
     *
     * @param nMinQAA The new minQAA value.
     */
    public void setMinQAA(final int nMinQAA) {
        this.minQAA = nMinQAA;
    }

    /**
     * Setter for maxQAA.
     *
     * @param nMaxQAA The new maxQAA value.
     */
    public void setMaxQAA(final int nMaxQAA) {
        this.maxQAA = nMaxQAA;
    }

    /**
     * Getter for maxQAA.
     *
     * @return The maxQAA value.
     */
    public int getMaxQAA() {
        return maxQAA;
    }
}
