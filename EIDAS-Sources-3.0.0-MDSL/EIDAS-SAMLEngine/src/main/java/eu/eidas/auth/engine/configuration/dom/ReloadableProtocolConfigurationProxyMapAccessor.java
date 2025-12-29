/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.engine.configuration.dom;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Each interface component (SamlEngineCoreProperties, SamlEngineSignI, SamlEngineEncryptionI, ExtensionProcessorI) in a
 * configuration can be cached as a reference in client code, therefore each such interface must be a proxy capable of
 * reloading its configuration itself.
 * <p>
 * This class builds cached dynamic proxies aware of the reloading process.
 *
 * @since 1.1
 */
final class ReloadableProtocolConfigurationProxyMapAccessor
        implements SingletonAccessor<Map<String, ProtocolEngineConfiguration>> {

    @Nonnull
    private static Map<String, ProtocolEngineConfiguration> buildProxyMap(
            @Nonnull Map<String, ProtocolEngineConfiguration> map,
            @Nonnull SingletonAccessor<Map<String, ProtocolEngineConfiguration>> accessor) {
        Map<String, ProtocolEngineConfiguration> resultMap = new LinkedHashMap<>();

        for (final Map.Entry<String, ProtocolEngineConfiguration> entry : map.entrySet()) {
            String name = entry.getKey();
            resultMap.put(name, ReloadableProtocolConfigurationInvocationHandler.newConfigurationProxy(name, accessor));
        }
        return resultMap;
    }

    @Nonnull
    private final SingletonAccessor<Map<String, ProtocolEngineConfiguration>> fileAccessor;

    @Nonnull
    private final Map<String, ProtocolEngineConfiguration> cachedMap;

    @Nonnull
    private final Map<String, ProtocolEngineConfiguration> proxyMap;

    ReloadableProtocolConfigurationProxyMapAccessor(
            @Nonnull SingletonAccessor<Map<String, ProtocolEngineConfiguration>> fileAccessor) {
        this.fileAccessor = fileAccessor;
        try {
            cachedMap = fileAccessor.get();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration: " + e, e);
        }
        proxyMap = buildProxyMap(cachedMap, fileAccessor);
    }

    @Nullable
    @Override
    public Map<String, ProtocolEngineConfiguration> get() throws IOException {
        Map<String, ProtocolEngineConfiguration> currentValue = fileAccessor.get();
        if (currentValue == cachedMap) {
            return proxyMap;
        }
        return buildProxyMap(currentValue, fileAccessor);
    }

    @Override
    public void set(@Nonnull Map<String, ProtocolEngineConfiguration> newValue)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
