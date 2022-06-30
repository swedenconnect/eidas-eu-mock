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
package eu.eidas.auth.commons.protocol.impl;

import java.util.Arrays;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.IRequestMessage;
import eu.eidas.util.Preconditions;

/**
 * Request Message
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class BinaryRequestMessage extends AbstractProtocolMessage implements IRequestMessage {

    @Nonnull
    private final IAuthenticationRequest request;

    public BinaryRequestMessage(@Nonnull IAuthenticationRequest request, @Nonnull byte[] requestBytes) {
        super(requestBytes);
        Preconditions.checkNotNull(request, "request");
        this.request = request;
    }

    @Override
    @Nonnull
    public IAuthenticationRequest getRequest() {
        return request;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BinaryRequestMessage that = (BinaryRequestMessage) o;

        if (!request.equals(that.request)) {
            return false;
        }
        return Arrays.equals(getMessageBytes(), that.getMessageBytes());

    }

    @Override
    public int hashCode() {
        int result = request.hashCode();
        result = 31 * result + Arrays.hashCode(getMessageBytes());
        return result;
    }

    @Override
    public String toString() {
        return "BinaryRequestMessage{" +
                "request=" + request +
                ", requestBytes=" + EidasStringUtil.encodeToBase64(getMessageBytes()) +
                '}';
    }
}
