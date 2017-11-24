package eu.eidas.auth.engine.configuration.dom;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.attribute.AttributeRegistries;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.io.PropertiesConverter;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.commons.lang.reflect.ReflectionUtil;
import eu.eidas.auth.engine.SamlEngineClock;
import eu.eidas.auth.engine.SamlEngineSystemClock;
import eu.eidas.auth.engine.configuration.ProtocolEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.auth.engine.core.DefaultCoreProperties;
import eu.eidas.auth.engine.core.ExtensionProcessorI;
import eu.eidas.auth.engine.core.ProtocolCipherI;
import eu.eidas.auth.engine.core.ProtocolProcessorI;
import eu.eidas.auth.engine.core.ProtocolSignerI;
import eu.eidas.auth.engine.core.SamlEngineCoreProperties;
import eu.eidas.auth.engine.core.SamlEngineEncryptionI;
import eu.eidas.auth.engine.core.eidas.EidasExtensionProcessor;
import eu.eidas.auth.engine.core.eidas.EidasProtocolProcessor;
import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.util.Preconditions;

/**
 * Creates typed configuration objects (BaseConfigurations) based on parsing results (InstanceMap).
 *
 * @since 1.1
 */
public final class DOMConfigurator {

    /**
     * The logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DOMConfigurator.class);

    /**
     * @since 1.1
     */
    @Nullable
    private static ProtocolCipherI configureCipher(@Nonnull String instanceName,
                                                   @Nonnull InstanceEntry instanceEntry,
                                                   @Nullable String defaultPath,
                                                   @Nullable String overrideFile) {
        // LOADING ENCRYPTION CONFIGURATION
        ConfigurationEntry encryptionConfigurationEntry = instanceEntry.get(ConfigurationKey.ENCRYPTION_CONFIGURATION);

        if (null == encryptionConfigurationEntry) {
            LOG.info("ERROR : Encryption module configuration not found. SAML Engine  '" + instanceName
                             + "' in non-encryption mode!");
        } else {
            try {
                LOG.trace("Loading Encryption for \"" + instanceName + "\"");

                String encryptionClassName = encryptionConfigurationEntry.get(ParameterKey.CLASS);

                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                @SuppressWarnings("unchecked") Class<ProtocolCipherI> encryptionClass =
                        (Class<ProtocolCipherI>) Class.forName(encryptionClassName, true, contextClassLoader);

                Constructor<ProtocolCipherI> constructor = encryptionClass.getConstructor(Map.class, String.class);

                ProtocolCipherI cipher = createReloadableProxyIfNeeded(instanceName, encryptionConfigurationEntry,
                                                                       ConfigurationKey.ENCRYPTION_CONFIGURATION,  defaultPath,
                                                                       ProtocolCipherI.class, constructor,
                                                                       contextClassLoader, overrideFile);
                return cipher;
            } catch (Exception e) {
                LOG.error("Encryption Module could not be loaded! SAML Engine '" + instanceName
                                  + "' in non-encryption mode! Because of " + e, e);
            }
        }
        return null;
    }

    @Nonnull
    private static SamlEngineClock configureClock(@Nonnull String instanceName, @Nonnull InstanceEntry instanceEntry)
            throws SamlEngineConfigurationException {
        ConfigurationEntry clockConfigurationEntry = instanceEntry.get(ConfigurationKey.CLOCK_CONFIGURATION);

        if (null == clockConfigurationEntry) {
            SamlEngineSystemClock clock = new SamlEngineSystemClock();
            LOG.warn("No custom clock configured for \"" + instanceName + "\", using default: " + clock);
            return clock;
        }

        boolean traceEnabled = LOG.isTraceEnabled();

        if (traceEnabled) {
            LOG.trace("Loading clock for \"" + instanceName + "\"");
        }

        String clockClassName = clockConfigurationEntry.get(ParameterKey.CLASS);
        try {
            Class<SamlEngineClock> clockClass = (Class<SamlEngineClock>) Class.forName(clockClassName, true,
                                                                                       Thread.currentThread()
                                                                                               .getContextClassLoader());
            SamlEngineClock clock = clockClass.newInstance();

            if (traceEnabled) {
                LOG.trace("Loaded clock for \"" + instanceName + "\": " + clock);
            }
            return clock;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            LOG.error("Error creating clock for SAML engine \"" + instanceName + "\" in " + clockClassName + " due to "
                              + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    /**
     * @deprecated since 1.1, use {@link #configureCipher(String, InstanceEntry, String)} instead.
     */
    @Deprecated
    @Nullable
    private static SamlEngineEncryptionI configureEncryption(@Nonnull String instanceName,
                                                             @Nonnull InstanceEntry instanceEntry,
                                                             @Nullable String defaultPath,
                                                             @Nullable String overrideFile) {
        // LOADING ENCRYPTION CONFIGURATION
        ConfigurationEntry encryptionConfigurationEntry = instanceEntry.get(ConfigurationKey.ENCRYPTION_CONFIGURATION);

        if (encryptionConfigurationEntry == null) {
            LOG.info("ERROR : Encryption module configuration not found. SAML Engine  '" + instanceName
                             + "' in non-encryption mode!");
        } else {
            try {
                LOG.trace("Loading Encryption for \"" + instanceName + "\"");

                String encryptionClassName = encryptionConfigurationEntry.get(ParameterKey.CLASS);

                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                @SuppressWarnings("unchecked") Class<SamlEngineEncryptionI> encryptionClass =
                        (Class<SamlEngineEncryptionI>) Class.forName(encryptionClassName, true, contextClassLoader);

                Constructor<SamlEngineEncryptionI> constructor = encryptionClass.getConstructor(Map.class);

                SamlEngineEncryptionI cipher = createReloadableProxyIfNeeded(instanceName, encryptionConfigurationEntry,
                                                                             ConfigurationKey.ENCRYPTION_CONFIGURATION, defaultPath,
                                                                             SamlEngineEncryptionI.class, constructor,
                                                                             contextClassLoader, overrideFile);
                return cipher;
            } catch (Exception e) {
                LOG.error("Encryption Module could not be loaded! SAML Engine '" + instanceName
                                  + "' in non-encryption mode! Because of " + e, e);
            }
        }
        return null;
    }

    /**
     * @deprecated since 1.1, use {@link #configureProtocolProcessor(String, InstanceEntry, MetadataSignerI)} instead.
     */
    @Deprecated
    @Nonnull
    private static ExtensionProcessorI configureExtensionProcessor(@Nonnull String instanceName,
                                                                   @Nonnull InstanceEntry instanceEntry,
                                                                   @Nullable MetadataSignerI metadataSigner)
            throws SamlEngineConfigurationException {
        ConfigurationEntry extensionProcessorConfigurationEntry =
                instanceEntry.get(ConfigurationKey.EXTENSION_PROCESSOR_CONFIGURATION);

        if (null == extensionProcessorConfigurationEntry) {
            ExtensionProcessorI extensionProcessor = EidasExtensionProcessor.INSTANCE;
            LOG.info("No custom ExtensionProcessor configured for \"" + instanceName + "\", using default: "
                             + extensionProcessor);
            return extensionProcessor;
        }

        LOG.trace("Loading extension processor for \"" + instanceName + "\"");

        String extensionProcessorClassName = extensionProcessorConfigurationEntry.get(ParameterKey.CLASS);
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            @SuppressWarnings("unchecked") Class<ExtensionProcessorI> extensionProcessorClass =
                    (Class<ExtensionProcessorI>) Class.forName(extensionProcessorClassName, true, contextClassLoader);
            ImmutableMap<String, String> parameters = extensionProcessorConfigurationEntry.getParameters();
            String coreAttributeRegistryFile = parameters.get(ParameterKey.CORE_ATTRIBUTE_REGISTRY_FILE.getKey());
            String additionalAttributeRegistryFile =
                    parameters.get(ParameterKey.ADDITIONAL_ATTRIBUTE_REGISTRY_FILE.getKey());
            String metadataFetcherClassName = parameters.get(ParameterKey.METADATA_FETCHER_CLASS.getKey());
            MetadataFetcherI metadataFetcher = null;
            if (StringUtils.isNotBlank(metadataFetcherClassName)) {
                @SuppressWarnings("unchecked") Class<MetadataFetcherI> metadataFetcherClass =
                        (Class<MetadataFetcherI>) Class.forName(metadataFetcherClassName, true, contextClassLoader);
                metadataFetcher = metadataFetcherClass.newInstance();
            }

            List<Class<?>> constructorParameterTypes = new ArrayList<>();
            List<Object> constructorParameters = new ArrayList<>();

            if (StringUtils.isNotBlank(additionalAttributeRegistryFile)) {
                constructorParameterTypes.add(String.class);
                constructorParameters.add(additionalAttributeRegistryFile);
                if (StringUtils.isNotBlank(coreAttributeRegistryFile)) {
                    constructorParameterTypes.add(String.class);
                    constructorParameters.add(0, coreAttributeRegistryFile);
                }
            }
            Class<?>[] constructorParameterTypesWithoutMetadata =
                    constructorParameterTypes.toArray(new Class[constructorParameterTypes.size()]);
            constructorParameterTypes.add(MetadataFetcherI.class);
            constructorParameterTypes.add(MetadataSignerI.class);
            Class<?>[] constructorParameterTypesWithMetadata =
                    constructorParameterTypes.toArray(new Class[constructorParameterTypes.size()]);

            Constructor<ExtensionProcessorI> constructorWithoutMetadata = null;
            Constructor<ExtensionProcessorI> constructorWithMetadata = null;

            Constructor<ExtensionProcessorI>[] constructors =
                    (Constructor<ExtensionProcessorI>[]) extensionProcessorClass.getConstructors();
            for (final Constructor<ExtensionProcessorI> constructor : constructors) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (Arrays.equals(parameterTypes, constructorParameterTypesWithoutMetadata)) {
                    constructorWithoutMetadata = constructor;
                } else if (Arrays.equals(parameterTypes, constructorParameterTypesWithMetadata)) {
                    constructorWithMetadata = constructor;
                }
            }

            ExtensionProcessorI extensionProcessor;
            Constructor<ExtensionProcessorI> constructor;
            if (null != constructorWithMetadata) {
                constructorParameters.add(metadataFetcher);
                constructorParameters.add(metadataSigner);
                constructor = constructorWithMetadata;
            } else if (null != constructorWithoutMetadata) {
                constructor = constructorWithoutMetadata;
            } else {
                throw new NoSuchMethodException(
                        "No available constructor matching " + constructorParameterTypes + " in class "
                                + extensionProcessorClassName);
            }
            extensionProcessor =
                    constructor.newInstance(constructorParameters.toArray(new Object[constructorParameters.size()]));

            LOG.trace("Loaded extension processor for \"" + instanceName + "\": " + extensionProcessor);

            extensionProcessor.configureExtension();

            LOG.trace("Configured extension processor for \"" + instanceName + "\": " + extensionProcessor);

            return extensionProcessor;
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.error("Error creating extension processor for SAML engine \"" + instanceName + "\" in "
                              + extensionProcessorClassName + " due to " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    /**
     * @since 1.1
     */
    @Nonnull
    private static ProtocolProcessorI configureProtocolProcessor(@Nonnull String instanceName,
                                                                 @Nullable String defaultPath,
                                                                 @Nonnull InstanceEntry instanceEntry,
                                                                 @Nullable MetadataSignerI metadataSigner)
            throws SamlEngineConfigurationException {
        ConfigurationEntry protocolProcessorConfigurationEntry =
                instanceEntry.get(ConfigurationKey.PROTOCOL_PROCESSOR_CONFIGURATION);

        if (null == protocolProcessorConfigurationEntry) {
            ProtocolProcessorI protocolProcessor = EidasProtocolProcessor.INSTANCE;
            LOG.info("No custom ProtocolProcessor configured for \"" + instanceName + "\", using default: "
                             + protocolProcessor);
            return protocolProcessor;
        }

        LOG.trace("Loading protocol processor for \"" + instanceName + "\"");

        String protocolProcessorClassName = protocolProcessorConfigurationEntry.get(ParameterKey.CLASS);
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            @SuppressWarnings("unchecked") Class<ProtocolProcessorI> protocolProcessorClass =
                    (Class<ProtocolProcessorI>) Class.forName(protocolProcessorClassName, true, contextClassLoader);
            ImmutableMap<String, String> parameters = protocolProcessorConfigurationEntry.getParameters();
            String coreAttributeRegistryFile = parameters.get(ParameterKey.CORE_ATTRIBUTE_REGISTRY_FILE.getKey());
            String additionalAttributeRegistryFile =
                    parameters.get(ParameterKey.ADDITIONAL_ATTRIBUTE_REGISTRY_FILE.getKey());
            String metadataFetcherClassName = parameters.get(ParameterKey.METADATA_FETCHER_CLASS.getKey());
            MetadataFetcherI metadataFetcher = null;
            if (StringUtils.isNotBlank(metadataFetcherClassName)) {
                @SuppressWarnings("unchecked") Class<MetadataFetcherI> metadataFetcherClass =
                        (Class<MetadataFetcherI>) Class.forName(metadataFetcherClassName, true, contextClassLoader);
                metadataFetcher = metadataFetcherClass.newInstance();
            }

            AttributeRegistry coreAttributeRegistry = null;

            if (StringUtils.isNotBlank(coreAttributeRegistryFile)) {
                coreAttributeRegistry = AttributeRegistries.fromFile(coreAttributeRegistryFile, defaultPath);
            }

            AttributeRegistry additionalAttributeRegistry = null;

            if (StringUtils.isNotBlank(additionalAttributeRegistryFile)) {
                additionalAttributeRegistry = AttributeRegistries.fromFile(additionalAttributeRegistryFile, defaultPath);
            }

            ProtocolProcessorI protocolProcessor = null;

            Class<?>[] constructorParameterTypesWithMetadata = {
                    AttributeRegistry.class, AttributeRegistry.class, MetadataFetcherI.class, MetadataSignerI.class};

            Constructor<ProtocolProcessorI>[] constructors =
                    (Constructor<ProtocolProcessorI>[]) protocolProcessorClass.getConstructors();
            for (final Constructor<ProtocolProcessorI> constructor : constructors) {

                Class<?>[] parameterTypes = constructor.getParameterTypes();
                if (Arrays.equals(parameterTypes, constructorParameterTypesWithMetadata)) {
                    protocolProcessor =
                            constructor.newInstance(coreAttributeRegistry, additionalAttributeRegistry, metadataFetcher,
                                                    metadataSigner);
                    break;
                }
            }

            if (null == protocolProcessor) {
                Constructor<ProtocolProcessorI> constructor =
                        protocolProcessorClass.getConstructor(AttributeRegistry.class, AttributeRegistry.class);
                protocolProcessor = constructor.newInstance(coreAttributeRegistry, additionalAttributeRegistry);
            }

            LOG.trace("Loaded protocol processor for \"" + instanceName + "\": " + protocolProcessor);

            protocolProcessor.configure();

            LOG.trace("Configured protocol processor for \"" + instanceName + "\": " + protocolProcessor);

            return protocolProcessor;
        } catch (ClassNotFoundException | ClassCastException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            LOG.error("Error creating protocol processor for SAML engine \"" + instanceName + "\" in "
                              + protocolProcessorClassName + " due to " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    @Nonnull
    private static SamlEngineCoreProperties configureSamlEngineCore(@Nonnull String instanceName,
                                                                    @Nonnull InstanceEntry instanceEntry,
                                                                    @Nullable String defaultPath,
                                                                    @Nullable String overrideFile)
            throws SamlEngineConfigurationException {
        ConfigurationEntry samlEngineConfigurationEntry = instanceEntry.get(ConfigurationKey.SAML_ENGINE_CONFIGURATION);

        if (null == samlEngineConfigurationEntry || samlEngineConfigurationEntry.getParameters().isEmpty()) {
            LOG.error("ConfigurationEntry: \"" + ConfigurationKey.SAML_ENGINE_CONFIGURATION.getKey()
                              + "\" does not exist for SAML Engine '" + instanceName + "'.");
            throw new SamlEngineConfigurationException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()),
                    "ConfigurationEntry: \"" + ConfigurationKey.SAML_ENGINE_CONFIGURATION.getKey()
                            + "\" does not exist for SAML Engine '" + instanceName + "'.");
        }

        try {
            Constructor<DefaultCoreProperties> constructor = DefaultCoreProperties.class.getConstructor(Map.class);

            return createReloadableProxyIfNeeded(instanceName, samlEngineConfigurationEntry,
                                                 ConfigurationKey.SAML_ENGINE_CONFIGURATION, defaultPath,
                                                 SamlEngineCoreProperties.class, constructor,
                                                 Thread.currentThread().getContextClassLoader(), overrideFile);
        } catch (Exception e) {
            throw new SamlEngineConfigurationException(
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                    EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()) + " : " +
                            "ConfigurationEntry: \"" + ConfigurationKey.SAML_ENGINE_CONFIGURATION.getKey()
                            + "\" in error for SAML Engine '" + instanceName + "': " + e, e);
        }
    }

    @Nonnull
    private static ProtocolSignerI configureSignature(@Nonnull String instanceName,
                                                      @Nonnull InstanceEntry instanceEntry,
                                                      @Nullable String defaultPath,
                                                      @Nullable String overrideFile)
            throws SamlEngineConfigurationException {
        ConfigurationEntry signatureConfigurationEntry = instanceEntry.get(ConfigurationKey.SIGNATURE_CONFIGURATION);

        if (null == signatureConfigurationEntry || signatureConfigurationEntry.getParameters().isEmpty()) {
            return throwConfigurationException(
                    "ConfigurationEntry: \"" + ConfigurationKey.SIGNATURE_CONFIGURATION.getKey()
                            + "\" does not exist for SAML Engine '" + instanceName + "'.");
        }

        LOG.trace("Loading signature for \"" + instanceName + "\"");

        String signerClassName = signatureConfigurationEntry.get(ParameterKey.CLASS);
        try {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            @SuppressWarnings("unchecked") Class<ProtocolSignerI> signerClass =
                    (Class<ProtocolSignerI>) Class.forName(signerClassName, true, contextClassLoader);

            Constructor<ProtocolSignerI> constructor;
            try {
                //try to get constructor supporting defaultPath first
                constructor = signerClass.getConstructor(Map.class, String.class);
            } catch (NoSuchMethodException e) {
                constructor = signerClass.getConstructor(Map.class);
            }

            ProtocolSignerI signer = createReloadableProxyIfNeeded(instanceName, signatureConfigurationEntry,
                                                                   ConfigurationKey.SIGNATURE_CONFIGURATION, defaultPath,
                                                                   ProtocolSignerI.class, constructor,
                                                                   contextClassLoader, overrideFile);

            //TODO Specific to the implementation for p12
            //signer loadCryptServiceProvider();
            LOG.trace("Loaded signature for \"" + instanceName + "\"");
            return signer;
        } catch (Exception e) {
            LOG.error("Error creating signature for SAML engine \"" + instanceName + "\" in " + signerClassName
                              + " due to " + e, e);
            throw new SamlEngineConfigurationException(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode(),
                                                       EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage(), e);
        }
    }

    @Nonnull
    @SuppressWarnings("squid:S2637")
    private static <T> T createReloadableProxyIfNeeded(@Nonnull final String instanceName,
                                                       @Nonnull final ConfigurationEntry configurationEntry,
                                                       @Nonnull final ConfigurationKey configurationKey,
                                                       @Nullable final String defaultPath,
                                                       @Nonnull final Class<T> type,
                                                       @Nonnull final Constructor<? extends T> constructor,
                                                       @Nonnull final ClassLoader contextClassLoader,
                                                       @Nullable String overrideFile)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        if (StringUtils.isBlank(overrideFile)) {
            return createReloadableProxyIfNeeded(instanceName, configurationEntry, configurationKey, defaultPath, type, constructor,
                                                 contextClassLoader, ImmutableMap.<String, String>of());
        } else {
            final SingletonAccessor<T> accessor =
                    SingletonAccessors.newPropertiesAccessor(overrideFile, defaultPath, new PropertiesConverter<T>() {

                        @Nonnull
                        @Override
                        public Properties marshal(@Nonnull T t) {
                            throw new UnsupportedOperationException();
                        }

                        @Nonnull
                        @Override
                        public T unmarshal(@Nonnull Properties properties) {
                            try {
                                return createReloadableProxyIfNeeded(instanceName, configurationEntry,
                                                                     configurationKey, defaultPath, type, constructor,
                                                                     contextClassLoader,
                                                                     Maps.fromProperties(properties));
                            } catch (InvocationTargetException e) {
                                //noinspection ThrowCaughtLocally
                                LOG.error("", e);
                                throw new IllegalStateException(e.getTargetException());
                            } catch (InstantiationException | IllegalAccessException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    });
            return newProxyInstance(contextClassLoader, type, constructor.getDeclaringClass(), accessor);
        }
    }

    @Nonnull
    private static <T> T createReloadableProxyIfNeeded(@Nonnull String instanceName,
                                                       @Nonnull ConfigurationEntry configurationEntry,
                                                       @Nonnull ConfigurationKey configurationKey,
                                                       @Nullable final String defaultPath,
                                                       @Nonnull Class<T> type,
                                                       @Nonnull final Constructor<? extends T> constructor,
                                                       @Nonnull ClassLoader contextClassLoader,
                                                       @Nonnull final ImmutableMap<String, String> overrideParameters)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        String fileConfiguration = configurationEntry.get(ParameterKey.FILE_CONFIGURATION);
        ImmutableMap<String, String> staticParameters = configurationEntry.getParameters();
        if (StringUtils.isNotBlank(fileConfiguration)) {
            final SingletonAccessor<T> accessor =
                    ExternalConfigurationFileAccessor.newAccessor(instanceName, configurationKey.getKey(),
                                                                  fileConfiguration.trim(), defaultPath, staticParameters,
                                                                  overrideParameters, new MapConverter<T>() {

                                @Override
                                public T convert(Map<String, String> map) {
                                    try {
                                        if (constructor.getParameterTypes().length == 1) {
                                            return constructor.newInstance(map);
                                        } else {
                                            return constructor.newInstance(map, defaultPath);
                                        }
                                    } catch (InvocationTargetException ite) {
                                        LOG.error("", ite);
                                        throw new IllegalStateException(ite.getTargetException());
                                    } catch (Exception e) {
                                        throw new IllegalStateException(e);
                                    }
                                }
                            });
            return newProxyInstance(contextClassLoader, type, constructor.getDeclaringClass(), accessor);
        } else {
            Map<String, String> allParameters = new LinkedHashMap<>();
            allParameters.putAll(staticParameters);
            allParameters.putAll(overrideParameters);
            if (constructor.getParameterTypes().length == 1) {
                return constructor.newInstance(ImmutableMap.copyOf(allParameters));
            } else {
                return constructor.newInstance(ImmutableMap.copyOf(allParameters),defaultPath);
            }
        }
    }

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the instance name
     * @throws SamlEngineConfigurationException the EIDASSAML engine exception
     * @since 1.1
     */
    public static SamlEngineConfiguration getConfiguration(@Nonnull String instanceName,
                                                           @Nonnull InstanceEntry instanceEntry)
            throws SamlEngineConfigurationException {
        return getConfiguration(instanceName, instanceEntry, null, null);
    }

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the instance name
     * @param instanceEntry the instance entry object
     * @param overrideFile the configuration properties file name containing overriding properties if any, otherwise
     * {@code null}.
     * @throws SamlEngineConfigurationException the SAML engine configuration exception
     * @deprecated since 1.1, use {@link #getProtocolConfiguration(String, InstanceEntry, String)} instead.
     */
    @Deprecated
    @SuppressWarnings("squid:S2259")
    public static SamlEngineConfiguration getConfiguration(@Nonnull String instanceName,
                                                           @Nonnull InstanceEntry instanceEntry,
                                                           @Nullable String defaultPath,
                                                           @Nullable String overrideFile)
            throws SamlEngineConfigurationException {
        if (null == instanceEntry) {
            throwConfigurationException("Instance : \"" + instanceName + "\" does not exist.");
        } else
        if (instanceEntry.getConfigurationEntries().isEmpty()) {
            throwConfigurationException("Instance: \"" + instanceName + "\" is empty.");
        } else
        if (!instanceEntry.getName().equals(instanceName)) {
            throwConfigurationException(
                    "Instance: \"" + instanceEntry.getName() + "\" does not match supplied name \"" + instanceName
                            + "\"");
        }

        Preconditions.checkNotBlank(instanceEntry.getName(), "instanceName");
        SamlEngineCoreProperties samlCore = configureSamlEngineCore(instanceName, instanceEntry, defaultPath, overrideFile);
        ProtocolSignerI signer = configureSignature(instanceName, instanceEntry, defaultPath, overrideFile);
        SamlEngineEncryptionI cipher = configureEncryption(instanceName, instanceEntry, defaultPath, overrideFile);
        MetadataSignerI metadataSigner = null;
        if (signer instanceof MetadataSignerI) {
            metadataSigner = (MetadataSignerI) signer;
        }
        ExtensionProcessorI extensionProcessor =
                configureExtensionProcessor(instanceName, instanceEntry, metadataSigner);
        SamlEngineClock clock = configureClock(instanceName, instanceEntry);
        return SamlEngineConfiguration.builder()
                .instanceName(instanceEntry.getName())
                .coreProperties(samlCore)
                .signer(signer)
                .cipher(cipher)
                .extensionProcessor(extensionProcessor)
                .clock(clock)
                .build();
    }

    /**
     * @deprecated since 1.1, use {@link #getProtocolConfigurationMap(InstanceMap)} instead.
     */
    @Deprecated
    @Nonnull
    public static ImmutableMap<String, SamlEngineConfiguration> getConfigurationMap(@Nonnull InstanceMap instanceMap)
            throws SamlEngineConfigurationException {
        return getConfigurationMap(instanceMap, null, null);
    }

    /**
     * @deprecated since 1.1, use {@link #getProtocolConfigurationMap(InstanceMap, String)} instead.
     */
    @Deprecated
    @Nonnull
    public static ImmutableMap<String, SamlEngineConfiguration> getConfigurationMap(@Nonnull InstanceMap instanceMap,
                                                                                    @Nullable String defaultPath,
                                                                                    @Nullable String overrideFile)
            throws SamlEngineConfigurationException {
        Preconditions.checkNotNull(instanceMap, "instanceMap");
        ImmutableMap.Builder<String, SamlEngineConfiguration> configurationBuilder = ImmutableMap.builder();
        for (final Map.Entry<String, InstanceEntry> entry : instanceMap.getInstances().entrySet()) {
            String instanceName = entry.getKey();
            SamlEngineConfiguration configuration = getConfiguration(instanceName, entry.getValue(), defaultPath, overrideFile);
            configurationBuilder.put(instanceName, configuration);
        }
        return configurationBuilder.build();
    }

    /**
     * Returns the corresponding configuration.
     *
     * @param instanceName the instance name
     * @param instanceEntry the instance entry object
     * @param overrideFile the configuration properties file name containing overriding properties if any, otherwise
     * {@code null}.
     * @throws SamlEngineConfigurationException the SAML engine configuration exception
     * @since 1.1
     */
    @SuppressWarnings("squid:S2259")
    public static ProtocolEngineConfiguration getProtocolConfiguration(@Nonnull String instanceName,
                                                                       @Nonnull InstanceEntry instanceEntry,
                                                                       @Nullable String defaultPath,
                                                                       @Nullable String overrideFile)
            throws SamlEngineConfigurationException {
        if (null == instanceEntry) {
            throwConfigurationException("Instance : \"" + instanceName + "\" does not exist.");
        } else
        if (instanceEntry.getConfigurationEntries().isEmpty()) {
            throwConfigurationException("Instance: \"" + instanceName + "\" is empty.");
        } else
        if (!instanceEntry.getName().equals(instanceName)) {
            throwConfigurationException(
                    "Instance: \"" + instanceEntry.getName() + "\" does not match supplied name \"" + instanceName
                            + "\"");
        }

        Preconditions.checkNotBlank(instanceEntry.getName(), "instanceName");
        SamlEngineCoreProperties samlCore = configureSamlEngineCore(instanceName, instanceEntry, defaultPath, overrideFile);
        ProtocolSignerI signer = configureSignature(instanceName, instanceEntry, defaultPath, overrideFile);
        ProtocolCipherI cipher = configureCipher(instanceName, instanceEntry, defaultPath, overrideFile);
        MetadataSignerI metadataSigner = null;
        if (signer instanceof MetadataSignerI) {
            metadataSigner = (MetadataSignerI) signer;
        }
        ProtocolProcessorI protocolProcessor = configureProtocolProcessor(instanceName, defaultPath, instanceEntry, metadataSigner);
        SamlEngineClock clock = configureClock(instanceName, instanceEntry);
        return ProtocolEngineConfiguration.builder()
                .instanceName(instanceEntry.getName())
                .coreProperties(samlCore)
                .signer(signer)
                .cipher(cipher)
                .protocolProcessor(protocolProcessor)
                .clock(clock)
                .build();
    }

    /**
     * @since 1.1
     */
    @Nonnull
    public static ImmutableMap<String, ProtocolEngineConfiguration> getProtocolConfigurationMap(
            @Nonnull InstanceMap instanceMap) throws SamlEngineConfigurationException {
        return getProtocolConfigurationMap(instanceMap, null, null);
    }

    /**
     * @since 1.1
     */
    @Nonnull
    public static ImmutableMap<String, ProtocolEngineConfiguration> getProtocolConfigurationMap(
            @Nonnull InstanceMap instanceMap, @Nullable String defaultPath, @Nullable String overrideFile) throws SamlEngineConfigurationException {
        Preconditions.checkNotNull(instanceMap, "instanceMap");
        ImmutableMap.Builder<String, ProtocolEngineConfiguration> configurationBuilder = ImmutableMap.builder();
        for (final Map.Entry<String, InstanceEntry> entry : instanceMap.getInstances().entrySet()) {
            String instanceName = entry.getKey();
            ProtocolEngineConfiguration configuration =
                    getProtocolConfiguration(instanceName, entry.getValue(), defaultPath, overrideFile);
            configurationBuilder.put(instanceName, configuration);
        }
        return configurationBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private static <T> T newProxyInstance(@Nonnull ClassLoader contextClassLoader,
                                          @Nonnull Class<T> interfaceType,
                                          @Nonnull Class<? extends T> concreteImplementation,
                                          @Nonnull final SingletonAccessor<T> accessor) {
        return (T) ReflectionUtil.newProxyInstance(contextClassLoader, interfaceType, concreteImplementation,
                                                   new InvocationHandler() {

                                                       @Override
                                                       public Object invoke(Object proxy, Method method, Object[] args)
                                                               throws Throwable {
                                                           try {
                                                               return method.invoke(accessor.get(), args);
                                                           } catch (InvocationTargetException e) { //NOSONAR
                                                               throw e.getTargetException();
                                                           } catch (IOException e) {
                                                               throw new IllegalStateException(e);
                                                           }
                                                       }
                                                   });
    }

    @Nonnull
    private static <T> T throwConfigurationException(@Nonnull String message) throws SamlEngineConfigurationException {
        LOG.error(message);
        throw new SamlEngineConfigurationException(
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorCode()),
                EidasErrors.get(EidasErrorKey.SAML_ENGINE_CONFIGURATION_ERROR.errorMessage()), message);
    }

    private DOMConfigurator() {
    }
}
