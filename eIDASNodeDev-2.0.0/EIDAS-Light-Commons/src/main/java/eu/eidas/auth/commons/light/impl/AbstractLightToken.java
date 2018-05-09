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
import eu.eidas.util.Preconditions;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;

/**
 * This abstract class representing the key of data exchange between EidasNode Core and MS-Specific parts.
 * It is encapsulating a collection of information provided by the originator party.
 * <p/>
 * This is not the class for the HTTP session, check {@link eu.eidas.auth.commons.tx.BinaryLightToken}.
 *
 * @since 2.0.0
 */
@Immutable
@ThreadSafe
public abstract class AbstractLightToken implements ILightToken, Serializable {

    private static final long serialVersionUID = 8020612292452922165L;

    /**
     * @id unique
     */
    @Nonnull
    private final String id;

    /**
     * @issuer of token
     */
    @Nonnull
    private final String issuer;

    /**
     * @SEPARATOR default separator "|"
     */
    @Nonnull
    static public final String SEPARATOR = "|";

    /**
     * @LIGHTTOKEN_DATE_FORMAT default DateTimeFormatter from Joda
     */
    static public final DateTimeFormatter LIGHTTOKEN_DATE_FORMAT =
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss SSS");

    /**
     * @timestamp on token created
     */
    @Nonnull
    private final DateTime createdOn;

    protected AbstractLightToken(@Nonnull AbstractBuilder<?, ?> builder) {
        id = builder.id;
        issuer = builder.issuer;
        createdOn = builder.createdOn;
    }

    /**
     * Abstract builder used to construct the AbstractLightToken
     * @param <B>
     * @param <T>
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends ILightToken> {

        private String id;

        private String issuer;

        private DateTime createdOn;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.id;
            issuer = copy.issuer;
            createdOn = copy.createdOn;
        }

        protected AbstractBuilder(@Nonnull ILightToken copy) {
            Preconditions.checkNotNull(copy, "copy");
            id = copy.getId();
            issuer = copy.getIssuer();
            createdOn = copy.getCreatedOn();
        }

        @Nonnull
        public final B id(String id) {
            this.id = id;
            return (B) this;
        }

        @Nonnull
        public final B issuer(String issuer) {
            this.issuer = issuer;
            return (B) this;
        }

        @Nonnull
        public final B createdOn(DateTime createdOn) {
            this.createdOn = createdOn;
            return (B) this;
        }


        /**
         * Validates the internal state of this Builder before allowing to create new instances of the built type {@code
         * T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         *                                  of a {@code T} instance.
         */
        private void internalValidate() throws IllegalArgumentException {
            Preconditions.checkNotBlank(id, "id");
            if (id.contains(SEPARATOR)) {
                throw new IllegalArgumentException("id contains separator character");
            }
            Preconditions.checkNotBlank(issuer, "issuer");
            if (issuer.contains(SEPARATOR)) {
                throw new IllegalArgumentException("issuer contains separator character");
            }
            Preconditions.checkNotNull(createdOn, "createdOn");
            validate();
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         *                                  of a {@code T} instance.
         */
        protected abstract void validate() throws IllegalArgumentException;

        /**
         * Builds a new {@code T} instance based on this Builder instance (Builder pattern for {@link ILightToken}).
         * <p/>
         * The {@link #validate()} is always called before creating a new instance through a call to the {@link
         * #newInstance()} method.
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        public final T build() {
            internalValidate();
            return newInstance();
        }

        /**
         * Method to be implemented by subtypes to create the right type {@code T} of instances.
         * <p/>
         * The typical implementation simply consists in writing <br/>{@code return new MySubType(this);}
         *
         * @return a new {@code T} instance based on this Builder instance.
         */
        @Nonnull
        protected abstract T newInstance();
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getId() {
        return id;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getIssuer() {
        return issuer;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public DateTime getCreatedOn() {
        return createdOn;
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getFormattedCreatedOn() {
        return LIGHTTOKEN_DATE_FORMAT.print(createdOn);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public String getKey() {
        return new StringBuilder().append(issuer).append(SEPARATOR).append(id).append(SEPARATOR).append(getFormattedCreatedOn()).toString();
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        AbstractLightToken lightTokenObj = (AbstractLightToken) obj;

        if (!id.equals(lightTokenObj.id)) {
            return false;
        }

        if (!issuer.equals(lightTokenObj.issuer)) {
            return false;
        }

        if (createdOn.compareTo(lightTokenObj.createdOn) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (issuer.hashCode());
        result = 31 * result + (createdOn.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return toString(new StringBuilder(getClass().getSimpleName()).append("{")).append('}').toString();
    }

    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder, @Nonnull ILightToken token) {
        Preconditions.checkNotNull(stringBuilder, "stringBuilder");
        Preconditions.checkNotNull(token, "token");
        return stringBuilder.append("id='")
                .append(token.getId())
                .append('\'')
                .append(", issuer='")
                .append(token.getIssuer())
                .append('\'')
                .append(", createdOn='")
                .append(LIGHTTOKEN_DATE_FORMAT.print(token.getCreatedOn()))
                .append('\'');
    }

}
