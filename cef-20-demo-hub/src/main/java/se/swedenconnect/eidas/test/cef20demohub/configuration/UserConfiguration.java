package se.swedenconnect.eidas.test.cef20demohub.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;
import se.swedenconnect.eidas.test.cef20demohub.data.user.YamlUserFactory;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestLegalYaml;
import se.swedenconnect.eidas.test.cef20demohub.data.user.yaml.TestUserYaml;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class UserConfiguration {

    //Static
    private static final String USER_DATA_BASE_FILE_NAME = "user-data-";
    // Autowired
    private final String configLocation;
    private final SPConfigurationProperties configurationProperties;
    @Qualifier("yamlObjectMapper")
    private final ObjectMapper yamlObjectMapper;
    // Fields
    private Map<String, Map<String, User>> countryUserMap;
    private Map<String, List<User>> countryNatUserListMap;
    private Map<String, List<User>> countryLegalUserListMap;

    @Autowired
    public UserConfiguration(
            @Value("${spring.config.additional-location:#{null}}") String configLocation,
            SPConfigurationProperties configurationProperties, ObjectMapper yamlObjectMapper) {
        this.configLocation = configLocation;
        this.configurationProperties = configurationProperties;
        this.yamlObjectMapper = yamlObjectMapper;

        countryNatUserListMap = new HashMap<>();
        countryLegalUserListMap = new HashMap<>();

        if (configLocation == null || configLocation.startsWith("classpath")) {
            log.info("Local config location ({}), using default user setup", configLocation == null ? "null" : configLocation);
            configurationProperties.getSp().keySet().stream().forEach(country -> {
                countryNatUserListMap.put(country, YamlUserFactory.getSortedNatuserList(YamlUserFactory.getPreconfiguredNatUserYamlList(), true));
                countryLegalUserListMap.put(country, YamlUserFactory.getSortedLegalPersonList(YamlUserFactory.getPreconfiguredLegalPersonYamlList(), true));
            });
            setCountryUserMap();
            return;
        }

        //External config folder specified. Create config files if not present and load user config
        configurationProperties.getSp().keySet().stream().forEach(country -> {
            try {

                File natYamlFile = new File(configLocation, USER_DATA_BASE_FILE_NAME + country + "-natural.yaml");
                File legalYamlFile = new File(configLocation, USER_DATA_BASE_FILE_NAME + country + "-legal.yaml");
                if (!natYamlFile.exists()) {
                    yamlObjectMapper.writeValue(natYamlFile, YamlUserFactory.getPreconfiguredNatUserYamlList());
                }
                if (!legalYamlFile.exists()) {
                    yamlObjectMapper.writeValue(legalYamlFile, YamlUserFactory.getPreconfiguredLegalPersonYamlList());
                }

                List<TestUserYaml> testUserYamlList = yamlObjectMapper.readValue(natYamlFile, yamlObjectMapper.getTypeFactory().constructCollectionType(List.class, TestUserYaml.class));
                List<TestLegalYaml> testLegalYamlList = yamlObjectMapper.readValue(legalYamlFile, yamlObjectMapper.getTypeFactory().constructCollectionType(List.class, TestLegalYaml.class));

                countryNatUserListMap.put(country, YamlUserFactory.getSortedNatuserList(testUserYamlList, true));
                countryLegalUserListMap.put(country, YamlUserFactory.getSortedLegalPersonList(testLegalYamlList, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        setCountryUserMap();

    }


    private void setCountryUserMap() {
        countryUserMap = new HashMap<>();
        countryNatUserListMap.keySet().stream().forEach(country -> setUserMapValues(countryNatUserListMap.get(country), country));
        countryLegalUserListMap.keySet().stream().forEach(country -> setUserMapValues(countryLegalUserListMap.get(country), country));
    }

    private void setUserMapValues(List<User> userList, String country) {
        Map<String, User> userMap;
        if (countryUserMap.containsKey(country)) {
            userMap = countryUserMap.get(country);
        } else {
            userMap = new HashMap<>();
            countryUserMap.put(country, userMap);
        }
        userList.stream().forEach(user -> {
            if (!user.getId().equalsIgnoreCase("{empty}")) {
                userMap.put(user.getId(), user);
            }
        });
    }

    public Map<String, Map<String, User>> getCountryUserMap() {
        return countryUserMap;
    }

    public Map<String, List<User>> getCountryNatUserListMap() {
        return countryNatUserListMap;
    }

    public Map<String, List<User>> getCountryLegalUserListMap() {
        return countryLegalUserListMap;
    }
}
