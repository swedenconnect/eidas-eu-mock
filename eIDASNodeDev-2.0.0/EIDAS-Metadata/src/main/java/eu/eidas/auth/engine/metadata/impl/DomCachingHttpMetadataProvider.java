/*
 * Copyright (c) 2017 by European Commission
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
 * limitations under the Licence.
 */

package eu.eidas.auth.engine.metadata.impl;

import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.apache.http.client.HttpClient;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.metadata.resolver.impl.HTTPMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;

/**
 * An HTTPMetadataProvider which does not release the DOM after parsing.
 *
 * @author vanegdi on 16/08/2015.
 */
final class DomCachingHttpMetadataProvider extends HTTPMetadataResolver {

    CriteriaSet c = new CriteriaSet();
    String url;
    String entityId;

    /**
     * Contructor that sets the entityId to the URL.
     *
     * @param client the http client
     * @param url the url of the metadata
     * @throws ResolverException
     */
    public DomCachingHttpMetadataProvider(HttpClient client, String url) throws ResolverException {
        super(client, url);
        this.url = url;
        this.entityId = url;
    }

    /**
     * Contructor that allows the entityId to be different from the URL.
     *
     * @param client the http client
     * @param url the url of the metadata
     * @throws ResolverException
     */
    public DomCachingHttpMetadataProvider(HttpClient client, String url, String entityId) throws ResolverException {
        super(client, url);
        this.url = url;
        this.entityId = entityId;
    }

    protected void releaseMetadataDOM(XMLObject metadata) { /* DON'T - because of the signature validation happens later */}

    public void initializeNonFinal() throws ComponentInitializationException {
        c.add(new EntityIdCriterion(entityId));
        setRequireValidMetadata(true);
        setId("Resolver:"+url);
        super.initialize();
    }

    public XMLObject getMetadata() throws ResolverException {
        //Quickfix to overcome cases where entityId is different from the URL
        EntityDescriptor entityDescriptor = resolveSingle(c);
        if (entityDescriptor != null) {
            return entityDescriptor;
        } else {
            return getCachedOriginalMetadata();
        }

    }

    public EntityDescriptor getEntityDescriptor(String url) throws ResolverException {
        return resolveSingle(c);
    }
}
