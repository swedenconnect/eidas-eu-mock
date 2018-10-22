/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
public class PropertiesBasedFileMarshaller<T> implements FileMarshaller<T> {

    @Nonnull
    private final PropertiesConverter<T> propertiesConverter;

    public PropertiesBasedFileMarshaller(@Nonnull PropertiesConverter<T> propsConverter) {
        Preconditions.checkNotNull(propsConverter, "propertiesConverter");
        propertiesConverter = propsConverter;
    }

    @Override
    public final void marshal(@Nonnull T newValue, @Nonnull File output) throws IOException {
        PropertiesFormat format = PropertiesFormat.getFormat(output);
        Properties properties = propertiesConverter.marshal(newValue);
        try (OutputStream fos = new BufferedOutputStream(new FileOutputStream(output))) {
            if (PropertiesFormat.PROPERTIES == format) {
                properties.store(fos, null);
            } else {
                properties.storeToXML(fos, null);
            }
        }
    }

    @Override
    public final T unmarshal(@Nonnull File input) throws IOException {
        PropertiesFormat format = PropertiesFormat.getFormat(input);
        Properties properties = new PrintSortedProperties();
        try (InputStream fis = new BufferedInputStream(new FileInputStream(input))) {
            if (PropertiesFormat.PROPERTIES == format) {
                properties.load(fis);
            } else {
                properties.loadFromXML(fis);
            }
        }
        return propertiesConverter.unmarshal(properties);
    }
}
