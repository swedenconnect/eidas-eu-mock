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
package eu.eidas.auth.commons;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang.StringUtils;

import eu.eidas.auth.commons.attribute.AttributeDefinition;
import eu.eidas.auth.commons.attribute.AttributeRegistry;
import eu.eidas.auth.commons.attribute.AttributeValue;
import eu.eidas.auth.commons.attribute.AttributeValueMarshaller;
import eu.eidas.auth.commons.attribute.AttributeValueMarshallingException;
import eu.eidas.auth.commons.attribute.ImmutableAttributeMap;
import eu.eidas.auth.commons.attribute.PersonType;

/**
 * This class is a bean used to store the information relative to the PersonalAttributeList.
 *
 * @see PersonalAttribute
 * @deprecated use {@link eu.eidas.auth.commons.attribute.ImmutableAttributeMap} instead.
 */
@NotThreadSafe
@Deprecated
public final class PersonalAttributeList implements IPersonalAttributeList {

    /**
     * A {@link java.util.Map} key.
     */
    private abstract static class AbstractKey {

        @Nonnull
        private final String key;

        AbstractKey(@Nonnull String keyParam) {
            key = keyParam;
        }

        @Nonnull
        final String getKey() {
            return key;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || !(o instanceof AbstractKey)) {
                return false;
            }

            AbstractKey other = (AbstractKey) o;

            return key.equals(other.key);

        }

        @Override
        public final int hashCode() {
            return key.hashCode();
        }
    }

    /**
     * A {@link java.util.Map} key which preserves the order of insertion.
     */
    private static final class CustomKey extends AbstractKey implements Comparable<CustomKey> {

        private final long index;

        CustomKey(long index, @Nonnull String key) {
            super(key);
            this.index = index;
        }

        long getIndex() {
            return index;
        }

        @Override
        @java.lang.SuppressWarnings("squid:S1210")
        public int compareTo(@Nonnull CustomKey o) {
            return Long.compare(index, o.index);
        }
    }

    /**
     * A {@link java.util.Map} key used only for Map retrieval operations such as {@link java.util.Map#get(Object)}.
     */
    private static final class RetrievalKey extends AbstractKey {

        RetrievalKey(@Nonnull String key) {
            super(key);
        }
    }

    /**
     * Unfortunately {@link java.util.concurrent.ConcurrentLinkedQueue} does not provide a {@link
     * Collection#equals(Object)} method.
     *
     * @param <E> the type of the items
     */
    private static final class ConcurrentLinkedQueue<E> extends java.util.concurrent.ConcurrentLinkedQueue<E> {

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof ConcurrentLinkedQueue)) {
                return false;
            }

            Iterator<E> e1 = ConcurrentLinkedQueue.this.iterator();
            Iterator<?> e2 = ((ConcurrentLinkedQueue<?>) o).iterator();
            while (e1.hasNext() && e2.hasNext()) {
                E o1 = e1.next();
                Object o2 = e2.next();
                if (!(o1 == null ? o2 == null : o1.equals(o2))) {
                    return false;
                }
            }
            return !(e1.hasNext() || e2.hasNext());
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }
    }

    /**
     * Static factory method to copy a {@link PersonalAttributeList}.
     * <p/>
     * This method is more robust and flexible than implementing {@link #clone()}.
     *
     * @param copy the instance to copy
     * @return a copy of the given argument
     */
    @Nonnull
    public static PersonalAttributeList copyOf(@Nonnull IPersonalAttributeList copy) {
        PersonalAttributeList list = new PersonalAttributeList();
        for (final PersonalAttribute personalAttribute : copy) {
            list.add(personalAttribute);
        }
        return list;
    }

    /**
     * Static factory method to copy an {@link eu.eidas.auth.commons.attribute.ImmutableAttributeMap} as a {@link
     * PersonalAttributeList}.
     * <p/>
     *
     * @param copy the instance to copy
     * @return a copy of the given argument
     */
    @Nonnull
    public static PersonalAttributeList copyOf(@Nonnull ImmutableAttributeMap copy) {
        PersonalAttributeList list = new PersonalAttributeList();
        if (null != copy) {
            for (final Map.Entry<AttributeDefinition<?>, ImmutableSet<? extends AttributeValue<?>>> entry : copy.getAttributeMap()
                    .entrySet()) {
                AttributeDefinition<?> attributeDefinition = entry.getKey();
                ImmutableSet<? extends AttributeValue<?>> values = entry.getValue();
                PersonalAttribute personalAttribute =
                        new PersonalAttribute(attributeDefinition.getNameUri().toASCIIString(),
                                              attributeDefinition.getFriendlyName());
                personalAttribute.setEidasLegalPersonAttr(
                        PersonType.LEGAL_PERSON == attributeDefinition.getPersonType());
                personalAttribute.setEidasNaturalPersonAttr(
                        PersonType.NATURAL_PERSON == attributeDefinition.getPersonType());
                personalAttribute.setIsRequired(attributeDefinition.isRequired());
                ImmutableList.Builder<String> builder = ImmutableList.builder();
                // do not marshal, we need raw values to be displayed
                /*AttributeValueMarshaller<?> attributeValueMarshaller =
                        attributeDefinition.getAttributeValueMarshaller();*/
                for (final AttributeValue value : values) {
                    //try {
                        //builder.add(attributeValueMarshaller.marshal(value));
                        builder.add(value.getValue().toString());
                    /*} catch (AttributeValueMarshallingException e) {
                        throw new IllegalStateException(e);
                    }*/
                }
                personalAttribute.setValue(builder.build());
                list.add(personalAttribute);
            }
        }
        return list;
    }

    /**
     * Static factory method to convert a {@link PersonalAttributeList} into an {@link
     * eu.eidas.auth.commons.attribute.ImmutableAttributeMap}.
     * <p/>
     *
     * @param copy the instance to copy
     * @return a copy of the given argument
     */
    @Nonnull
    public static ImmutableAttributeMap copyOf(@Nonnull IPersonalAttributeList copy,
                                               @Nonnull AttributeRegistry... attributeRegistries) {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
        for (final PersonalAttribute personalAttribute : copy) {
            AttributeDefinition<?> attributeDefinition = getByName(personalAttribute.getName(), attributeRegistries);
            if (!personalAttribute.isEmptyValue()) {
                List<String> personalAttributeValue = personalAttribute.getValue();
                ImmutableSet<AttributeValue<?>> attributeValues =
                        toAttributeValues(attributeDefinition, personalAttributeValue);
                builder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) attributeValues);
            } else if (!personalAttribute.isEmptyComplexValue()) {
                builder.put(attributeDefinition, personalAttribute.getComplexValue().toString());
            } else if (personalAttribute.isEmpty()) {
                builder.put(attributeDefinition);
            }

        }
        return builder.build();
    }

    public static ImmutableSet<AttributeValue<?>> toAttributeValues(AttributeDefinition<?> attributeDefinition,
                                                                    List<String> personalAttributeValues) {
        AttributeValueMarshaller<?> attributeValueMarshaller = attributeDefinition.getAttributeValueMarshaller();
        ImmutableSet.Builder<AttributeValue<?>> setBuilder = ImmutableSet.builder();
        for (final String value : personalAttributeValues) {
            try {
                setBuilder.add(attributeValueMarshaller.unmarshal(value, false));
            } catch (AttributeValueMarshallingException e) {
                throw new IllegalStateException(e);
            }
        }
        return setBuilder.build();
    }

    private static AttributeDefinition getByName(String name, @Nonnull AttributeRegistry... attributeRegistries) {
        for (AttributeRegistry attributeRegistry : attributeRegistries) {
            AttributeDefinition attributeDefinition = attributeRegistry.getByName(name);
            if (null != attributeDefinition) {
                return attributeDefinition;
            }
        }
        return null;
    }

    /**
     * Static factory method to convert a {@link PersonalAttributeList} into an {@link
     * eu.eidas.auth.commons.attribute.ImmutableAttributeMap}. Only retains/copies the attributes where an
     * attributeDefinition is found in the attributeRegistry
     * <p/>
     *
     * @param copy the instance to copy
     * @param attributeRegistries the attribute registry where the attributes from copy are searched for
     * @return a copy of the given argument
     */
    @Nonnull
    public static ImmutableAttributeMap retainAttrsExistingInRegistry(@Nonnull IPersonalAttributeList copy,
                                                                      @Nonnull
                                                                              AttributeRegistry... attributeRegistries) {
        ImmutableAttributeMap.Builder builder = ImmutableAttributeMap.builder();
        for (final PersonalAttribute personalAttribute : copy) {
            AttributeDefinition<?> attributeDefinition = getByName(personalAttribute.getName(), attributeRegistries);
            if (null != attributeDefinition) {
                if (!personalAttribute.isEmptyValue()) {
                    List<String> personalAttributeValue = personalAttribute.getValue();
                    ImmutableSet<AttributeValue<?>> attributeValues =
                            toAttributeValues(attributeDefinition, personalAttributeValue);
                    builder.put((AttributeDefinition) attributeDefinition, (ImmutableSet) attributeValues);
                } else if (!personalAttribute.isEmptyComplexValue()) {
                    builder.put(attributeDefinition, personalAttribute.getComplexValue().toString());
                } else if (personalAttribute.isEmpty()) {
                    builder.put(attributeDefinition);
                }
            }
        }
        return builder.build();
    }

    @Nullable
    public static PersonalAttributeList fromString(@Nullable String personalAttributeListString) {
        if (null == personalAttributeListString) {
            return null;
        }
        return PersonalAttributeString.fromStringList(personalAttributeListString);
    }

    @Nullable
    public static String toString(@Nullable PersonalAttributeList personalAttributeList) {
        if (null == personalAttributeList) {
            return null;
        }
        return PersonalAttributeString.toStringList(personalAttributeList);
    }

    private final AtomicLong sequence = new AtomicLong(Long.MIN_VALUE);

    /**
     * Do not inherit from CHM but compose it.
     */
    private final ConcurrentMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>> map;

    private NavigableMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>> asNavigableMap() {
        return new TreeMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>>(map);
    }

    private PersonalAttributeList(@Nonnull ConcurrentMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>> map) {
        this.map = map;
    }

    /**
     * Default constructor.
     */
    public PersonalAttributeList() {
        this(new ConcurrentHashMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>>());
    }

    /**
     * Constructor with initial capacity for the PersonalAttributeList size.
     *
     * @param capacity The initial capacity for the PersonalAttributeList.
     */
    public PersonalAttributeList(int capacity) {
        this(new ConcurrentHashMap<CustomKey, ConcurrentLinkedQueue<PersonalAttribute>>(capacity));
    }

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    public Iterator<PersonalAttribute> iterator() {
        final Iterator<ConcurrentLinkedQueue<PersonalAttribute>> outerIterator = map.values().iterator();
        Iterator<PersonalAttribute> personalAttributeIterator = new Iterator<PersonalAttribute>() {

            Iterator<PersonalAttribute> currentInnerIterator;

            @Override
            public boolean hasNext() {
                return (currentInnerIterator != null && currentInnerIterator.hasNext()) || outerIterator.hasNext();
            }

            @Override
            public PersonalAttribute next() {
                if (null == currentInnerIterator || !currentInnerIterator.hasNext()) {
                    currentInnerIterator = outerIterator.next().iterator();

                }
                return currentInnerIterator.next();
            }

            @Override
            public void remove() {
                if (null != currentInnerIterator) {
                    currentInnerIterator.remove();
                }
                if (null != currentInnerIterator && !currentInnerIterator.hasNext() && null != outerIterator) {
                        outerIterator.remove();
                }
            }
        };
        return personalAttributeIterator;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public PersonalAttribute getByFriendlyName(@Nonnull String friendlyName) {
        Collection<PersonalAttribute> values = values();
        if (null == values) {
            return null;
        }
        for (final PersonalAttribute value : values) {
            if (value.getFriendlyName().equals(friendlyName)) {
                return value;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Collection<PersonalAttribute> values(@Nullable String key) {
        if (null == key) {
            return null;
        }
        ConcurrentLinkedQueue<PersonalAttribute> personalAttributes = map.get(asRetrievalKey(key));
        if (null == personalAttributes || personalAttributes.isEmpty()) {
            return null;
        }
        return personalAttributes;
    }

    private AbstractKey asRetrievalKey(String attrName) {
        return new RetrievalKey(attrName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(@Nullable PersonalAttribute value) {
        if (null == value) {
            return false;
        }
        String key = value.getName();
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        ConcurrentLinkedQueue<PersonalAttribute> existingList = map.get(asRetrievalKey(key));
        if (null == existingList) {
            ConcurrentLinkedQueue<PersonalAttribute> newList = new ConcurrentLinkedQueue<PersonalAttribute>();
            newList.offer(value);
            existingList = map.putIfAbsent(new CustomKey(sequence.getAndIncrement(), key), newList);
            if (null == existingList) {
                return true;
//            } else {
//                // already inserted in another thread
//                sequence.decrementAndGet();
            }
        }
        return existingList.offer(value);
    }

    @Override
    @SuppressWarnings("squid:S2250")
    public int size() {
        int size = 0;
        for (final ConcurrentLinkedQueue<PersonalAttribute> personalAttributes : map.values()) {
            size += personalAttributes.size();
        }
        return size;
    }

    @Override
    public boolean containsFriendlyName(String friendlyName) {
        return null != getByFriendlyName(friendlyName);
    }

    @Override
    public PersonalAttribute getFirst(PersonalAttribute personalAttribute) {
        if (null == personalAttribute) {
            return null;
        }
        String key = personalAttribute.getName();
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        ConcurrentLinkedQueue<PersonalAttribute> personalAttributes = map.get(asRetrievalKey(key));
        if (null == personalAttributes) {
            return null;
        }
        return personalAttributes.peek();
    }

    @Override
    public boolean contains(PersonalAttribute personalAttribute) {
        if (null == personalAttribute) {
            return false;
        }
        String key = personalAttribute.getName();
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        return map.containsKey(asRetrievalKey(key));
    }

    @Nullable
    @Override
    public PersonalAttribute removeByFriendlyName(@Nonnull String friendlyName) {
        for (Iterator<ConcurrentLinkedQueue<PersonalAttribute>> i = map.values().iterator(); i.hasNext(); ) {
            ConcurrentLinkedQueue<PersonalAttribute> queue = i.next();
            PersonalAttribute personalAttribute = queue.peek();
            if (null != personalAttribute && personalAttribute.getFriendlyName().equals(friendlyName)) {
                i.remove();
                return personalAttribute;
            }
        }
        return null;
    }

    @Override
    public Collection<PersonalAttribute> values() {
        Collection<ConcurrentLinkedQueue<PersonalAttribute>> values = asNavigableMap().values();
        ImmutableList.Builder<PersonalAttribute> builder = new ImmutableList.Builder<PersonalAttribute>();
        for (final ConcurrentLinkedQueue<PersonalAttribute> value : values) {
            builder.addAll(value);
        }
        return builder.build();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersonalAttributeList that = (PersonalAttributeList) o;

        return map != null ? map.equals(that.map) : that.map == null;

    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }

    /**
     * Use {@link #toString(PersonalAttributeList)} instead.
     */
    @Nonnull
    @Override
    public String toString() {
        return PersonalAttributeString.toStringList(this);
    }
}
