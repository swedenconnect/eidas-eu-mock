package eu.eidas.auth.commons.attribute;

import javax.annotation.Nonnull;

/**
 * Marshals and unmarshals a value of type T to and from a {@link java.lang.String}.
 *
 * @param <T> the type of the value
 * @since 1.1
 */
public interface AttributeValueMarshaller<T> {

    /**
     * Marshals the given typed value as a {@link java.lang.String}.
     *
     * @param value the typed value to marshal as a {@link java.lang.String}.
     * @return a {@link java.lang.String} representation of the given typed value.
     */
    @Nonnull
    String marshal(@Nonnull AttributeValue<T> value) throws AttributeValueMarshallingException;

    /**
     * Unmarshals the given {@link java.lang.String} as a typed value.
     *
     * @param value the {@link java.lang.String} representation to unmarshal to a typed value.
     * @param isNonLatinScriptAlternateVersion indicates whether the provided value is the non-latin script alternate
     * version of another attribute value.
     * @return a typed value corresponding to the given {@link java.lang.String} representation.
     */
    @Nonnull
    AttributeValue<T> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException;
}
