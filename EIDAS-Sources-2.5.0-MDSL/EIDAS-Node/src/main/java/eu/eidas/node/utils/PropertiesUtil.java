/*
 * Copyright (c) 2020 by European Commission
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
package eu.eidas.node.utils;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.EidasParameterKeys;
import eu.eidas.auth.commons.IEIDASConfigurationProxy;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.node.auth.AUNODEUtil;
import eu.eidas.node.auth.connector.AUCONNECTORUtil;
import eu.eidas.node.auth.service.AUSERVICEUtil;
import eu.eidas.node.logging.LoggingMarkerMDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static eu.eidas.node.BeanProvider.getBean;

/**
 * Util to retrieve a property value. Contains the properties loaded by the placeholderConfig
 * bean on spring initialization
 */
public class PropertiesUtil extends PropertyPlaceholderConfigurer implements IEIDASConfigurationProxy {
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesUtil.class.getName());
    private static Map<String, String> propertiesMap = new HashMap<>();
    private static String eidasXmlLocation=null;
    private static final String MASTER_CONF_FILE="eidas.xml";
    private static final String MASTER_CONF_FILE_PARAM="eidas.engine.repo";

    @Override
    public void setLocations(Resource... locations) {
        super.setLocations(locations);
        List<Resource> locations1 = new ArrayList<>();
        for(Resource location:locations){
            locations1.add(location);
            try {
                if (location.getURL() != null && location.getFilename()!=null && MASTER_CONF_FILE.equalsIgnoreCase(location.getFilename())) {
                    PropertiesUtil.setEidasXmlLocation(location.getURL().toString());
                }
            }catch(IOException ioe){
                LOG.error("cannot retrieve the url of "+MASTER_CONF_FILE+" {}",ioe);
            }
        }
    }
    private static void setEidasXmlLocation(String location){
        eidasXmlLocation = location;
    }

    private static void initProps(Properties props){
        LOG.info(LoggingMarkerMDC.SYSTEM_EVENT, "Loading properties");
        propertiesMap = new HashMap<>();
        for (Object key : props.keySet()) {
            String keyStr = key.toString();
            propertiesMap.put(keyStr, props.getProperty(keyStr));
        }
        if(eidasXmlLocation!=null && !props.containsKey(MASTER_CONF_FILE_PARAM)){
            String fileRepositoryDir=eidasXmlLocation.substring(0, eidasXmlLocation.length() - MASTER_CONF_FILE.length());
            propertiesMap.put(MASTER_CONF_FILE_PARAM, fileRepositoryDir);
            props.put(MASTER_CONF_FILE_PARAM, fileRepositoryDir);
        }

    }
    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory,
                                     Properties props) throws BeansException {
        super.processProperties(beanFactory, props);
        PropertiesUtil.initProps(props);

    }

  public static String getProperty(String name) {
    return propertiesMap.get(name);
  }
    public String getEidasParameterValue(String parameterName){
        return PropertiesUtil.getProperty(parameterName);
    }

    private static String getConnectorConfigParameter(EidasParameterKeys parameter) {
        return getConnectorConfigParameter(parameter.toString());
    }

    private static String getConnectorConfigParameter(String parameterName) {
        AUCONNECTORUtil util = getBean(AUCONNECTORUtil.class);
        String value = getConfigParameter(parameterName, util);
        return value;
    }

    private static String getConfigParameter(String parameterName, AUNODEUtil util) {
        String value = null;
        if (util != null && util.getConfigs() != null) {
            value = util.getConfigs().getProperty(parameterName);
        }
        return value;
    }

    /**
     * Verify if Connector instance should be active.
     * If eidas parameter 'active.module.connector' is present and false
     * Then this method will throw an EidasNodeException.
     */
    public static void checkConnectorActive() {
        String active = getConnectorConfigParameter(EidasParameterKeys.EIDAS_CONNECTOR_ACTIVE);
        if (active != null && !Boolean.valueOf(active)) {
            String msg = "Connector module is inactive by configuration setting";
            LOG.warn(msg);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID.errorCode()), EidasErrors.get(EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID.errorMessage()));
        }
    }

    private static String getProxyServiceConfigParameter(EidasParameterKeys parameter) {
        return getProxyServiceConfigParameter(parameter.toString());
    }

    private static String getProxyServiceConfigParameter(String parameterName) {
        AUSERVICEUtil util = getBean(AUSERVICEUtil.class);
        String value = getConfigParameter(parameterName, util);
        return value;
    }

    /**
     * Verify if ProxyService instance should be active.
     * If eidas parameter 'active.module.service' is present and false
     * Then this method will throw an EidasNodeException.
     */
    public static void checkProxyServiceActive() {
        String active = getProxyServiceConfigParameter(EidasParameterKeys.EIDAS_SERVICE_ACTIVE);
        if (active != null && !Boolean.valueOf(active)) {
            String msg = "ProxyService module is inactive by configuration setting";
            LOG.warn(msg);
            throw new EidasNodeException(EidasErrors.get(EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID.errorCode()), EidasErrors.get(EidasErrorKey.SP_COUNTRY_SELECTOR_INVALID.errorMessage()));
        }
    }

    /**
     * Get the value of the metadata enabled parameter.
     * @return true if the metadata of the node are enabled, false otherwise.
     * @deprecated use {@link #isConnectorMetadataEnabled()} or {@link #isProxyServiceMetadataEnabled()} instead.
     */
    @Deprecated
    public static boolean isMetadataEnabled(){
        return getConfigParameterBooleanValue(EidasParameterKeys.METADATA_ACTIVE.toString());
    }

    /**
    * Get the value of the metadata enabled parameter for the connector.
    * @return true if the metadata of the connector are enabled, false otherwise.
    */
    public static boolean isConnectorMetadataEnabled() {
        String isConnectorMetadataEnabledParamValue = getConnectorConfigParameter(EidasParameterKeys.METADATA_ACTIVE);
        return Boolean.parseBoolean(isConnectorMetadataEnabledParamValue);
    }

    /**
     * Get the value of the metadata enabled parameter for the proxy service.
     * @return true if the metadata of the ProxyService are enabled, false otherwise.
     */
    public static boolean isProxyServiceMetadataEnabled() {
        String isProxyServiceMetadataEnabledParamValue = getProxyServiceConfigParameter(EidasParameterKeys.METADATA_ACTIVE);
        return Boolean.parseBoolean(isProxyServiceMetadataEnabledParamValue);
    }

    private static boolean getConfigParameterBooleanValue(String paramName) {
        String active = getConnectorConfigParameter(paramName);
        return Boolean.parseBoolean(active);
    }

    public static String getEidasXmlLocation(){
        if(propertiesMap.containsKey(MASTER_CONF_FILE_PARAM)){
            return propertiesMap.get(MASTER_CONF_FILE_PARAM);
        }
        return null;
    }
}

