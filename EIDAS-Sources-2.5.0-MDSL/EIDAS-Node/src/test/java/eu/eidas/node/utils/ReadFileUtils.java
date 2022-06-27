/*
 *
 *  Copyright (c) 2019 by European Commission
 *
 *  Licensed under the EUPL, Version 1.2 or - as soon they will be
 *  approved by the European Commission - subsequent versions of the
 *  EUPL (the "Licence");
 *  You may not use this work except in compliance with the Licence.
 *  You may obtain a copy of the Licence at:
 *  https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licence is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.
 *  See the Licence for the specific language governing permissions and
 *  limitations under the Licence
 *
 */

package eu.eidas.node.utils;

import eu.eidas.auth.commons.xml.DocumentBuilderFactoryUtil;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

/**
 * Test helper class with file reading features
 *
 * @since 2.4
 */
public class ReadFileUtils {

    /**
     * Read text file and convert it into a byte array
     * @return file content as a byte array.
     */
    public static byte[] readFileAsByteArray(String fileName) {
        ClassLoader classLoader = ReadFileUtils.class.getClassLoader();
        try(InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            df.setIgnoringComments(true);
            DocumentBuilder builder = df.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(data)));
            document.getDocumentElement().normalize();

            return DocumentBuilderFactoryUtil.marshall(document, true);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
