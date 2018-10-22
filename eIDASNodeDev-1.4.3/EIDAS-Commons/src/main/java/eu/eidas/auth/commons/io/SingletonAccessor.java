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

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Accessor and mutator of a singleton instance.
 *
 * @since 1.1
 */
public interface SingletonAccessor<T> {

    /**
     * Retrieves the current value.
     *
     * @return the current value.
     * @throws IOException if an exception happens while retrieving the value.
     */
    @Nullable
    T get() throws IOException;

    /**
     * Sets the new value.
     *
     * @param newValue the new value to set.
     * @throws IOException if an exception happens while setting the value.
     */
    void set(@Nonnull T newValue) throws IOException;
}
