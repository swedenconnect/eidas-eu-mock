/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */
package eu.eidas.auth.engine.configuration.dom;

import com.google.common.collect.ImmutableMap;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <p>
 * Instance Entry in the SAML Engine configuration file.
 *
 * Example instance entry:
 * <pre>
 *   &lt;!-- Configuration name--&gt;
 * 	&lt;instance name=&quot;CONF1&quot;&gt;
 * 		&lt;!-- Configurations parameters SamlEngine  --&gt;
 * 		&lt;configuration name=&quot;SamlEngineConf&quot;&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SamlEngine_Conf1.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 *
 * 		&lt;!-- Settings module signature--&gt;
 * 		&lt;configuration name=&quot;SignatureConf&quot;&gt;
 * 			&lt;!-- Specific signature module --&gt;
 * 			&lt;parameter name=&quot;class&quot; value=&quot;eu.eidas.auth.engine.core.impl.SignSW&quot; /&gt;
 * 			&lt;!-- Settings specific module --&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SignModule_Conf1.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 * 	&lt;/instance&gt;
 * </pre>
 *
 * @since 1.1
 */
public final class InstanceEntry {

    @Nonnull
    private final String name;

    @Nonnull
    private final ImmutableMap<String, ConfigurationEntry> configurationEntries;

    public InstanceEntry(@Nonnull String name, @Nonnull ImmutableMap<String, ConfigurationEntry> configurationEntries) {
        Preconditions.checkNotBlank(name, "name");
        Preconditions.checkNotNull(configurationEntries, "configurationEntries");
        this.name = name;
        this.configurationEntries = configurationEntries;
    }

    @Nullable
    public ConfigurationEntry get(@Nonnull ConfigurationKey key) {
        Preconditions.checkNotNull(key, "key");
        return configurationEntries.get(key.getKey());
    }

    @Nonnull
    public ImmutableMap<String, ConfigurationEntry> getConfigurationEntries() {
        return configurationEntries;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceEntry that = (InstanceEntry) o;

        if (!name.equals(that.name)) {
            return false;
        }
        return configurationEntries.equals(that.configurationEntries);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + configurationEntries.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstanceEntry{" +
                "name='" + name + '\'' +
                ", configurationEntries=" + configurationEntries +
                '}';
    }
}
