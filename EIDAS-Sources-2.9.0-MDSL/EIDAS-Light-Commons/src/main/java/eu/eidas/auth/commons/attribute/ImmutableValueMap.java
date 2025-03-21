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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * An immutable Map of {@link AttributeDefinition}s to typed values.
 *
 * @since 1.1
 */
@SuppressWarnings("ConstantConditions")
@Immutable
@ThreadSafe
public final class ImmutableValueMap implements Serializable {

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p>
     * The serialVersionUID of this class is not used because a Serialization Proxy is used instead.
     */
    private static final long serialVersionUID = 8138045507851775749L;

    /**
     * <p>
     * Builder pattern for the {@link ImmutableValueMap} class.
     * <p>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     *
     */
    @SuppressWarnings("ParameterHidesMemberVariable")
    @NotThreadSafe
    public static final class Builder {

        /**
         * Typesafe heterogeneous container pattern.
         * <p>
         * See item 29 of Effective Java 2nd Edition.
         */
        @Nonnull
        private final Map<AttributeDefinition<?>, Set<?>> definitionsToValues = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, AttributeDefinition<?>> nameUrisToDefinitions = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, Set<?>> nameUrisToValues = new LinkedHashMap<>();

        @Nonnull
        private final Map<String, Set<AttributeDefinition<?>>> friendlyNamesToDefinitions = new LinkedHashMap<>();

        public Builder() {
        }

        public Builder(@Nonnull Builder copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy.definitionsToValues);
        }

        public Builder(@Nonnull ImmutableValueMap copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy);
        }

        public Builder(@Nonnull Map<AttributeDefinition<?>, ? extends Iterable<?>> copy) {
            Preconditions.checkNotNull(copy, "copy");
            putAll(copy);
        }

        @Nonnull
        public ImmutableValueMap build() {
            ImmutableValueMap result = new ImmutableValueMap(this);
            if (result.isEmpty()) {
                return EMPTY;
            }
            return result;
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute) {
            Preconditions.checkNotNull(attribute, "attribute");
            Set<T> valueSet = Set.of();

            return put(attribute, valueSet);
        }

        @Nonnull
        private <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull Set<T> values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");

            // Typesafe heterogeneous container pattern.
            // See item 29 of Effective Java 2nd Edition.
            // Typesafety check using class#cast():
            Class<T> parameterizedType = attribute.getParameterizedType();
            for (T value : values) {
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
            definitionsToValues.put(attribute, values);
            nameUrisToDefinitions.put(nameUri, attribute);
            //noinspection unchecked unfortunate but due to the heterogeneous container pattern
            nameUrisToValues.put(nameUri, values);

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
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull Iterable<T> values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            Set<T> valueSet = StreamSupport.stream(values.spliterator(), false)
                    .collect(Collectors.collectingAndThen(Collectors.toSet(), Set::copyOf));

            return put(attribute, valueSet);
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull T... values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            Set<T> valueSet = Set.copyOf(Arrays.asList(values));

            return put(attribute, valueSet);
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
        public Builder putAll(@Nonnull Map<AttributeDefinition<?>, ? extends Iterable<?>> attributeMap) {
            Preconditions.checkNotNull(attributeMap, "attributeMap");
            for (final Map.Entry<AttributeDefinition<?>, ? extends Iterable<?>> entry : attributeMap.entrySet()) {
                putWithIterable(entry.getKey(), entry.getValue());
            }
            return this;
        }

        private <T> void putWithIterable(AttributeDefinition<T> key, Iterable<?> value) {
            Set<T> valueSet = StreamSupport.stream(value.spliterator(), false)
                    .map(item -> (T) item)
                    .collect(Collectors.toSet());
            put(key, valueSet);
        }

        @Nonnull
        public Builder putAll(@Nonnull ImmutableValueMap attributeMap) {
            Preconditions.checkNotNull(attributeMap, "attributeMap");
            putAll(attributeMap.attributeMap);
            return this;
        }
    }

    /**
     * <p>
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     *
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     */
    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = -8841554594396122128L;

        @Nonnull
        private transient Map<AttributeDefinition<?>, Set<?>> attributeMap;

        private SerializationProxy(@Nonnull ImmutableValueMap attributeMap) {
            this.attributeMap = attributeMap.attributeMap;
        }

        private void readObject(@Nonnull ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            Map<AttributeDefinition<?>, Set<?>> map = new LinkedHashMap<>();
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

    private static final ImmutableValueMap EMPTY = new ImmutableValueMap(new Builder());

    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    @Nonnull
    public static Builder builder(@Nonnull Builder copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull ImmutableValueMap copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static Builder builder(@Nonnull Map<AttributeDefinition<?>, ? extends Iterable<?>> copy) {
        return new Builder(copy);
    }

    @Nonnull
    public static ImmutableValueMap copyOf(@Nonnull Map<AttributeDefinition<?>, ? extends Iterable<?>> map) {
        return new Builder(map).build();
    }

    @Nonnull
    public static ImmutableValueMap of() {
        return EMPTY;
    }

    @Nonnull
    public static <T> ImmutableValueMap of(@Nonnull AttributeDefinition<T> attributeDefinition,
                                           @Nonnull Iterable<T> values) {
        return new Builder().put(attributeDefinition, values).build();
    }

    @Nonnull
    public static <T> ImmutableValueMap of(@Nonnull AttributeDefinition<T> attributeDefinition, @Nonnull T... values) {
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

    @Nonnull
    private final transient Map<AttributeDefinition<?>, Set<?>> attributeMap;

    @Nonnull
    private final transient Map<URI, AttributeDefinition<?>> nameUrisToDefinitions;

    @Nonnull
    private final transient Map<URI, Set<?>> nameUrisToValues;

    @Nonnull
    private final transient Map<String, Map<AttributeDefinition<?>, Set<?>>> friendlyNameMap;

    private ImmutableValueMap(@Nonnull Builder builder) {
        attributeMap = new LinkedHashMap<>(builder.definitionsToValues);
        nameUrisToDefinitions = new LinkedHashMap<>(builder.nameUrisToDefinitions);
        nameUrisToValues = new LinkedHashMap<>(builder.nameUrisToValues);
        Map<String, Map<AttributeDefinition<?>, Set<?>>> friendlyNameMapBuilder = new LinkedHashMap<>();
        for (final Map.Entry<String, Set<AttributeDefinition<?>>> entry : builder.friendlyNamesToDefinitions.entrySet()) {
            String friendlyName = entry.getKey();
            Set<AttributeDefinition<?>> attributeDefinitions = entry.getValue();
            Map<AttributeDefinition<?>, Set<?>> subMapBuilder = new HashMap<>();
            for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                subMapBuilder.put(attributeDefinition, attributeMap.get(attributeDefinition));
            }
            friendlyNameMapBuilder.put(friendlyName, Collections.unmodifiableMap(subMapBuilder));
        }
        friendlyNameMap = Collections.unmodifiableMap(friendlyNameMapBuilder);
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
        Map<AttributeDefinition<?>, Set<?>> subMap = friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return subMap.keySet();
    }

    /**
     * Returns the first typed value for the given {@link AttributeDefinition} or returns {@code null} if no attribute
     * in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @param <T> the type of the first value (if any) corresponding to the given {@link AttributeDefinition}.
     * @return The first typed value (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> T getFirstValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        Set<T> values = getValues(attributeDefinition);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.iterator().next();
    }

    /**
     * Returns the content of this object as an {@link Map} where keys are {@link AttributeDefinition}s and
     * values are {@link Set}s of typed values.
     *
     * @return the content of this object as an {@link Map}.
     */
    @Nonnull
    public Map<AttributeDefinition<?>, Set<?>> getMap() {
        return attributeMap;
    }

    /**
     * Returns the typed values for the given {@link AttributeDefinition} as an {@link Set} or returns {@code
     * null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @param <T> the type of the values for the given {@link AttributeDefinition}.
     * @return The typed values (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> Set<T> getValues(@Nonnull AttributeDefinition<T> attributeDefinition) {
        //noinspection unchecked unfortunate but due to the heterogeneous container pattern
        return (Set<T>) attributeMap.get(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition}s as Map keys and {@link Set} of typed values as Map
     * values.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link Set}s of typed values or returns {@code null} if no attribute in
     * this map has that friendly name.
     */
    @Nullable
    public ImmutableValueMap getValuesByFriendlyName(@Nonnull String friendlyName) {
        Map<AttributeDefinition<?>, Set<?>> subMap = friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return builder().putAll(subMap).build();
    }

    @Nullable
    public <T> Set<T> getValuesByNameUri(@Nonnull String name) {
        URI nameUri = toUri(name);
        return getValuesByNameUri(nameUri);
    }

    @Nullable
    public <T> Set<T> getValuesByNameUri(@Nonnull URI name) {
        return (Set<T>) nameUrisToValues.get(name);
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

        ImmutableValueMap that = (ImmutableValueMap) o;

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
