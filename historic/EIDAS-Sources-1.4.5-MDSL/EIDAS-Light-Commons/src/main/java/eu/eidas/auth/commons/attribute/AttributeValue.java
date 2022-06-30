package eu.eidas.auth.commons.attribute;

import java.io.Serializable;

/**
 * Represents one value of an {@link AttributeDefinition}.
 * <p>
 * Implementations of this interface must implement {@link Serializable}.
 *
 * @param <T> the type of the value
 * @since 1.1
 */
public interface AttributeValue<T> extends Serializable {

    /**
     * Returns the typed value.
     *
     * @return the typed value.
     */
    T getValue();

    /**
     * Returns {@code true} when the given value is the alternate version of a value in a non-latin script, {@code
     * false} otherwise.
     *
     * @return {@code true} when the given value is the alternate version of a value in a non-latin script, {@code
     * false} otherwise.
     */
    boolean isNonLatinScriptAlternateVersion();
}
