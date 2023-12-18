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
 * Represents the SAML Engine configuration file as a Map.
 *
 * Example configuration file:
 * <pre>
 * &lt;instances&gt;
 * 	&lt;instance name=&quot;CONF1&quot;&gt;
 * 		&lt;!-- Configurations parameters SamlEngine  --&gt;
 * 		&lt;configuration name=&quot;SamlEngineConf&quot;&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SamlEngine_Conf1.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 * 		&lt;!-- Settings module signature--&gt;
 * 		&lt;configuration name=&quot;SignatureConf&quot;&gt;
 * 			&lt;!-- Specific signature module --&gt;
 * 			&lt;parameter name=&quot;class&quot; value=&quot;eu.eidas.auth.engine.core.impl.SignSW&quot; /&gt;
 * 			&lt;!-- Settings specific module --&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SignModule_Conf1.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 * 	&lt;/instance&gt;
 * 	&lt;instance name=&quot;CONF2&quot;&gt;
 * 		&lt;configuration name=&quot;SamlEngineConf&quot;&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SamlEngine_Conf2.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 * 		&lt;configuration name=&quot;SignatureConf&quot;&gt;
 * 			&lt;parameter name=&quot;class&quot; value=&quot;eu.eidas.auth.engine.core.impl.SignSW&quot; /&gt;
 * 			&lt;parameter name=&quot;fileConfiguration&quot; value=&quot;SignModule_Conf2.xml&quot; /&gt;
 * 		&lt;/configuration&gt;
 * 	&lt;/instance&gt;
 * &lt;/instances&gt;
 * </pre>
 *
 * @since 1.1
 */
public final class InstanceMap {

    @Nonnull
    private final ImmutableMap<String, InstanceEntry> instances;

    public InstanceMap(@Nonnull ImmutableMap<String, InstanceEntry> instances) {
        Preconditions.checkNotEmpty(instances, "instanceMap");
        this.instances = instances;
    }

    @Nonnull
    public ImmutableMap<String, InstanceEntry> getInstances() {
        return instances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InstanceMap that = (InstanceMap) o;

        return instances.equals(that.instances);
    }

    @Override
    public int hashCode() {
        return instances.hashCode();
    }

    @Override
    public String toString() {
        return "InstanceMap{" + instances + '}';
    }
}
