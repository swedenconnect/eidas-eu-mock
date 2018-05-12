package se.swedenconnect.eidas.test.cef20demohub.controller;

import eu.eidas.SimpleProtocol.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.swedenconnect.eidas.test.cef20demohub.data.*;
import se.swedenconnect.eidas.test.cef20demohub.process.RequestGenerator;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class RequestController {

    private final RequestGenerator requestGenerator;

    @Autowired
    public RequestController(RequestGenerator requestGenerator) {
        this.requestGenerator = requestGenerator;
    }

    @RequestMapping("/sp")
    public String getRequestPage() {
        return "request";
    }

    @RequestMapping("/request")
    public String getRequest(Model model) throws JAXBException {
        String request = getJsonRequest();
        List<FormPostData> formData = new ArrayList<>();
        formData.add(new FormPostData("SMSSPRequest", request, FormPostDataType.textArea));
        formData.add(new FormPostData("sendmethods", "POST", FormPostDataType.input));
        model.addAttribute("targetUrl", "http://localhost:8900/SpecificConnector/ServiceProvider");
        model.addAttribute("fpDataList", formData);
        return "formpost";

    }

    private String getJsonRequest() throws JAXBException {
        RequestModel rm = new RequestModel();
        rm.setReturnUrl("http://localhost:8900/SP/ReturnPage");
        rm.setCitizenCountry("SE");
        rm.setEidasloa("A");
        rm.setEidasloaCompareType(LevelOfAssuranceComparison.MINIMUM);
        rm.setEidasNameIdentifier(RequestModel.UNSPECIFIED);
        rm.setEidasSPType(SpType.PUBLIC);
        rm.setProviderName("Demo SP for country XA");

        List<Attribute> simpleAttributes = new ArrayList<>();
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.birthName));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.currentAddress));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.familyName, true));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.firstName, true));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.dateOfBirth, true));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.gender));
        simpleAttributes.add(getAttribute(EidasAttributeFriendlyName.personIdentifier, true));

        rm.setSimpleAttributes(simpleAttributes);
        RequestData request = requestGenerator.getRequest(rm);
        return request.getBase64Request();
    }

    private Attribute getAttribute(EidasAttributeFriendlyName attributeFriendlyName) {
        return getAttribute(attributeFriendlyName, false);
    }

    private Attribute getAttribute(EidasAttributeFriendlyName attributeFriendlyName, boolean required) {
        return getAttribute(attributeFriendlyName, required, false);
    }

    private Attribute getAttribute(EidasAttributeFriendlyName attributeFriendlyName, boolean required, boolean representative) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeFriendlyName.getFrendlyName(representative));
        attribute.setRequired(required);
        return attribute;
    }
}
