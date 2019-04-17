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

package eu.eidas.node.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Abstract Caching class for saml message logging.
 */
public abstract class AbstractLogSamlCaching implements ILogSamlCachingService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractLogSamlCaching.class);

    @Override
    public final LogSamlHolder getAndRemove(String id) {
        final Map<String, LogSamlHolder> map = getMap();
        if(map !=null){
            return map.remove(id);
        }
        return null;
    }

    @Override
    public void put(String id, LogSamlHolder logSamlHolder) {
        final Map<String, LogSamlHolder> map = getMap();
        if(map !=null){
            if(logSamlHolder==null){
                map.remove(id);
            }else {
                map.put(id, logSamlHolder);
            }
        }
    }

    protected abstract Map<String, LogSamlHolder> getMap();

}
