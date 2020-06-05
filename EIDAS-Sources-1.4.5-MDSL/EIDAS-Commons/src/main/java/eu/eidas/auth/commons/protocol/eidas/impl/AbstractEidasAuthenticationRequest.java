/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */

/**
 * This work is Open Source and licensed by the European Commission under the conditions of the European Public License
 * v1.1
 * <p/>
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 * <p/>
 * any use of this file implies acceptance of the conditions of this license. Unless required by applicable law or
 * agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package eu.eidas.auth.commons.protocol.eidas.impl;

import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.lang.StringUtils;

import eu.eidas.auth.commons.protocol.IAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.IEidasAuthenticationRequest;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssurance;
import eu.eidas.auth.commons.protocol.eidas.LevelOfAssuranceComparison;
import eu.eidas.auth.commons.protocol.impl.AbstractAuthenticationRequest;
import eu.eidas.util.Preconditions;

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
     * Abstract Builder pattern with self-bounding generics for {@link IEidasAuthenticationRequest} subtypes.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     * See Self-bounding generics:<p/> http://www.artima.com/weblogs/viewpost.jsp?thread=136394<p/>
     * http://www.artima.com/forums/flat.jsp?forum=106&thread=136394<p/> http://en.wikipedia.org/wiki/Covariance_and_contravariance<p/>
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
                throw new IllegalArgumentException("Invalid levelOfAssurance: \"" + levelOfAssuranceStr + "\"");
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

        if (levelOfAssuranceComparison != that.levelOfAssuranceComparison) {
            return false;
        }
        return true;
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
