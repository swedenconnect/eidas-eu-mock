/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.auth.commons.cache;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import eu.eidas.auth.commons.exceptions.InvalidParameterEIDASException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

/**
 * Initialize Hazelcast by configuration
 */
public class HazelcastInstanceInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastInstanceInitializer.class.getName());
    protected String hazelcastConfigfileName;
    protected String hazelcastInstanceName;

    public static HazelcastInstance getInstance(String name) {
        HazelcastInstance instance = Hazelcast.getHazelcastInstanceByName(name);
        if (instance == null) {
            throw new InvalidParameterEIDASException("Distributed Cache Configuration mismatch for HazelCast instance : "+name);
        }
        return instance;
    }

    public void initializeInstance() throws FileNotFoundException {
        Config cfg;
        if (hazelcastConfigfileName != null) {
            LOG.trace("loading hazelcast config from " + hazelcastConfigfileName);
            cfg = new FileSystemXmlConfig(hazelcastConfigfileName);
        } else {
            LOG.trace("loading hazelcast instance '"+hazelcastInstanceName+"' config "+hazelcastConfigfileName);
            cfg = new Config();
        }
        cfg.setInstanceName(hazelcastInstanceName);
        Hazelcast.getOrCreateHazelcastInstance(cfg);
    }

    public void setHazelcastConfigfileName(String configFileName) {
        hazelcastConfigfileName = configFileName;
    }

    public String getHazelcastInstanceName() {
        return hazelcastInstanceName;
    }

    public void setHazelcastInstanceName(String hazelcastInstanceName) {
        this.hazelcastInstanceName = hazelcastInstanceName;
    }

}
