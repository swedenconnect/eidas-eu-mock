package se.swedenconnect.eidas.test.cef20demohub.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.eidas.SimpleProtocol.AuthenticationRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;
import se.swedenconnect.eidas.test.cef20demohub.configuration.UserConfiguration;
import se.swedenconnect.eidas.test.cef20demohub.data.DemoLevelOfAssurance;
import se.swedenconnect.eidas.test.cef20demohub.data.FormPostData;
import se.swedenconnect.eidas.test.cef20demohub.data.FormPostDataType;
import se.swedenconnect.eidas.test.cef20demohub.data.ResponseData;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;
import se.swedenconnect.eidas.test.cef20demohub.process.AuthnRequestParser;
import se.swedenconnect.eidas.test.cef20demohub.process.AuthnResponseGenerator;
import se.swedenconnect.eidas.test.cef20demohub.process.GeneralUtils;
import se.swedenconnect.eidas.test.cef20demohub.process.StaticUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@Log4j2
public class IdpController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final GeneralUtils generalUtils;
    private final SPConfigurationProperties spConfigurationProperties;
    private final AuthnRequestParser authnRequestParser;
    private final HttpSession httpSession;
    private final AuthnResponseGenerator responseGenerator;
    private final UserConfiguration userConfiguration;

    @Autowired
    public IdpController(GeneralUtils generalUtils, SPConfigurationProperties spConfigurationProperties, AuthnRequestParser authnRequestParser, HttpSession httpSession, AuthnResponseGenerator responseGenerator, UserConfiguration userConfiguration) {
        this.generalUtils = generalUtils;
        this.spConfigurationProperties = spConfigurationProperties;
        this.authnRequestParser = authnRequestParser;
        this.httpSession = httpSession;
        this.responseGenerator = responseGenerator;
        this.userConfiguration = userConfiguration;
    }

    @RequestMapping("/idp/**")
    public String authenticateUser(Model model, HttpServletRequest request, @RequestParam("SMSSPRequest") String b64request) throws JAXBException {
        String spCountry = generalUtils.getCountry(request);
        AuthenticationRequest authenticationRequest = authnRequestParser.parseAuthnRequest(b64request);
        httpSession.setAttribute("authnRequest", authenticationRequest);

        String reqLoa = "C";
        try {
            reqLoa = authenticationRequest.getAuthContext().getContextClass().get(0);
        } catch (Exception ex) {
            log.warn("Received Authn request contains no requested level of assurance");
        }

        //Map<String, User> testUsers = userConfiguration.getCountryUserMap().get(spCountry);
        List<User> natUsers = userConfiguration.getCountryNatUserListMap().get(spCountry);
        List<User> legalUsers = userConfiguration.getCountryLegalUserListMap().get(spCountry);

        //String jsonUsers = GSON.toJson(testUsers.keySet().stream().map(s -> testUsers.get(s)).collect(Collectors.toList()));
        final SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
        model.addAttribute("spCountry", spCountry);
        model.addAttribute("spName", spConfig.getIdpName());
        model.addAttribute("natUsers", natUsers);
        model.addAttribute("legalUsers", legalUsers);
        model.addAttribute("loaList", DemoLevelOfAssurance.getList());
        model.addAttribute("reqLoa", reqLoa);

        return "authenticate";
    }

    @RequestMapping("/authn/**")
    public String sendAuthnResponse(Model model, HttpServletRequest request) {
        String country = generalUtils.getCountry(request);
        Map<String, String[]> parameterMap = request.getParameterMap();
        AuthenticationRequest authenticationRequest;
        String remoteIpAdress = StaticUtils.getRemoteIpAdress(request);
        ResponseData authnResponse;
        String authenticationRequestServiceUrl = null;
        boolean error = false;
        String errorMessage = "";
        try {
            authenticationRequest = (AuthenticationRequest) httpSession.getAttribute("authnRequest");
            authenticationRequestServiceUrl = authenticationRequest.getServiceUrl();
            authnResponse = responseGenerator.getAuthnResponse(parameterMap, authenticationRequest, remoteIpAdress, country);
        } catch (Exception e) {
            authnResponse = new ResponseData();
            authnResponse.setError(true);
            authnResponse.setErrorMessageTitle("Authentication failed");
            authnResponse.setErrorMessage(e.getMessage());
            log.warn("Authentication request failed: {}", e.getMessage());
        }

        if (authenticationRequestServiceUrl == null) {
            throw new IllegalArgumentException("Authentication response url not specified in request");
        }

        List<FormPostData> formData = new ArrayList<>();
        formData.add(new FormPostData("SMSSPResponse", authnResponse.getB64Response(), FormPostDataType.textArea));
        formData.add(new FormPostData("errorMessage", authnResponse.getErrorMessage(), FormPostDataType.input));
        formData.add(new FormPostData("errorMessageTitle", authnResponse.getErrorMessageTitle(), FormPostDataType.input));

        model.addAttribute("targetUrl", authenticationRequestServiceUrl);
        model.addAttribute("fpDataList", formData);
        return "formpost";
    }

}
