/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.eidas.auth.commons;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Class to test the correct functioning of {@link EidasParameters}.
 */
public class EidasParametersTest {

    /**
     * Max prefix.
     */
    private static final String MAX_PARAM_PREFIX = "max.";

    /**
     * param's size prefix to get max param size.
     */
    private static final String MAX_PARAM_SUFFIX = ".size";

    /**
     * example key to retrieve a value.
     */
    final String key = MAX_PARAM_PREFIX + EidasParameterKeys.EIDAS_CONNECTOR_REDIRECT_URL.toString() + MAX_PARAM_SUFFIX;


    /**
     * Method to test the retrieval of values loaded in {@link EidasParameters}
     * Must succed.
     *
     */
    @Test
    public void testGet() {
        assertEquals("300",EidasParameters.get(key));
    }

    /**
     * Method to test the retrieval of values loaded in {@link EidasParameters}
     * Must fail.
     */
    @Test
    public void testGetFail() {
        assertNotEquals("301",EidasParameters.get(key));
    }

}