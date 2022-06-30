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

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.lang.Canonicalizers;
import eu.eidas.auth.commons.lang.EnumMapper;
import eu.eidas.auth.commons.lang.KeyAccessor;

/**
 * Configuration Keys pertaining to KeyStores.
 *
 * @since 1.1
 */
public enum KeyStoreKey {

    KEYSTORE_PATH("keyStorePath"),

    KEYSTORE_TYPE("keyStoreType"),

    /**
     * The JCA provider name
     */
    KEYSTORE_PROVIDER("keyStoreProvider"),

    KEYSTORE_PASSWORD("keyStorePassword"),

    KEY_ALIAS("keyAlias"),

    KEY_PASSWORD("keyPassword"),

    // put the ; on a separate line to make merges easier
    ;

    private static final EnumMapper<String, KeyStoreKey> MAPPER =
            new EnumMapper<String, KeyStoreKey>(new KeyAccessor<String, KeyStoreKey>() {

                @Nonnull
                @Override
                public String getKey(@Nonnull KeyStoreKey keyStoreKey) {
                    return keyStoreKey.getKey();
                }
            }, Canonicalizers.trimLowerCase(), values());

    @Nullable
    public static KeyStoreKey fromString(@Nonnull String value) {
        return MAPPER.fromKey(value);
    }

    public static EnumMapper<String, KeyStoreKey> mapper() {
        return MAPPER;
    }

    @Nonnull
    private final transient String key;

    KeyStoreKey(@Nonnull String key) {
        this.key = key;
    }

    @Nonnull
    public String getKey() {
        return key;
    }

    @Nullable
    public <T> T getAsString(@Nonnull Map<String, T> map) {
        return map.get(key);
    }

    @Nullable
    public <T> T get(@Nonnull Map<KeyStoreKey, T> map) {
        return map.get(this);
    }

    @Override
    public String toString() {
        return key;
    }
}
