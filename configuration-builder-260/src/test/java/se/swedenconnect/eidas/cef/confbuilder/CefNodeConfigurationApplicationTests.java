/*
 * Copyright (c) 2022.  Agency for Digital Government (DIGG)
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

package se.swedenconnect.eidas.cef.confbuilder;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.File;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
@ActiveProfiles("test")
class CefNodeConfigurationApplicationTests {

  @BeforeAll
  static void init() throws Exception{
    // Set test property
    String testConfFilePath = CefNodeConfigurationApplication.class.getClassLoader()
      .getResources("conf.properties")
      .nextElement()
      .getFile();
    File testConfFile = new File(testConfFilePath);
    System.setProperty("spring.config.additional-location", testConfFile.getParent() + "/");

    File targetDir = new File(System.getProperty("user.dir"), "target/test-data/test-conf");
    System.setProperty("option.target-dir", targetDir.getAbsolutePath());
  }

  @Test
  void contextLoads() {
  }

}
