package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.lang.reflect.ReflectionUtil;
import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReloadableConfiguration InvocationHandler
 *
 * @since 1.1
 */
@VisibleForTesting
public final class ReloadableProtocolConfigurationInvocationHandler<T> implements InvocationHandler {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ReloadableProtocolConfigurationInvocationHandler.class);

    @Nonnull
    private final String name;

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> fileAccessor;

    @Nonnull
    private final ConfigurationGetter<T> getter;

    interface ConfigurationGetter<T> {

        T get(@Nonnull ProtocolEngineConfiguration configuration);
    }

    ReloadableProtocolConfigurationInvocationHandler(@Nonnull final String nameVal,
                                                     @Nonnull
                                                     final SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> fileAccessorVal,
                                                     @Nonnull final ConfigurationGetter<T> getterVal) {
        name = nameVal;
        fileAccessor = fileAccessorVal;
        getter = getterVal;
    }

    @Nonnull
    private static ProtocolEngineConfiguration getNamedConfiguration(@Nonnull String name,
                                                                     @Nonnull
                                                                             SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor) {
        ProtocolEngineConfiguration configuration;
        try {
            configuration = accessor.get().get(name);
        } catch (IOException e) {
            throw new IllegalStateException("Configuration instance \"" + name + "\" could not be loaded: " + e, e);
        }
        if (null == configuration) {
            throw new IllegalStateException("Configuration instance \"" + name + "\" does not exist.");
        }
        return configuration;
    }

    @Nonnull
    static ProtocolEngineConfiguration newConfigurationProxy(@Nonnull String name,
                                                             @Nonnull
                                                                     SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor) {
        SamlEngineCoreProperties samlEngineCoreProperties =
                newProxyInstance(SamlEngineCoreProperties.class, name, accessor,
                                 new ConfigurationGetter<SamlEngineCoreProperties>() {

                                     @Override
                                     public SamlEngineCoreProperties get(
                                             @Nonnull ProtocolEngineConfiguration configuration) {
                                         return configuration.getCoreProperties();
                                     }
                                 });
        ProtocolSignerI signer =
                newProxyInstance(ProtocolSignerI.class, name, accessor, new ConfigurationGetter<ProtocolSignerI>() {

                    @Override
                    public ProtocolSignerI get(@Nonnull ProtocolEngineConfiguration configuration) {
                        return configuration.getSigner();
                    }
                });
        ProtocolCipherI cipher =
                newProxyInstance(ProtocolCipherI.class, name, accessor, new ConfigurationGetter<ProtocolCipherI>() {

                    @Override
                    public ProtocolCipherI get(@Nonnull ProtocolEngineConfiguration configuration) {
                        return configuration.getCipher();
                    }
                });
        ProtocolProcessorI protocolProcessor = newProxyInstance(ProtocolProcessorI.class, name, accessor,
                                                                new ConfigurationGetter<ProtocolProcessorI>() {

                                                                    @Override
                                                                    public ProtocolProcessorI get(@Nonnull
                                                                                                          ProtocolEngineConfiguration configuration) {
                                                                        return configuration.getProtocolProcessor();
                                                                    }
                                                                });
        SamlEngineClock clock =
                newProxyInstance(SamlEngineClock.class, name, accessor, new ConfigurationGetter<SamlEngineClock>() {

                    @Override
                    public SamlEngineClock get(@Nonnull ProtocolEngineConfiguration configuration) {
                        return configuration.getClock();
                    }
                });

        return ProtocolEngineConfiguration.builder()
                .instanceName(name)
                .coreProperties(samlEngineCoreProperties)
                .signer(signer)
                .cipher(cipher)
                .protocolProcessor(protocolProcessor)
                .clock(clock)
                .build();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T newProxyInstance(@Nonnull Class<T> type,
                                          @Nonnull String name,
                                          @Nonnull
                                                  SingletonAccessor<ImmutableMap<String, ProtocolEngineConfiguration>> accessor,
                                          @Nonnull ConfigurationGetter<T> getter) {
        ProtocolEngineConfiguration configuration = getNamedConfiguration(name, accessor);
        T proxiedObject = getter.get(configuration);
        if (null == proxiedObject) {
            return null;
        }
        ReloadableProtocolConfigurationInvocationHandler<T> invocationHandler =
                new ReloadableProtocolConfigurationInvocationHandler<>(name, accessor, getter);

        return ReflectionUtil.newProxyInstance(ReloadableConfigurationMap.class.getClassLoader(), type,
                                               (Class<? extends T>) proxiedObject.getClass(), invocationHandler);
    }

    public T getProxiedObject() {
        ProtocolEngineConfiguration configuration = getNamedConfiguration(name, fileAccessor);
        return getter.get(configuration);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            T instance = getProxiedObject();
            if (null == instance) {
                // Houston we have a problem!
                // The configuration has been modified and is now allowing a null configuration property (e.g. null cipher)
                // TODO to fix this, we should not allow a null cipher but instead we should have a Cipher object doing nothing
                return null;
            }
            return method.invoke(instance, args);
        } catch (InvocationTargetException e) {
            LOG.error("", e);
            throw e.getTargetException();
        }
    }
}
