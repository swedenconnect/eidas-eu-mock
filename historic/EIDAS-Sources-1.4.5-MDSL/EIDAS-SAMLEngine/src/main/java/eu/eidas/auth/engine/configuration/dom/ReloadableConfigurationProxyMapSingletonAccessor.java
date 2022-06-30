package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;

/**
 * Each interface component (SamlEngineCoreProperties, ProtocolSignerI, SamlEngineEncryptionI, ExtensionProcessorI) in a
 * configuration can be cached as a reference in client code, therefore each such interface must be a proxy capable of
 * reloading its configuration itself.
 * <p>
 * This class builds cached dynamic proxies aware of the reloading process.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link ReloadableProtocolConfigurationProxyMapAccessor} instead.
 */
@Deprecated
final class ReloadableConfigurationProxyMapSingletonAccessor
        implements SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> {

    @Nonnull
    private static ImmutableMap<String, SamlEngineConfiguration> buildProxyMap(
            @Nonnull ImmutableMap<String, SamlEngineConfiguration> map,
            @Nonnull SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> accessor) {
        ImmutableMap.Builder<String, SamlEngineConfiguration> builder = ImmutableMap.builder();

        for (final Map.Entry<String, SamlEngineConfiguration> entry : map.entrySet()) {
            String name = entry.getKey();
            builder.put(name, ReloadableConfigurationInvocationHandler.newConfigurationProxy(name, accessor));
        }
        return builder.build();
    }

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> fileAccessor;

    @Nonnull
    private final ImmutableMap<String, SamlEngineConfiguration> cachedMap;

    @Nonnull
    private final ImmutableMap<String, SamlEngineConfiguration> proxyMap;

    ReloadableConfigurationProxyMapSingletonAccessor(
            @Nonnull SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> fileAccessor) {
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
    public ImmutableMap<String, SamlEngineConfiguration> get() throws IOException {
        ImmutableMap<String, SamlEngineConfiguration> currentValue = fileAccessor.get();
        if (currentValue == cachedMap) {
            return proxyMap;
        }
        return buildProxyMap(currentValue, fileAccessor);
    }

    @Override
    public void set(@Nonnull ImmutableMap<String, SamlEngineConfiguration> newValue)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }
}
