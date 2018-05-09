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

package eu.eidas.auth.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;

/**
 * Class used to dynamically load resources.
 *
 * @author vanegdi
 * @since 1.2.2
 */
public final class PropertiesLoader {

    /**
     * Logger object.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesLoader.class);

    private PropertiesLoader() {
    }

    /**
     * Loads the properties defined in an xml file with the format <//?xml version="1.0" encoding="UTF-8"
     * standalone="no"?> <//!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"> <properties>
     * <comment>Comment</comment> <entry key="keyName">Some Value</entry> </properties>
     *
     * @param xmlFilePath the file path
     * @return Object @Properties
     */
    @Nonnull
    public static Properties loadPropertiesXMLFile(@Nonnull String xmlFilePath) {
        Properties props;
        InputStream fileProperties = null;
        try {
            if (StringUtils.isEmpty(xmlFilePath) || !StringUtils.endsWith(xmlFilePath, "xml")) {
                throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                      EidasErrorKey.INTERNAL_ERROR.errorMessage(), "Not valid file!");
            }
            props = new Properties();
            fileProperties = new FileInputStream(xmlFilePath);
            //load the xml file into properties format
            props.loadFromXML(fileProperties);
            return props;
        } catch (InternalErrorEIDASException e) {
            LOG.error("ERROR : " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("ERROR : " + e.getMessage());
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                                                  EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        } finally {
            try {
                if (fileProperties != null) {
                    fileProperties.close();
                }
            } catch (IOException ioe) {
                LOG.error("error closing the file: " + ioe, ioe);
            }
        }
    }
}
