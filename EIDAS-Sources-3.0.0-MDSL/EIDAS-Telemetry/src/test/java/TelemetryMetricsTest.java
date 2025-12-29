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

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.MockClock;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Unit tests for verifying basic SimpleMeterRegistry functionality
 */
public class TelemetryMetricsTest {

    private final MockClock clock = new MockClock();

    /**
     * Verifies that JVM memory metrics can be bound to the SimpleMeterRegistry
     */
    @Test
    public void testJvmMemoryMetrics() {
        final SimpleMeterRegistry registry = createRegistry();
        new JvmMemoryMetrics().bindTo(registry);

        Assert.assertNotNull("JVM memory used metric should exist", registry.find("jvm.memory.used").gauge());
    }

    /**
     * Verifies that system processor metrics can be bound to the SimpleMeterRegistry
     */
    @Test
    public void testProcessorMetrics() {
        final SimpleMeterRegistry registry = createRegistry();
        new ProcessorMetrics().bindTo(registry);

        Assert.assertNotNull("System CPU usage metric should exist", registry.find("system.cpu.usage").gauge());
    }

    /**
     * Verifies that an SimpleMeterRegistry can be created successfully
     */
    @Test
    public void testCreateMeterRegistry() {
        final SimpleMeterRegistry registry = createRegistry();
        Assert.assertNotNull("Registry should not be null", registry);
    }

    /**
     * Verifies basic Counter functionality: incrementing and checking the count
     */
    @Test
    public void testFunctionCounterIncrement() {
        final SimpleMeterRegistry registry = createRegistry();
        final AtomicLong counter = new AtomicLong();

        final FunctionCounter functionCounter = FunctionCounter
                .builder("test.counter", counter, AtomicLong::doubleValue)
                .register(registry);

        counter.addAndGet(3);

        clock.add(Duration.ofMinutes(1));

        Assert.assertEquals("Counter value should be 3.0", 3.0, functionCounter.count(), 0.001);
    }

    /**
     * Verifies that the SimpleMeterRegistry can be closed cleanly
     */
    @Test
    public void testRegistryClose() {
        final SimpleMeterRegistry registry = createRegistry();
        registry.close();
        Assert.assertTrue("Registry should be closed", registry.isClosed());
    }

    /**
     * Creates a basic SimpleMeterRegistry instance with default configuration
     *
     * @return a new SimpleMeterRegistry
     */
    private SimpleMeterRegistry createRegistry() {
        return new SimpleMeterRegistry();
    }
}
