/*
 * Copyright (c) 2021-2022.  Agency for Digital Government (DIGG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.swedenconnect.eidas.cef.confbuilder.configuration;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Extracting essential configuration properties
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@Configuration
@PropertySource(value = "file://${spring.config.additional-location}conf.properties", encoding = "Utf8")
@ConfigurationProperties(prefix = "metadata")
@Data
@ToString
public class MetadataProperties {

  MetadataContacts contact;
  MetadataOrganization organization;

  @Data
  public static class MetadataContacts {
    private MetadataContactData support;
    private MetadataContactData technical;
  }

  @Data
  public static class MetadataContactData {
    private String email;
    private String company;
    private String givenname;
    private String surname;
    private String phone;
  }

  @Data
  public static class MetadataOrganization {
    private String name;
    private String displayname;
    private String url;
  }

}
