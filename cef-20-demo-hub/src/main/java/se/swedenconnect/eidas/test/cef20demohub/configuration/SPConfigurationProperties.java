package se.swedenconnect.eidas.test.cef20demohub.configuration;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Map;

@Configuration
@PropertySource(value = "${cef20demohub.config.location}spconfig.properties", encoding = "Utf8")
@ConfigurationProperties(prefix = "")
@Data
@ToString
public class SPConfigurationProperties {
    private String baseReturnUrl;
    private Map<String, SpConfig> sp;
    private Map<String, ExtSPConfig> extSp;

    @Data
    public static class SpConfig {
        private String cefVersion;
        private String name;
        private String description;
        private String idpName;
        private String requestUrl;
        private String flagUrl;
        private boolean nonNotified;
        private Map<String, PsCountry> country;
    }

    @Data
    public static class PsCountry {
        private String name;
        private String flag;
        private String countryCode;
    }

    @Data
    public static class ExtSPConfig {
        private String cefVersion;
        private String description;
        private String flagUrl;
        private String spUrl;
    }
}
