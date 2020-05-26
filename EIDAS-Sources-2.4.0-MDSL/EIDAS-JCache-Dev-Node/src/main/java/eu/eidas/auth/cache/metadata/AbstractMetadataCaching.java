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
package eu.eidas.auth.cache.metadata;

import eu.eidas.auth.engine.metadata.EidasMetadataParametersI;
import eu.eidas.auth.engine.metadata.IMetadataCachingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract class that implements getter and setter for {@link EidasMetadataParametersI}
 */
public abstract class AbstractMetadataCaching implements IMetadataCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMetadataCaching.class);

    /**
     * Getter for the map of url, {@link EidasMetadataParametersI} pairs.
     *
     * @return the instance of the map
     */
    protected abstract Map<String, EidasMetadataParametersI> getMap();

    /**
     * Gets the instance of {@link EidasMetadataParametersI}
     * related to url that is in {@link AbstractMetadataCaching#getMap()}.
     *
     * @param url the URL that contains the eIDAS Metadata
     * @return the instance of {@link EidasMetadataParametersI} that contains the metadata parameters
     */
    @Override
    public final EidasMetadataParametersI getEidasMetadataParameters(String url) {
        if(getMap()!=null){
            EidasMetadataParametersI content = getMap().get(url);
            if(content != null) {
                return content;
            }
        }
        return null;
    }

    /**
     * Puts the instance of {@link EidasMetadataParametersI} received as parameter
     * in {@link AbstractMetadataCaching#getMap()} with the key of the map being url
     *
     * @param url the URL to be used as key in the map
     * @param eidasMetadataParameters the instance of {@link EidasMetadataParametersI} to be put as value in the map
     *
     */
    @Override
    public final void putEidasMetadataParameters(String url, EidasMetadataParametersI eidasMetadataParameters) {
        if( getMap() != null){
            if(eidasMetadataParameters == null) {
                getMap().remove(url);
            } else {
                getMap().put(url, eidasMetadataParameters);
            }
        }
    }

}
