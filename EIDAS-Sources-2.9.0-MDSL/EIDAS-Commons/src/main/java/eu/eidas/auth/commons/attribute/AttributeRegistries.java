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

import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Static factory methods pertaining to {@link AttributeRegistry}.
 *
 * @since 1.1
 */
public final class AttributeRegistries {

    private static final AttributeRegistry EMPTY = new AttributeRegistry(MemoryAttributeDefinitionDao.EMPTY);

    /**
     * Copies an attribute registry in memory from another existing AttributeRegistry.
     *
     * @param attributeRegistry the attribute registry to copy.
     * @param otherRegistries   the other attribute registry
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry copyOf(@Nonnull AttributeRegistry attributeRegistry,
                                           @Nullable AttributeRegistry... otherRegistries) {
        Preconditions.checkNotNull(attributeRegistry, "attributeRegistry");
        if (null == otherRegistries || otherRegistries.length <= 0) {
            return of(attributeRegistry.getAttributes());
        }
        SortedSet<AttributeDefinition<?>> combinedSet = new TreeSet<>(attributeRegistry.getAttributes());
        for (AttributeRegistry registry : otherRegistries) {
            combinedSet.addAll(registry.getAttributes());
        }
        return of(combinedSet);
    }

    /**
     * Returns an empty immutable registry.
     * <p>
     * This convenient method is equivalent to calling:
     * <p>
     * {@code AttributeRegistry empty = AttributeRegistries.of();}
     *
     * @return an empty immutable registry.
     */
    @Nonnull
    public static AttributeRegistry empty() {
        return EMPTY;
    }

    /**
     * Creates an attribute registry based on the given configuration file.
     * <p>
     * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the configuration file.
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry fromFile(@Nonnull String fileName) {
        return fromFile(fileName, null);
    }

    /**
     * Creates an attribute registry based on the given configuration file.
     * <p>
     * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the configuration file.
     * @param defaultPath optional path to registry file.
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry fromFile(@Nonnull String fileName, @Nullable String defaultPath) {
        Preconditions.checkNotNull(fileName, "fileName");
        return new AttributeRegistry(new FileAttributeDefinitionDao(fileName, defaultPath));
    }

    /**
     * Creates an attribute registry based on the given configuration files.
     * <p>
     * The given files must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the first configuration file.
     * @param defaultPath the default path
     * @param fileNames the names of the other configuration files.
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry fromFiles(@Nonnull String fileName, @Nullable String defaultPath, @Nonnull String... fileNames) {
        Preconditions.checkNotNull(fileName, "fileName");
        Preconditions.checkNotNull(fileNames, "fileNames");
        return new AttributeRegistry(new FileAttributeDefinitionDao(fileName, defaultPath, fileNames));
    }

    /**
     * Creates an attribute registry based on the given AttributeDefinitions.
     *
     * @param definitions the attribute definitions constituting the registry.
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry of(@Nonnull Iterable<AttributeDefinition<?>> definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        MemoryAttributeDefinitionDao attributeDefinitionDao = MemoryAttributeDefinitionDao.copyOf(definitions);
        if (MemoryAttributeDefinitionDao.EMPTY == attributeDefinitionDao) {
            return EMPTY;
        }
        return new AttributeRegistry(attributeDefinitionDao);
    }

    /**
     * Creates an attribute registry based on the given AttributeDefinitions.
     *
     * @param definitions the attribute definitions constituting the registry.
     * @return the attribute registry
     */
    @Nonnull
    public static AttributeRegistry of(@Nonnull AttributeDefinition<?>... definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        MemoryAttributeDefinitionDao attributeDefinitionDao = MemoryAttributeDefinitionDao.copyOf(definitions);
        if (MemoryAttributeDefinitionDao.EMPTY == attributeDefinitionDao) {
            return EMPTY;
        }
        return new AttributeRegistry(attributeDefinitionDao);
    }

    private AttributeRegistries() {
    }
}
