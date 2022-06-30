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
import java.util.Properties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

import eu.eidas.util.Preconditions;

/**
 * A ReloadableProperties implementation, each time {@link #getProperties()} is invoked, properties are checked for
 * modifications and can be reloaded.
 *
 * @since 1.1
 */
@Immutable
@ThreadSafe
public final class ReloadableProperties {

    @Nonnull
    private final SingletonAccessor<Properties> accessor;

    /**
     * Creates a new ReloadableProperties instance.
     *
     * @param fileName the name of the properties file.
     */
    public ReloadableProperties(@Nonnull String fileName, @Nullable String defaultPath) {
        Preconditions.checkNotNull(fileName, "fileName");
        accessor = SingletonAccessors.newPropertiesAccessor(fileName, defaultPath, PropertiesConverter.IdemConverter.INSTANCE);
    }

    /**
     * Returns the current snapshot of the Properties.
     * <p>
     * Do not keep a reference to the returned object as it is not reloadable itself.
     *
     * @return the current snapshot of the Properties.
     * @throws IOException if the properties cannot be reloaded
     */
    @Nonnull
    public Properties getProperties() throws IOException {
        return accessor.get();
    }

    public void setProperties(@Nonnull Properties newValue) throws IOException {
        Preconditions.checkNotNull(newValue, "newValue");
        accessor.set(newValue);
    }
}
