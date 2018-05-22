package se.swedenconnect.eidas.test.cef20demohub.process;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import eu.eidas.SimpleProtocol.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import se.swedenconnect.eidas.test.cef20demohub.configuration.SPConfigurationProperties;
import se.swedenconnect.eidas.test.cef20demohub.data.CitizenCountry;
import se.swedenconnect.eidas.test.cef20demohub.data.DemoLevelOfAssurance;
import se.swedenconnect.eidas.test.cef20demohub.data.ValueAttribute;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Log4j2
public class GeneralUtils {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final SPConfigurationProperties spConfigurationProperties;

    @Autowired
    public GeneralUtils(SPConfigurationProperties spConfigurationProperties) {
        this.spConfigurationProperties = spConfigurationProperties;
    }

    public List<ValueAttribute> getAttributeList(Response response) {

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
        return "<pre><code class='json'>" + jsonString + "</code></pre>";
    }

    public String getLoa(Response response) {
        Optional<DemoLevelOfAssurance> loaFromDemoLevel = DemoLevelOfAssurance.getLoaFromDemoLevel(response.getAuthContextClass());
        return loaFromDemoLevel.isPresent() ? loaFromDemoLevel.get().getUri() : response.getAuthContextClass();
    }

    public List<CitizenCountry> getCountryList(SPConfigurationProperties.SpConfig spConfig) {
        List<CitizenCountry> countryList = new ArrayList<>();
        Map<String, SPConfigurationProperties.PsCountry> countryMap = spConfig.getCountry();
        List<SPConfigurationProperties.PsCountry> psCountries = countryMap.keySet().stream()
                .map(s -> {
                    SPConfigurationProperties.PsCountry psCountry = countryMap.get(s);
                    psCountry.setCountryCode(s);
                    return psCountry;
                })
                .collect(Collectors.toList());
        Collections.sort(psCountries, new Comparator<SPConfigurationProperties.PsCountry>() {
            @Override
            public int compare(SPConfigurationProperties.PsCountry o1, SPConfigurationProperties.PsCountry o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        psCountries.stream()
                .forEach(country -> {
                    countryList.add(new CitizenCountry(country.getCountryCode(), getCountryImage("../img/flags/" + country.getFlag()), country.getName()));
                });
        return countryList;
    }

    private String getCountryImage(String imgUrl) {
        return "<img src='" + imgUrl + "'>";
    }

    public String getCountry(HttpServletRequest request) throws IllegalArgumentException {
        try {
            String servletPath = request.getServletPath();
            String spCountry = servletPath.substring(servletPath.lastIndexOf("/") + 1).toUpperCase();
            SPConfigurationProperties.SpConfig spConfig = spConfigurationProperties.getSp().get(spCountry);
            if (spConfig != null) {
                return spCountry;
            }
        } catch (Exception ex) {
            log.error("Illegal service url: {}", ex.getMessage());
        }
        log.error("URL path provided for unsupported country");
        throw new IllegalArgumentException("Bad service URL");
    }

}
