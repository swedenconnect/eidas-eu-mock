/*
 * Copyright (c) 2015 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

package eu.eidas.node.utils;

import eu.eidas.node.logging.LoggingMarkerMDC;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * property loader for integration plugins
 */
public class PluginPropertyLoader extends PropertyPlaceholderConfigurer  {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginPropertyLoader.class.getName());
    String locationProp=null;
    String[] locationNames=null;
    Resource[] locations=null;
    public void setLocationProp(String locationProp){
        this.locationProp=locationProp;
    }
    public void setLocationNames(String[] locations){
        if(locations!=null) {
            locationNames = new String[locations.length];
            System.arraycopy(locations, 0, locationNames, 0, locationNames.length);
        }
    }
    @Override
    protected String resolvePlaceholder(String placeholder, Properties props){
        String result=super.resolvePlaceholder(placeholder, props);
        if(result==null || result.isEmpty()) {
            result = PropertiesUtil.getProperty(placeholder);
        }
        if(result==null) {
            result = "";
        }
        return result;
    }

    @Override
    protected void loadProperties(Properties props)
            throws IOException{
        updatePropLocation();
        LOGGER.info(LoggingMarkerMDC.SYSTEM_EVENT, "Loading properties");
        if(props!=null && locations!=null && locations.length>0) {
            super.loadProperties(props);
        }
    }
    private boolean isLocationReadable(String locationPropValue){
        for(int i=0;i<locationNames.length && locationPropValue!=null;i++) {
            if(!locations[0].isReadable()){
                LOGGER.error("Not readable config file "+locationNames[i]);
                locations=null;
                return false;
            }
        }
        return true;

    }
    private void updatePropLocation(){
        if(locationProp!=null && locationNames!=null && locationNames.length>0){
            List<Resource> loadedLocations=new ArrayList<Resource>();
            DefaultResourceLoader drl=new DefaultResourceLoader();
            String locationPropValue=PropertiesUtil.getProperty(locationProp);
            for(int i=0;i<locationNames.length && locationPropValue!=null;i++){
                String currentLocation=locationPropValue+locationNames[i];
                loadedLocations.add(drl.getResource("file:"+currentLocation.replace(File.separatorChar, '/')));
            }
            locations=new Resource[loadedLocations.size()];
            loadedLocations.toArray(locations);
            if(isLocationReadable(locationPropValue) && locations.length>0) {
                setLocations(locations);
            }
        }
    }
}
