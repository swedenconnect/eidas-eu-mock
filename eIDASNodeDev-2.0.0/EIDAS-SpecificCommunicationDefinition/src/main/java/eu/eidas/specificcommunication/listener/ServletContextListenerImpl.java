/*
 * Copyright (c) 2017 by European Commission
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

package eu.eidas.specificcommunication.listener;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.LifecycleService;
import eu.eidas.auth.commons.cache.HazelcastInstanceInitializer;
import eu.eidas.specificcommunication.SpecificCommunicationApplicationContextProvider;
import eu.eidas.specificcommunication.SpecificCommunicationDefinitionBeanNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Servlet Context listener implementation to allow the execution of code
 * when the Servlet context is initialized or when it is destroyed.
 */
@WebListener
public class ServletContextListenerImpl implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextListenerImpl.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        shutdownHazelcastInstance();
    }

    /**
     * Shutdowns {@link HazelcastInstance} gracefully, waits for partition operations to be completed.
     */
    private void shutdownHazelcastInstance() {
        final HazelcastInstanceInitializer eidasHazelcastInstanceInitializer =
                (HazelcastInstanceInitializer) SpecificCommunicationApplicationContextProvider.getApplicationContext().getBean(SpecificCommunicationDefinitionBeanNames.EIDAS_HAZELCAST_INSTANCE_INITIALIZER.toString());

        final HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(eidasHazelcastInstanceInitializer.getHazelcastInstanceName());

        final LifecycleService lifecycleService = hazelcastInstance.getLifecycleService();
        if (null != hazelcastInstance && lifecycleService.isRunning()) {
            lifecycleService.shutdown();
            getLogger().info("Shutdown has been executed for hazelcast instance: " + hazelcastInstance.getName());
        }
    }

    private static Logger getLogger() {
        return LOGGER;
    }
}
