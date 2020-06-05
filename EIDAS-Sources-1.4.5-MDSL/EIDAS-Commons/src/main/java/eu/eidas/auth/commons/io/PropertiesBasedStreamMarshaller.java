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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.annotation.Nonnull;

import eu.eidas.auth.commons.collections.PrintSortedProperties;
import eu.eidas.util.Preconditions;

/**
 * PropertiesBasedFileMarshaller
 *
 * @since 1.1
 */
public final class PropertiesBasedStreamMarshaller<T> implements StreamMarshaller<T> {

    private final boolean useXml;

    @Nonnull
    private final PropertiesConverter<T> propertiesConverter;

    public PropertiesBasedStreamMarshaller(@Nonnull PropertiesFormat propertiesFormat,
                                           @Nonnull PropertiesConverter<T> propsConverter) {
        Preconditions.checkNotNull(propertiesFormat, "propertiesFormat");
        Preconditions.checkNotNull(propsConverter, "propertiesConverter");
        this.useXml = PropertiesFormat.XML == propertiesFormat;
        propertiesConverter = propsConverter;
    }

    public PropertiesBasedStreamMarshaller(boolean useXml, @Nonnull PropertiesConverter<T> propsConverter) {
        Preconditions.checkNotNull(propsConverter, "propertiesConverter");
        this.useXml = useXml;
        propertiesConverter = propsConverter;
    }

    @Override
    public final void marshal(@Nonnull T newValue, @Nonnull OutputStream output) throws IOException {
        Properties properties = propertiesConverter.marshal(newValue);
        try (OutputStream out = new BufferedOutputStream(output)) {
            if (!useXml) {
                properties.store(out, null);
            } else {
                properties.storeToXML(out, null);
            }
        }
    }

    @Override
    public final T unmarshal(@Nonnull InputStream input) throws IOException {
        Properties properties = new PrintSortedProperties();
        try (InputStream in = new BufferedInputStream(input)) {
            if (!useXml) {
                properties.load(in);
            } else {
                properties.loadFromXML(in);
            }
        }
        return propertiesConverter.unmarshal(properties);
    }
}
