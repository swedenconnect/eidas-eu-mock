package eu.eidas.auth.engine.configuration.dom;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import eu.eidas.util.Preconditions;

/**
 * Represents the SAML Engine configuration file as a Map.
 * <p/>
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
