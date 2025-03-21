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

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.auth.commons.io.SingletonAccessors;
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

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
final class FileAttributeDefinitionDao implements AttributeDefinitionDao {

    private static SingletonAccessor<SortedSet<AttributeDefinition<?>>> newSingletonAccessor(
            @Nonnull String fileName, @Nullable String defaultPath) {
        return SingletonAccessors.newPropertiesAccessor(fileName, defaultPath, AttributeSetPropertiesConverter.INSTANCE);
    }

    private static List<SingletonAccessor<SortedSet<AttributeDefinition<?>>>> newSingletonAccessors(
            @Nonnull String fileName, @Nullable String defaultPath, @Nonnull String[] fileNames) {
        List<SingletonAccessor<SortedSet<AttributeDefinition<?>>>> singletonAccessors = new ArrayList<>();
        Set<String> duplicates = new HashSet<>();
        duplicates.add(fileName);
        singletonAccessors.add(newSingletonAccessor(fileName, defaultPath));
        for (final String name : fileNames) {
            if (duplicates.add(name)) {
                singletonAccessors.add(newSingletonAccessor(name, defaultPath));
            }
        }
        return singletonAccessors;
    }

    @Nonnull
    private final List<SingletonAccessor<SortedSet<AttributeDefinition<?>>>> accessors;

    /**
     * Creates an attribute registry based on the given configuration file.
     * <p>
     * The given file must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the configuration file.
     */
    FileAttributeDefinitionDao(@Nonnull String fileName, @Nullable String defaultPath) {
        Preconditions.checkNotNull(fileName, "fileName");
        accessors = List.of(newSingletonAccessor(fileName, defaultPath));
    }

    /**
     * Creates an attribute registry based on the given configuration files.
     * <p>
     * The given files must comply with the attribute registry format (see {@link AttributeSetPropertiesConverter}.
     *
     * @param fileName the name of the first configuration file.
     * @param fileNames the names of the other configuration files.
     */
    FileAttributeDefinitionDao(@Nonnull String fileName, @Nullable String defaultPath, @Nonnull String... fileNames) {
        Preconditions.checkNotNull(fileName, "fileName");
        Preconditions.checkNotNull(fileNames, "fileNames");
        accessors = newSingletonAccessors(fileName, defaultPath, fileNames);
    }

    @Nonnull
    @Override
    public List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> getAttributeDefinitionAccessors() {
        return accessors;
    }
}
