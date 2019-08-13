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

/**
 * <p>
 * Configuration Entry in the SAML Engine configuration file.
 *
 * Example configuration entry:
 * <pre>
 *   &lt;!-- Settings module signature--&gt;
 * 	&lt;configuration name=&quot;SignatureConf&quot;&gt;
 * 		&lt;!-- Specific signature module --&gt;
 * 		&lt;parameter name=&quot;class&quot; value=&quot;eu.eidas.auth.engine.core.impl.SignSW&quot; /&gt;
 * 		&lt;!-- Settings specific module --&gt;
 * 		&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SignModule_Conf1.xml&quot; /&gt;
 * 	&lt;/configuration&gt;
 * </pre>
 *
 * @since 1.1
 */
public final class ConfigurationEntry {

    @Nonnull
    private final String name;

    @Nonnull
    private final ImmutableMap<String, String> parameters;

    public ConfigurationEntry(@Nonnull String name, @Nonnull ImmutableMap<String, String> parameters) {
        Preconditions.checkNotBlank(name, "name");
        Preconditions.checkNotNull(parameters, "parameters");
        this.name = name;
        this.parameters = parameters;
    }

    @Nonnull
    public String get(@Nonnull ParameterKey parameter) {
        Preconditions.checkNotNull(parameter, "parameter");
        return parameters.get(parameter.getKey());
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public ImmutableMap<String, String> getParameters() {
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConfigurationEntry that = (ConfigurationEntry) o;

        if (!name.equals(that.name)) {
            return false;
        }
        return parameters.equals(that.parameters);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ConfigurationEntry{" +
                "name='" + name + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
