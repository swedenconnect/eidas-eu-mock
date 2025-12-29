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
package eu.eidas.auth.engine;

import javax.annotation.Nonnull;

/**
 * Mixin interface for any kind of responses which can be correlated to a request.
 *
 * @since 1.1
 */
public interface Correlated {

    /**
     * Returns this response ID.
     * <p>
     * This is a unique ID which must be used to prevent replay attacks.
     *
     * @return this response ID.
     */
    @Nonnull
    String getId();

    /**
     * Returns the ID of the request corresponding to this response.
     * <p>
     * This is the unique ID of the request which permits to correlate this response to the originating request.
     *
     * @return the ID of the request corresponding to this response.
     */
    @Nonnull
    String getInResponseToId();
}