package se.swedenconnect.eidas.test.cef20demohub.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.swedenconnect.eidas.test.cef20demohub.data.user.DemoUserFactory;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;
import se.swedenconnect.eidas.test.cef20demohub.process.GeneralUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class IdpController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final GeneralUtils generalUtils;

    @Autowired
    public IdpController(GeneralUtils generalUtils) {
        this.generalUtils = generalUtils;
    }

    @RequestMapping ("idp/**")
    public String authenticateUser(Model model, HttpServletRequest request){
        String country = generalUtils.getCountry(request);
        Map<String, User> testUsers = DemoUserFactory.testUserMap;
        List<User> natUsers = DemoUserFactory.naturalUsers;
        List<User> legalUsers = DemoUserFactory.legalUsers;

        String jsonUsers = GSON.toJson(testUsers.keySet().stream().map(s -> testUsers.get(s)).collect(Collectors.toList()));

        model.addAttribute("country" , country);
        model.addAttribute("natUsers", natUsers);
        model.addAttribute("legalUsers", legalUsers);

        return "authenticate";
    }

    @RequestMapping("userSelect/**")
    public String sendAuthnResponse(Model model, HttpServletRequest request){
        String country = generalUtils.getCountry(request);

        //TODO generate Authn response

        return "formpost";
    }

}
