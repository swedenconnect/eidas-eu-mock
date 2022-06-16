/*
 * Copyright (c) 2021 by European Commission
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
package eu.eidas.auth.commons;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * Interface for {@link PersonalAttributeList}.
 *
 * @author ricardo.ferreira@multicert.com, renato.portela@multicert.com, luis.felix@multicert.com,
 *         hugo.magalhaes@multicert.com, paulo.ribeiro@multicert.com
 * @version $Revision: 1.16 $, $Date: 2010-11-17 05:15:28 $
 * @see PersonalAttribute
 */
public interface IPersonalAttributeList extends Iterable<PersonalAttribute> {

    /**
     * Returns the first value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param friendlyName whose associated value is to be returned.
     * @return The first value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * @see PersonalAttribute
     * @deprecated use {@link #values(String)} instead.
     */
    @Deprecated
    PersonalAttribute getByFriendlyName(String friendlyName);

    /**
     * Returns the first value to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param personalAttribute whose associated value is to be returned.
     * @return The first value to which the specified key is mapped, or null if this map contains no mapping for the key.
     * @see PersonalAttribute
     */
    PersonalAttribute getFirst(PersonalAttribute personalAttribute);

    /**
     * Returns all the values to which the specified key is mapped, or null if this map contains no mapping for the key.
     *
     * @param key whose associated value is to be returned.
     * @return all the values to which the specified key is mapped, or null if this map contains no mapping for the key.
     * @see PersonalAttribute
     */
    @Nullable
    Collection<PersonalAttribute> values(@Nullable String key);

    /**
     * Adds to the PersonalAttributeList the given PersonalAttribute. It sets the attribute name as the key to the
     * attribute value.
     *
     * @param value PersonalAttribute to add to the PersonalAttributeList
     * @return true if was added, false otherwise
     */
    boolean add(PersonalAttribute value);

    /**
     * Get the size of the Personal Attribute List.
     *
     * @return size of the Personal Attribute List.
     */
    int size();

    /**
     * Checks if the Personal Attribute List contains the given key.
     *
     * @param friendlyName with which the specified value is to be associated.
     * @return true if the Personal Attribute List contains the given key, false otherwise.
     */
    boolean containsFriendlyName(String friendlyName);

    /**
     * Checks if the Personal Attribute List contains the given key.
     *
     * @param personalAttribute the personalAttribute to check.
     * @return true if the Personal Attribute List contains the given key, false otherwise.
     */
    boolean contains(PersonalAttribute personalAttribute);

    /**
     * Removes the mapping for this key from this map if present.
     *
     * @param friendlyName key whose mapping is to be removed from the map.
     * @return previous value associated with specified key, or {@code null} if there was no mapping for key. A
     * {@code null} return can also indicate that the map previously associated {@code null} with the specified key.
     */
    PersonalAttribute removeByFriendlyName(String friendlyName);

    /**
     * Returns a collection view of the values contained in this map. The collection is backed by the map, so changes to
     * the map are reflected in the collection, and vice-versa. The collection supports element removal, which removes
     * the corresponding mapping from this map, via the {@link java.util.Iterator#remove}, {@link java.util.Collection#remove},
     * {@literal removeAll}, {@literal retainAll}, and {@literal clear} operations. It does not support the {@literal add} or
     * {@literal addAll} operations.
     *
     * @return a collection view of the values contained in this map.
     */
    Collection<PersonalAttribute> values();

    /**
     * Returns {@code true} if this map contains no key-value mappings.
     *
     * @return {@code true} if this map contains no key-value mappings.
     */
    boolean isEmpty();
}
