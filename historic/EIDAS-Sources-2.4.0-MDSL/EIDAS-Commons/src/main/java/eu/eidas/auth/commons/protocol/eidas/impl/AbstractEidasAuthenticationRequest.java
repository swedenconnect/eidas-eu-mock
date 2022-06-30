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
 * limitations under the Licence.
 *
 */
package eu.eidas.auth.commons.protocol.eidas.impl;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.EidasErrors;
import eu.eidas.auth.commons.exceptions.EidasNodeException;
import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import eu.eidas.util.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.io.Serializable;

/**
 * Abstract class for implementations of the {@link IAuthenticationRequest} interface which adds support for the eIDAS
 * protocol.
 * <p>
 * This class uses the Builder Pattern.
 * <p>
 * Implementors of the {@link IEidasAuthenticationRequest} should extend this class and its Builder.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
public abstract class AbstractEidasAuthenticationRequest extends AbstractAuthenticationRequest
        implements IEidasAuthenticationRequest, Serializable {
    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AbstractEidasAuthenticationRequest.class);
    /**
     * Abstract Builder pattern with self-bounding generics for {@link IEidasAuthenticationRequest} subtypes.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p>
     * See Self-bounding generics:<p> http://www.artima.com/weblogs/viewpost.jsp?thread=136394<p>
     * http://www.artima.com/forums/flat.jsp?forum=106&amp;thread=136394<p> http://en.wikipedia.org/wiki/Covariance_and_contravariance<p>
     *
     * @param B the type of the Builder itself
     * @param T the type being built by the {@link #build()} method of this builder.
     */
    @SuppressWarnings({"ParameterHidesMemberVariable", "unchecked"})
    @NotThreadSafe
    public abstract static class AbstractBuilder<B extends AbstractBuilder<B, T>, T extends IEidasAuthenticationRequest>
            extends AbstractAuthenticationRequest.AbstractBuilder<B, T> {

        private LevelOfAssuranceComparison levelOfAssuranceComparison;

        protected AbstractBuilder() {
        }

        protected AbstractBuilder(@Nonnull AbstractBuilder<?, ?> copy) {
            super(copy);
            levelOfAssuranceComparison = copy.levelOfAssuranceComparison;
        }

        protected AbstractBuilder(@Nonnull IEidasAuthenticationRequest copy) {
            super(copy);
            levelOfAssuranceComparison = copy.getLevelOfAssuranceComparison();
        }

        @Nonnull
        public final B levelOfAssurance(@Nonnull LevelOfAssurance levelOfAssurance) {
            Preconditions.checkNotNull(levelOfAssurance, "levelOfAssurance");
            levelOfAssurance(levelOfAssurance.getValue());
            return (B) this;
        }

        @Nonnull
        public final B levelOfAssuranceComparison(LevelOfAssuranceComparison levelOfAssuranceComparison) {
            this.levelOfAssuranceComparison = levelOfAssuranceComparison;
            return (B) this;
        }

        @Nonnull
        public final B levelOfAssuranceComparison(String levelOfAssuranceComparison) {
            LevelOfAssuranceComparison comparison = LevelOfAssuranceComparison.fromString(levelOfAssuranceComparison);
            if (StringUtils.isNotBlank(levelOfAssuranceComparison) && null == comparison) {
                throw new IllegalArgumentException(
                        "Invalid levelOfAssuranceComparison \"" + levelOfAssuranceComparison + "\"");
            }
            this.levelOfAssuranceComparison = comparison;
            return (B) this;
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        @Override
        protected final void validate() throws IllegalArgumentException {
            if (null == levelOfAssuranceComparison) {
                levelOfAssuranceComparison = LevelOfAssuranceComparison.MINIMUM;
            }
            validateOtherFields();
        }

        /**
         * Validates the state of this Builder before allowing to create new instances of the built type {@code T}.
         *
         * @throws IllegalArgumentException if the builder is not in a legal state allowing to proceed with the creation
         * of a {@code T} instance.
         */
        protected abstract void validateOtherFields() throws IllegalArgumentException;
    }

    private static final long serialVersionUID = -7818930049279284084L;

    /**
     * @serial
     */
    @Nullable
    private final LevelOfAssurance levelOfAssurance;

    /**
     * The eIDAS level of assurance compare type.
     *
     * @serial
     */
    @Nonnull
    private final LevelOfAssuranceComparison levelOfAssuranceComparison; // Always set to minimum

    protected AbstractEidasAuthenticationRequest(@Nonnull AbstractBuilder<?, ?> builder) {
        super(builder);
        String levelOfAssuranceStr = getLevelOfAssurance();
        if (null != levelOfAssuranceStr) {
            LevelOfAssurance levelOfAssuranceEnum = LevelOfAssurance.fromString(levelOfAssuranceStr);
            if (null == levelOfAssuranceEnum) {
                String message = "Node LoA is not set, authentication request has no levelOfAssurance";
                LOG.error(message);
                throw new EidasNodeException(
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorCode()),
                        EidasErrors.get(EidasErrorKey.INVALID_LOA_VALUE.errorMessage()));
            }
            this.levelOfAssurance = levelOfAssuranceEnum;
        } else {
            this.levelOfAssurance = null;
        }
        levelOfAssuranceComparison =
                builder.levelOfAssuranceComparison; //Kept but since 1.0 technical specs - only minimum is allowed
    }

    @Override
    @Nullable
    public final LevelOfAssurance getEidasLevelOfAssurance() {
        return levelOfAssurance;
    }

    @Override
    @Nonnull
    public final LevelOfAssuranceComparison getLevelOfAssuranceComparison() {
        return levelOfAssuranceComparison;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        AbstractEidasAuthenticationRequest that = (AbstractEidasAuthenticationRequest) o;

        return levelOfAssuranceComparison == that.levelOfAssuranceComparison;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (levelOfAssuranceComparison.hashCode());
        return result;
    }

    @Override
    protected StringBuilder toString(@Nonnull StringBuilder stringBuilder) {
        return toString(stringBuilder, this);
    }

    public static StringBuilder toString(@Nonnull StringBuilder stringBuilder,
                                         @Nonnull IEidasAuthenticationRequest request) {
        return AbstractAuthenticationRequest.toString(stringBuilder, request)
                .append(", levelOfAssuranceComparison='")
                .append(request.getLevelOfAssuranceComparison())
                .append('\'');
    }
}
