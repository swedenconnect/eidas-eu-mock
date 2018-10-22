package eu.eidas.auth.commons.attribute;

import javax.annotation.Nonnull;

/**
 * Exception thrown when marshalling or unmarshalling an attribute value.
 *
 * @since 1.1
 */
public class AttributeValueMarshallingException extends Exception {

    private static final long serialVersionUID = 8517915248912704811L;

    public AttributeValueMarshallingException() {
    }

    public AttributeValueMarshallingException(@Nonnull String message) {
        super(message);
    }

    public AttributeValueMarshallingException(@Nonnull String message, @Nonnull Throwable cause) {
        super(message, cause);
    }

    public AttributeValueMarshallingException(@Nonnull Throwable cause) {
        super(cause);
    }
}
