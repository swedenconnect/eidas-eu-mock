/*
 * Copyright (c) 2024 by European Commission
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
package eu.eidas.auth.commons.attribute;

import eu.eidas.auth.commons.EidasErrorKey;
import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import eu.eidas.auth.commons.io.ReloadableFileAccessor;
import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A registry of {@link AttributeDefinition}s based on files on the classpath or on the filesystem.
 * <p>
 * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
 * <p>
 * If the configuration files are available as a file in the classpath or on the filesystem, then they are reloaded
 * automatically when their last-modified-date changes.
 * <p>
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
        List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> definitionAccessors =
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
            for (final SingletonAccessor<SortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                SortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
                if (null != attributeDefinitions && attributeDefinitions.contains(attributeDefinition)) {
                    return true;
                }
            }
        } catch (IOException ioe) {
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),ioe.getMessage(),ioe);
        }
        return false;
    }

    private List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> getAccessors() {
        return attributeDefinitionDao.getAttributeDefinitionAccessors();
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getAttributes() {
        try {
            List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> accessors =
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
    public SortedSet<AttributeDefinition<?>> getByFilter(@Nonnull AttributeDefinitionFilter filter) {
        Preconditions.checkNotNull(filter, "filter");
        try {
            TreeSet<AttributeDefinition<?>> filteredAttributes = new TreeSet<>();
            for (final SingletonAccessor<SortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                SortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
                if (null != attributeDefinitions) {
                    for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                        if (filter.accept(attributeDefinition)) {
                            filteredAttributes.add(attributeDefinition);
                        }
                    }
                }
            }
            return filteredAttributes;
        } catch (IOException ioe) {
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(), ioe.getMessage(), ioe);
        }
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getByFriendlyName(@Nonnull final String friendlyName) {
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
    public SortedSet<AttributeDefinition<?>> getByPersonType(@Nonnull final PersonType type) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.getPersonType() == type;
            }
        });
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getByRequired(final boolean required) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isRequired() == required;
            }
        });
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getByTransliteration(final boolean isTransliterationMandatory) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isTransliterationMandatory() == isTransliterationMandatory;
            }
        });
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getByUniqueIdentifier(final boolean isUniqueIdentifier) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.isUniqueIdentifier() == isUniqueIdentifier;
            }
        });
    }

    @Nonnull
    public SortedSet<AttributeDefinition<?>> getByXmlType(@Nonnull final QName xmlType) {
        return getByFilter(new AttributeDefinitionFilter() {

            @Override
            public boolean accept(@Nonnull AttributeDefinition attributeDefinition) {
                return attributeDefinition.getXmlType().equals(xmlType);
            }
        });
    }

    private void logRetrievedAttributes() {
        if (LOG.isDebugEnabled()) {
            for (final SingletonAccessor<SortedSet<AttributeDefinition<?>>> accessor : getAccessors()) {
                try {
                    SortedSet<AttributeDefinition<?>> attributeDefinitions = accessor.get();
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
