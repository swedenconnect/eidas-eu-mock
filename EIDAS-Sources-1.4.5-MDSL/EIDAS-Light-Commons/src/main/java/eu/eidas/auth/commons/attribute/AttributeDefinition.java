/*
 * Copyright (c) 2016 by European Commission
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 *
 */
package eu.eidas.auth.commons.attribute;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.QName;

import eu.eidas.util.Preconditions;

/**
 * The definition of a personal attribute.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
@SuppressWarnings("ConstantConditions")
public final class AttributeDefinition<T> implements Comparable<AttributeDefinition<?>>, Serializable {

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p>
     * The serialVersionUID of this class is not used because a Serialization Proxy is used instead.
     */
    private static final long serialVersionUID = 5132258784285815491L;

    /**
     * Builder pattern for the {@link AttributeDefinition} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder<T> {

        private String friendlyName;

        private URI nameUri;

        private PersonType personType;

        private boolean required;

        private boolean transliterationMandatory;

        private boolean uniqueIdentifier;

        private QName xmlType;

        private AttributeValueMarshaller<T> attributeValueMarshaller;

        public Builder() {
        }

        public Builder(@Nonnull Builder<T> copy) {
            Preconditions.checkNotNull(copy, "copy");
            friendlyName = copy.friendlyName;
            nameUri = copy.nameUri;
            personType = copy.personType;
            required = copy.required;
            transliterationMandatory = copy.transliterationMandatory;
            uniqueIdentifier = copy.uniqueIdentifier;
            xmlType = copy.xmlType;
            attributeValueMarshaller = copy.attributeValueMarshaller;
        }

        public Builder(@Nonnull AttributeDefinition<T> copy) {
            Preconditions.checkNotNull(copy, "copy");
            friendlyName = copy.friendlyName;
            nameUri = copy.nameUri;
            personType = copy.personType;
            required = copy.required;
            transliterationMandatory = copy.transliterationMandatory;
            uniqueIdentifier = copy.uniqueIdentifier;
            xmlType = copy.xmlType;
            attributeValueMarshaller = copy.attributeValueMarshaller;
        }

        @Nonnull
        public Builder<T> attributeValueMarshaller(final AttributeValueMarshaller<T> attributeValueMarshaller) {
            this.attributeValueMarshaller = attributeValueMarshaller;
            return this;
        }

        @Nonnull
        public Builder<T> attributeValueMarshaller(final String attributeValueMarshallerClassName) {
            try {
                //noinspection unchecked
                this.attributeValueMarshaller =
                        (AttributeValueMarshaller<T>) AttributeValueMarshallerFactory.newAttributeValueMarshallerInstance(
                                attributeValueMarshallerClassName);
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Unable to configure the attributeValueMarshaller: \"" + attributeValueMarshallerClassName
                                + "\": due to " + ex, ex);
            }
            return this;
        }

        @Nonnull
        public AttributeDefinition<T> build() {
            validate();
            return new AttributeDefinition<T>(this);
        }

        @Nonnull
        public Builder<T> friendlyName(final String friendlyName) {
            this.friendlyName = friendlyName;
            return this;
        }

        @Nonnull
        public Builder<T> nameUri(final URI nameUri) {
            this.nameUri = nameUri;
            return this;
        }

        @Nonnull
        public Builder<T> nameUri(final String nameUri) {
            try {
                this.nameUri = new URI(nameUri);
            } catch (URISyntaxException use) {
                throw new IllegalArgumentException("Invalid name URI \"" + nameUri + "\": " + use, use);
            }
            return this;
        }

        @Nonnull
        public Builder<T> personType(final PersonType personType) {
            this.personType = personType;
            return this;
        }

        @Nonnull
        public Builder<T> required(final boolean required) {
            this.required = required;
            return this;
        }

        @Nonnull
        public Builder<T> transliterationMandatory(final boolean transliterationMandatory) {
            this.transliterationMandatory = transliterationMandatory;
            return this;
        }

        @Nonnull
        public Builder<T> uniqueIdentifier(final boolean uniqueIdentifier) {
            this.uniqueIdentifier = uniqueIdentifier;
            return this;
        }

        private void validate() throws IllegalArgumentException {
            Preconditions.checkNotNull(nameUri, "nameUri");
            Preconditions.checkNotBlank(friendlyName, "friendlyName");
            Preconditions.checkNotNull(personType, "personType");
            Preconditions.checkNotNull(xmlType, "xmlType");
            Preconditions.checkNotNull(attributeValueMarshaller, "attributeValueMarshaller");
            String xmlNamespaceUriStr = xmlType.getNamespaceURI();
            Preconditions.checkNotBlank(xmlNamespaceUriStr, "xmlType.getNamespaceURI()");
            Preconditions.checkNotBlank(xmlType.getLocalPart(), "xmlType.getLocalPart()");
            Preconditions.checkNotBlank(xmlType.getPrefix(), "xmlType.getPrefix()");
            if (!nameUri.isAbsolute()) {
                throw new IllegalArgumentException("name must be an absolute URI, but was: \"" + nameUri + "\"");
            }
            URI xmlNamespaceUri;
            try {
                xmlNamespaceUri = new URI(xmlNamespaceUriStr);
            } catch (URISyntaxException use) {
                throw new IllegalArgumentException(
                        "Invalid XML personType NamespaceURI \"" + xmlNamespaceUriStr + "\": " + use, use);
            }
            if (!xmlNamespaceUri.isAbsolute()) {
                throw new IllegalArgumentException(
                        "XML personType NamespaceURI must be an absolute URI, but was: \"" + xmlNamespaceUriStr + "\"");
            }
        }

        @Nonnull
        public Builder<T> xmlType(final QName xmlType) {
            this.xmlType = xmlType;
            return this;
        }

        @Nonnull
        public Builder<T> xmlType(final String namespaceUri, final String localPart, final String prefix) {
            this.xmlType = new QName(namespaceUri, localPart, prefix);
            return this;
        }
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p>
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     */
    private static final class SerializationProxy<T> implements Serializable {

        private static final long serialVersionUID = -5864667076864193650L;

        /**
         * @serial
         */
        @Nonnull
        private final URI nameUri;

        /**
         * @serial
         */
        @Nonnull
        private final String friendlyName;

        /**
         * @serial
         */
        @Nonnull
        private final PersonType personType;

        /**
         * @serial
         */
        private final boolean required;

        /**
         * @serial
         */
        private final boolean transliterationMandatory;

        /**
         * @serial
         */
        private final QName xmlType;

        /**
         * @serial
         */
        private final boolean uniqueIdentifier;

        /**
         * @serial
         */
        private final String attributeValueMarshallerClassName;

        private SerializationProxy(@Nonnull AttributeDefinition<T> attributeDefinition) {
            nameUri = attributeDefinition.getNameUri();
            friendlyName = attributeDefinition.getFriendlyName();
            personType = attributeDefinition.getPersonType();
            required = attributeDefinition.isRequired();
            transliterationMandatory = attributeDefinition.isTransliterationMandatory();
            xmlType = attributeDefinition.getXmlType();
            uniqueIdentifier = attributeDefinition.isUniqueIdentifier();
            attributeValueMarshallerClassName = attributeDefinition.getAttributeValueMarshaller().getClass().getName();
        }

        // The declarations below are necessary for serialization

        /**
         * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
         * <p/>
         * Used upon de-serialization, not serialization.
         * <p/>
         * The state of this class is transformed back into the class it represents.
         */
        private Object readResolve() throws ObjectStreamException {
            try {
                @SuppressWarnings("unchecked") AttributeValueMarshaller<T> marshaller =
                        (AttributeValueMarshaller<T>) AttributeValueMarshallerFactory.newAttributeValueMarshallerInstance(
                                attributeValueMarshallerClassName);
                return new Builder<T>().nameUri(nameUri)
                        .friendlyName(friendlyName)
                        .personType(personType)
                        .required(required)
                        .transliterationMandatory(transliterationMandatory)
                        .uniqueIdentifier(uniqueIdentifier)
                        .xmlType(xmlType)
                        .attributeValueMarshaller(marshaller)
                        .build();
            } catch (Exception ex) {
                InvalidObjectException invalidObjectException = new InvalidObjectException(ex.getMessage());
                invalidObjectException.initCause(ex);
                throw invalidObjectException;
            }
        }
    }

    @Nonnull
    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    @Nonnull
    public static <T> Builder<T> builder(@Nonnull Builder<T> copy) {
        return new Builder<T>(copy);
    }

    @Nonnull
    public static <T> Builder<T> builder(@Nonnull AttributeDefinition<T> copy) {
        return new Builder<T>(copy);
    }

    @Nonnull
    private final transient String friendlyName;

    @Nonnull
    private final transient URI nameUri;

    @Nonnull
    private final transient PersonType personType;

    private final transient boolean required;

    private final transient boolean transliterationMandatory;

    private final transient boolean uniqueIdentifier;

    @Nonnull
    private final transient QName xmlType;

    @Nonnull
    private final transient AttributeValueMarshaller<T> attributeValueMarshaller;

    @Nonnull
    private final transient Class<T> parameterizedType;

    // Effective Java: cache the result of hashCode for immutable objects

    /**
     * Cache the hash code for the immutable state.
     */
    private transient volatile int cachedHashCode; // Default to 0

    /**
     * Cache the toString() computation for the immutable state.
     */
    private transient volatile String cachedToString;

    /**
     * Private constructor for builder pattern.
     *
     * @param builder the builder
     */
    private AttributeDefinition(@Nonnull Builder<T> builder) {
        friendlyName = builder.friendlyName;
        nameUri = builder.nameUri;
        personType = builder.personType;
        required = builder.required;
        transliterationMandatory = builder.transliterationMandatory;
        uniqueIdentifier = builder.uniqueIdentifier;
        xmlType = builder.xmlType;
        attributeValueMarshaller = builder.attributeValueMarshaller;
        parameterizedType = AttributeValueMarshallerFactory.getParameterizedType(attributeValueMarshaller);
    }

    @Nonnull
    public AttributeValueMarshaller<T> getAttributeValueMarshaller() {
        return attributeValueMarshaller;
    }

    /**
     * Returns the friendly name of the personal attribute.
     *
     * @return the Friendly name of the personal attribute.
     */
    @Nonnull
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Returns the complete name of the personal attribute.
     *
     * @return the complete name of the personal attribute.
     */
    @Nonnull
    public URI getNameUri() {
        return nameUri;
    }

    /**
     * Returns the class of the parameterized type {@code T} of this AttributeDefinition.
     *
     * @return the class of the parameterized type {@code T} of this AttributeDefinition.
     */
    @Nonnull
    public Class<T> getParameterizedType() {
        return parameterizedType;
    }

    /**
     * Returns the personType of person this attribute is used with.
     *
     * @return the personType of person this attribute is used with.
     */
    @Nonnull
    public PersonType getPersonType() {
        return personType;
    }

    /**
     * Returns the XML qualified name to designate this attribute.
     * <p/>
     * The qualified name must have a non-null namespace URI, a non-null local part and a non-null namespace prefix.
     *
     * @return the XML qualified name to designate this attribute.
     */
    @Nonnull
    public QName getXmlType() {
        return xmlType;
    }

    /**
     * Returns {@code true} if the personal attribute is required, {@code false} otherwise.
     *
     * @return {@code true} if the personal attribute is required, {@code false} otherwise.
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Returns {@code true} if the personal attribute must be transliterated, {@code false} otherwise.
     *
     * @return {@code true} if the personal attribute must be transliterated, {@code false} otherwise.
     */
    public boolean isTransliterationMandatory() {
        return transliterationMandatory;
    }

    /**
     * Returns {@code true} if the personal attribute is a unique identifier (uid), {@code false} otherwise.
     *
     * @return {@code true} if the personal attribute is a unique identifier (uid), {@code false} otherwise.
     */
    public boolean isUniqueIdentifier() {
        return uniqueIdentifier;
    }

    @Nonnull
    public String marshal(@Nonnull AttributeValue<T> value) throws AttributeValueMarshallingException {
        return attributeValueMarshaller.marshal(value);
    }

    @Nonnull
    public AttributeValue<T> unmarshal(@Nonnull String value, boolean isNonLatinScriptAlternateVersion)
            throws AttributeValueMarshallingException {
        return attributeValueMarshaller.unmarshal(value, isNonLatinScriptAlternateVersion);
    }

    @Override
    public int compareTo(@Nonnull AttributeDefinition<?> o) {
        int c = nameUri.compareTo(o.nameUri);
        if (c != 0) {
            return c;
        }
        c = friendlyName.compareTo(o.friendlyName);
        if (c != 0) {
            return c;
        }
        c = personType.getValue().compareTo(o.personType.getValue());
        if (c != 0) {
            return c;
        }
        c = xmlType.getNamespaceURI().compareTo(o.xmlType.getNamespaceURI());
        if (c != 0) {
            return c;
        }
        c = xmlType.getLocalPart().compareTo(o.xmlType.getLocalPart());
        if (c != 0) {
            return c;
        }
        c = Boolean.compare(required, o.required);
        if (c != 0) {
            return c;
        }
        c = Boolean.compare(transliterationMandatory, o.transliterationMandatory);
        if (c != 0) {
            return c;
        }
        return Boolean.compare(uniqueIdentifier, o.uniqueIdentifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AttributeDefinition<?> that = (AttributeDefinition<?>) o;

        if (required != that.required) {
            return false;
        }
        if (transliterationMandatory != that.transliterationMandatory) {
            return false;
        }
        if (uniqueIdentifier != that.uniqueIdentifier) {
            return false;
        }
        if (personType != that.personType) {
            return false;
        }
        if (!friendlyName.equals(that.friendlyName)) {
            return false;
        }
        if (!nameUri.equals(that.nameUri)) {
            return false;
        }
        return xmlType.equals(that.xmlType);
    }

    @Override
    public int hashCode() {
        // Effective Java, 2nd Ed. : Item 9 : Lazily initialized, cached hashCode for immutable class
        int h = cachedHashCode;
        if (h == 0) {
            h = nameUri != null ? nameUri.hashCode() : 0;
            h = 31 * h + (friendlyName != null ? friendlyName.hashCode() : 0);
            h = 31 * h + (personType != null ? personType.hashCode() : 0);
            h = 31 * h + (required ? 1 : 0);
            h = 31 * h + (transliterationMandatory ? 1 : 0);
            h = 31 * h + (uniqueIdentifier ? 1 : 0);
            h = 31 * h + (xmlType.hashCode());
            cachedHashCode = h;
        }
        return h;
    }

    @Override
    public String toString() {
        String toString = cachedToString;
        if (null == toString) {
            toString = "AttributeDefinition{" +
                    "nameUri='" + (null != nameUri ? nameUri.toASCIIString() : "") + '\'' +
                    ", friendlyName='" + friendlyName + '\'' +
                    ", personType=" + personType +
                    ", required=" + required +
                    ", transliterationMandatory=" + transliterationMandatory +
                    ", uniqueIdentifier=" + uniqueIdentifier +
                    ", xmlType='" + xmlType + '\'' +
                    ", attributeValueMarshaller='" + attributeValueMarshaller.getClass().getName() + '\'' +
                    '}';
            cachedToString = toString;
        }
        return toString;
    }

    // The declarations below are necessary for serialization

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     */
    private void readObject(@Nonnull ObjectInputStream objectInputStream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization Proxy required");
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     */
    private void writeObject(@Nonnull ObjectOutputStream out) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization Proxy required");
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     */
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy<T>(this);
    }
}
