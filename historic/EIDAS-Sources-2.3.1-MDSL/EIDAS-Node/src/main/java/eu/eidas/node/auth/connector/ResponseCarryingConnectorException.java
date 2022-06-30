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
package eu.eidas.node.auth.connector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import eu.eidas.auth.commons.exceptions.AbstractEIDASException;
import eu.eidas.util.Preconditions;

/**
 * Copied from {@link eu.eidas.auth.commons.exceptions.InternalErrorEIDASException} which is Anti-Pattern of using an
 * exception to carry a failure response to a Servlet.
 * <p>
 *
 * @see eu.eidas.auth.commons.exceptions.InternalErrorEIDASException
 * @deprecated Anti-Pattern of using an exception to carry a failure response to a Servlet
 */
@Deprecated
public final class ResponseCarryingConnectorException extends AbstractEIDASException {

    private static final long serialVersionUID = 408795925268855362L;

    @Nonnull
    private final String errorRedirectUrl;

    @Nullable
    private final String relayState;

    /**
     * Exception Constructor with three strings representing the errorCode, errorMessage and encoded samlToken as
     * parameters.
     *
     * @param errorCode The error code value.
     * @param errorMessage The error message value.
     * @param samlTokenFail The error SAML Token.
     */
    public ResponseCarryingConnectorException(@Nonnull String errorCode,
                                              @Nonnull String errorMessage,
                                              @Nonnull String samlTokenFail,
                                              @Nonnull String errorRedirectUrl,
                                              @Nullable String relayState) {
        super(errorCode, errorMessage, samlTokenFail);
        Preconditions.checkNotNull(samlTokenFail, "samlTokenFail");
        Preconditions.checkNotNull(errorRedirectUrl, "errorRedirectUrl");
        this.errorRedirectUrl = errorRedirectUrl;
        this.relayState = relayState;
    }

    @Nonnull
    public String getErrorRedirectUrl() {
        return errorRedirectUrl;
    }

    @Nullable
    public String getRelayState() {
        return relayState;
    }
}
