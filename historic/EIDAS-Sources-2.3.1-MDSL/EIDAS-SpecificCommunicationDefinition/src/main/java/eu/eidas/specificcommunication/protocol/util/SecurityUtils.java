/*
 * Copyright (c) 2019 by European Commission
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
 * limitations under the Licence
 */

package eu.eidas.specificcommunication.protocol.util;


import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

/**
 * Util class with methods related to security.
 */
public final class SecurityUtils {

    private static final String LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";

    private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";

    private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    /**
     * Creates a {@link SAXSource} with the security features turned on
     * for the input.
     *
     * @param input the string to be used to create the {@link SAXSource} instance
     * @return the secured {@link SAXSource} instance
     * @throws ParserConfigurationException if a parser cannot
     *     be created which satisfies the requested configuration.
     * @throws SAXNotRecognizedException When the underlying XMLReader does
     *            not recognize the property EXTERNAL_GENERAL_ENTITIES.     */
    @Nonnull
    public static final SAXSource createSecureSaxSource(String input) throws ParserConfigurationException, SAXException {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        // Ignore the external DTD completely
        // Note: this is for Xerces only:
        saxParserFactory.setFeature(LOAD_EXTERNAL_DTD, Boolean.FALSE);
        // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
        // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
        saxParserFactory.setFeature(DISALLOW_DOCTYPE_DECL, Boolean.TRUE);

        // If you can't completely disable DTDs, then at least do the following:
        // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
        // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
        saxParserFactory.setFeature(EXTERNAL_GENERAL_ENTITIES, Boolean.FALSE);

        // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
        // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
        saxParserFactory.setFeature(EXTERNAL_PARAMETER_ENTITIES, Boolean.FALSE);

        SAXParser saxParser = saxParserFactory.newSAXParser();
        StringReader stringReader = new StringReader(input);
        InputSource inputSource = new InputSource(stringReader);
        XMLReader xmlReader = saxParser.getXMLReader();
        SAXSource saxSource = new SAXSource(xmlReader, inputSource);

        return saxSource;
    }

    private SecurityUtils() {
    }
}
