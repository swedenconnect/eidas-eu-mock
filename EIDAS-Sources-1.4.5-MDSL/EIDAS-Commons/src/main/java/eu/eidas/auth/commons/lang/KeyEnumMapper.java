/*
 * This work is Open Source and licensed by the European Commission under the
 * conditions of the European Public License v1.1
 *
 * (http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1);
 *
 * any use of this file implies acceptance of the conditions of this license.
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package eu.eidas.auth.commons.lang;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * EnumMapper for KeyMappings.
 *
 * @param <K> the type of the key obtained from the enum values
 * @param <E> the type of the enum which must implement {@link KeyMapping}.
 * @since 1.1
 */
public final class KeyEnumMapper<K, E extends Enum<E> & KeyMapping<K>> extends EnumMapper<K, E> {

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> KeyAccessor<K, E> getKeyAccessor() {
        return new KeyAccessor<K, E>() {

            @Nonnull
            @Override
            public K getKey(@Nonnull E e) {
                return e.getKey();
            }
        };
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> K[] toKeyArray(@Nonnull Canonicalizer<K> canonicalizer,
                                                                        @Nonnull Collection<E> enumConstants) {
        return EnumMapper.toKeyArray(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> K[] toKeyArray(@Nonnull Canonicalizer<K> canonicalizer,
                                                                        @Nonnull E... enumConstants) {
        return EnumMapper.toKeyArray(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> List<K> toKeyList(@Nonnull Canonicalizer<K> canonicalizer,
                                                                           @Nonnull Collection<E> enumConstants) {
        return EnumMapper.toKeyList(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> List<K> toKeyList(@Nonnull Canonicalizer<K> canonicalizer,
                                                                           @Nonnull E... enumConstants) {
        return EnumMapper.toKeyList(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> Set<K> toKeySet(@Nonnull Canonicalizer<K> canonicalizer,
                                                                         @Nonnull Collection<E> enumConstants) {
        return EnumMapper.toKeySet(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> Set<K> toKeySet(@Nonnull Canonicalizer<K> canonicalizer,
                                                                         @Nonnull E... enumConstants) {
        return EnumMapper.toKeySet(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> List<K> unmodifiableKeyList(
            @Nonnull Canonicalizer<K> canonicalizer, @Nonnull Collection<E> enumConstants) {
        return EnumMapper.unmodifiableKeyList(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> List<K> unmodifiableKeyList(
            @Nonnull Canonicalizer<K> canonicalizer, @Nonnull E... enumConstants) {
        return EnumMapper.unmodifiableKeyList(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> Set<K> unmodifiableKeySet(
            @Nonnull Canonicalizer<K> canonicalizer, @Nonnull Collection<E> enumConstants) {
        return EnumMapper.unmodifiableKeySet(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    @Nonnull
    public static <K, E extends Enum<E> & KeyMapping<K>> Set<K> unmodifiableKeySet(
            @Nonnull Canonicalizer<K> canonicalizer, @Nonnull E... enumConstants) {
        return EnumMapper.unmodifiableKeySet(KeyEnumMapper.<K, E>getKeyAccessor(), canonicalizer, enumConstants);
    }

    public KeyEnumMapper(@Nonnull E[] values) {
        this(null, values);
    }

    public KeyEnumMapper(@Nullable Canonicalizer<K> keyCanonicalizer, @Nonnull E[] values) {
        super(KeyEnumMapper.<K, E>getKeyAccessor(), keyCanonicalizer, values);
    }
}
