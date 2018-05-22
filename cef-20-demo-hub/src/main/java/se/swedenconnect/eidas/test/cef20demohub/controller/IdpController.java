package se.swedenconnect.eidas.test.cef20demohub.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.swedenconnect.eidas.test.cef20demohub.data.user.DemoUserFactory;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class IdpController {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @RequestMapping ("idp/**")
    public String authenticateUser(Model model, HttpServletRequest request){
        List<User> testUsers = DemoUserFactory.testUsers;
        String jsonUsers = GSON.toJson(testUsers);

        model.addAttribute("natUsers", DemoUserFactory.getSortedFilteredUserList(testUsers, User.PersonType.natural));
        model.addAttribute("nlegalUsers", DemoUserFactory.getSortedFilteredUserList(testUsers, User.PersonType.legal));

        return "authenticate";
    }

}
