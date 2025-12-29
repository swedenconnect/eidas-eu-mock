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

package eu.eidas.telemetry.servlet;

import eu.eidas.telemetry.HealthCheckResult;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet that exposes a simple health status based on Micrometer metrics.
 * <p>
 * It verifies whether the node is properly connected to at least one external
 * distributed cache node via the communication cache. The health status is reported
 * as UP when the connection is intact, and DOWN when no external connections are detected.
 * The servlet returns a JSON response including failure reasons when applicable.
 */
public class HealthStatusServlet extends HttpServlet {

    private static final Logger LOG = LoggerFactory.getLogger(HealthStatusServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.info("Received health check request");

        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        MeterRegistry registry = context.getBean(MeterRegistry.class);

        HealthCheckResult result = checkHealthIndicators(registry);

        resp.setContentType("application/json");
        resp.setStatus(HttpServletResponse.SC_OK);

        JsonbConfig config = new JsonbConfig().withFormatting(true);
        try (Jsonb jsonb = JsonbBuilder.create(config);
             PrintWriter writer = resp.getWriter()) {

            if (result.healthy()) {
                LOG.info("Health status: UP");
                writer.write(jsonb.toJson(Map.of("status", "UP")));
            } else {
                LOG.warn("Health status: DOWN, reasons: {}", result.failureReasons());
                Map<String, Object> responseBody = new LinkedHashMap<>();
                responseBody.put("status", "DOWN");
                responseBody.put("reasons", result.failureReasons());
                writer.write(jsonb.toJson(responseBody));
            }
        } catch (Exception e) {
            throw new ServletException("Failed to serialize health check response to JSON", e);
        }
    }

    /**
     * Evaluates the node's connectivity to external distributed cache nodes
     * using Micrometer metrics.
     * <p>
     * The method checks the values of {@code cache.cluster.remotes} for both
     * internal and specific communication caches, and determines whether there
     * is at least one external connection present. If no such connection exists,
     * a failure reason is recorded.
     *
     * @param registry the Micrometer {@link MeterRegistry} containing the cache cluster metrics
     * @return a {@link HealthCheckResult} indicating whether the node is healthy and listing failure reasons if any
     */
    public HealthCheckResult checkHealthIndicators(MeterRegistry registry) {
        List<String> failureReasons = new ArrayList<>();

        try {
            final Gauge internalCacheReplicas = registry.find("cache.cluster.remotes")
                    .tag("specificCommunication", "false")
                    .gauge();

            final Gauge specificCommunicationConnectedApps = registry.find("cache.cluster.remotes")
                    .tag("specificCommunication", "true")
                    .gauge();

            if (internalCacheReplicas != null && specificCommunicationConnectedApps != null) {
                final double externalApps = specificCommunicationConnectedApps.value() - internalCacheReplicas.value();
                if (externalApps < 1) {
                    failureReasons.add("Specific Communication interrupted: Not connected to external distributed cache nodes");
                }
            }

        } catch (Exception ex) {
            failureReasons.add("Exception while checking health indicators: " + ex.getMessage());
            LOG.error("Exception during health check", ex);
        }

        boolean isHealthy = failureReasons.isEmpty();
        if (isHealthy) {
            LOG.info("Health check passed: system is healthy");
        } else {
            LOG.warn("Health check failed: {}", String.join("; ", failureReasons));
        }

        return new HealthCheckResult(isHealthy, failureReasons);
    }

}
