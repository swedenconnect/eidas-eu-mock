package eu.eidas.idp.metadata;
/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

import com.google.common.cache.CacheBuilder;
import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.xml.opensaml.OpenSamlHelper;
import eu.eidas.auth.engine.metadata.*;
import eu.eidas.encryption.exception.MarshallException;
import eu.eidas.encryption.exception.UnmarshallException;
import eu.eidas.engine.exceptions.EIDASMetadataProviderException;
import eu.eidas.idp.IDPUtil;
import org.opensaml.saml2.metadata.EntityDescriptor;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.signature.SignableXMLObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class IDPMetadataCache implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(IDPMetadataCache.class);

    private static final String SIGNATURE_HOLDER_ID_PREFIX="signatureholder";

    private ConcurrentMap<String, SerializedEntityDescriptor> map = null;

    protected Map<String, SerializedEntityDescriptor> getMap() {
        if (map == null) {
            map = CacheBuilder.newBuilder()
                    .expireAfterAccess(Long.parseLong(IDPUtil.loadIDPConfigs().getProperty("idp.metadata.retention", "86400")),
                            TimeUnit.SECONDS)
                    .maximumSize(10000L).<String, SerializedEntityDescriptor>build().asMap();
        }
        return map;
    }

    private class SerializedEntityDescriptor {
        /**
         * the entitydescriptor serialized as xml
         */
        private String serializedEntityDescriptor;

        /**
         * the type/origin (either statically loaded or retrieved from the network)
         */
        private EntityDescriptorType type;

        public SerializedEntityDescriptor(String descriptor, EntityDescriptorType type) {
            setSerializedEntityDescriptor(descriptor);
            setType(type);
        }

        public String getSerializedEntityDescriptor() {
            return serializedEntityDescriptor;
        }

        public void setSerializedEntityDescriptor(String serializedEntityDescriptor) {
            this.serializedEntityDescriptor = serializedEntityDescriptor;
        }

        public EntityDescriptorType getType() {
            return type;
        }

        public void setType(EntityDescriptorType type) {
            this.type = type;
        }
    }


    @Override
    public final EntityDescriptor getDescriptor(String url) throws EIDASMetadataProviderException {
        if(getMap()!=null){
            SerializedEntityDescriptor content=getMap().get(url);
            if(content!=null && !content.getSerializedEntityDescriptor().isEmpty()) {
                try {
                    return deserializeEntityDescriptor(content.getSerializedEntityDescriptor());
                } catch (UnmarshallException e) {
                    LOG.error("Unable to deserialize metadata entity descriptor from cache for "+url);
                    LOG.error(e.getStackTrace().toString());
                    throw new EIDASMetadataProviderException(e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public final void putDescriptor(String url, EntityDescriptor ed, EntityDescriptorType type) {
        if(getMap()!=null){
            if(ed==null){
                getMap().remove(url);
            }else {
                String content = serializeEntityDescriptor(ed);
                if (content != null && !content.isEmpty()) {
                    getMap().put(url, new SerializedEntityDescriptor(content, type));
                }
            }
        }
    }
    @Override
    public final EntityDescriptorType getDescriptorType(String url) {
        if (getMap() != null) {
            SerializedEntityDescriptor content = getMap().get(url);
            if (content != null) {
                return content.getType();
            }
        }
        return null;
    }

    private String serializeEntityDescriptor(XMLObject ed){
        try {
            return EidasStringUtil.toString(OpenSamlHelper.marshall(ed));
        } catch (MarshallException e) {
            throw new IllegalStateException(e);
        }
    }

    private EntityDescriptor deserializeEntityDescriptor(String content) throws UnmarshallException {
        EntityDescriptorContainer container = MetadataUtil.deserializeEntityDescriptor(content);
        return container.getEntityDescriptors().isEmpty()?null:container.getEntityDescriptors().get(0);
    }

    @Override
    public void putDescriptorSignatureHolder(String url, SignableXMLObject container){
        getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(serializeEntityDescriptor(container), EntityDescriptorType.NONE));
    }

    @Override
    public void putDescriptorSignatureHolder(String url, EntityDescriptorContainer container){
        if(container.getSerializedEntitesDescriptor()!=null){
            getMap().put(SIGNATURE_HOLDER_ID_PREFIX+url, new SerializedEntityDescriptor(EidasStringUtil.toString(container.getSerializedEntitesDescriptor()), EntityDescriptorType.SERIALIZED_SIGNATURE_HOLDER));
        }else{
            putDescriptorSignatureHolder(url, container.getEntitiesDescriptor());
        }
    }

}
