/*
 * Copyright (c) 2020 by European Commission
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
 * limitations under the Licence.
 */
package eu.eidas.auth.commons.light.impl;

import eu.eidas.auth.commons.light.IResponseStatus;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Concrete class implementing the {@link IResponseStatus} interface.
 * <p>
 * This class uses the Builder Pattern and is immutable thus thread-safe.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe

@XmlType(factoryMethod="newInstance")
@XmlAccessorType(XmlAccessType.FIELD)
public final class ResponseStatus extends AbstractResponseStatus implements Serializable {

	private static ResponseStatus newInstance(){
		return builder()
				.statusCode("##")
				.build();
	}
	
    /**
     * <p>
     * Builder pattern for the {@link ResponseStatus} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     */
    @NotThreadSafe
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static final class Builder extends AbstractResponseStatus.AbstractBuilder<Builder, ResponseStatus> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull IResponseStatus copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected ResponseStatus newInstance() {
            return new ResponseStatus(this);
        }
    }

    private static final long serialVersionUID = -3301069405898822193L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull IResponseStatus copy) {
        return new Builder(copy);
    }

    private ResponseStatus(@Nonnull Builder builder) {
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
     * @return any {@link ResponseStatus} object
     * @throws ObjectStreamException exception bulding the {@link ResponseStatus}
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
