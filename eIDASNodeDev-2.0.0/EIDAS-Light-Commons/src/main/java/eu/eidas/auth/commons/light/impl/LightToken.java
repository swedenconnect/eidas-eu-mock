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
package eu.eidas.auth.commons.light.impl;

import eu.eidas.auth.commons.light.ILightToken;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * This class is implementing the key of data exchange between EidasNode Core and MS-Specific parts.
 * It is encapsulating a collection of information provided by the originator party.
 *
 * This is not the class for the HTTP session, check {@link eu.eidas.auth.commons.tx.BinaryLightToken}.
 *
 * @since 2.0.0
 */
@Immutable
@ThreadSafe
public final class LightToken extends AbstractLightToken implements Serializable {

    private static final long serialVersionUID = -6014445106026657701L;

    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder extends AbstractLightToken.AbstractBuilder<Builder, LightToken> implements Serializable {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull ILightToken copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected LightToken newInstance() {
            return new LightToken(this);
        }
    }


    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull LightToken copy) {
        return new Builder(copy);
    }

    private LightToken(@Nonnull Builder builder) {
        super(builder);
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p/>
     * Used upon de-serialization, not serialization.
     * <p/>
     * The state of this class is transformed back into the class it represents.
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }

}
