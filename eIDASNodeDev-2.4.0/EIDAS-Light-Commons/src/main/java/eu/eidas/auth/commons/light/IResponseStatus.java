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
package eu.eidas.auth.commons.light;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

/**
 * Interface representing the status of a response.
 * <p>
 * The status must contain a status code and may contain a sub-status code and a status message.
 * <p>
 * A response is either a success response or a failure response hence the corresponding flag {@link #isFailure()}.
 *
 * @since 1.1
 */
public interface IResponseStatus extends Serializable {

    /**
     * Returns the status code of the response.
     *
     * @return the status code of the response.
     */
    @Nonnull
    String getStatusCode();

    /**
     * Returns the detailed status message of the response.
     *
     * @return the detailed status message of the response.
     */
    @Nullable
    String getStatusMessage();

    /**
     * Returns the secondary status code of the response.
     *
     * @return the secondary status code of the response.
     */
    @Nullable
    String getSubStatusCode();

    /**
     * Returns {@code true} if the authentication failed, returns {@code false} otherwise.
     *
     * @return {@code true} if the authentication failed, returns {@code false} otherwise.
     */
    boolean isFailure();
}
