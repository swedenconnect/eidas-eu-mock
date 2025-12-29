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
package eu.eidas.auth.commons.lang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A Mapper for Enum constants.
 *
 * @param <K> the type of the key obtained from the enum values
 * @param <E> the type of the enum
 * @since 1.1
 */
public class EnumMapper<K, E extends Enum<E>> {

    @Nonnull
    private static <K, E extends Enum<E>> K getCanonicalKey(@Nonnull KeyAccessor<K, E> accessor,
                                                            @Nonnull Canonicalizer<K> canonicalizer,
                                                            @Nonnull E enumConstant) {
        K key = accessor.getKey(enumConstant);
        //noinspection ConstantConditions
        return canonicalizer.canonicalize(key);
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
                                                                     @Nonnull E... enumConstants) {
        List<K> keys = toKeyList(accessor, canonicalizer, enumConstants);
        return Collections.unmodifiableList(keys);
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

    @Nonnull
    public final List<K> unmodifiableKeyList(@Nonnull E... enumConstants) {
        return unmodifiableKeyList(keyAccessor, keyCanonicalizer, enumConstants);
    }

    @Nonnull
    public final Set<K> unmodifiableKeySet(@Nonnull E... enumConstants) {
        return unmodifiableKeySet(keyAccessor, keyCanonicalizer, enumConstants);
    }
}
