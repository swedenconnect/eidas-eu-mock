package se.swedenconnect.eidas.test.cef20demohub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {


    @Value("${cef20demohub.config.serviceUrl}")
    String serviceUrl;
    private final SPConfigurationProperties spConfigurationProperties;

    @Autowired
    public HomeController(SPConfigurationProperties spConfigurationProperties) {
        this.spConfigurationProperties = spConfigurationProperties;
    }

    @RequestMapping({"/home", "/"})
    public String getHomePage(Model model) {
        final Map<String, SPConfigurationProperties.SpConfig> spMap = spConfigurationProperties.getSp();
        List<String> countryCodeList = spMap.keySet().stream().collect(Collectors.toList());
        Collections.sort(countryCodeList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        model.addAttribute("countryList", countryCodeList);
        model.addAttribute("spConfigMap", spMap);
        model.addAttribute("serviceUrl", serviceUrl+"/sp/");
        return "home";
    }
}
