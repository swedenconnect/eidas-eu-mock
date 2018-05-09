/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons.attribute;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.io.ReloadableFileAccessor;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.util.Preconditions;

/**
 * A registry of {@link AttributeDefinition}s based on files on the classpath or on the filesystem.
 * <p/>
 * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
 * <p/>
 * If the configuration files are available as a file in the classpath or on the filesystem, then they are reloaded
 * automatically when their last-modified-date changes.
 * <p/>
 * If the configuration files are available inside a jar in the classpath, then they are loaded once and for all and
 * cannot be reloaded.
 *
 * @see AttributeSetPropertiesConverter
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class AttributeRegistry {

    /**
     * Instances of this interface are used to filter the result of the {@link #getByFilter(AttributeDefinitionFilter)}
     * method.
     *
     * @see AttributeRegistry#getByFilter(AttributeDefinitionFilter)
     */
    public interface AttributeDefinitionFilter {

        /**
         * Tests if a specified {@link AttributeDefinition} should be included in a result.
         *
         * @param attributeDefinition the attribute definition to test.
         * @return <code>true</code> if and only if the {@link AttributeDefinition} should be included in the result;
         * <code>false</code> otherwise.
         */
        boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition);
    }

    private static final AttributeDefinitionFilter ALL_FILTER = new AttributeDefinitionFilter() {

        @Override
        public boolean accept(@Nonnull AttributeDefinition<?> attributeDefinition) {
            return true;
        }
    };

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeRegistry.class);

    private final AttributeDefinitionDao attributeDefinitionDao;

    /**
     * Creates an attribute registry based on the given {@link SingletonAccessor}s.
     *
     * @param attributeDefinitionDao the attributeDefinitionDao.
     */
    public AttributeRegistry(@Nonnull AttributeDefinitionDao attributeDefinitionDao) {
        Preconditions.checkNotNull(attributeDefinitionDao, "attributeDefinitionDao");
        ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> definitionAccessors =
                attributeDefinitionDao.getAttributeDefinitionAccessors();
        Preconditions.checkNotNull(definitionAccessors, "accessors");
        this.attributeDefinitionDao = attributeDefinitionDao;
        logRetrievedAttributes();
    }

    /**
     * Returns {@code true} when this instance contains the given attribute definition, {@code false} otherwise.
     *
     * @param attributeDefinition the attribute definition to look up
     * @return {@code true} when this instance contains the given attribute definition, {@code false} otherwise.
     */
    public boolean contains(@Nonnull AttributeDefinition<?> attributeDefinition) {
        Preconditions.checkNotNull(attributeDefinition, "attributeDefinition");
        try {
            for (final SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
                if (null != attributeDefinitions && attributeDefinitions.contains(attributeDefinition)) {
                    return true;
                }
            }
        } catch (IOException ioe) {
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),ioe.getMessage(),ioe);
        }
        return false;
    }

    private ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> getAccessors() {
        return attributeDefinitionDao.getAttributeDefinitionAccessors();
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getAttributes() {
        try {
            ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> accessors =
                    getAccessors();
            if (accessors.size() == 1) {
                return accessors.get(0).get();
            }
            return getByFilter(ALL_FILTER);
        } catch (IOException ioe) {
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),ioe.getMessage(),ioe);
        }
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByFilter(@Nonnull AttributeDefinitionFilter filter) {
        Preconditions.checkNotNull(filter, "filter");
        try {
            ImmutableSortedSet.Builder<AttributeDefinition<?>> builder =
                    new ImmutableSortedSet.Builder<>(Ordering.natural());
            for (final SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
                if (null != attributeDefinitions) {
                    for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                        if (filter.accept(attributeDefinition)) {
                            builder.add(attributeDefinition);
                        }
                    }
                }
            }
            return builder.build();
        } catch (IOException ioe) {
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),ioe.getMessage(),ioe);
        }
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByFriendlyName(@Nonnull final String friendlyName) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.getFriendlyName().equals(friendlyName);
            }
        });
    }

    @Nullable
    public AttributeDefinition<?> getByName(@Nonnull String name) {
        URI nameUri;
        try {
            nameUri = new URI(name);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Invalid name URI \"" + name + "\": " + use, use);
        }
        return getByName(nameUri);
    }

    @Nullable
    public AttributeDefinition<?> getByName(@Nonnull URI nameUri) {
        for (final AttributeDefinition attribute : getAttributes()) {
            if (attribute.getNameUri().equals(nameUri)) {
                return attribute;
            }
        }
        return null;
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByPersonType(@Nonnull final PersonType type) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.getPersonType() == type;
            }
        });
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByRequired(final boolean required) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isRequired() == required;
            }
        });
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByTransliteration(final boolean isTransliterationMandatory) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isTransliterationMandatory() == isTransliterationMandatory;
            }
        });
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByUniqueIdentifier(final boolean isUniqueIdentifier) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isUniqueIdentifier() == isUniqueIdentifier;
            }
        });
    }

    @Nonnull
    public ImmutableSortedSet<AttributeDefinition<?>> getByXmlType(@Nonnull final QName xmlType) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.getXmlType().equals(xmlType);
            }
        });
    }

    private void logRetrievedAttributes() {
        if (LOG.isDebugEnabled()) {
            for (final SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                try {
                    ImmutableSortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
                    if (LOG.isTraceEnabled()) {
                        LOG.trace(
                                "AttributeRegistry contains attributes" + (accessor instanceof ReloadableFileAccessor ?
                                                                           " from file \"" + ((ReloadableFileAccessor<?>) accessor)
                                                                                   .getFilename() + "\"" : "") + ": " + attributeDefinitions);
                    }
                } catch (IOException ioe) {
                    throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),ioe.getMessage(),ioe);
                }
            }
        }
    }
}
