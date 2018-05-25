package se.swedenconnect.eidas.test.cef20demohub.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.test.cef20demohub.data.user.DemoUserFactory;
import se.swedenconnect.eidas.test.cef20demohub.data.user.User;
import se.swedenconnect.eidas.test.cef20demohub.utils.FileOps;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Log4j2
public class UserConfiguration {

    //Static
    private static final String USER_DATA_BASE_FILE_NAME = "user-data-";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type CN_USER_MAP_TYPE = new TypeToken<Map<String, User>>() {
    }.getType();
    // Autowired
    private final String configLocation;
    private final SPConfigurationProperties configurationProperties;
    // Fields
    private Map<String, Map<String, User>> countryUserMap;
    private Map<String, List<User>> countryNatUserListMap;
    private Map<String, List<User>> countryLegalUserListMap;

    public UserConfiguration(
            @Value("${spring.config.location:#{null}}") String configLocation,
            SPConfigurationProperties configurationProperties) {
        this.configLocation = configLocation;
        this.configurationProperties = configurationProperties;

        countryUserMap = new HashMap<>();
        countryNatUserListMap = new HashMap<>();
        countryLegalUserListMap = new HashMap<>();

        if (configLocation == null || configLocation.startsWith("classpath")) {
            log.info("Local config location ({}), using default user setup", configLocation == null ? "null" : configLocation);
            configurationProperties.getSp().keySet().stream().forEach(country -> {
                Map<String, User> userMap = DemoUserFactory.testUserMap;
                putCountryUserData(userMap, country);
            });
            return;
        }

        //External config folder specified. Create config files if not present and load user config
        configurationProperties.getSp().keySet().stream().forEach(country -> {
            File userFile = new File(configLocation, USER_DATA_BASE_FILE_NAME + country + ".json");
            if (!userFile.exists()) {
                byte[] jsonBytes = GSON.toJson(DemoUserFactory.testUserMap).getBytes(StandardCharsets.UTF_8);
                FileOps.saveByteFile(jsonBytes, userFile);
            }
            String userJson = new String(FileOps.readBinaryFile(userFile),StandardCharsets.UTF_8);
            Map<String, User> userMap = GSON.fromJson(userJson, CN_USER_MAP_TYPE);
            putCountryUserData(userMap, country);
        });


    }

    private void putCountryUserData(Map<String,User> userMap, String country) {
        countryUserMap.put(country, userMap);
        countryNatUserListMap.put(country, DemoUserFactory.getSortedFilteredUserList(userMap, User.PersonType.natural, true));
        countryLegalUserListMap.put(country, DemoUserFactory.getSortedFilteredUserList(userMap, User.PersonType.legal, true));
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
