/*
 * Copyright (c) 2017 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the
 * "NOTICE" text file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution.
 * Any derivative works that you distribute must include a readable
 * copy of the "NOTICE" text file.
 */
package eu.eidas.auth.commons.tx;

import eu.eidas.auth.commons.EidasStringUtil;
import eu.eidas.auth.commons.light.ILightToken;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;

/**
 * This class is implementing the reference token to be used in HTTP clients forwarded between Specific and Node core parts.
 * It encapsulates both the "binary" and the logical token.
 *
 * @since 2.0.0
 */
@Immutable
@ThreadSafe
public final class BinaryLightToken {

    /**
     * Logical LightToken
     */
    @Nonnull
    private final ILightToken lightToken;

    /**
     * Physical LightToken part (need to be BASE64 encoded in the requests)
     */
    @Nonnull
    private final byte[] tokenBytes;

    public BinaryLightToken(@Nonnull ILightToken lightToken, @Nonnull byte[] tokenBytes) {
        Preconditions.checkNotNull(lightToken, "lightToken");
        Preconditions.checkNotEmpty(tokenBytes, "tokenBytes");
        this.tokenBytes = tokenBytes.clone();
        this.lightToken = lightToken;
    }

    /**
     * Logical LightToken what is equal to the encoded physical one
     * @return logical light token
     */
    @Nonnull
    public ILightToken getToken() {
        return lightToken;
    }

    /**
     * Binary or physical LightToken what is equal to the logical one
     * @return physical light token (need to be BASE64 encoded in the requests)
     */
    @Nonnull
    public byte[] getTokenBytes() {
        return tokenBytes.clone();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BinaryLightToken that = (BinaryLightToken) o;

        if (!lightToken.equals(that.lightToken)) {
            return false;
        }
        return Arrays.equals(getTokenBytes(), that.getTokenBytes());
    }

    @Override
    public int hashCode() {
        int result = lightToken.hashCode();
        result = 31 * result + Arrays.hashCode(getTokenBytes());
        return result;
    }

    @Override
    public String toString() {
        return "BinaryLightToken{" +
                "lightToken=" + lightToken +
                ", tokenBytes=" + EidasStringUtil.encodeToBase64(getTokenBytes()) +
                '}';
    }

}
