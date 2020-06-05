package eu.eidas.node.auth.metadata;

import javax.annotation.Nonnull;

import org.opensaml.saml2.metadata.EntityDescriptor;

import eu.eidas.auth.engine.metadata.MetadataFetcherI;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.engine.exceptions.EIDASSAMLEngineException;
import eu.eidas.node.ApplicationContextProvider;

/**
 * Spring-Managed MetadataFetcher
 *
 * @since 1.1
 */
public final class SpringManagedMetadataFetcher implements MetadataFetcherI {

    public MetadataFetcherI getMetadataFetcher() {
        return ApplicationContextProvider.getApplicationContext().getBean(MetadataFetcherI.class);
    }

    @Override
    @Nonnull
    public EntityDescriptor getEntityDescriptor(@Nonnull String url, @Nonnull MetadataSignerI metadataSigner)
            throws EIDASSAMLEngineException {
        return getMetadataFetcher().getEntityDescriptor(url, metadataSigner);
    }
}
