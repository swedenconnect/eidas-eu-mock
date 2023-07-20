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
package eu.eidas.SimpleProtocol.utils;

import eu.eidas.SimpleProtocol.adapter.DateAdapter;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Converts to and from Json.
 */
public class SimpleProtocolProcess {

    /**
     * Convert a JSON String into the specified type
     *
     * @param jSonRequestString the json {@link String} to be converted
     * @param c the java class used as the target of the unmarshalling operation.
     * @param <T> the expected type used as the target of the unmarshalling operation.
     * @return the new instance of class T converted from the JSON String
     * @throws JAXBException If any unexpected errors occur while unmarshalling
     *
     */
    public <T> T convertFromJson(StringReader jSonRequestString, Class<T> c) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(c);

        // Create an instance of Date adapter what marshals/unmarshals into yyyy-mm-dd (only date part)
        DateAdapter dateAdapter = new DateAdapter();

        // Create an unmarshaller
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        // Setup for unmarshaller
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_INCLUDE_ROOT, true);
        // Add the adapter class
        unmarshaller.setAdapter(dateAdapter);

        final T unmarshal = (T) unmarshaller.unmarshal(jSonRequestString);
        return unmarshal;
    }

    /**
     * Marshall an object into a JSON String
     *
     * @param object The root of content tree to be marshalled.
     * @return The Marshalled JSON String
     * @throws JAXBException If any unexpected problem occurs during the marshalling.
     */
    public String convert2Json(Object object) throws JAXBException {

        // Introduce domain model classes to JAXB
        JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
        // Create a marshaller
        Marshaller marshaller = jaxbContext.createMarshaller();
        // Create an instance of Date adapter what marshals/unmarshals into yyyy-mm-dd (only date part)
        DateAdapter dateAdapter = new DateAdapter();
        java.io.StringWriter sw = new StringWriter();

        // Setup the marshaller
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
        marshaller.setProperty(MarshallerProperties.JSON_INCLUDE_ROOT, true);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // Add the adapter class
        marshaller.setAdapter(dateAdapter);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.marshal(object, sw);

        final String jSonString = sw.toString();
        return jSonString;
    }
}
