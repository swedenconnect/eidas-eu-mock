package eu.eidas.auth.commons.attribute.impl;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.util.Preconditions;

import java.io.Serializable;

/**
 * Abstract AttributeValue
 *
 * @since 1.1
 */
public abstract class AbstractAttributeValue<T extends Serializable> implements AttributeValue<T> {

    private static final long serialVersionUID = 7154869930698510327L;

    /**
     * @serial
     */
    @Nonnull
    private final T value;

    /**
     * @serial
     */
    private final boolean nonLatinScriptAlternateVersion;

    protected AbstractAttributeValue(@Nonnull T val, boolean nonLatinScriptAlternateVer) {
        Preconditions.checkNotNull(val, "value");
        value = val;
        nonLatinScriptAlternateVersion = nonLatinScriptAlternateVer;
    }

    @Nonnull
    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isNonLatinScriptAlternateVersion() {
        return nonLatinScriptAlternateVersion;
    }

    @Override
    @SuppressWarnings("squid:S2097")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || (!AttributeValue.class.isAssignableFrom(o.getClass()))) {
            return false;
        }

        AttributeValue<?> that = (AttributeValue<?>) o;

        if (nonLatinScriptAlternateVersion != that.isNonLatinScriptAlternateVersion()) {
            return false;
        }
        return value.equals(that.getValue());
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (nonLatinScriptAlternateVersion ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
