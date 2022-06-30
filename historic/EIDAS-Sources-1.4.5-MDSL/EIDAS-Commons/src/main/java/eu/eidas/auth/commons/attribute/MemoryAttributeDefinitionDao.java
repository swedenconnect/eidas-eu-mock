package eu.eidas.auth.commons.attribute;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.auth.commons.io.SingletonAccessor;
import eu.eidas.util.Preconditions;

/**
 * MemoryAttributeDefinitionDao
 *
 * @since 1.1
 */
final class MemoryAttributeDefinitionDao implements AttributeDefinitionDao {

    private static final class AtomicState implements SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>> {

        @Nonnull
        private final AtomicReference<ImmutableSortedSet<AttributeDefinition<?>>> reference;

        private AtomicState(@Nonnull ImmutableSortedSet<AttributeDefinition<?>> definitions) {
            Preconditions.checkNotNull(definitions, "definitions");
            reference = new AtomicReference<>(definitions);
        }

        @Nullable
        @Override
        public ImmutableSortedSet<AttributeDefinition<?>> get() {
            return reference.get();
        }

        @Override
        public void set(@Nonnull ImmutableSortedSet<AttributeDefinition<?>> newValue) {
            Preconditions.checkNotNull(newValue, "newValue");
            reference.set(newValue);
        }
    }

    static final MemoryAttributeDefinitionDao EMPTY = new MemoryAttributeDefinitionDao(
            ImmutableList.of(new SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>() {

                @Nullable
                @Override
                public ImmutableSortedSet<AttributeDefinition<?>> get() throws IOException {
                    return ImmutableSortedSet.of();
                }

                @Override
                public void set(@Nonnull ImmutableSortedSet<AttributeDefinition<?>> newValue) throws IOException {
                    throw new UnsupportedOperationException();
                }
            }));

    static MemoryAttributeDefinitionDao copyOf(@Nonnull Iterable<AttributeDefinition<?>> definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        ImmutableSortedSet<AttributeDefinition<?>> copyOf = ImmutableSortedSet.copyOf(definitions);
        if (copyOf.isEmpty()) {
            return EMPTY;
        }
        return new MemoryAttributeDefinitionDao(copyOf);
    }

    static MemoryAttributeDefinitionDao copyOf(@Nonnull AttributeDefinition<?>... definitions) {
        Preconditions.checkNotNull(definitions, "definitions");
        ImmutableSortedSet<AttributeDefinition<?>> copyOf = ImmutableSortedSet.copyOf(definitions);
        if (copyOf.isEmpty()) {
            return EMPTY;
        }
        return new MemoryAttributeDefinitionDao(copyOf);
    }

    private final ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> accessors;

    private MemoryAttributeDefinitionDao(@Nonnull final ImmutableSortedSet<AttributeDefinition<?>> definitions) {
        this(ImmutableList.of(new AtomicState(definitions)));
    }

    private MemoryAttributeDefinitionDao(
            @Nonnull ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> accessors) {
        this.accessors = accessors;
    }

    @Nonnull
    @Override
    public ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> getAttributeDefinitionAccessors() {
        return accessors;
    }
}
