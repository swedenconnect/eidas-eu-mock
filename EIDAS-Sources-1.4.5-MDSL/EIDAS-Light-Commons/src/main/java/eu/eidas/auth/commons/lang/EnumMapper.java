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
package eu.eidas.auth.commons.lang;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A Mapper for Enum constants.
 *
 * @param <K> the type of the key obtained from the enum values
 * @param <E> the type of the enum
 * @since 1.1
 */
public class EnumMapper<K, E extends Enum<E>> {

    @Nonnull
    private static <K, E extends Enum<E>> K getCanonicalKey(@Nonnull EnumMapper<K, E> mapper, @Nonnull E enumConstant) {
        return getCanonicalKey(mapper.keyAccessor, mapper.keyCanonicalizer, enumConstant);
    }

    @Nonnull
    private static <K, E extends Enum<E>> K getCanonicalKey(@Nonnull KeyAccessor<K, E> accessor,
                                                            @Nonnull Canonicalizer<K> canonicalizer,
                                                            @Nonnull E enumConstant) {
        K key = accessor.getKey(enumConstant);
        //noinspection ConstantConditions
        return canonicalizer.canonicalize(key);
    }

    @Nonnull
    public static <K, E extends Enum<E>> K[] toKeyArray(@Nonnull KeyAccessor<K, E> accessor,
                                                        @Nonnull Canonicalizer<K> canonicalizer,
                                                        @Nonnull Collection<E> enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.size() == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        final int size = enumConstants.size();
        K[] keys = null;
        int i = 0;
        for (E enumConstant : enumConstants) {
            if (null == enumConstant) {
                throw new IllegalArgumentException("enumConstants cannot contain a null object");
            }
            K key = getCanonicalKey(accessor, canonicalizer, enumConstant);
            if (null == keys) {
                //noinspection unchecked
                keys = (K[]) Array.newInstance(key.getClass(), size);
            }
            keys[i++] = key;
        }
        //noinspection ConstantConditions
        return keys;
    }

    @Nonnull
    public static <K, E extends Enum<E>> K[] toKeyArray(@Nonnull KeyAccessor<K, E> accessor,
                                                        @Nonnull Canonicalizer<K> canonicalizer,
                                                        @Nonnull E... enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.length == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        return toKeyArray(accessor, canonicalizer, Arrays.asList(enumConstants));
    }

    @Nonnull
    public static <K, E extends Enum<E>> List<K> toKeyList(@Nonnull KeyAccessor<K, E> accessor,
                                                           @Nonnull Canonicalizer<K> canonicalizer,
                                                           @Nonnull Collection<E> enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.size() == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        List<K> keys = new ArrayList<K>(enumConstants.size());
        for (E enumConstant : enumConstants) {
            if (null == enumConstant) {
                throw new IllegalArgumentException("enumConstants cannot contain a null object");
            }
            keys.add(getCanonicalKey(accessor, canonicalizer, enumConstant));
        }
        return keys;
    }

    @Nonnull
    public static <K, E extends Enum<E>> List<K> toKeyList(@Nonnull KeyAccessor<K, E> accessor,
                                                           @Nonnull Canonicalizer<K> canonicalizer,
                                                           @Nonnull E... enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.length == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        return toKeyList(accessor, canonicalizer, Arrays.asList(enumConstants));
    }

    @Nonnull
    public static <K, E extends Enum<E>> Set<K> toKeySet(@Nonnull KeyAccessor<K, E> accessor,
                                                         @Nonnull Canonicalizer<K> canonicalizer,
                                                         @Nonnull Collection<E> enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.size() == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        Set<K> keys = new LinkedHashSet<K>(enumConstants.size());
        for (E enumConstant : enumConstants) {
            if (null == enumConstant) {
                throw new IllegalArgumentException("enumConstants cannot contain a null object");
            }
            keys.add(getCanonicalKey(accessor, canonicalizer, enumConstant));
        }
        return keys;
    }

    @Nonnull
    public static <K, E extends Enum<E>> Set<K> toKeySet(@Nonnull KeyAccessor<K, E> accessor,
                                                         @Nonnull Canonicalizer<K> canonicalizer,
                                                         @Nonnull E... enumConstants) {
        //noinspection ConstantConditions
        if (null == enumConstants || enumConstants.length == 0) {
            throw new IllegalArgumentException("enumConstants cannot be null or empty");
        }
        return toKeySet(accessor, canonicalizer, Arrays.asList(enumConstants));
    }

    @Nonnull
    public static <K, E extends Enum<E>> List<K> unmodifiableKeyList(@Nonnull KeyAccessor<K, E> accessor,
                                                                     @Nonnull Canonicalizer<K> canonicalizer,
                                                                     @Nonnull Collection<E> enumConstants) {
        List<K> keys = toKeyList(accessor, canonicalizer, enumConstants);
        return Collections.unmodifiableList(keys);
    }

    @Nonnull
    public static <K, E extends Enum<E>> List<K> unmodifiableKeyList(@Nonnull KeyAccessor<K, E> accessor,
                                                                     @Nonnull Canonicalizer<K> canonicalizer,
                                                                     @Nonnull E... enumConstants) {
        List<K> keys = toKeyList(accessor, canonicalizer, enumConstants);
        return Collections.unmodifiableList(keys);
    }

    @Nonnull
    public static <K, E extends Enum<E>> Set<K> unmodifiableKeySet(@Nonnull KeyAccessor<K, E> accessor,
                                                                   @Nonnull Canonicalizer<K> canonicalizer,
                                                                   @Nonnull Collection<E> enumConstants) {
        Set<K> keys = toKeySet(accessor, canonicalizer, enumConstants);
        return Collections.unmodifiableSet(keys);
    }

    @Nonnull
    public static <K, E extends Enum<E>> Set<K> unmodifiableKeySet(@Nonnull KeyAccessor<K, E> accessor,
                                                                   @Nonnull Canonicalizer<K> canonicalizer,
                                                                   @Nonnull E... enumConstants) {
        Set<K> keys = toKeySet(accessor, canonicalizer, enumConstants);
        return Collections.unmodifiableSet(keys);
    }

    @Nonnull
    private final KeyAccessor<K, E> keyAccessor;

    @Nonnull
    private final Canonicalizer<K> keyCanonicalizer;

    @Nonnull
    private final Map<K, E> fromKeys;

    public EnumMapper(@Nonnull KeyAccessor<K, E> keyAccessor, @Nonnull E[] enumConstants) {
        this(keyAccessor, null, enumConstants);
    }

    public EnumMapper(@Nonnull KeyAccessor<K, E> keyAccessor,
                      @Nullable Canonicalizer<K> keyCanonicalizer,
                      @Nonnull E[] enumConstants) {
        this.keyAccessor = keyAccessor;
        if (null == keyCanonicalizer) {
            //noinspection AssignmentToMethodParameter
            keyCanonicalizer = Canonicalizers.idem();
        }
        this.keyCanonicalizer = keyCanonicalizer;
        Map<K, E> fromKeysMap = new HashMap<K, E>();
        for (E enumConstant : enumConstants) {
            K key = getCanonicalKey(enumConstant);
            E previous = fromKeysMap.put(key, enumConstant);
            if (null != previous) {
                throw new AssertionError("Duplicate key \"" + key + "\" for enum constant \"" + previous.name()
                                                 + "\" and enum constant \"" + enumConstant.name() + "\"");
            }
        }
        fromKeys = Collections.unmodifiableMap(fromKeysMap);
    }

    private void addAllKeys(@Nonnull Collection<K> keys, @Nonnull Collection<E> toEnumConstants) {
        for (K key : keys) {
            if (null == key) {
                throw new IllegalArgumentException("keys cannot contain a null key");
            }
            E enumConstant = fromKey(key);
            if (null == enumConstant) {
                throw new IllegalArgumentException("keys contain unknown  key \"" + key + "\"");
            }
            toEnumConstants.add(enumConstant);
        }
    }

    public final boolean containsKey(@Nullable K key) {
        return null != key && existsKey(key);
    }

    public final boolean existsKey(@Nonnull K key) {
        return null != fromKey(key);
    }

    @Nullable
    public final E fromKey(@Nonnull K key) {
        return fromKeys.get(keyCanonicalizer.canonicalize(key));
    }

    /**
     * Returns the canonical version of the key derived from the given enum constant.
     *
     * @param enumConstant the enum constant to convert
     * @return the canonical version of the key derived from the given enum constant.
     */
    @Nonnull
    public final K getCanonicalKey(@Nonnull E enumConstant) {
        return getCanonicalKey(keyAccessor, keyCanonicalizer, enumConstant);
    }

    @Nullable
    public final E getEnum(@Nullable K key) {
        if (null == key) {
            return null;
        }
        return fromKey(key);
    }

    public final boolean matches(@Nullable K key, @Nullable E e) {
        if (null == key || null == e) {
            return false;
        }
        E fromKey = fromKey(key);
        return fromKey == e;
    }

    /**
     * Returns an array of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an array of corresponding values.
     */
    @Nonnull
    public final E[] toEnumArray(@Nonnull Collection<K> keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.size() == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        final int size = keys.size();
        E[] enumConstants = null;
        int i = 0;
        for (K key : keys) {
            if (null == key) {
                throw new IllegalArgumentException("keys cannot contain a null key");
            }
            E enumConstant = fromKey(key);
            if (null == enumConstant) {
                throw new IllegalArgumentException("keys contain unknown  key \"" + key + "\"");
            }
            if (null == enumConstants) {
                //noinspection unchecked
                enumConstants = (E[]) Array.newInstance(enumConstant.getClass(), size);
            }
            enumConstants[i++] = enumConstant;
        }

        //noinspection ConstantConditions
        return enumConstants;
    }

    /**
     * Returns an array of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an array of corresponding values.
     */
    @Nonnull
    public final E[] toEnumArray(@Nonnull K... keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.length == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        return toEnumArray(Arrays.asList(keys));
    }

    /**
     * Returns a list of corresponding values.
     *
     * @param keys the  keys to convert
     * @return a list of corresponding values.
     */
    @Nonnull
    public final List<E> toEnumList(@Nonnull Collection<K> keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.size() == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        List<E> enumConstants = new ArrayList<E>(keys.size());
        addAllKeys(keys, enumConstants);
        return enumConstants;
    }

    /**
     * Returns a list of corresponding values.
     *
     * @param keys the  keys to convert
     * @return a list of corresponding values.
     */
    @Nonnull
    public final List<E> toEnumList(@Nonnull K... keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.length == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        return toEnumList(Arrays.asList(keys));
    }

    /**
     * Returns a predictable iteration-order Set of corresponding values.
     *
     * @param keys the  keys to convert
     * @return a predictable iteration-order Set of corresponding values.
     */
    @Nonnull
    public final Set<E> toEnumSet(@Nonnull Collection<K> keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.size() == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        LinkedHashSet<E> enumConstants = new LinkedHashSet<E>(keys.size());
        addAllKeys(keys, enumConstants);
        return enumConstants;
    }

    /**
     * Returns a predictable iteration-order Set of corresponding values.
     *
     * @param keys the  keys to convert
     * @return a predictable iteration-order Set of corresponding values.
     */
    @Nonnull
    public final Set<E> toEnumSet(@Nonnull K... keys) {
        //noinspection ConstantConditions
        if (null == keys || keys.length == 0) {
            throw new IllegalArgumentException("keys cannot be null or empty");
        }
        return toEnumSet(Arrays.asList(keys));
    }

    @Nonnull
    public final K[] toKeyArray(@Nonnull Collection<E> enumConstants) {
        return toKeyArray(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final K[] toKeyArray(@Nonnull E... enumConstants) {
        return toKeyArray(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final List<K> toKeyList(@Nonnull Collection<E> enumConstants) {
        return toKeyList(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final List<K> toKeyList(@Nonnull E... enumConstants) {
        return toKeyList(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final Map<K, E> toKeyMap() {
        return new HashMap<K, E>(fromKeys);
    }

    @Nonnull
    public final Set<K> toKeySet(@Nonnull Collection<E> enumConstants) {
        return toKeySet(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final Set<K> toKeySet(@Nonnull E... enumConstants) {
        return toKeySet(keyAccessor, keyCanonicalizer, enumConstants);
    }

    /**
     * Returns an unmodifiable List of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an unmodifiable List of corresponding values.
     */
    @Nonnull
    public final List<E> unmodifiableEnumList(@Nonnull K... keys) {
        List<E> enumConstants = toEnumList(keys);
        return Collections.unmodifiableList(enumConstants);
    }

    /**
     * Returns an unmodifiable List of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an unmodifiable list of corresponding values.
     */
    @Nonnull
    public final List<E> unmodifiableEnumList(@Nonnull Collection<K> keys) {
        List<E> enumConstants = toEnumList(keys);
        return Collections.unmodifiableList(enumConstants);
    }

    /**
     * Returns an unmodifiable predictable iteration-order Set of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an unmodifiable predictable iteration-order Set of corresponding values.
     */
    @Nonnull
    public final Set<E> unmodifiableEnumSet(@Nonnull K... keys) {
        Set<E> enumConstants = toEnumSet(keys);
        return Collections.unmodifiableSet(enumConstants);
    }

    /**
     * Returns an unmodifiable predictable iteration-order Set of corresponding values.
     *
     * @param keys the  keys to convert
     * @return an unmodifiable predictable iteration-order Set of corresponding values.
     */
    @Nonnull
    public final Set<E> unmodifiableEnumSet(@Nonnull Collection<K> keys) {
        Set<E> enumConstants = toEnumSet(keys);
        return Collections.unmodifiableSet(enumConstants);
    }

    @Nonnull
    public final List<K> unmodifiableKeyList(@Nonnull Collection<E> enumConstants) {
        return unmodifiableKeyList(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final List<K> unmodifiableKeyList(@Nonnull E... enumConstants) {
        return unmodifiableKeyList(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final Map<K, E> unmodifiableKeyMap() {
        return fromKeys;
    }

    @Nonnull
    public final Set<K> unmodifiableKeySet(@Nonnull Collection<E> enumConstants) {
        return unmodifiableKeySet(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final Set<K> unmodifiableKeySet(@Nonnull E... enumConstants) {
        return unmodifiableKeySet(keyAccessor, keyCanonicalizer, enumConstants);
    }
}
