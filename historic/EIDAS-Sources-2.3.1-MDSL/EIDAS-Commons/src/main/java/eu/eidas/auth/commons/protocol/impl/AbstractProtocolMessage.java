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

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.protocol.IProtocolMessage;
import eu.eidas.util.Preconditions;

/**
 * Abstract Protocol Message.
 *
 * @since 1.1
 */
public abstract class AbstractProtocolMessage implements IProtocolMessage {

    @Nonnull
    private final byte[] messageBytes;

    protected AbstractProtocolMessage(@Nonnull byte[] messageBytes) {
        Preconditions.checkNotEmpty(messageBytes, "messageBytes");
        // defensive copy
        this.messageBytes = messageBytes.clone();
    }

    @Override
    @Nonnull
    public byte[] getMessageBytes() {
        // defensive copy
        return messageBytes.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractProtocolMessage that = (AbstractProtocolMessage) o;

        return Arrays.equals(messageBytes, that.messageBytes);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(messageBytes);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "messageBytes=" + EidasStringUtil.encodeToBase64(messageBytes) +
                '}';
    }
}
