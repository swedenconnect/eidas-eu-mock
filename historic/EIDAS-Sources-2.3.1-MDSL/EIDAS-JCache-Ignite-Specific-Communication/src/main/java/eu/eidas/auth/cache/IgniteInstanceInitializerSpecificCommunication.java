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

package eu.eidas.auth.cache;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Initialize Ignite by configuration
 */
public class IgniteInstanceInitializerSpecificCommunication {

    private static final Logger LOG = LoggerFactory.getLogger(IgniteInstanceInitializerSpecificCommunication.class.getName());

    protected static String configFileName;

    private static Ignite instance;

    private static String CONFIG_BEAN_ID = "igniteSpecificCommunication.cfg";

    public void initializeInstance() throws FileNotFoundException {
        if (null == instance) {
            Path path = Paths.get(configFileName);
            if (Files.exists(path)) {
                File file = new File(configFileName);
                FileInputStream springXmlStream = new FileInputStream(file);
                final IgniteConfiguration icfg = Ignition.loadSpringBean(springXmlStream, CONFIG_BEAN_ID);
                instance = Ignition.start(icfg);
            }
        }
    }


    public void destroyInstance() {
        instance = null;
    }

    public Ignite getInstance() {
        return instance;
    }

    public void setConfigFileName(String configFileName) {
        this.configFileName = configFileName;
    }

}
