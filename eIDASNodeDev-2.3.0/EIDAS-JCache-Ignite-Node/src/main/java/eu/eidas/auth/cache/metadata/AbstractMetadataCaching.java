/*
 * Copyright (c) 2018 by European Commission
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
package eu.eidas.auth.cache.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;

import javax.cache.Cache;

public abstract class AbstractMetadataCaching implements IMetadataCachingService {

    protected abstract Cache<String, EidasMetadataParametersI> getCache();

    @Override
    public final EidasMetadataParametersI getEidasMetadataParameters(String url) {
        if(getCache()!=null){
            EidasMetadataParametersI content = getCache().get(url);
            if(content != null) {
                return content;
            }
        }
        return null;
    }

    @Override
    public final void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        if(getCache() != null){
            if(eidasMetadataParameters == null) {
                getCache().remove(url);
            } else {
                getCache().put(url, eidasMetadataParameters);
            }
        }
    }

}
