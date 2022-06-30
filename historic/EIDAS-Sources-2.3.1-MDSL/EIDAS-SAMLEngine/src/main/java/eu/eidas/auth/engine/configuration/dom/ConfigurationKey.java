/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * ConfigurationKey
 *
 * @since 1.1
 */
public enum ConfigurationKey {

    SAML_ENGINE_CONFIGURATION("SamlEngineConf"),

    SIGNATURE_CONFIGURATION("SignatureConf"),

    ENCRYPTION_CONFIGURATION("EncryptionConf"),

    /**
     * @deprecated since 1.1, use {@link #PROTOCOL_PROCESSOR_CONFIGURATION} instead.
     */
    @Deprecated
    EXTENSION_PROCESSOR_CONFIGURATION("ExtensionProcessorConf"),

    PROTOCOL_PROCESSOR_CONFIGURATION("ProtocolProcessorConf"),

    CLOCK_CONFIGURATION("ClockConf"), 
    
    METADATA_FETCHER_CONFIGURATION("MetadataFetcherConf") ,

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, ConfigurationKey> MAPPER =
            new EnumMapper<String, ConfigurationKey>(new KeyAccessor<String, ConfigurationKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull ConfigurationKey configurationKey) {
                    return configurationKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static ConfigurationKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, ConfigurationKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    ConfigurationKey(@Nonnull String key) {
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key;
    }
}
