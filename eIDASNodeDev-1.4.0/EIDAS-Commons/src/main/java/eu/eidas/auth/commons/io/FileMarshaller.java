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

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Marshals and unmarshals a type T to and from a File.
 *
 * @param <T> the type converted from the file and persisted into the file.
 * @since 1.1
 */
public interface FileMarshaller<T> {

    /**
     * Marshals the given newValue into a file.
     *
     * @param newValue the value to marshal into the file
     * @param output the destination file
     * @throws IOException
     */
    void marshal(@Nonnull T newValue, @Nonnull File output) throws IOException;

    /**
     * Unmarshals the given file into an instance of T.
     *
     * @param input the file to unmarshal
     * @return a new instance of T unmarshalled from the given file.
     * @throws IOException
     */
    T unmarshal(@Nonnull File input) throws IOException;
}
