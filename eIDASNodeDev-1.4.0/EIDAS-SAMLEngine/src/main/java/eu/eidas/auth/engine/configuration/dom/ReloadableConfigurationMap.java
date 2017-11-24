package eu.eidas.auth.engine.configuration.dom;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableMap;

import eu.eidas.auth.commons.io.FileMarshaller;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.auth.commons.io.StreamMarshaller;
import eu.eidas.auth.engine.configuration.SamlEngineConfiguration;
import eu.eidas.auth.engine.configuration.SamlEngineConfigurationException;
import eu.eidas.engine.exceptions.EIDASSAMLEngineRuntimeException;
import eu.eidas.util.Preconditions;

/**
 * Reloadable ConfigurationMap.
 * <p>
 * Remove this class in 1.2.
 *
 * @deprecated since 1.1, use {@link ReloadableProtocolConfigurationMap} instead.
 */
@Deprecated
public final class ReloadableConfigurationMap {

    static ImmutableMap<String, SamlEngineConfiguration> unmarshalStream(@Nonnull String configurationFileName,
                                                                         @Nullable final String defaultPath,
                                                                         @Nonnull InputStream input,
                                                                         @Nullable String overrideFileName)
            throws IOException {
        try {
            InstanceMap instanceMap = DOMConfigurationParser.parseConfiguration(configurationFileName, input);
            return DOMConfigurator.getConfigurationMap(instanceMap, defaultPath, overrideFileName);
        } catch (SamlEngineConfigurationException e) {
            throw new IOException(e);
        }
    }

    @Nonnull
    private final SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> accessor;

    public ReloadableConfigurationMap(@Nonnull final String configurationFileName, @Nullable String defaultPath) {
        this(configurationFileName, defaultPath, null);
    }

    /**
     * @since 1.1
     */
    public ReloadableConfigurationMap(@Nonnull final String configurationFileName,
                                      @Nullable final String defaultPath,
                                      @Nullable final String overrideFileName) {
        Preconditions.checkNotNull(configurationFileName, "configurationFileName");

        SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> fileAccessor =
                SingletonAccessors.newFileAccessor(configurationFileName, defaultPath,
                                                   new FileMarshaller<ImmutableMap<String, SamlEngineConfiguration>>() {

                                                       @Override
                                                       public void marshal(@Nonnull
                                                                                   ImmutableMap<String, SamlEngineConfiguration> newValue,
                                                                           @Nonnull File output)
                                                               throws UnsupportedEncodingException {
                                                           throw new UnsupportedEncodingException();
                                                       }

                                                       @Override
                                                       public ImmutableMap<String, SamlEngineConfiguration> unmarshal(
                                                               @Nonnull File input) throws IOException {
                                                           return unmarshalStream(configurationFileName,
                                                                   defaultPath,
                                                                   new BufferedInputStream(
                                                                                          new FileInputStream(input)),
                                                                                  overrideFileName);
                                                       }
                                                   },
                                                   new StreamMarshaller<ImmutableMap<String, SamlEngineConfiguration>>() {

                                                       @Override
                                                       public void marshal(@Nonnull
                                                                                   ImmutableMap<String, SamlEngineConfiguration> newValue,
                                                                           @Nonnull OutputStream output)
                                                               throws UnsupportedEncodingException {
                                                           throw new UnsupportedEncodingException();
                                                       }

                                                       @Override
                                                       public ImmutableMap<String, SamlEngineConfiguration> unmarshal(
                                                               @Nonnull InputStream input) throws IOException {
                                                           return unmarshalStream(configurationFileName, defaultPath, input,
                                                                                  overrideFileName);
                                                       }
                                                   });
        // each interface component in the returned configuration can be cached as a reference in client code,
        // therefore each such interface must be a proxy capable of reloading its configuration
        accessor = new ReloadableConfigurationProxyMapSingletonAccessor(fileAccessor);
    }

    @Nonnull
    public SingletonAccessor<ImmutableMap<String, SamlEngineConfiguration>> getAccessor() {
        return accessor;
    }

    @Nonnull
    public ImmutableMap<String, SamlEngineConfiguration> getConfigurationMap() {
        try {
            return accessor.get();
        } catch (IOException e) {
            if (e.getCause() instanceof SamlEngineConfigurationException) {
                throw new EIDASSAMLEngineRuntimeException((SamlEngineConfigurationException) e.getCause());
            }
            throw new EIDASSAMLEngineRuntimeException(e);
        }
    }
}
