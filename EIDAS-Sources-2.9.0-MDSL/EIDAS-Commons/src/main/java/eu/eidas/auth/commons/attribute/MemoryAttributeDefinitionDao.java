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
import eu.eidas.util.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MemoryAttributeDefinitionDao
 *
 * @since 1.1
 */
final class MemoryAttributeDefinitionDao implements AttributeDefinitionDao {

    private static final class AtomicState implements SingletonAccessor<SortedSet<AttributeDefinition<?>>> {

        @Nonnull
        private final AtomicReference<SortedSet<AttributeDefinition<?>>> reference;

        private AtomicState(@Nonnull SortedSet<AttributeDefinition<?>> definitions) {
            Preconditions.checkNotNull(definitions, "definitions");
            reference = new AtomicReference<>(definitions);
        }

        @Nullable
        @Override
        public SortedSet<AttributeDefinition<?>> get() {
            return reference.get();
        }

        @Override
        public void set(@Nonnull SortedSet<AttributeDefinition<?>> newValue) {
            Preconditions.checkNotNull(newValue, "newValue");
            reference.set(newValue);
        }
    }

    static final MemoryAttributeDefinitionDao EMPTY = new MemoryAttributeDefinitionDao(
            List.of(new AtomicState(Collections.emptySortedSet())));

    static MemoryAttributeDefinitionDao copyOf(@Nonnull Iterable<AttributeDefinition<?>> definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        SortedSet<AttributeDefinition<?>> copyOf = new TreeSet<>();
        for (AttributeDefinition<?> definition : definitions) {
            copyOf.add(definition);
        }
        if (copyOf.isEmpty()) {
            return EMPTY;
        }
        return new MemoryAttributeDefinitionDao(copyOf);
    }

    static MemoryAttributeDefinitionDao copyOf(@Nonnull AttributeDefinition<?>... definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        SortedSet<AttributeDefinition<?>> copyOf = new TreeSet<>(List.of(definitions));
        if (copyOf.isEmpty()) {
            return EMPTY;
        }
        return new MemoryAttributeDefinitionDao(copyOf);
    }

    private final List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> accessors;

    private MemoryAttributeDefinitionDao(@Nonnull final SortedSet<AttributeDefinition<?>> definitions) {
        this(List.of(new AtomicState(definitions)));
    }

    private MemoryAttributeDefinitionDao(
            @Nonnull List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> accessors) {
        this.accessors = accessors;
    }

    @Nonnull
    @Override
    public List<? extends SingletonAccessor<SortedSet<AttributeDefinition<?>>>> getAttributeDefinitionAccessors() {
        return accessors;
    }
}
