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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Marshals and unmarshals one instance of a type T to an OutputStream and from a InputStream.
 *
 * @param <T> the type converted from the file and persisted into the file.
 * @since 1.1
 */
public interface StreamMarshaller<T> {

    /**
     * Marshals the given newValue into a file.
     *
     * @param newValue the value to marshal into the file
     * @param output the destination file
     * @throws IOException
     */
    void marshal(@Nonnull T newValue, @Nonnull OutputStream output) throws IOException;

    /**
     * Unmarshals the given file into an instance of T.
     *
     * @param uri the URI of the file to unmarshal
     * @param input the file to unmarshal
     * @return a new instance of T unmarshalled from the given file.
     * @throws IOException
     */
    T unmarshal(@Nonnull InputStream input) throws IOException;
}
