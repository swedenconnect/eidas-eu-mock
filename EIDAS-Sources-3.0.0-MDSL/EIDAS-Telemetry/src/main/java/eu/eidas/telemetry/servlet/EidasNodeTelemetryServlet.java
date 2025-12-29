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

import eu.eidas.telemetry.Metric;
import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.ContextLoader;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet that exposes telemetry metrics as JSON.
 * <p>
 * Retrieves all registered Micrometer meters from the Spring application context
 * and serializes them into a JSON response.
 */
public class EidasNodeTelemetryServlet extends HttpServlet {

    /**
     * Handles HTTP GET requests by collecting application metrics,
     * serializing them to JSON, and writing the result to the response.
     *
     * @param req  the HttpServletRequest object
     * @param resp the HttpServletResponse object
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs during response writing
     */
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        MeterRegistry registry = ContextLoader.getCurrentWebApplicationContext().getBean(MeterRegistry.class);

        List<Meter> meterList = registry.getMeters();
        List<Metric> metrics = new ArrayList<>();

        for (Meter meter : meterList) {
            Metric metric = new Metric();
            metric.setName(meter.getId().getName()); //create map of IDs to clearer names
            metric.setDescription(meter.getId().getDescription());
            metric.setUnit(meter.getId().getBaseUnit());
            metric.setTags(meter.getId().getTags());
            metric.setValue(sanitizeMeasurements(meter.measure()));

            if (metric.getName().contains("disk")) {
                metric.setTags(obfuscatePath(metric.getTags()));
            }

            metrics.add(metric);
        }

        resp.setContentType("application/json");
        try (Jsonb jsonb = JsonbBuilder.create();
             PrintWriter out = resp.getWriter()) {
            String json = jsonb.toJson(metrics);
            out.print(json);
        } catch (Exception e) {
            throw new ServletException("Failed to serialize metrics to JSON", e);
        }
    }

    /**
     * Obfuscates the value of tags with the key {@code "path"} to avoid leaking full filesystem details.
     * This method is typically used for disk-related metrics, where absolute paths might otherwise
     * reveal sensitive information. Tags with other keys are returned unchanged.
     * <p>
     * Obfuscation rules are delegated to {@link #obfuscatePathValue(String)}:
     * <ul>
     *   <li>Unix-style paths - keep root and first segment.</li>
     *   <li>Windows drive paths - keep drive root only.</li>
     *   <li>UNC paths - collapse to "\\".</li>
     *   <li>Other values - truncate to first two characters.</li>
     * </ul>
     *
     * @param tags the input tags
     * @return a collection of tags with obfuscated path values where applicable
     */
    private Iterable<Tag> obfuscatePath(Iterable<Tag> tags) {
        List<Tag> out = new ArrayList<>();
        for (Tag tag : tags) {
            if ("path".equals(tag.getKey())) {
                out.add(Tag.of("path", obfuscatePathValue(tag.getValue())));
            } else {
                out.add(tag);
            }
        }
        return out;
    }

    /**
     * Returns an obfuscated version of the given path value to prevent leaking full filesystem details.
     * <ul>
     *   <li>Unix-style paths: keep root and first segment (e.g., "/var/").</li>
     *   <li>Windows drive paths: keep drive root only (e.g., "C:\").</li>
     *   <li>UNC paths: collapse to "\\".</li>
     *   <li>Other values: truncate to the first two characters.</li>
     * </ul>
     *
     * @param value the original path value to obfuscate
     * @return an obfuscated path string with reduced information
     */
    private static String obfuscatePathValue(String value) {
        if (value == null || value.isEmpty()) return value;

        // Unix-style: "/dir/subdir" -> "/dir/"
        if (value.startsWith("/")) {
            int secondSlash = value.indexOf('/', 1);
            return (secondSlash > 0) ? value.substring(0, secondSlash + 1) : "/";
        }

        // UNC: "\\server\share\..." -> "\\"
        if (value.startsWith("\\\\")) {
            return "\\\\";
        }

        // Windows drive: "C:\..." or "D:/..." -> "C:\"
        if (value.length() >= 2 && Character.isLetter(value.charAt(0)) && value.charAt(1) == ':') {
            return Character.toUpperCase(value.charAt(0)) + ":\\";
        }

        // Fallback: first 2 chars
        return value.length() >= 2 ? value.substring(0, 2) : value;
    }

    /**
     * Sanitizes a collection of Measurement objects by replacing any invalid
     * floating-point values (NaN or Infinity) with 0.0 to ensure safe JSON serialization.
     *
     * @param measurements the iterable list of measurements to be checked
     * @return a new iterable of sanitized Measurement instances
     */
    private Iterable<Measurement> sanitizeMeasurements(Iterable<Measurement> measurements) {
        List<Measurement> sanitized = new ArrayList<>();
        for (Measurement m : measurements) {
            double value = m.getValue();
            sanitized.add(new Measurement(() ->
                    (Double.isNaN(value) || Double.isInfinite(value)) ? 0.0 : value,
                    m.getStatistic()
            ));
        }
        return sanitized;
    }

}
