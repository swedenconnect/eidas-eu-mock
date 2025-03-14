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

import eu.eidas.auth.commons.io.MapSerializationHelper;
import eu.eidas.util.Preconditions;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An immutable Map of {@link AttributeDefinition}s to {@link AttributeValue}s.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
@XmlType(factoryMethod="newInstance")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso(value= {AttributeDefinition.class})
public final class ImmutableAttributeMap implements Serializable {

    private static ImmutableAttributeMap newInstance(){
    	return new ImmutableAttributeMap(new Builder());
    } 
    
	/**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p>
     * The serialVersionUID of this class is not used because a Serialization Proxy is used instead.
     */
    private static final long serialVersionUID = 3407315670529047180L;

    /**
     * <p>
     * Builder pattern for the {@link ImmutableAttributeMap} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
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
        private static <T> Set<AttributeValue<T>> unmarshal(@Nonnull AttributeDefinition<T> attribute,
                                                            @Nonnull Iterable<String> marshalledValues) {
            Set<AttributeValue<T>> attributeValues = new LinkedHashSet<>();
            if (attribute.isTransliterationMandatory()) {
                for (final String marshalledValue : marshalledValues) {
                    if (AttributeValueTransliterator.needsTransliteration(marshalledValue)) {
                        String transliterated = AttributeValueTransliterator.transliterate(marshalledValue);
                        attributeValues.add(unmarshal(attribute, transliterated, false));
                        attributeValues.add(unmarshal(attribute, marshalledValue, true));
                    } else {
                        attributeValues.add(unmarshal(attribute, marshalledValue, false));
                    }
                }
            } else {
                for (final String marshalledValue : marshalledValues) {
                    attributeValues.add(unmarshal(attribute, marshalledValue, false));
                }
            }
            return Collections.unmodifiableSet(attributeValues);
        }

        @Nonnull
        static <T> Set<AttributeValue<T>> unmarshal(@Nonnull AttributeDefinition<T> attribute,
                                                             @Nonnull String... marshalledValues) {
            return unmarshal(attribute, Set.of(marshalledValues));
        }

        /**
         * Typesafe heterogeneous container pattern.
         * <p>
         * See item 29 of Effective Java 2nd Edition.
         */
        @Nonnull
        private final Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> definitionsToValues =
                new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, AttributeDefinition<?>> nameUrisToDefinitions = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, Set<? extends AttributeValue<?>>> nameUrisToValues = new LinkedHashMap<>();

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
            for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : definitionsToValues
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
            Set<? extends AttributeValue<T>> valueSet = Set.of();

            return put(attribute, valueSet);
        }

        @Nonnull
        private <T> Builder put(@Nonnull AttributeDefinition<T> attribute,
                                @Nonnull Set<? extends AttributeValue<T>> values) {
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
            definitionsToValues.put(attribute, (Set<? extends AttributeValue<?>>) (Object) values);
            nameUrisToDefinitions.put(nameUri, attribute);
            //noinspection unchecked unfortunate but due to the heterogeneous container pattern
            nameUrisToValues.put(nameUri, (Set<? extends AttributeValue<?>>) (Object) values);

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
            Set<? extends AttributeValue<T>> valueSet = StreamSupport.stream(values.spliterator(), false)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            return put(attribute, valueSet);
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull AttributeValue<T>... values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            Set<AttributeValue<T>> valueSet = Arrays.stream(values)
                    .collect(Collectors.toSet());

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
         * @param <T> the type of this Builder.
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
                putWithIterable(entry.getKey(), entry.getValue());
            }
            return this;
        }

        private <T> void putWithIterable(@Nonnull AttributeDefinition<T> attribute,
                                         @Nonnull Iterable<? extends AttributeValue<?>> values) {
            Set<AttributeValue<?>> set = new LinkedHashSet<>();
            for (AttributeValue<?> value : values) {
                set.add(value);
            }
            put(attribute, (Set<AttributeValue<T>>) (Set<?>) set);
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
         * @param <T> the type of this Builder.
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
         * @param <T> the type of this Builder.
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
        private final Set<? extends AttributeValue<T>> attributeValues;

        public ImmutableAttributeEntry(@Nonnull AttributeDefinition<T> attributeDefinition,
                                       @Nonnull Set<? extends AttributeValue<T>> attributeValues) {
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
        public Set<? extends AttributeValue<T>> getValues() {
            return attributeValues;
        }
    }

    /**
     * <p>
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     *
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     */
    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = -6933762015200310735L;

        @Nonnull
        private transient Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> attributeMap;

        private SerializationProxy(@Nonnull ImmutableAttributeMap attributeMap) {
            this.attributeMap = attributeMap.attributeMap;
        }

        private void readObject(@Nonnull ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> map = new LinkedHashMap<>();
            MapSerializationHelper.readMap(in, map);

            attributeMap = Map.copyOf(map);
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
            @Nullable Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> map) {
        if (null == map) {
            return null;
        }
        ImmutableValueMap.Builder builder = ImmutableValueMap.builder();
        for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : map.entrySet()) {
            builder.put(entry.getKey(), toValues((Set) entry.getValue()));
        }
        return builder.build();
    }

    @Nonnull
    public static <T> Set<? extends T> toValues(
            @Nullable Iterable<? extends AttributeValue<T>> attributeValues) {
        if (null == attributeValues) {
            return Collections.emptySet();
        }
        Set<T> values = new LinkedHashSet<>();
        for (final AttributeValue<T> attributeValue : attributeValues) {
            values.add(attributeValue.getValue());
        }
        return Collections.unmodifiableSet(values);
    }

    @Nonnull
    private final transient Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> attributeMap;

    @Nonnull
    private final transient Map<URI, AttributeDefinition<?>> nameUrisToDefinitions;

    @Nonnull
    private final transient Map<URI, Set<? extends AttributeValue<?>>> nameUrisToValues;

    @Nonnull
    private final transient Map<String, Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>>>
            friendlyNameMap;

    @Nonnull
    private final transient ImmutableValueMap valueMap;

    private ImmutableAttributeMap(@Nonnull Builder builder) {
        attributeMap = new LinkedHashMap<>(builder.definitionsToValues);
        nameUrisToDefinitions = new LinkedHashMap<>(builder.nameUrisToDefinitions);
        nameUrisToValues = new LinkedHashMap<>(builder.nameUrisToValues);
        Map<String, Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>>> friendlyNameMapBuilder = new LinkedHashMap<>();
        for (final Map.Entry<String, Set<AttributeDefinition<?>>> entry : builder.friendlyNamesToDefinitions.entrySet()) {
            String friendlyName = entry.getKey();
            Set<AttributeDefinition<?>> attributeDefinitions = entry.getValue();
            Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> definitionSetHashMap = new LinkedHashMap<>();
            for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                definitionSetHashMap.put(attributeDefinition, attributeMap.get(attributeDefinition));
            }
            friendlyNameMapBuilder.put(friendlyName, Collections.unmodifiableMap(definitionSetHashMap));
        }
        friendlyNameMap = Collections.unmodifiableMap(friendlyNameMapBuilder);
        valueMap = toValueMap(attributeMap);
    }

    @Nonnull
    public Set<ImmutableAttributeEntry<?>> entrySet() {
        Set<ImmutableAttributeEntry<?>> immutableAttributeEntries = new LinkedHashSet<>();
        for (final Map.Entry<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> entry : attributeMap.entrySet()) {
            //noinspection unchecked
            immutableAttributeEntries.add(new ImmutableAttributeEntry(entry.getKey(), entry.getValue()));
        }
        return Collections.unmodifiableSet(immutableAttributeEntries);
    }

    /**
     * Returns the content of this object as an {@link Map} where keys are {@link AttributeDefinition}s and
     * values are {@link Set}s of {@link AttributeValue}s.
     *
     * @return the content of this object as an {@link Map}.
     */
    @Nonnull
    public Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> getAttributeMap() {
        return attributeMap;
    }

    /**
     * Returns the {@link AttributeValue}s for the given {@link AttributeDefinition} as an {@link Set} or
     * returns {@code null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @param <T> the type of the {@link AttributeValue}.
     * @return The {@link AttributeValue}s (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> Set<? extends AttributeValue<T>> getAttributeValues(@Nonnull AttributeDefinition<T> attributeDefinition) {
        //noinspection unchecked unfortunate but due to the heterogeneous container pattern
        return (Set<? extends AttributeValue<T>>) attributeMap.get(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition}s as Map keys and {@link Set} of {@link AttributeValue}s
     * as Map values.
     * <p>
     * If you want to have a sub-map of values instead of {@link AttributeValue}s, use {@link
     * #getValuesByFriendlyName(String)} instead.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link Set}s of {@link AttributeValue}s or returns {@code null} if no
     * attribute in this map has that friendly name.
     */
    @Nullable
    public ImmutableAttributeMap getAttributeValuesByFriendlyName(@Nonnull String friendlyName) {
        Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> subMap =
                friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return builder().putAll(subMap).build();
    }

    @Nullable
    public <T> Set<? extends AttributeValue<T>> getAttributeValuesByNameUri(@Nonnull String name) {
        URI nameUri = toUri(name);
        return getAttributeValuesByNameUri(nameUri);
    }

    @Nullable
    public <T> Set<? extends AttributeValue<T>> getAttributeValuesByNameUri(@Nonnull URI name) {
        return (Set<? extends AttributeValue<T>>) nameUrisToValues.get(name);
    }

    /**
     * Returns all the {@link AttributeDefinition}s in the map matching the given attribute name URI (i.e. full
     * attribute name) or returns {@code null} if no attribute in this map matches the given name URI.
     *
     * @param name the attribute name URI to look up.
     * @param <T> the type of the {@link AttributeDefinition}.
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
     * @param <T> the type of the {@link AttributeDefinition}.
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
    public Set<AttributeDefinition<?>> getDefinitions() {
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
    public Set<AttributeDefinition<?>> getDefinitionsByFriendlyName(@Nonnull String friendlyName) {
        Map<AttributeDefinition<?>, Set<? extends AttributeValue<?>>> subMap =
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
     * @param <T> the type of the {@link AttributeValue}.
     * @return The first {@link AttributeValue} (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> AttributeValue<T> getFirstAttributeValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        Set<? extends AttributeValue<T>> values = getAttributeValues(attributeDefinition);
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
     * @param <T> the type of the first value (if any) corresponding to the given {@link AttributeDefinition}.
     * @return The first value (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> T getFirstValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        return valueMap.getFirstValue(attributeDefinition);
    }

    /**
     * Returns the content of this object as an {@link ImmutableValueMap} where keys are {@link AttributeDefinition}s
     * and values are {@link Set}s of typed values.
     *
     * @return the content of this object as an {@link ImmutableValueMap}.
     */
    @Nonnull
    public ImmutableValueMap getValueMap() {
        return valueMap;
    }

    /**
     * Returns the typed values for the given {@link AttributeDefinition} as an {@link Set} or returns {@code
     * null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @param <T> the type of the values for the given {@link AttributeDefinition}.
     * @return The values (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> Set<? extends T> getValues(@Nonnull AttributeDefinition<T> attributeDefinition) {
        return valueMap.getValues(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition} as Map keys and {@link Set} of typed values matching
     * the types of the definition as Map values.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link Set}s of typed values  or returns {@code null} if no attribute in
     * this map has that friendly name.
     */
    @Nullable
    public ImmutableValueMap getValuesByFriendlyName(@Nonnull String friendlyName) {
        return valueMap.getValuesByFriendlyName(friendlyName);
    }

    @Nullable
    public <T> Set<? extends T> getValuesByNameUri(@Nonnull String name) {
        return valueMap.getValuesByNameUri(name);
    }

    @Nullable
    public <T> Set<? extends T> getValuesByNameUri(@Nonnull URI name) {
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
     *
     * @param objectInputStream an {@link ObjectInputStream}
     * @throws InvalidObjectException the validation object is null
     */
    private void readObject(@Nonnull ObjectInputStream objectInputStream) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization Proxy required");
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     *
     * @param out an {@link ObjectOutputStream}
     * @throws InvalidObjectException the validation object is null
     */
    private void writeObject(@Nonnull ObjectOutputStream out) throws InvalidObjectException {
        throw new InvalidObjectException("Serialization Proxy required");
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     *
     * @return an Object
     * @throws ObjectStreamException the validation object is null
     */
    private Object writeReplace() throws ObjectStreamException {
        return new SerializationProxy(this);
    }
}
