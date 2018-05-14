package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import eu.eidas.auth.commons.attribute.AttributeDefinition;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.Map;
import java.util.Set;

@Component
public class ResponseParser {

    public Response parseResponse(String b64Response) throws JAXBException {
        Response response = null;
        String jsonResponseStatus = null;
        Map<AttributeDefinition<?>, Set<String>> jsonAttributes = null;

        SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();

        StringReader stringReaderJson = new StringReader(b64Response);
        response = simpleProtocolProcess.convertFromJson(stringReaderJson, Response.class);

        return response;
    }

}
