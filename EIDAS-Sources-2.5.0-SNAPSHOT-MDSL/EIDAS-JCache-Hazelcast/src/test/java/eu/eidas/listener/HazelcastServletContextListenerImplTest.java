/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.listener;

import org.junit.Test;

import javax.servlet.ServletContextEvent;

/**
 * Test class for {@link HazelcastServletContextListenerImpl}
 */
public class HazelcastServletContextListenerImplTest {

    /**
     * Test method for
     * {@link HazelcastServletContextListenerImpl#contextInitialized(ServletContextEvent)}
     * when {@link ServletContextEvent} as parameter is null
     * <p/>
     * Must succeed.
     */
    @Test
    public void contextInitialized() {
        HazelcastServletContextListenerImpl hazelcastServletContextListenerImpl = createHazelcastServletContextListenerImpl();
        hazelcastServletContextListenerImpl.contextInitialized(null);
    }

    /**
     * Test method for
     * {@link HazelcastServletContextListenerImpl#contextDestroyed(ServletContextEvent)}
     * when {@link ServletContextEvent} as parameter is null
     * <p/>
     * Must succeed.
     */
    @Test
    public void contextDestroyed() {
        HazelcastServletContextListenerImpl hazelcastServletContextListenerImpl = createHazelcastServletContextListenerImpl();
        hazelcastServletContextListenerImpl.contextDestroyed(null);
    }

    /**
     * Auxiliary method to create an instance of {@link HazelcastServletContextListenerImpl}
     *
     * @return the instance of {@link HazelcastServletContextListenerImpl}
     */
    private HazelcastServletContextListenerImpl createHazelcastServletContextListenerImpl() {
        return new HazelcastServletContextListenerImpl();
    }
}
