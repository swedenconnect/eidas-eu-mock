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
 * This is not the class for the HTTP session, check @see eu.eidas.auth.commons.tx.BinaryLightToken.
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
     * <p>
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p>
     * Used upon de-serialization, not serialization.
     *
     * The state of this class is transformed back into the class it represents.
     *
     * @return any {@link LightToken} object
     * @throws ObjectStreamException exception bulding the {@link LightToken}
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }

}
