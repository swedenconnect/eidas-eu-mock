package se.swedenconnect.eidas.test.cef20demohub.controller;

import eu.eidas.SimpleProtocol.Attribute;
import eu.eidas.SimpleProtocol.DateAttribute;
import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.StringListAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.swedenconnect.eidas.test.cef20demohub.data.*;
import se.swedenconnect.eidas.test.cef20demohub.process.RequestGenerator;
import se.swedenconnect.eidas.test.cef20demohub.process.ResponseParser;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RequestController {

    private final RequestGenerator requestGenerator;
    private final ResponseParser responseParser;

    @Autowired
    public RequestController(RequestGenerator requestGenerator, ResponseParser responseParser) {
        this.requestGenerator = requestGenerator;
        this.responseParser = responseParser;
    }

    @RequestMapping("/sp")
    public String getRequestPage(Model model) {
        model.addAttribute("naturalAttr", Arrays.asList(EidasNaturalAttributeFriendlyName.values()));
        model.addAttribute("legalAttr", Arrays.asList(EidasLegalAttributeFriendlyName.values()));
        model.addAttribute("countryList", getCountryList());
        model.addAttribute("loaList", DemoLevelOfAssurance.getList());
        model.addAttribute("loaComparisonList", Arrays.asList(LevelOfAssuranceComparison.values()));
        model.addAttribute("spTypeList", Arrays.asList(SpType.values()));
        model.addAttribute("nameIdTypeList", Arrays.asList(RequestModel.UNSPECIFIED, RequestModel.PERSISTENT, RequestModel.TRANSIENT));
        return "request";
    }

    @RequestMapping("/request")
    public String getRequest(Model model, HttpServletRequest httpRequest) throws JAXBException {
        Map<String, String[]> parameterMap = httpRequest.getParameterMap();
        String request = getJsonRequest(parameterMap);
        List<FormPostData> formData = new ArrayList<>();
        formData.add(new FormPostData("SMSSPRequest", request, FormPostDataType.textArea));
        formData.add(new FormPostData("sendmethods", "POST", FormPostDataType.input));
        model.addAttribute("targetUrl", "http://localhost:8900/SpecificConnector/ServiceProvider");
        model.addAttribute("fpDataList", formData);
        return "formpost";

    }


    @RequestMapping("/return")
    public String getReturn(Model model, @RequestParam("SMSSPResponse") String b64Response) throws JAXBException {
        String jsonResponse = new String(Base64.getDecoder().decode(b64Response), StandardCharsets.UTF_8);
        Response response = responseParser.parseResponse(jsonResponse);
        List<ValueAttribute> attributeList = getAttributeList(response);
        model.addAttribute("jsonResponse", jsonResponse);
        model.addAttribute("response", response);
        model.addAttribute("loa", getLoa(response));
        model.addAttribute("attrList", attributeList);
        return "result";
    }

    private List<ValueAttribute> getAttributeList(Response response) {

        return response.getAttributes().stream()
                .map(attribute -> {
                    if (attribute instanceof StringListAttribute) {
                        StringListAttribute strlAttr = (StringListAttribute) attribute;
                        return new ValueAttribute(strlAttr.getName(), strlAttr.getValues().get(0).getValue());
                    }
                    if (attribute instanceof DateAttribute) {
                        DateAttribute dateAttr = (DateAttribute) attribute;
                        return new ValueAttribute(dateAttr.getName(), dateAttr.getValue().toString());
                    }
                    return new ValueAttribute(attribute.getName(), "#null");
                }).collect(Collectors.toList());
    }

    private String getLoa(Response response) {
        switch (response.getAuthContextClass()) {
            case "A":
            case "B":
                return "http://eidas.europa.eu/LoA/low";
            case "C":
            case "D":
                return "http://eidas.europa.eu/LoA/substantial";
            case "E":
                return "http://eidas.europa.eu/LoA/high";
            default:
                return response.getAuthContextClass();
        }

    }

    private List<CitizenCountry> getCountryList() {
        List<CitizenCountry> countryList = new ArrayList<>();
        countryList.add(new CitizenCountry("SE", getCountryImage("img/flags/SE.png"), "Sweden"));
        countryList.add(new CitizenCountry("XX", getCountryImage("img/flags/EU.png"), "Test Country CEF node Version 2.0"));
        countryList.add(new CitizenCountry("XY", getCountryImage("img/flags/EU.png"), "Test Country CEF node Version 2.0"));
        return countryList;
    }

    private String getCountryImage(String imgUrl) {
        String imgStr = "<img src='" + imgUrl + "'>";
        return imgStr;
    }

    private String getJsonRequest(Map<String, String[]> parameterMap) throws JAXBException {
        RequestModel rm = new RequestModel();
        //rm.setReturnUrl("http://localhost:8900/SP/ReturnPage");
        rm.setReturnUrl("http://localhost:8080/cef20-apps/return");
        rm.setCitizenCountry("SE");
        rm.setEidasloa("A");
        rm.setEidasloaCompareType(LevelOfAssuranceComparison.MINIMUM);
        rm.setEidasNameIdentifier(RequestModel.UNSPECIFIED);
        rm.setEidasSPType(SpType.PUBLIC);
        rm.setProviderName("Demo SP for country XA");

        List<Attribute> simpleAttributes = requestGenerator.getAttributeList(parameterMap);

        rm.setSimpleAttributes(simpleAttributes);
        RequestData request = requestGenerator.getRequest(rm);
        return request.getBase64Request();
    }

    private Attribute getAttribute(EidasNaturalAttributeFriendlyName attributeFriendlyName) {
        return getAttribute(attributeFriendlyName, false);
    }

    private Attribute getAttribute(EidasNaturalAttributeFriendlyName attributeFriendlyName, boolean required) {
        return getAttribute(attributeFriendlyName, required, false);
    }

    private Attribute getAttribute(EidasNaturalAttributeFriendlyName attributeFriendlyName, boolean required, boolean representative) {
        Attribute attribute = new Attribute();
        attribute.setName(attributeFriendlyName.getFrendlyName(representative));
        attribute.setRequired(required);
        return attribute;
    }
}
