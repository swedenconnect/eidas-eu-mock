/*
 * Copyright (c) 2025 by European Commission
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

package eu.eidas.telemetry.tomcat;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import jakarta.servlet.ServletContext;
import org.apache.catalina.Manager;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Class for registering Tomcat session metrics at application startup and context refresh.
 * <p>
 * This class ensures that metrics related to Tomcat session management
 * are correctly registered using Micrometer. It is triggered automatically when the Spring context is initialized
 * at startup and whenever it is refreshed.
 * <p>
 * Depends on Tomcat internals, allowing compatibility across different servlet containers
 * without direct Tomcat dependencies.
 * </p>
 * <p>
 * Registered as a Spring bean in {@code applicationContext.xml}.
 */
public class TomcatMetricsRegistrar implements MeterBinder {

    private static final Logger LOG = LoggerFactory.getLogger(TomcatMetricsRegistrar.class);
    private static final String TOMCAT_RESOURCES_ATTRIBUTE = "org.apache.catalina.resources";
    private final ServletContext servletContext;

    private String applicationName = "eidasNode";


    public TomcatMetricsRegistrar(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        registerTomcatMetrics(servletContext, registry);
    }

    /**
     * Registers Tomcat session metrics using Micrometer.
     * <p>
     * This method dynamically interacts with Tomcat internals via reflection to extract session management data.
     * </p>
     *
     * @param servletContext the {@link ServletContext} providing access to Tomcat internals
     */
    private void registerTomcatMetrics(ServletContext servletContext, MeterRegistry registry) {
        try {
            if (null == servletContext) {
                LOG.warn("ServletContext is null. Skipping metrics registration.");
                return;
            }

            final Object tomcatStandardRoot = servletContext.getAttribute(TOMCAT_RESOURCES_ATTRIBUTE);
            if (tomcatStandardRoot instanceof StandardRoot standardRoot) {
                final Manager manager = standardRoot.getContext().getManager();
                TomcatMetrics.monitor(registry, manager, Tags.of("application", applicationName));
                removeObsoleteServletMetrics(servletContext, registry);

                LOG.info("Tomcat session metrics registered successfully.");
            } else {
                LOG.info("Tomcat metrics not available.");
            }
        } catch (Exception e) {
            LOG.warn("Could not register Tomcat session metrics: {}", e.getMessage(), e);
        }
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * Removes Micrometer servlet-related metrics that are not relevant for application monitoring,
     * including obsolete metrics from unregistered servlets and container-generated metrics for
     * "default" and "jsp" servlets. This avoids confusion and ensures that only metrics for
     * application-specific servlets remain after context reloads.
     *
     * @param servletContext the current servlet context containing registered servlets
     * @param registry       the Micrometer MeterRegistry to clean
     */
    private void removeObsoleteServletMetrics(ServletContext servletContext, MeterRegistry registry) {
        Set<String> allowedServlets = servletContext.getServletRegistrations().keySet();
        Set<String> excludedServlets = new HashSet<>(Arrays.asList("default", "jsp"));

        registry.getMeters().stream()
                .filter(meter -> meter.getId().getName().startsWith("tomcat.servlet."))
                .filter(meter -> {
                    String servletName = meter.getId().getTag("name");
                    return excludedServlets.contains(servletName) || !allowedServlets.contains(servletName);
                })
                .forEach(registry::remove);
    }
}
