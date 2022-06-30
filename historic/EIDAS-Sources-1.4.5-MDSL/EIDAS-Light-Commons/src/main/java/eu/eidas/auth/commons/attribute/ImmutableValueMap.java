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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.collections.CollectionUtils;

import eu.eidas.auth.commons.io.MapSerializationHelper;
import eu.eidas.util.Preconditions;

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
     * Builder pattern for the {@link ImmutableValueMap} class.
     * <p/>
     * Effective Java, 2nd Ed. : Item 2: Builder Pattern.
     * <p/>
     * This Builder is not thread-safe but is thread-compliant, it is supposed to be used by only one thread.
     * <p/>
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
        private final Map<AttributeDefinition<?>, ImmutableSet<?>> definitionsToValues = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, AttributeDefinition<?>> nameUrisToDefinitions = new LinkedHashMap<>();

        @Nonnull
        private final Map<URI, ImmutableSet<?>> nameUrisToValues = new LinkedHashMap<>();

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
            ImmutableSet<T> valueSet = ImmutableSet.of();

            return put(attribute, valueSet);
        }

        @Nonnull
        private <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull ImmutableSet<T> values) {
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
            ImmutableSet<T> valueSet = ImmutableSet.copyOf(values);

            return put(attribute, valueSet);
        }

        @Nonnull
        public <T> Builder put(@Nonnull AttributeDefinition<T> attribute, @Nonnull T... values) {
            Preconditions.checkNotNull(attribute, "attribute");
            Preconditions.checkNotNull(values, "values");
            ImmutableSet<T> valueSet = ImmutableSet.copyOf(values);

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
                // unfortunate but due to the heterogeneous container pattern
                //noinspection unchecked,rawtypes
                put(entry.getKey(), (ImmutableSet) ImmutableSet.copyOf(entry.getValue()));
            }
            return this;
        }

        @Nonnull
        public Builder putAll(@Nonnull ImmutableValueMap attributeMap) {
            Preconditions.checkNotNull(attributeMap, "attributeMap");
            putAll(attributeMap.attributeMap);
            return this;
        }
    }

    /**
     * Effective Java, 2nd Ed. : Item 78: Serialization Proxy pattern.
     * <p/>
     * Defensive serialization ensuring that the validation rules defined in the Builder are always used.
     */
    private static final class SerializationProxy implements Serializable {

        private static final long serialVersionUID = -8841554594396122128L;

        @Nonnull
        private transient ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>> attributeMap;

        private SerializationProxy(@Nonnull ImmutableValueMap attributeMap) {
            this.attributeMap = attributeMap.attributeMap;
        }

        private void readObject(@Nonnull ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();

            Map<AttributeDefinition<?>, ImmutableSet<?>> map = new LinkedHashMap<>();
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
    private final transient ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>> attributeMap;

    @Nonnull
    private final transient ImmutableMap<URI, AttributeDefinition<?>> nameUrisToDefinitions;

    @Nonnull
    private final transient ImmutableMap<URI, ImmutableSet<?>> nameUrisToValues;

    @Nonnull
    private final transient ImmutableMap<String, ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>>> friendlyNameMap;

    private ImmutableValueMap(@Nonnull Builder builder) {
        attributeMap = ImmutableMap.copyOf(builder.definitionsToValues);
        nameUrisToDefinitions = ImmutableMap.copyOf(builder.nameUrisToDefinitions);
        nameUrisToValues = ImmutableMap.copyOf(builder.nameUrisToValues);
        ImmutableMap.Builder<String, ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>>> friendlyNameMapBuilder =
                new ImmutableMap.Builder<>();
        for (final Map.Entry<String, Set<AttributeDefinition<?>>> entry : builder.friendlyNamesToDefinitions.entrySet()) {
            String friendlyName = entry.getKey();
            Set<AttributeDefinition<?>> attributeDefinitions = entry.getValue();
            ImmutableMap.Builder<AttributeDefinition<?>, ImmutableSet<?>> subMapBuilder = new ImmutableMap.Builder<>();
            for (final AttributeDefinition<?> attributeDefinition : attributeDefinitions) {
                subMapBuilder.put(attributeDefinition, attributeMap.get(attributeDefinition));
            }
            friendlyNameMapBuilder.put(friendlyName, subMapBuilder.build());
        }
        friendlyNameMap = friendlyNameMapBuilder.build();
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
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>> subMap = friendlyNameMap.get(friendlyName);
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
     * @return The first typed value (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> T getFirstValue(@Nonnull AttributeDefinition<T> attributeDefinition) {
        ImmutableSet<T> values = getValues(attributeDefinition);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.iterator().next();
    }

    /**
     * Returns the content of this object as an {@link ImmutableMap} where keys are {@link AttributeDefinition}s and
     * values are {@link ImmutableSet}s of typed values.
     *
     * @return the content of this object as an {@link ImmutableMap}.
     */
    @Nonnull
    public ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>> getMap() {
        return attributeMap;
    }

    /**
     * Returns the typed values for the given {@link AttributeDefinition} as an {@link ImmutableSet} or returns {@code
     * null} if no attribute in this map matches the given {@link AttributeDefinition}.
     *
     * @param attributeDefinition the attribute definition to look up.
     * @return The typed values (if any) corresponding to the given {@link AttributeDefinition}.
     */
    @Nullable
    public <T> ImmutableSet<T> getValues(@Nonnull AttributeDefinition<T> attributeDefinition) {
        //noinspection unchecked unfortunate but due to the heterogeneous container pattern
        return (ImmutableSet<T>) attributeMap.get(attributeDefinition);
    }

    /**
     * Returns the sub-map of this map where all the keys have the given friendly name or returns {@code null} if no
     * attribute in this map has that friendly name.
     * <p>
     * The returned map has {@link AttributeDefinition}s as Map keys and {@link ImmutableSet} of typed values as Map
     * values.
     *
     * @param friendlyName the friendly name which returned attribute keys must possess
     * @return a sub-map of this map where the keys are the {@link AttributeDefinition}s which have the given friendly
     * name and where the values are {@link ImmutableSet}s of typed values or returns {@code null} if no attribute in
     * this map has that friendly name.
     */
    @Nullable
    public ImmutableValueMap getValuesByFriendlyName(@Nonnull String friendlyName) {
        ImmutableMap<AttributeDefinition<?>, ImmutableSet<?>> subMap = friendlyNameMap.get(friendlyName);
        if (null == subMap) {
            return null;
        }
        return builder().putAll(subMap).build();
    }

    @Nullable
    public <T> ImmutableSet<T> getValuesByNameUri(@Nonnull String name) {
        URI nameUri = toUri(name);
        return getValuesByNameUri(nameUri);
    }

    @Nullable
    public <T> ImmutableSet<T> getValuesByNameUri(@Nonnull URI name) {
        return (ImmutableSet<T>) nameUrisToValues.get(name);
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
