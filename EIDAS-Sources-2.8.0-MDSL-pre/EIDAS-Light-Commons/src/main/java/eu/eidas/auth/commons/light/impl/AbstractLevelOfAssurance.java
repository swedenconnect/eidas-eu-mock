/*
 * Copyright (c) 2021 by European Commission
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
import eu.eidas.auth.commons.light.LevelOfAssuranceType;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

/**
 * Abstract class for implementations of the {@link ILevelOfAssurance} interface.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link ILevelOfAssurance} should extend this class and its Builder.
 *
 * @since 2.5
 */
@XmlType
public abstract class AbstractLevelOfAssurance implements ILevelOfAssurance {

    /**
     * <p>
     * Abstract Builder pattern with self-bounding generics for {@link ILevelOfAssurance} subtypes.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     * <p>See Self-bounding generics: <p>http://www.artima.com/weblogs/viewpost.jsp?thread=136394<p>
     * http://www.artima.com/forums/flat.jsp?forum=106&amp;thread=136394<p> http://en.wikipedia.org/wiki/Covariance_and_contravariance
     *
     * @param <B> the type of the Builder itself
     * @param <T> the type being built by the {@link #build()} method of this builder.
     */
    @NotThreadSafe
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    public abstract static class AbstractBuilder<B extends AbstractLevelOfAssurance.AbstractBuilder<B, T>, T extends ILevelOfAssurance> {

        private String type;

        private String value;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractLevelOfAssurance.AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            type = copy.type;
            value = copy.value;
        }

        protected AbstractBuilder(@Nonnull ILevelOfAssurance copy) {
            Preconditions.checkNotNull(copy, "copy");
            type = copy.getType();
            value = copy.getValue();
        }

        @Nonnull
        public final B type(String type) {
            this.type = type;
            return self();
        }

        @Nonnull
        public final B value(String value) {
            this.value = value;
            return self();
        }

        private void ifNullInferTypeFromValue() {
            if (this.type == null) {
                LevelOfAssuranceType inferredType = LevelOfAssuranceType.fromLoAValue(value);
                this.type = (inferredType != null) ? inferredType.stringValue() : null;
            }
        }

        /**
         * Validates the internal state of this Builder before allowing to create new instances of the built type {@code
         * T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        private void internalValidate() throws IllegalArgumentException {
            Preconditions.checkNotBlank(value, "LevelOfAssurance");
            Preconditions.checkURISyntax(value, "LevelOfAssurance");
            validateTypeValue();
        }
        
        private void validateTypeValue() throws IllegalArgumentException {
            if (this.type != null) {
                LevelOfAssuranceType givenType = LevelOfAssuranceType.fromString(this.type);
                Preconditions.checkNotNull(givenType, "LevelOfAssurance.type");
                LevelOfAssuranceType inducedType = LevelOfAssuranceType.fromLoAValue(this.value);
                if (givenType != inducedType) {
                    throw new IllegalArgumentException("LevelOfAssurance.type should be coherent with the Level of Assurance value");
                }
            }
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        protected void validate() throws IllegalArgumentException {
            internalValidate();
        }

        /**
         * <p>
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link
         * ILevelOfAssurance}).
         *
         * The {@link #validate()} is always called before creating a new instance through a call to the {@link
         * #newInstance()} method.
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        public final T build() {
            ifNullInferTypeFromValue();
            validate();
            return newInstance();
        }

        /**
         * <p>
         * Method to be implemented by subtypes to create the right type {@code T} of instances.
         *
         * <br>The typical implementation simply consists in writing {@code return new MySubType(this);}
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        protected abstract T newInstance();

        /**
         * Typesafe method to avoid casting to obtain subclasses builder instances.
         * @return the current instance implementation.
         */
        protected abstract B self();
    }

    private static final long serialVersionUID = -7069697598645647019L;

    /**
     * @serial
     */
    @XmlAttribute
    private final String type;

    /**
     * @serial
     */
    @XmlValue
    private final String value;

    protected AbstractLevelOfAssurance(@Nonnull AbstractLevelOfAssurance.AbstractBuilder<?, ?> builder) {
        this.type = builder.type;
        this.value = builder.value;
    }

    @Nonnull
    @Override
    public final String getType() {
        return (type == null) ? LevelOfAssuranceType.NOTIFIED.stringValue() : type;
    }

    @Nonnull
    @Override
    public final String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractLevelOfAssurance that = (AbstractLevelOfAssurance) o;

        if (!Objects.equals(getType(), that.getType())) {
            return false;
        }
        return Objects.equals(value, that.value);

    }

    @Override
    public int hashCode() {
        int result = getType().hashCode();
        result = 31 * result + (value.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder, @Nonnull ILevelOfAssurance loa) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(loa, "LevelOfAssurance");
        return stringBuilder.append("type='")
                .append(loa.getType())
                .append('\'')
                .append(", ")
                .append("value='")
                .append(loa.getValue())
                .append('\'');
    }

}
