package se.swedenconnect.eidas.test.cef20demohub.controller;

import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.ResponseStatus;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;
import se.swedenconnect.eidas.test.cef20demohub.data.*;
import se.swedenconnect.eidas.test.cef20demohub.process.GeneralUtils;
import se.swedenconnect.eidas.test.cef20demohub.process.RequestGenerator;
import se.swedenconnect.eidas.test.cef20demohub.process.ResponseParser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@Log4j2
public class RequestController {

    private final RequestGenerator requestGenerator;
    private final ResponseParser responseParser;
    private final HttpSession httpSession;
    private final SPConfigurationProperties spConfigurationProperties;
    private final GeneralUtils utils;

    @Autowired
    public RequestController(RequestGenerator requestGenerator, ResponseParser responseParser, HttpSession httpSession, SPConfigurationProperties spConfigurationProperties, GeneralUtils utils) {
        this.requestGenerator = requestGenerator;
        this.responseParser = responseParser;
        this.httpSession = httpSession;
        this.spConfigurationProperties = spConfigurationProperties;
        this.utils = utils;
    }

    @RequestMapping("/sp/**")
    public String getRequestPage(Model model, HttpServletRequest request) {
        String spCountry = utils.getCountry(request);
        final SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
        model.addAttribute("spCountry", spCountry);
        model.addAttribute("spName", spConfig.getName());
        model.addAttribute("naturalAttr", Arrays.asList(EidasNaturalAttributeFriendlyName.values()));
        model.addAttribute("legalAttr", Arrays.asList(EidasLegalAttributeFriendlyName.values()));
        model.addAttribute("countryList", utils.getCountryList(spConfig));
        model.addAttribute("loaList", DemoLevelOfAssurance.getList());
        model.addAttribute("loaComparisonList", Arrays.asList(LevelOfAssuranceComparison.values()));
        model.addAttribute("spTypeList", Arrays.asList(SpType.values()));
        model.addAttribute("nameIdTypeList", Arrays.asList(RequestModel.UNSPECIFIED, RequestModel.PERSISTENT, RequestModel.TRANSIENT));
        return "request";
    }

    @RequestMapping("/request/**")
    public String getRequest(Model model, HttpServletRequest request) throws JAXBException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        String spCountry = utils.getCountry(request);
        final SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
        RequestData requestData = requestGenerator.getRequest(parameterMap, spConfigurationProperties.getBaseReturnUrl() + spCountry, spConfig.getName());
        httpSession.setAttribute("requestData", requestData);
        String jsonRequest = requestData.getBase64Request();
        List<FormPostData> formData = new ArrayList<>();
        formData.add(new FormPostData("SMSSPRequest", jsonRequest, FormPostDataType.textArea));
        formData.add(new FormPostData("sendmethods", "POST", FormPostDataType.input));
        model.addAttribute("targetUrl", spConfig.getRequestUrl());
        model.addAttribute("fpDataList", formData);
        return "formpost";

    }


    @RequestMapping("/return/**")
    public String getReturn(Model model, @RequestParam("SMSSPResponse") String b64Response, HttpServletRequest request) throws JAXBException {
        String spCountry = utils.getCountry(request);
        final SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
        model.addAttribute("spCountry", spCountry);
        model.addAttribute("spName", spConfig.getName());
        String jsonResponse = new String(Base64.getDecoder().decode(b64Response), StandardCharsets.UTF_8);
        Response response = responseParser.parseResponse(jsonResponse);
        ResponseStatus status = response.getStatus();
        if (status.getStatusCode().equalsIgnoreCase("success")) {
            //Retrieve request
            RequestData requestData = (RequestData) httpSession.getAttribute("requestData");
            String jsonRequest = new String(Base64.getDecoder().decode(requestData.getBase64Request()), StandardCharsets.UTF_8);

            List<ValueAttribute> attributeList = utils.getAttributeList(response);
            model.addAttribute("jsonRequest", jsonRequest);
            model.addAttribute("jsonResponse", jsonResponse);
            model.addAttribute("response", response);
            model.addAttribute("loa", utils.getLoa(response));
            model.addAttribute("attrList", attributeList);
            return "result";
        } else {
            model.addAttribute("errorMessage", status.getStatusMessage());
            return "errorResponse";
        }
    }

}
