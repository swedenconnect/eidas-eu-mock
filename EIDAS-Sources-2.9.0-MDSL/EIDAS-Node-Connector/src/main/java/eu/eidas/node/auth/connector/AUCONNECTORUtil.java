/*
 * Copyright (c) 2024 by European Commission
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
import eu.eidas.auth.commons.RequestState;
import eu.eidas.auth.commons.WebRequest;
import eu.eidas.auth.commons.cache.ConcurrentCacheService;
import eu.eidas.auth.commons.light.ILightRequest;
import eu.eidas.node.auth.AUNODEUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This Util class is used by {@link AUCONNECTORSAML} to get a configuration from
 * a loaded configuration file or to validate the SP.
 *
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
     * Loads the URL of a ServiceProxy metadata, with the Id serviceId, from the properties file.
     *
     * @param serviceId the Id of the ServiceProxy.
     * @return String with the URL of the ServiceProxy metadata. null if no URL was found.
     */
    public String loadConfigServiceMetadataURL(final String serviceId) {
        return loadServiceAttribute(serviceId, "metadata.url");
    }

    /**
     * Loads all the metadata URLs configured
     * */
    @Nonnull
    public List<String> loadConfigServiceMetadataURLs() {
        final int nServices = Integer.parseInt(configs.getProperty(EidasParameterKeys.EIDAS_NUMBER.toString()));
        final List<String> urls = new ArrayList<>(nServices);

        // load URL
        for (int i = 1; i <= nServices; i++) {
            final String serviceCons = EIDASValues.EIDAS_SERVICE_PREFIX.index(i);
            if (configs.containsKey(serviceCons)) {
                final String metadataUrl = configs.getProperty(EIDASValues.EIDAS_SERVICE_PREFIX.attribute("metadata.url", i));
                urls.add(metadataUrl);
            }
        }

        return urls;
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
     * @param skewType the skew time
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
     * Checks if a web request contains levels of assurance.
     *
     * @param requestState request state contained in the web request.
     * @return true if the request state contains levels of assurance; false otherwise.
     */
    public boolean validateRequestHasLoas(RequestState requestState) {
        List<String> spLoAs = requestState.getLevelsOfAssurance();

        return spLoAs != null && !spLoAs.isEmpty();
    }

    /**
     * Gets the Destination Country Code from the request.
     * Can be given as parameter "country" in the web request
     * or as the citizen country code in the light request.
     *
     * The country code may be suffixed with -EIDASNODE, this suffix is removed in this process.
     *
     * @param lightRequest The light authentication Request object.
     * @param webRequest the webRequest.
     * @return the country code value.
     */
    public String getCountryCode(ILightRequest lightRequest, WebRequest webRequest) {
        // Country: Mandatory if the destination is a ProxyService.
        String serviceCode;
        if (lightRequest.getCitizenCountryCode() == null) {
            serviceCode = webRequest.getEncodedLastParameterValue(EidasParameterKeys.COUNTRY);

        } else {
            serviceCode = lightRequest.getCitizenCountryCode();
        }

        // Compatibility
        if (null != serviceCode && serviceCode.endsWith(EIDASValues.EIDAS_SERVICE_SUFFIX.toString())) {
            serviceCode = serviceCode.replace(EIDASValues.EIDAS_SERVICE_SUFFIX.toString(), StringUtils.EMPTY);
        }

        return serviceCode;
    }

    /**
     * Get the SP type of the connector
     *
     * @return the SpType value is defined and not blank, null otherwise
     */
    public String getSPType() {
        final String nodeSpType = configs.getProperty(EIDASValues.EIDAS_SPTYPE.toString());
        return StringUtils.isBlank(nodeSpType) ? null : nodeSpType;
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
}
