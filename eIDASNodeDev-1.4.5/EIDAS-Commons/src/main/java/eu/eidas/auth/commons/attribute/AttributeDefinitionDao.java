package eu.eidas.auth.commons.attribute;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

import eu.eidas.auth.commons.io.SingletonAccessor;

/**
 * AttributeDefinition Data Access Object (DAO).
 *
 * @since 1.1
 */
public interface AttributeDefinitionDao {

    /**
     * Returns the list of accessors to attribute definition sets which can use any kind of storage or format.
     *
     * @return the list of accessors to attribute definition sets.
     */
    @Nonnull
    ImmutableList<? extends SingletonAccessor<ImmutableSortedSet<AttributeDefinition<?>>>> getAttributeDefinitionAccessors();
}
