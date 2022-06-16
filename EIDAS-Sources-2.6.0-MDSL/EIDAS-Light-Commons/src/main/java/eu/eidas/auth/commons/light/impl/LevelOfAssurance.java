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

import eu.eidas.auth.commons.light.ILevelOfAssurance;

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
 * Concrete class implementing the {@link ILevelOfAssurance} interface.
 * <p>
 * This class uses the Builder Pattern and is immutable thus thread-safe.
 *
 * @since 2.5
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
@XmlType(factoryMethod="newInstance")
@XmlAccessorType(XmlAccessType.FIELD)
public class LevelOfAssurance extends AbstractLevelOfAssurance implements Serializable {

    private static LevelOfAssurance newInstance(){
        return new LevelOfAssurance(builder());
    }

    /**
     * <p>
     * Builder pattern for the {@link LevelOfAssurance} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     */
    @NotThreadSafe
    @SuppressWarnings("ParameterHidesMemberVariable")
    public static final class Builder extends AbstractLevelOfAssurance.AbstractBuilder<LevelOfAssurance.Builder, LevelOfAssurance> {

        public Builder() {
        }

        public Builder(@Nonnull LevelOfAssurance.Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull ILevelOfAssurance copy) {
            super(copy);
        }

        @Override
        protected Builder self() {
            return this;
        }

        @Nonnull
        @Override
        protected LevelOfAssurance newInstance() {
            return new LevelOfAssurance(this);
        }
    }

    private static final long serialVersionUID = -3301069405646822193L;

    @Nonnull
    public static LevelOfAssurance build(String value) {
        return LevelOfAssurance.builder()
                .value(value)
                .build();
    }

    @Nonnull
    public static LevelOfAssurance.Builder builder() {
        return new LevelOfAssurance.Builder();
    }

    @Nonnull
    public static LevelOfAssurance.Builder builder(@Nonnull LevelOfAssurance.Builder copy) {
        return new LevelOfAssurance.Builder(copy);
    }

    @Nonnull
    public static LevelOfAssurance.Builder builder(@Nonnull ILevelOfAssurance copy) {
        return new LevelOfAssurance.Builder(copy);
    }

    private LevelOfAssurance(@Nonnull LevelOfAssurance.Builder builder) {
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
     * @return any {@link LevelOfAssurance} object
     * @throws ObjectStreamException exception bulding the {@link LevelOfAssurance}
     */
    private Object readResolve() throws ObjectStreamException {
        return new LevelOfAssurance.Builder(this).build();
    }
}
