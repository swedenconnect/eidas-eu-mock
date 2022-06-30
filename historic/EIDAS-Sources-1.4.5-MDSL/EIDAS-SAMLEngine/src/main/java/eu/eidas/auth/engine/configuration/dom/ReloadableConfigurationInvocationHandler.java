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
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.SamlEngineEncryptionI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReloadableConfiguration InvocationHandler.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link ReloadableProtocolConfigurationInvocationHandler} instead.
 */
@Deprecated
@VisibleForTesting
public final class ReloadableConfigurationInvocationHandler<T> implements InvocationHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ReloadableConfigurationInvocationHandler.class);

    @Nonnull
    private final String name;

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> fileAccessor;

    @Nonnull
    private final ConfigurationGetter<T> getter;

    interface ConfigurationGetter<T> {

        T get(@Nonnull SamlEngineConfiguration configuration);
    }

    ReloadableConfigurationInvocationHandler(@Nonnull final String nameVal,
                                             @Nonnull
                                             final SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> fileAccessorVal,
                                             @Nonnull final ConfigurationGetter<T> getterVal) {
        name = nameVal;
        fileAccessor = fileAccessorVal;
        getter = getterVal;
    }


    @Nonnull
    private static SamlEngineConfiguration getNamedConfiguration(@Nonnull String name,
                                                                 @Nonnull
                                                                         SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> accessor) {
        SamlEngineConfiguration configuration;
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
    static SamlEngineConfiguration newConfigurationProxy(@Nonnull String name,
                                                         @Nonnull
                                                                 SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> accessor) {
        SamlEngineCoreProperties samlEngineCoreProperties =
                newProxyInstance(SamlEngineCoreProperties.class, name, accessor,
                                 new ConfigurationGetter<SamlEngineCoreProperties>() {

                                     @Override
                                     public SamlEngineCoreProperties get(
                                             @Nonnull SamlEngineConfiguration configuration) {
                                         return configuration.getCoreProperties();
                                     }
                                 });
        ProtocolSignerI signer =
                newProxyInstance(ProtocolSignerI.class, name, accessor, new ConfigurationGetter<ProtocolSignerI>() {

                    @Override
                    public ProtocolSignerI get(@Nonnull SamlEngineConfiguration configuration) {
                        return configuration.getSigner();
                    }
                });
        SamlEngineEncryptionI cipher = newProxyInstance(SamlEngineEncryptionI.class, name, accessor,
                                                        new ConfigurationGetter<SamlEngineEncryptionI>() {

                                                            @Override
                                                            public SamlEngineEncryptionI get(
                                                                    @Nonnull SamlEngineConfiguration configuration) {
                                                                return configuration.getCipher();
                                                            }
                                                        });
        ExtensionProcessorI extensionProcessor = newProxyInstance(ExtensionProcessorI.class, name, accessor,
                                                                  new ConfigurationGetter<ExtensionProcessorI>() {

                                                                      @Override
                                                                      public ExtensionProcessorI get(@Nonnull
                                                                                                             SamlEngineConfiguration configuration) {
                                                                          return configuration.getExtensionProcessor();
                                                                      }
                                                                  });
        SamlEngineClock clock =
                newProxyInstance(SamlEngineClock.class, name, accessor, new ConfigurationGetter<SamlEngineClock>() {

                    @Override
                    public SamlEngineClock get(@Nonnull SamlEngineConfiguration configuration) {
                        return configuration.getClock();
                    }
                });

        return SamlEngineConfiguration.builder()
                .instanceName(name)
                .coreProperties(samlEngineCoreProperties)
                .signer(signer)
                .cipher(cipher)
                .extensionProcessor(extensionProcessor)
                .clock(clock)
                .build();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private static <T> T newProxyInstance(@Nonnull Class<T> type,
                                          @Nonnull String name,
                                          @Nonnull
                                                  SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> accessor,
                                          @Nonnull ConfigurationGetter<T> getter) {
        SamlEngineConfiguration configuration = getNamedConfiguration(name, accessor);
        T proxiedObject = getter.get(configuration);
        if (null == proxiedObject) {
            return null;
        }
        ReloadableConfigurationInvocationHandler<T> invocationHandler =
                new ReloadableConfigurationInvocationHandler<>(name, accessor, getter);

        return ReflectionUtil.newProxyInstance(ReloadableConfigurationMap.class.getClassLoader(), type,
                                               (Class<? extends T>) proxiedObject.getClass(), invocationHandler);
    }

    public T getProxiedObject() {
        SamlEngineConfiguration configuration = getNamedConfiguration(name, fileAccessor);
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
