package se.swedenconnect.eidas.test.cef20demohub.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ObjectMapper yamlObjectMapper () {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper;
    }

}
