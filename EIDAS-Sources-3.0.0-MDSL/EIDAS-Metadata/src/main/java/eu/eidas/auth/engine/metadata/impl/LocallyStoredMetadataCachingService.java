/*
 * Copyright (c) 2024 by European Commission
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

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.EntityDescriptorContainer;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import eu.eidas.auth.engine.metadata.IStaticMetadataChangeListener;
import eu.eidas.auth.engine.metadata.MetadataLoaderPlugin;
import eu.eidas.auth.engine.metadata.MetadataSignerI;
import eu.eidas.auth.engine.metadata.MetadataUtil;
import eu.eidas.engine.exceptions.EIDASMetadataException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * perform post construct task, eg populating the cache with file based metadata
 */
class LocallyStoredMetadataCachingService implements IMetadataCachingService, IStaticMetadataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(LocallyStoredMetadataCachingService.class);
    
    private final IMetadataCachingService internalCachingService;
    private MetadataSignerI metadataSigner;
    private final MetadataLoaderPlugin metadataLoaderPlugin;
    /**
     * When true, code will try to consume offline metadata when wrapping cache returns null
     */
    private boolean metadataLoaderPluginEnabled = true;
    /**
     * initialized with a list of urls corresponding to EntityDescriptor not needing signature validation
     */
    private String trustedEntityDescriptors = "";

    /**
     * whether to enable the signature validation for EntityDescriptors
     */
    private boolean validateEntityDescriptorSignature = true;
    

    public LocallyStoredMetadataCachingService(
            MetadataSignerI metadataSigner,
            MetadataLoaderPlugin metadataLoaderPlugin,
            IMetadataCachingService wrapAroundCachingService
    ) {
        this.internalCachingService = wrapAroundCachingService;
        this.metadataSigner = metadataSigner;
        this.metadataLoaderPlugin = metadataLoaderPlugin;
        initProcessor();
    }

    public void initProcessor() {
        if (metadataLoaderPlugin != null) {
            metadataLoaderPlugin.addListenerContentChanged(this); // Subscribe to observe subject
        }
    }

    @Override
    public EidasMetadataParametersI getEidasMetadataParameters(String url) throws EIDASMetadataProviderException {
        EidasMetadataParametersI metadataParameters = this.internalCachingService.getEidasMetadataParameters(url);
        if (null == metadataParameters) {
            if (metadataLoaderPlugin != null && metadataLoaderPluginEnabled) {
                try {
                    metadataParameters = loadWithPlugin(url, metadataSigner);
                } catch (EIDASMetadataException e) {
                    throw new EIDASMetadataProviderException(e);
                }
            }
        }
        return metadataParameters;
    }

    @Override
    public void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        internalCachingService.putEidasMetadataParameters(url, eidasMetadataParameters);
    }

    protected EidasMetadataParametersI loadWithPlugin(String url, MetadataSignerI metadataSigner) throws EIDASMetadataException {
        EidasMetadataParametersI eidasMetadataParameters = null;
        if (metadataLoaderPlugin != null) {
            List<EntityDescriptorContainer> fileStoredDescriptors = metadataLoaderPlugin.getEntityDescriptors();
            if (internalCachingService != null) {
                for (EntityDescriptorContainer edc : fileStoredDescriptors) {
                    for (EntityDescriptor ed : edc.getEntityDescriptors()) {
                        if (ed.getEntityID().equals(url)) {
                            add(ed, metadataSigner);
                            if (edc.getEntitiesDescriptor() != null && ed.getSignature() == null) {
                                eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
                                internalCachingService.putEidasMetadataParameters(ed.getEntityID(), eidasMetadataParameters);
                            }
                            eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
                        }
                    }
                }
            }
        }
        return eidasMetadataParameters;
    }

    public void add(EntityDescriptor ed, MetadataSignerI metadataSigner) throws EIDASMetadataException {
        String url = ed.getEntityID();
        if (mustValidateSignature(url)) {
            try {
                metadataSigner.validateMetadataSignature(ed);
            } catch (EIDASMetadataException e) {
                LOG.error("Signature validation failed for " + url);
                LOG.error(e.toString());
                throw (e);
            }
        }
        if (null != internalCachingService) {
            EidasMetadataParametersI eidasMetadataParameters = MetadataUtil.convertEntityDescriptor(ed);
            internalCachingService.putEidasMetadataParameters(ed.getEntityID(), eidasMetadataParameters);
        }
    }

    @Override
    public void remove(String entityID) {
        removeFromCache(entityID);
    }

    protected void removeFromCache(String url) {
        if (null != internalCachingService) {
            internalCachingService.putEidasMetadataParameters(url, null);
        }
    }

    protected boolean mustValidateSignature(@Nonnull String url) {
        return isValidateEidasMetadataSignature() && !trustedEntityDescriptors.contains(url);
    }
    
    public boolean isValidateEidasMetadataSignature() {
        return validateEntityDescriptorSignature;
    }

    public LocallyStoredMetadataCachingService setMetadataLoaderPluginEnabled(boolean metadataLoaderPluginEnabled) {
        this.metadataLoaderPluginEnabled = metadataLoaderPluginEnabled;
        return this;
    }

    public LocallyStoredMetadataCachingService setTrustedEntityDescriptors(String trustedEntityDescriptors) {
        this.trustedEntityDescriptors = trustedEntityDescriptors;
        return this;
    }

    public LocallyStoredMetadataCachingService setValidateEntityDescriptorSignature(boolean validateEntityDescriptorSignature) {
        this.validateEntityDescriptorSignature = validateEntityDescriptorSignature;
        return this;
    }
}
