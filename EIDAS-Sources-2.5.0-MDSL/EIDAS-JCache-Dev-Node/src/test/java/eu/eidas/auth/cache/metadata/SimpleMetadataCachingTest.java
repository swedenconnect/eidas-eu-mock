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
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Test class for {@link SimpleMetadataCaching}
 */
public class SimpleMetadataCachingTest {

    /**
     * Test method for
     * {@link SimpleMetadataCaching#getMap()}
     * when the {@link SimpleMetadataCaching#SimpleMetadataCaching(long)} is called
     * and with a valid retention period as parameter
     * <p/>
     * Must succeed.
     */
    @Test
    public void getMap() {
        SimpleMetadataCaching simpleMetadataCaching = new SimpleMetadataCaching(86400);
        Map<String, EidasMetadataParametersI> actualMap = simpleMetadataCaching.getMap();

        Assert.assertNotNull(actualMap);
    }

}