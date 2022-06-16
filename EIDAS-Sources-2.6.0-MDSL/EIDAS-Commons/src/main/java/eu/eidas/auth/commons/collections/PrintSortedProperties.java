/*
 * Copyright (c) 2019 by European Commission
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
 *
 */
package eu.eidas.auth.commons.collections;

import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

/**
 * Sorted Properties for printing.
 * <p>
 * The sets, iterators and enumerations returned by this implementation are {@link
 * Collections#unmodifiableSortedSet(SortedSet) unmodifiable} snapshots of this Properties instance.
 * <p>
 * Changes in this Properties instance are <em>not</em> reflected in the returned sets, iterators and enumerations,
 * which is ok for one-time atomic operations such as printing to a file.
 * <p>
 * Therefore it is not ok to keep a reference to one of the returned sets and expect to observe changes in this
 * Properties instance.
 *
 * @since 1.1
 */
public final class PrintSortedProperties extends Properties {

    private static final long serialVersionUID = 4651455163913124179L;

    @Override
    public synchronized Enumeration<Object> elements() {
        return Collections.enumeration(unmodifiableSortedMap().values());
    }

    @Override
    public synchronized Set<Map.Entry<Object, Object>> entrySet() {
        return unmodifiableSortedMap().entrySet();
    }

    @Override
    public synchronized Set<Object> keySet() {
        return unmodifiableSortedMap().keySet();
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return Collections.enumeration(keySet());
    }

    private synchronized SortedMap<Object, Object> unmodifiableSortedMap() {
        TreeMap<Object, Object> treeMap;
        treeMap = new TreeMap<Object, Object>();
        for (Map.Entry<Object, Object> e : super.entrySet()) {
            treeMap.put(e.getKey(), e.getValue());
        }
        return Collections.unmodifiableSortedMap(treeMap);
    }

    @Override
    public synchronized Collection<Object> values() {
        return unmodifiableSortedMap().values();
    }

    @Override
    public synchronized String toString() {
        return unmodifiableSortedMap().toString();
    }
}
