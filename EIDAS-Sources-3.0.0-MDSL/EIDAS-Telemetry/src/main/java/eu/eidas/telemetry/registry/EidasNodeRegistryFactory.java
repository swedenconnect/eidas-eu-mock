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

package eu.eidas.telemetry.registry;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;

import java.util.Arrays;
import java.util.List;

/**
 * Factory for assembling a composite Micrometer {@link io.micrometer.core.instrument.MeterRegistry}
 * used by the eIDAS Node for telemetry and monitoring.
 * <p>
 * This class supports binding JVM, system, application-specific, and cache metrics
 * via {@link io.micrometer.core.instrument.binder.MeterBinder} instances provided
 * at configuration time.
 */
public class EidasNodeRegistryFactory {

    private final CompositeMeterRegistry eidasNodeRegistry = new CompositeMeterRegistry();

    public EidasNodeRegistryFactory(MeterRegistry... meterRegistries) {
        Arrays.asList(meterRegistries).forEach(eidasNodeRegistry::add);
    }

    public MeterRegistry getInstance() {
        return eidasNodeRegistry;
    }

    public void setMeterBinders(List<MeterBinder> meterBinders) {
        meterBinders.forEach(meterBinder -> meterBinder.bindTo(eidasNodeRegistry));
    }
}
