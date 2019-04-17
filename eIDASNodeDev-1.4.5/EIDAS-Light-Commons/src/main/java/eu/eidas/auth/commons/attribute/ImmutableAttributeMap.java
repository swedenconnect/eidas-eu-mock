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

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.collections.CollectionUtils;

import eu.eidas.auth.commons.io.MapSerializationHelper;
import eu.eidas.util.Preconditions;

/**
 * An immutable Map of {@link AttributeDefinition}s to {@link AttributeValue}s.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
public final class ImmutableAttributeMap implements Serializable {

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p>
     * The serialVersionUID of this class is not used because a Serialization Proxy is used instead.
     */
    private static final long serialVersionUID = 3407315670529047180L;

    /**
     * Builder pattern for the {@link ImmutableAttributeMap} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        @Nonnull
        private static <T> AttributeValue<T> unmarshal(@Nonnull AttributeDefinition<T> attribute,
                                                       @Nonnull String value,
                                                       boolean isNonLatinScriptAlternateVersion) {
            AttributeValue<T> attributeValue;
            try {
                attributeValue = attribute.unmarshal(value, isNonLatinScriptAlternateVersion);
            } catch (AttributeValueMarshallingException ame) {
                throw new IllegalArgumentException(ame);
            }
            return attributeValue;
        }

        @Nonnull
        private static <T> ImmutableSet<AttributeValue<T>> unmarshal(@Nonnull AttributeDefinition<T> attribute,
                                                                     @Nonnull Iterable<String> marshalledValues) {
            ImmutableSet.Builder<AttributeValue<T>> setBuilder = ImmutableSet.builder();
            if (attribute.isTransliterationMandatory()) {
                for (final String marshalledValue : marshalledValues) {
                    if (AttributeValueTransliterator.needsTransliteration(marshalledValue)) {
                        String transliterated = AttributeValueTransliterator.transliterate(marshalledValue);
                        setBuilder.add(unmarshal(attribute, transliterated, false));
                        setBuilder.add(unmarshal(attribute, marshalledValue, true));
                    } else {
                        setBuilder.add(unmarshal(attribute, marshalledValue, false));
                    }
                }
            } else {
                for (final String marshalledValue : marshalledValues) {
                    setBuilder.add(unmarshal(attribute, marshalledValue, false));
                }
            }
            return setBuilder.build();
        }

        @VisibleForTesting
        @Nonnull
        static <T> ImmutableSet<AttributeValue<T>> unmarshal(@Nonnull AttributeDefinition<T> attribute,
                                                             @Nonnull String... marshalledValues) {
            return unmarshal(attribute, ImmutableSet.copyOf(marshalledValues));
        }

        /**
         * Typesafe heterogeneous container pattern.
         * <p>
         * See item 29 of Effective Java 2nd Edition.
         */
        @Nonnull
        private final Map<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> definitionsToValues =
                new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, AttributeDefinition<?>> nameUrisToDefinitions = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, ImmutableSet<? extends AttributeValue<?>>> nameUrisToValues = new LinkedHashMap<>();

        @Nonnull
        private final Map<String, Set<AttributeDefinition<?>>> friendlyNamesToDefinitions = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy.definitionsToValues);
        }

        public Builder(@Nonnull ImmutableAttributeMap copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy);
        }

        public Builder(@Nonnull Map<AttributeDefinition<?>, ? extends Iterable<? extends AttributeValue<?>>> copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy);
        }

        private void validate() throws IllegalArgumentException {
            for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : definitionsToValues
                    .entrySet()) {
            }
        }

        /**
         * Builds the {@link ImmutableAttributeMap} instance.
         * <p>
         * This method first calls the validation logic which iterates over all entries put in the Map and detects
         * missing transliterations for attribute definitions which are defined as {@code transliterationMandatory ==
         * true}. When a missing transliteration is detected, an IllegalArgumentException is thrown
         *
         * @return the {@link ImmutableAttributeMap} instance.
         * @throws IllegalArgumentException when the state of the builder is not consistent, for example when a
         * mandatory transliteration is missing.
         */
        @Nonnull
        public ImmutableAttributeMap build() {
            if (definitionsToValues.isEmpty()) {
                return EMPTY;
            }
            return new ImmutableAttributeMap(this);
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute) {
            Preconditions.checkNotNull(attribute, "attribute");
            ImmutableSet<? extends AttributeValue<T>> valueSet = ImmutableSet.of();

            return put(attribute, valueSet);
        }

        @Nonnull
        private <T> Builder put(@Nonnull AttributeDefinition<T> attribute,
                                @Nonnull ImmutableSet<? extends AttributeValue<T>> values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");

            // Typesafe heterogeneous container pattern.
            // See item 29 of Effective Java 2nd Edition.
            // Typesafety check using class#cast():
            Class<T> parameterizedType = attribute.getParameterizedType();
            for (final AttributeValue<T> attributeValue : values) {
                Preconditions.checkNotNull(attributeValue, "attributeValue");
                T value = attributeValue.getValue();
                Preconditions.checkNotNull(value, "value");
                // ensure all values can be cast to the actual parameterized class:
                parameterizedType.cast(value);
            }

            if (definitionsToValues.containsKey(attribute)) {
                throw new IllegalArgumentException("Duplicate values for attribute \"" + attribute + "\"");
            }
            URI nameUri = attribute.getNameUri();
            AttributeDefinition<?> existing = nameUrisToDefinitions.get(nameUri);
            if (null != existing) {
                throw new IllegalArgumentException(
                        "Non-unique attribute name URIs for 2 attributes: " + attribute + " and " + existing);
            }

            //noinspection unchecked unfortunate but due to the heterogeneous container pattern
            definitionsToValues.put(attribute, (ImmutableSet<? extends AttributeValue<?>>) (Object) values);
            nameUrisToDefinitions.put(nameUri, attribute);
            //noinspection unchecked unfortunate but due to the heterogeneous container pattern
            nameUrisToValues.put(nameUri, (ImmutableSet<? extends AttributeValue<?>>) (Object) values);

            String friendlyName = attribute.getFriendlyName();
            Set<AttributeDefinition<?>> attributeDefinitions = friendlyNamesToDefinitions.get(friendlyName);
            if (null == attributeDefinitions) {
                attributeDefinitions = new LinkedHashSet<>();
                friendlyNamesToDefinitions.put(friendlyName, attributeDefinitions);
            }
            attributeDefinitions.add(attribute);

            return this;
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute,
                               @Nonnull Iterable<? extends AttributeValue<T>> values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            ImmutableSet<? extends AttributeValue<T>> valueSet = ImmutableSet.copyOf(values);

            return put(attribute, valueSet);
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull AttributeValue<T>... values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            ImmutableSet<? extends AttributeValue<T>> valueSet = ImmutableSet.copyOf(values);

            return put(attribute, valueSet);
        }

        /**
         * Puts a value marshalled as a {@code String} which is first unmarshalled and then put into the map.
         * <p>
         * <em>Important note</em>: this method automatically transliterates any given value which is not in Latin
         * Script for attribute definitions which have the {@link AttributeDefinition#isTransliterationMandatory()} flag
         * set to {@code true}.
         *
         * @param attribute the definition
         * @param primaryValue a value marshalled as a {@code String} which is to be unmarshalled first using the {@link
         * AttributeDefinition#getAttributeValueMarshaller() attribute-value marshaller}.
         * @return this Builder
         * @see #putPrimaryValues(AttributeDefinition, String...)
         * @since 1.1.1
         */
        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull String primaryValue) {
            return putPrimaryValues(attribute, primaryValue);
        }

        /**
         * put all a collection of attributeDefinitions without values
         *
         * @param attributeDefinitions the collection of attributeDefinitions
         * @return the builder itself
         */
        @Nonnull
        public Builder putAll(@Nonnull Collection<AttributeDefinition<?>> attributeDefinitions) {
            Preconditions.checkNotNull(attributeDefinitions, "attributeDefinitions");
            for (AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                put(attributeDefinition);
            }
            return this;
        }

        @Nonnull
        public Builder putAll(
                @Nonnull Map<AttributeDefinition<?>, ? extends Iterable<? extends AttributeValue<?>>> attributeMap) {
            Preconditions.checkNotNull(attributeMap, "attributeMap");
            for (final Map.Entry<AttributeDefinition<?>, ? extends Iterable<? extends AttributeValue<?>>> entry : attributeMap
                    .entrySet()) {
                //noinspection unchecked,rawtypes unfortunate but due to the heterogeneous container pattern
                put((AttributeDefinition) entry.getKey(), (ImmutableSet) ImmutableSet.copyOf(entry.getValue()));
            }
            return this;
        }

        @Nonnull
        public Builder putAll(@Nonnull ImmutableAttributeMap attributeMap) {
            Preconditions.checkNotNull(attributeMap, "attributeMap");
            putAll(attributeMap.attributeMap);
            return this;
        }

        /**
         * Puts values marshalled as {@code String}s which are first unmarshalled and then put into the map.
         * <p>
         * <em>Important note</em>: this method automatically transliterates values which are not in Latin Script for
         * attribute definitions which have the {@link AttributeDefinition#isTransliterationMandatory()} flag set to
         * {@code true}.
         *
         * @param attribute the definition
         * @param primaryValues values marshalled as {@code String}s which are to be unmarshalled first using the {@link
         * AttributeDefinition#getAttributeValueMarshaller() attribute-value marshaller}.
         * @return this Builder
         */
        @Nonnull
        public <T> Builder putPrimaryValues(@Nonnull AttributeDefinition<T> attribute,
                                            @Nonnull Iterable<String> primaryValues) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(primaryValues, "primaryValues");

            return put(attribute, unmarshal(attribute, primaryValues));
        }

        /**
         * Puts values marshalled as {@code String}s which are first unmarshalled and then put into the map.
         * <p>
         * <em>Important note</em>: this method automatically transliterates values which are not in Latin Script for
         * attribute definitions which have the {@link AttributeDefinition#isTransliterationMandatory()} flag set to
         * {@code true}.
         *
         * @param attribute the definition
         * @param primaryValues values marshalled as {@code String}s which are to be unmarshalled first using the {@link
         * AttributeDefinition#getAttributeValueMarshaller()}  attribute-value marshaller}.
         * @return this Builder
         * @since 1.1.1
         */
        @Nonnull
        public <T> Builder putPrimaryValues(@Nonnull AttributeDefinition<T> attribute,
                                            @Nonnull String... primaryValues) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(primaryValues, "primaryValues");

            return put(attribute, unmarshal(attribute, primaryValues));
        }
    }

    /**
     * A typesafe and immutable map entry (key-value pair).
     *
     * @see #entrySet()
     */
    @Immutable
    public static final class ImmutableAttributeEntry<T> {

        @Nonnull
        private final AttributeDefinition<T> attributeDefinition;

        @Nonnull
        private final ImmutableSet<? extends AttributeValue<T>> attributeValues;

        public ImmutableAttributeEntry(@Nonnull AttributeDefinition<T> attributeDefinition,
                                       @Nonnull ImmutableSet<? extends AttributeValue<T>> attributeValues) {
            this.attributeDefinition = attributeDefinition;
            this.attributeValues = attributeValues;
        }

        /**
         * Returns the key corresponding to this entry.
         *
         * @return the key corresponding to this entry
         */
        @Nonnull
        public AttributeDefinition<T> getKey() {
            return attributeDefinition;
        }

        /**
         * Returns the value corresponding to this entry.
         *
         * @return the value corresponding to this entry
         */
        @Nonnull
        public ImmutableSet<? extends AttributeValue<T>> getValues() {
            return attributeValues;
        }
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p/>
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     */
    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = -6933762015200310735L;

        @Nonnull
        private transient ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> attributeMap;

        private SerializationProxy(@Nonnull ImmutableAttributeMap attributeMap) {
            this.attributeMap = attributeMap.attributeMap;
        }

        private void readObject(@Nonnull ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            Map<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> map = new LinkedHashMap<>();
            MapSerializationHelper.readMap(in, map);

            attributeMap = ImmutableMap.copyOf(map);
        }

        private void writeObject(@Nonnull ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            MapSerializationHelper.writeMap(out, attributeMap);
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
            return new Builder().putAll(attributeMap).build();
        }
    }

    private static final ImmutableAttributeMap EMPTY = new ImmutableAttributeMap(new Builder());

    @Nonnull
    public static ImmutableAttributeMap.Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static ImmutableAttributeMap.Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static ImmutableAttributeMap.Builder builder(@Nonnull ImmutableAttributeMap copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static ImmutableAttributeMap.Builder builder(
            @Nonnull Map<AttributeDefinition<?>, ? extends Iterable<? extends AttributeValue<?>>> copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static ImmutableAttributeMap copyOf(
            @Nonnull Map<AttributeDefinition<?>, ? extends Iterable<? extends AttributeValue<?>>> map) {
        return new Builder(map).build();
    }

    @Nonnull
    public static ImmutableAttributeMap of() {
        return EMPTY;
    }

    @Nonnull
    public static <T> ImmutableAttributeMap of(@Nonnull AttributeDefinition<T> attributeDefinition,
                                               @Nonnull Iterable<? extends AttributeValue<T>> values) {
        return new Builder().put(attributeDefinition, values).build();
    }

    @Nonnull
    public static <T> ImmutableAttributeMap of(@Nonnull AttributeDefinition<T> attributeDefinition,
                                               @Nonnull AttributeValue<T>... values) {
        return new Builder().put(attributeDefinition, values).build();
    }

    @Nonnull
    private static URI toUri(@Nonnull String name) {
        URI nameUri;
        try {
            nameUri = new URI(name);
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException("Invalid name URI \"" + name + "\": " + use, use);
        }
        return nameUri;
    }

    @Nullable
    public static ImmutableValueMap toValueMap(
            @Nullable Map<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> map) {
        if (null == map) {
            return null;
        }
        ImmutableValueMap.Builder builder = ImmutableValueMap.builder();
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : map.entrySet()) {
            builder.put(entry.getKey(), toValues((ImmutableSet) entry.getValue()));
        }
        return builder.build();
    }

    @Nullable
    public static <T> ImmutableSet<? extends T> toValues(
            @Nullable Iterable<? extends AttributeValue<T>> attributeValues) {
        if (null == attributeValues) {
            return null;
        }
        ImmutableSet.Builder<T> builder = ImmutableSet.builder();
        for (final AttributeValue<T> attributeValue : attributeValues) {
            builder.add(attributeValue.getValue());
        }
        return builder.build();
    }

    @Nonnull
    private final transient ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>>
            attributeMap;

    @Nonnull
    private final transient ImmutableMap<URI, AttributeDefinition<?>> nameUrisToDefinitions;

    @Nonnull
    private final transient ImmutableMap<URI, ImmutableSet<? extends AttributeValue<?>>> nameUrisToValues;

    @Nonnull
    private final transient ImmutableMap<String, ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>>>
            friendlyNameMap;

    @Nonnull
    private final transient ImmutableValueMap valueMap;

    private ImmutableAttributeMap(@Nonnull Builder builder) {
        attributeMap = ImmutableMap.copyOf(builder.definitionsToValues);
        nameUrisToDefinitions = ImmutableMap.copyOf(builder.nameUrisToDefinitions);
        nameUrisToValues = ImmutableMap.copyOf(builder.nameUrisToValues);
        ImmutableMap.Builder<String, ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>>>
                friendlyNameMapBuilder = new ImmutableMap.Builder<>();
        for (final Map.Entry<String, Set<AttributeDefinition<?>>> entry : builder.friendlyNamesToDefinitions.entrySet()) {
            String friendlyName = entry.getKey();
            Set<AttributeDefinition<?>> attributeDefinitions = entry.getValue();
            ImmutableMap.Builder<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> subMapBuilder =
                    new ImmutableMap.Builder<>();
            for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                subMapBuilder.put(attributeDefinition, attributeMap.get(attributeDefinition));
            }
            friendlyNameMapBuilder.put(friendlyName, subMapBuilder.build());
        }
        friendlyNameMap = friendlyNameMapBuilder.build();
        valueMap = toValueMap(attributeMap);
    }

    @Nonnull
    public ImmutableSet<ImmutableAttributeEntry<?>> entrySet() {
        ImmutableSet.Builder<ImmutableAttributeEntry<?>> builder = ImmutableSet.builder();
        for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : attributeMap.entrySet()) {
            //noinspection unchecked
            builder.add(new ImmutableAttributeEntry(entry.getKey(), entry.getValue()));
        }
        return builder.build();
    }

    /**
     * Returns the content of this object as an {@link ImmutableMap} where keys are {@link AttributeDefinition}s and
     * values are {@link ImmutableSet}s of {@link AttributeValue}s.
     *
     * @return the content of this object as an {@link ImmutableMap}.
     */
    @Nonnull
    public ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> getAttributeMap() {
        return attributeMap;
    }

    /**
     * Returns the {@link AttributeValue}s for the given {@link AttributeDefinition} as an {@link ImmutableSet} or
     * returns {@code null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @return The {@link AttributeValue}s (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> ImmutableSet<? extends AttributeValue<T>> getAttributeValues(
            @Nonnull AttributeDefinition<T> attributeDefinition) {
        //noinspection unchecked unfortunate but due to the heterogeneous container pattern
        return (ImmutableSet<? extends AttributeValue<T>>) attributeMap.get(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition}s as Map keys and {@link ImmutableSet} of {@link AttributeValue}s
     * as Map values.
     * <p>
     * If you want to have a sub-map of values instead of {@link AttributeValue}s, use {@link
     * #getValuesByFriendlyName(String)} instead.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link ImmutableSet}s of {@link AttributeValue}s or returns {@code null} if no
     * attribute in this map has that friendly name.
     */
    @Nullable
    public ImmutableAttributeMap getAttributeValuesByFriendlyName(@Nonnull String friendlyName) {
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> subMap =
                friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return builder().putAll(subMap).build();
    }

    @Nullable
    public <T> ImmutableSet<? extends AttributeValue<T>> getAttributeValuesByNameUri(@Nonnull String name) {
        URI nameUri = toUri(name);
        return getAttributeValuesByNameUri(nameUri);
    }

    @Nullable
    public <T> ImmutableSet<? extends AttributeValue<T>> getAttributeValuesByNameUri(@Nonnull URI name) {
        return (ImmutableSet<? extends AttributeValue<T>>) nameUrisToValues.get(name);
    }

    /**
     * Returns all the {@link AttributeDefinition}s in the map matching the given attribute name URI (i.e. full
     * attribute name) or returns {@code null} if no attribute in this map matches the given name URI.
     *
     * @param name the attribute name URI to look up.
     * @return all the {@link AttributeDefinition}s in the map matching the given attribute name URI or returns {@code
     * null} when there is no match.
     */
    @Nullable
    public <T> AttributeDefinition<T> getDefinitionByNameUri(@Nonnull String name) {
        URI nameUri = toUri(name);
        return getDefinitionByNameUri(nameUri);
    }

    /**
     * Returns all the {@link AttributeDefinition}s in the map matching the given attribute name URI (i.e. full
     * attribute name) or returns {@code null} if no attribute in this map matches the given name URI.
     *
     * @param name the attribute name URI to look up.
     * @return all the {@link AttributeDefinition}s in the map matching the given attribute name URI or returns {@code
     * null} when there is no match.
     */
    @Nullable
    public <T> AttributeDefinition<T> getDefinitionByNameUri(@Nonnull URI name) {
        return (AttributeDefinition<T>) nameUrisToDefinitions.get(name);
    }

    /**
     * Returns all the {@link AttributeDefinition}s in the map, can be empty but never {@code null}.
     *
     * @return all the {@link AttributeDefinition}s in the map, can be empty but never {@code null}.
     */
    @Nonnull
    public ImmutableSet<AttributeDefinition<?>> getDefinitions() {
        return attributeMap.keySet();
    }

    /**
     * Returns all the {@link AttributeDefinition}s in the map matching the given friendly name or returns {@code null}
     * if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param friendlyName the attribute friendly name to look up.
     * @return all the {@link AttributeDefinition}s in the map matching the given friendly name or returns {@code null}
     * when there is no match.
     */
    @Nullable
    public ImmutableSet<AttributeDefinition<?>> getDefinitionsByFriendlyName(@Nonnull String friendlyName) {
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> subMap =
                friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return subMap.keySet();
    }

    /**
     * Returns the first {@link AttributeValue} for the given {@link AttributeDefinition} or returns {@code null} if no
     * attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @return The first {@link AttributeValue} (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> AttributeValue<T> getFirstAttributeValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        ImmutableSet<? extends AttributeValue<T>> values = getAttributeValues(attributeDefinition);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.iterator().next();
    }

    /**
     * Returns the first typed value for the given {@link AttributeDefinition} or returns {@code null} if no attribute
     * in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @return The first value (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> T getFirstValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        return valueMap.getFirstValue(attributeDefinition);
    }

    /**
     * Returns the content of this object as an {@link ImmutableValueMap} where keys are {@link AttributeDefinition}s
     * and values are {@link ImmutableSet}s of typed values.
     *
     * @return the content of this object as an {@link ImmutableValueMap}.
     */
    @Nonnull
    public ImmutableValueMap getValueMap() {
        return valueMap;
    }

    /**
     * Returns the typed values for the given {@link AttributeDefinition} as an {@link ImmutableSet} or returns {@code
     * null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @return The values (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> ImmutableSet<? extends T> getValues(@Nonnull AttributeDefinition<T> attributeDefinition) {
        return valueMap.getValues(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition} as Map keys and {@link ImmutableSet} of typed values matching
     * the types of the definition as Map values.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link ImmutableSet}s of typed values  or returns {@code null} if no attribute in
     * this map has that friendly name.
     */
    @Nullable
    public ImmutableValueMap getValuesByFriendlyName(@Nonnull String friendlyName) {
        return valueMap.getValuesByFriendlyName(friendlyName);
    }

    @Nullable
    public <T> ImmutableSet<? extends T> getValuesByNameUri(@Nonnull String name) {
        return valueMap.getValuesByNameUri(name);
    }

    @Nullable
    public <T> ImmutableSet<? extends T> getValuesByNameUri(@Nonnull URI name) {
        return valueMap.getValuesByNameUri(name);
    }

    public boolean isEmpty() {
        return attributeMap.isEmpty();
    }

    public int size() {
        return attributeMap.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImmutableAttributeMap that = (ImmutableAttributeMap) o;

        return null != attributeMap ? attributeMap.equals(that.attributeMap) : that.attributeMap == null;
    }

    @Override
    public int hashCode() {
        return null != attributeMap ? attributeMap.hashCode() : 0;
    }

    @Override
    public String toString() {
        return null != attributeMap ? attributeMap.toString() : "";
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
        return new SerializationProxy(this);
    }
}
