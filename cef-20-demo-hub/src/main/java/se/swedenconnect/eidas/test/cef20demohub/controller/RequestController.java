package se.swedenconnect.eidas.test.cef20demohub.controller;

import eu.eidas.SimpleProtocol.Response;
import eu.eidas.SimpleProtocol.ResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.xml.bind.JAXBException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;
import se.swedenconnect.eidas.test.cef20demohub.data.*;
import se.swedenconnect.eidas.test.cef20demohub.process.GeneralUtils;
import se.swedenconnect.eidas.test.cef20demohub.process.RequestGenerator;
import se.swedenconnect.eidas.test.cef20demohub.process.ResponseParser;

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

    @Value("${name-id-select}") boolean nameIdSelect;
    @Value("${name-id-default}") String nameIdDefault;

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
        model.addAttribute("appVersion", spConfig.getCefVersion());
        model.addAttribute("spName", spConfig.getName());
        model.addAttribute("naturalAttr", Arrays.asList(EidasNaturalAttributeFriendlyName.values()));
        model.addAttribute("legalAttr", Arrays.asList(EidasLegalAttributeFriendlyName.values()));
        model.addAttribute("countryList", utils.getCountryList(spConfig));
        model.addAttribute("loaList", DemoLevelOfAssurance.getList());
        model.addAttribute("nonNotified", spConfig.isNonNotified());
        model.addAttribute("loaComparisonList", Arrays.asList(LevelOfAssuranceComparison.values()));
        model.addAttribute("spTypeList", Arrays.asList(SpType.values()));
        model.addAttribute("nameIdTypeList", Arrays.asList(RequestModel.UNSPECIFIED, RequestModel.PERSISTENT, RequestModel.TRANSIENT));
        model.addAttribute("nameIdSelect", nameIdSelect);
        model.addAttribute("nameIdDefault", nameIdDefault);
        return "sc-request";
    }

    @RequestMapping("/request/**")
    public String getRequest(Model model, HttpServletRequest request, @RequestParam(required = false) String nidFormat) throws
        JAXBException {
        Map<String, String[]> parameterMap = request.getParameterMap();
        nidFormat = nidFormat == null ? nameIdDefault : nidFormat;
        String spCountry = utils.getCountry(request);
        final SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
        RequestData requestData = requestGenerator.getRequest(parameterMap, spConfigurationProperties.getBaseReturnUrl() + spCountry, spConfig.getName(), nidFormat);
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
        model.addAttribute("appVersion", spConfig.getCefVersion());
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
            return "sc-result";
        } else {
            model.addAttribute("errorMessage", status.getStatusMessage());
            return "sc-errorResponse";
        }
    }

}
