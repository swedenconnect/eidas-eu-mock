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

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Test class for {@link EidasNodeTelemetryServlet}
 */
public class EidasNodeTelemetryServletTest {

    private EidasNodeTelemetryServlet servlet;
    private SimpleMeterRegistry testRegistry;
    private MockedStatic<ContextLoader> contextLoaderMock;

    @Before
    public void setUp() throws Exception {
        servlet = new EidasNodeTelemetryServlet();
        testRegistry = new SimpleMeterRegistry();

        Gauge.builder("test.nan.gauge", () -> Double.NaN)
                .description("Gauge returning NaN")
                .register(testRegistry);

        Gauge.builder("test.valid.gauge", () -> 123.45)
                .description("Gauge returning a valid number")
                .register(testRegistry);

        contextLoaderMock = Mockito.mockStatic(ContextLoader.class);
        WebApplicationContext mockContext = Mockito.mock(WebApplicationContext.class);
        Mockito.when(mockContext.getBean(MeterRegistry.class)).thenReturn(testRegistry);
        contextLoaderMock.when(ContextLoader::getCurrentWebApplicationContext).thenReturn(mockContext);
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#doGet(HttpServletRequest, HttpServletResponse)}.
     * This test verifies that NaN values in metric measurements are properly sanitized to 0.0,
     * and that the resulting JSON is valid and serializable.
     * <p>
     * Must succeed.
     */
    @Test
    public void testDoGetSanitizesNaNAndOutputsValidJson() throws Exception {
        final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        Mockito.when(response.getWriter()).thenReturn(writer);

        servlet.doGet(request, response);
        writer.flush();

        String outputJson = stringWriter.toString();
        Jsonb jsonb = JsonbBuilder.create();
        List<?> metrics = jsonb.fromJson(outputJson, List.class);

        Assert.assertEquals(2, metrics.size());
        Assert.assertTrue(outputJson.contains("test.nan.gauge"));
        Assert.assertTrue(outputJson.contains("test.valid.gauge"));
        Assert.assertTrue(outputJson.contains("0.0"));
        Assert.assertTrue(outputJson.contains("123.45"));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that Unix style paths are correctly obfuscated by retaining only the first segment,
     * ensuring sensitive directory structures are not exposed.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathUnixStyle() throws Exception {
        final String inputPath = "/usr/local/bin";
        final String expectedObfuscatedPath = "/usr/";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that short Unix style paths (e.g., "/a") are correctly obfuscated to "/",
     * ensuring consistent handling of minimal directory structures.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathShortUnixStyle() throws Exception {
        final String inputPath = "/a";
        final String expectedObfuscatedPath = "/";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that Windows style paths (e.g., "C:\Program Files\Apache") are correctly obfuscated to "C:\",
     * ensuring sensitive directory structures are not exposed.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathWindowsDrive() throws Exception {
        final String inputPath = "C:\\Program Files\\Apache";
        final String expectedObfuscatedPath = "C:\\";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that Windows style paths using forward slashes (e.g., "D:/Path") are correctly obfuscated to "D:\",
     * ensuring consistent handling of drive letter formats regardless of slash direction.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathWindowsDriveWithSlash() throws Exception {
        final String inputPath = "D:/Path";
        final String expectedObfuscatedPath = "D:\\";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that UNC style paths (e.g., "\\\\server\\share\\folder") are correctly obfuscated to "\\\\",
     * ensuring that network share information is not exposed.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathUNCStyle() throws Exception {
        final String inputPath = "\\\\server\\share\\folder";
        final String expectedObfuscatedPath = "\\\\";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that non-path strings (e.g., "xyz") are obfuscated by retaining only the first two characters,
     * ensuring fallback logic is applied when no recognizable path format is detected.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathFallbackLogic() throws Exception {
        final String inputPath = "xyz";
        final String expectedObfuscatedPath = "xy";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that an empty path string ("") is returned unchanged,
     * ensuring that the obfuscation logic handles empty input safely.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathEmptyInput() throws Exception {
        final String inputPath = "";
        final String expectedObfuscatedPath = "";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that short Unix style paths with a trailing slash (e.g., "/var/")
     * are correctly handled by returning the path unchanged, as there's only one segment.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathShortUnixWithTrailingSlash() throws Exception {
        final String inputPath = "/var/";
        final String expectedObfuscatedPath = "/var/";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that mixed slash Windows paths (e.g., "C:\\Users/user") are correctly
     * obfuscated to the standard Windows drive format "C:\".
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathWindowsMixedSlashes() throws Exception {
        final String inputPath = "C:\\Users/user/AppData";
        final String expectedObfuscatedPath = "C:\\";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that a path with no recognizable format (neither Unix nor Windows)
     * is handled by the fallback logic, which returns the first two characters.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathNonStandardFormat() throws Exception {
        final String inputPath = "ab:c";
        final String expectedObfuscatedPath = "ab";

        Assert.assertEquals(expectedObfuscatedPath, invokeObfuscatePathWithSingleTag(inputPath));
    }

    /**
     * Test method for {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)}.
     * Verifies that a list of tags containing both a "path" tag and a non-path tag
     * is correctly processed: the "path" value is obfuscated, while other tags remain unchanged.
     * <p>
     * Must succeed.
     */
    @Test
    public void testObfuscatePathMultipleTags() throws Exception {
        final Tag pathTag = Tag.of("path", "/usr/local/bin");
        final Tag nameTag = Tag.of("name", "disk.total");
        final List<Tag> inputTags = List.of(pathTag, nameTag);

        final Method obfuscatePathMethod = getObfuscatePathMethod();
        final Iterable<Tag> resultTags = (Iterable<Tag>) obfuscatePathMethod.invoke(servlet, inputTags);
        final List<Tag> resultList = new ArrayList<>();
        resultTags.forEach(resultList::add);

        Assert.assertEquals("/usr/", resultList.get(0).getValue());
        Assert.assertEquals("disk.total", resultList.get(1).getValue());
    }

    /**
     * Helper method to invoke {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)} with a single "path" tag.
     *
     * @param inputPath the input path string to obfuscate
     * @return the obfuscated path value from the resulting tag
     * @throws Exception if reflection fails
     */
    private String invokeObfuscatePathWithSingleTag(final String inputPath) throws Exception {
        final Tag inputTag = Tag.of("path", inputPath);
        final List<Tag> inputTags = List.of(inputTag);

        final Method obfuscatePathMethod = getObfuscatePathMethod();
        final Iterable<Tag> resultTags = (Iterable<Tag>) obfuscatePathMethod.invoke(servlet, inputTags);
        final Tag obfuscatedTag = resultTags.iterator().next();

        return obfuscatedTag.getValue();
    }

    /**
     * Utility method to access the private {@link EidasNodeTelemetryServlet#obfuscatePath(Iterable)} method via reflection.
     * Used in unit tests to verify internal obfuscation logic without changing method visibility.
     *
     * @return a {@link Method} object representing the obfuscatePath method
     * @throws NoSuchMethodException if the method signature is not found
     */
    private Method getObfuscatePathMethod() throws NoSuchMethodException {
        final Method method = EidasNodeTelemetryServlet.class.getDeclaredMethod("obfuscatePath", Iterable.class);
        method.setAccessible(true);
        return method;
    }

    @After
    public void tearDown() {
        contextLoaderMock.close();
    }

}