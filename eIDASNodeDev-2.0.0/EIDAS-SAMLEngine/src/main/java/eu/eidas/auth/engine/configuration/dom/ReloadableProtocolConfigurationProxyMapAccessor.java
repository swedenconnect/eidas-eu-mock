package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;

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
        implements SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> {

    @Nonnull
    private static ImmutableMap<String, ProtocolEngineConfiguration> buildProxyMap(
            @Nonnull ImmutableMap<String, ProtocolEngineConfiguration> map,
            @Nonnull SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor) {
        ImmutableMap.Builder<String, ProtocolEngineConfiguration> builder = ImmutableMap.builder();

        for (final Map.Entry<String, ProtocolEngineConfiguration> entry : map.entrySet()) {
            String name = entry.getKey();
            builder.put(name, ReloadableProtocolConfigurationInvocationHandler.newConfigurationProxy(name, accessor));
        }
        return builder.build();
    }

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> fileAccessor;

    @Nonnull
    private final ImmutableMap<String, ProtocolEngineConfiguration> cachedMap;

    @Nonnull
    private final ImmutableMap<String, ProtocolEngineConfiguration> proxyMap;

    ReloadableProtocolConfigurationProxyMapAccessor(
            @Nonnull SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> fileAccessor) {
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
    public ImmutableMap<String, ProtocolEngineConfiguration> get() throws IOException {
        ImmutableMap<String, ProtocolEngineConfiguration> currentValue = fileAccessor.get();
        if (currentValue == cachedMap) {
            return proxyMap;
        }
        return buildProxyMap(currentValue, fileAccessor);
    }

    @Override
    public void set(@Nonnull ImmutableMap<String, ProtocolEngineConfiguration> newValue)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
