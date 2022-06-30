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
package eu.eidas.auth.commons.io;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Properties;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSortedMap;

import eu.eidas.auth.commons.collections.PrintSortedProperties;

/**
 * Marshals and unmarshals to and from properties.
 *
 * @since 1.1
 */
public interface PropertiesConverter<T> {

    enum IdemConverter implements PropertiesConverter<Properties> {

        INSTANCE;

        @Nonnull
        @Override
        public Properties marshal(@Nonnull Properties value) {
            return value;
        }

        @Nonnull
        @Override
        public Properties unmarshal(@Nonnull Properties properties) {
            return properties;
        }
    }

    enum MapPropertiesConverter implements PropertiesConverter<NavigableMap<String, String>> {

        INSTANCE;

        @Nonnull
        @Override
        public Properties marshal(@Nonnull NavigableMap<String, String> value) {
            Properties properties = new PrintSortedProperties();
            //noinspection UseOfPropertiesAsHashtable
            properties.putAll(value);
            return properties;
        }

        @Nonnull
        @Override
        public NavigableMap<String, String> unmarshal(@Nonnull Properties properties) {
            //noinspection unchecked,rawtypes
            return ImmutableSortedMap.<String, String>copyOf((Map) properties);
        }
    }

    @Nonnull
    Properties marshal(@Nonnull T value);

    @Nonnull
    T unmarshal(@Nonnull Properties properties);
}
