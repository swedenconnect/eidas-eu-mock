package se.swedenconnect.eidas.test.cef20demohub.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.eidas.SimpleProtocol.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.swedenconnect.eidas.test.cef20demohub.data.*;
import se.swedenconnect.eidas.test.cef20demohub.process.RequestGenerator;
import se.swedenconnect.eidas.test.cef20demohub.process.ResponseParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class RequestController {

    private final RequestGenerator requestGenerator;
    private final ResponseParser responseParser;
    private final HttpSession httpSession;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    public RequestController(RequestGenerator requestGenerator, ResponseParser responseParser, HttpSession httpSession) {
        this.requestGenerator = requestGenerator;
        this.responseParser = responseParser;
        this.httpSession = httpSession;
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
        RequestData requestData = requestGenerator.getRequest(parameterMap, "http://localhost:8080/cef20-apps/return", "Demo SP for country XA");
        httpSession.setAttribute("requestData", requestData);
        String request = requestData.getBase64Request();
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
        ResponseStatus status = response.getStatus();
        if (status.getStatusCode().equalsIgnoreCase("success")) {
            //Retrieve request
            RequestData requestData = (RequestData) httpSession.getAttribute("requestData");
            String jsonRequest = new String(Base64.getDecoder().decode(requestData.getBase64Request()), StandardCharsets.UTF_8);

            List<ValueAttribute> attributeList = getAttributeList(response);
            model.addAttribute("jsonRequest", jsonRequest);
            model.addAttribute("jsonResponse", jsonResponse);
            model.addAttribute("response", response);
            model.addAttribute("loa", getLoa(response));
            model.addAttribute("attrList", attributeList);
            return "result";
        } else {
            model.addAttribute("errorMessage", status.getStatusMessage());
            return "errorResponse";
        }
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
                    if (attribute instanceof AddressAttribute) {
                        AddressAttribute addressAttr = (AddressAttribute) attribute;
                        ComplexAddressAttribute addressAttrValue = addressAttr.getValue();
                        return new ValueAttribute(addressAttr.getName(), preCode(GSON.toJson(addressAttrValue)));
                    }
                    return new ValueAttribute(attribute.getName(), "#null");
                }).collect(Collectors.toList());
    }

    private String preCode(String jsonString) {
        return "<pre><code class='json'>"+jsonString+"</code></pre>";
    }

    private String getLoa(Response response) {
        Optional<DemoLevelOfAssurance> loaFromDemoLevel = DemoLevelOfAssurance.getLoaFromDemoLevel(response.getAuthContextClass());
        return loaFromDemoLevel.isPresent() ? loaFromDemoLevel.get().getUri() : response.getAuthContextClass();
    }

    private List<CitizenCountry> getCountryList() {
        List<CitizenCountry> countryList = new ArrayList<>();
        countryList.add(new CitizenCountry("SE", getCountryImage("img/flags/SE.png"), "Sweden"));
        countryList.add(new CitizenCountry("XA", getCountryImage("img/flags/EU.png"), "Test Country XA - CEF node Version 2.0"));
        countryList.add(new CitizenCountry("XB", getCountryImage("img/flags/EU.png"), "Test Country XB - CEF node Version 2.0"));
        return countryList;
    }

    private String getCountryImage(String imgUrl) {
        return "<img src='" + imgUrl + "'>";
    }

}
