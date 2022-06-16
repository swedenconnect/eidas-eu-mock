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

import eu.eidas.auth.commons.light.ILightResponse;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Concrete implementation of the {@link ILightResponse} interface.
 * <p>
 * This class uses the Builder Pattern and is immutable thus thread-safe.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
@XmlRootElement
@XmlType(factoryMethod="newInstance")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(AbstractLightResponse.class)
public final class LightResponse extends AbstractLightResponse implements Serializable {

	@SuppressWarnings("unused")
	private static LightResponse newInstance(){
		return new LightResponse(builder());
	}
    /**
     * <p>
     * Builder pattern for the {@link LightResponse} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder extends AbstractLightResponse.AbstractBuilder<Builder, LightResponse> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull ILightResponse copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected LightResponse newInstance() {
            return new LightResponse(this);
        }
    }

    private static final long serialVersionUID = 3778981569989729382L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull ILightResponse copy) {
        return new Builder(copy);
    }

    private LightResponse(@Nonnull Builder builder) {
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
     *
     * @return any {@link LightResponse} object
     * @throws ObjectStreamException exception bulding the {@link LightResponse}
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
