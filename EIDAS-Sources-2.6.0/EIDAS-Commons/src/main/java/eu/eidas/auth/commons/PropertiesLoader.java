/*
 * Copyright (c) 2021 by European Commission
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
 */
package eu.eidas.auth.commons;

import eu.eidas.auth.commons.exceptions.InternalErrorEIDASException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class used to dynamically load resources.
 *
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
     * Loads the properties defined in an xml file with the format &lt;//?xml version="1.0" encoding="UTF-8"
     * standalone="no"?&gt; &lt;//!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd"&gt; &lt;properties&gt;
     * &lt;comment&gt;Comment&lt;/comment&gt; &lt;entry key="keyName"&gt;Some Value&lt;/entry&gt; &lt;/properties&gt;
     *
     * @param xmlFilePath the file path
     * @return Object @Properties
     */
    @Nonnull
    public static Properties loadPropertiesXMLFile(@Nonnull String xmlFilePath) {
        Properties properties;
        try {
            if (StringUtils.isEmpty(xmlFilePath) || !StringUtils.endsWith(xmlFilePath, "xml")) {
                throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                        EidasErrorKey.INTERNAL_ERROR.errorMessage(), "Not valid file!");
            }

            properties = new Properties();
            ClassLoader classLoader = PropertiesLoader.class.getClassLoader();
            final InputStream inputStream = classLoader.getResourceAsStream(xmlFilePath);

            properties.loadFromXML(inputStream);

            return properties;
        } catch (InternalErrorEIDASException e) {
            LOG.error("ERROR : " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOG.error("ERROR : " + e.getMessage());
            throw new InternalErrorEIDASException(EidasErrorKey.INTERNAL_ERROR.errorCode(),
                    EidasErrorKey.INTERNAL_ERROR.errorMessage(), e);
        }
    }
}
