/*
 * Copyright (c) 2017 by European Commission
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

public class SimpleProtocolProcess {


    public <T> T convertFromJson(StringReader jSonRequestString, Class c) throws JAXBException {

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

        return (T) unmarshaller.unmarshal(jSonRequestString);

        // The AuthenticationRequest object is ready at this point !


    }
    public String convert2Json(Object object) throws JAXBException {
        // Now the other direction: marshall an existing (example) object to JSON to the screen

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

        return sw.toString();

        // The AuthenticationRequest JSON ready at this point !
/*
        System.out.println("\n-------------------------response------------------------");

        // Now reuse the marshaller and unmarshaller with the example response file, in production
        // check thread safety before implementing!


        // Create an unmarshaller
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        StringReader jSonResponseString = new StringReader("{    \"response\" : {        \"version\" : \"1\",        \"id\" : \"0a88c46e-24a7-4194-90f1-35485977bb18\",        \"inresponse_to\" : \"e7d5db08-0818-449f-bec2-d257bf9593d7\",        \"created_on\" : \"2012-04-23T18:28:43.511Z\",        \"authentication_context_class\" : \"high\",        \"issuer\" : \"DEMO-IDP\",        \"status\" : {            \"status_code\" : \"success\",            \"sub_status_code\" : \"AuthnFailed\",            \"status_message\" : \"all hands on deck\"        },        \"attribute_list\" : [            {                \"name\" : \"gender\",                \"type\" : \"string\",                \"value\" : \"Male\"            },            {                \"name\" : \"birth_name\",                \"type\" : \"string_list\",                \"values\" : [                \t{                        \"latin_script\" : \"false\",                        \"value\" : \"Árvíztûrõ Tükörfúrógép\"                    },                    {                        \"value\" : \"Arvizturo Tukorfurogep\"                    }                                   ]            },            {                \"name\" : \"date_of_birth\",                \"type\" : \"date\",                \"value\" : \"1905-04-20\"            }, \t\t\t{                \"name\" : \"current_address\",                \"type\" : \"addressId\",                \"value\" : {                    \"addressId\" : \"Rue Belliard  28\",                    \"po_box\" : \"1234\",                    \"locator_designator\" : \"2\",                    \"locator_name\" : \"Floor\",                    \"cv_address_area\" : \"Etterbeek\",                    \"thoroughfare\" : \"N23\",                    \"post_name\" : \"ETTERBEEK CHASSE\",                    \"admin_unit_first_line\" : \"ETTERBEEK\",                    \"admin_unit_second_line\" :  \"BE\",                    \"post_code\" : \"1040\",                    \"full_cvaddress\" : \"Rue Belliard  28\\nFloor 2\\nBE-1040 Etterbeek\"                }            }                    ]            }}");
        Response responseElement = (Response) unmarshaller.unmarshal(jSonResponseString);
        //Response response = responseElement.getValue();
        marshaller.marshal(responseElement, System.out);*/

    }
}
