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
