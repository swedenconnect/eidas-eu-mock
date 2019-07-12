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

package eu.eidas.listener;

import org.apache.ignite.Ignition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Servlet Context listener implementation to allow the execution of code
 * when the Servlet context is initialized or when it is destroyed for Ignite instances.
 */
@WebListener
public class IgniteServletContextListenerImpl implements ServletContextListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgniteServletContextListenerImpl.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        shutdownIgniteInstance();
    }

    /**
     * Shutdowns Ignite.
     */
    private void shutdownIgniteInstance() {
        Ignition.stopAll(true);
        getLogger().info("Stop has been executed for all started grids in current JVMIgnite instances in this JVM.");
    }

    private static Logger getLogger() {
        return LOGGER;
    }
}
