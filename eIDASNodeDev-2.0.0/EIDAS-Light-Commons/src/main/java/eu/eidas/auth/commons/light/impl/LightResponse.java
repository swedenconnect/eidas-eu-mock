package eu.eidas.auth.commons.light.impl;

import java.io.ObjectStreamException;
import java.io.Serializable;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import eu.eidas.auth.commons.light.ILightResponse;

/**
 * Concrete implementation of the {@link ILightResponse} interface.
 * <p>
 * This class uses the Builder Pattern and is immutable thus thread-safe.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
@XmlRootElement
@XmlType(factoryMethod="newInstance")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(AbstractLightResponse.class)
public final class LightResponse extends AbstractLightResponse implements Serializable {

	@SuppressWarnings("unused")
	private static LightResponse newInstance(){
		return new LightResponse(builder());
	}
    /**
     * Builder pattern for the {@link LightResponse} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder extends AbstractLightResponse.AbstractBuilder<Builder, LightResponse> {

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            super(copy);
        }

        public Builder(@Nonnull ILightResponse copy) {
            super(copy);
        }

        @Override
        protected void validate() throws IllegalArgumentException {
        }

        @Nonnull
        @Override
        protected LightResponse newInstance() {
            return new LightResponse(this);
        }
    }

    private static final long serialVersionUID = 3778981569989729382L;

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull ILightResponse copy) {
        return new Builder(copy);
    }

    private LightResponse(@Nonnull Builder builder) {
        super(builder);
    }

    /**
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     * <p/>
     * Used upon de-serialization, not serialization.
     * <p/>
     * The state of this class is transformed back into the class it represents.
     */
    private Object readResolve() throws ObjectStreamException {
        return new Builder(this).build();
    }
}
