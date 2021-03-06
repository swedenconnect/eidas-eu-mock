package se.swedenconnect.eidas.test.cef20demohub.process;

import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.utils.SimpleProtocolProcess;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.StringReader;

@Component
public class ResponseParser {

    public Response parseResponse(String jsonResponse) throws JAXBException {
        Response response = null;

        SimpleProtocolProcess simpleProtocolProcess = new SimpleProtocolProcess();

        StringReader stringReaderJson = new StringReader(jsonResponse);
        response = simpleProtocolProcess.convertFromJson(stringReaderJson, Response.class);

        return response;
    }

}
