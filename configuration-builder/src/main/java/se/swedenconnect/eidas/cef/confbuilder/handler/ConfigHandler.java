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

package se.swedenconnect.eidas.cef.confbuilder.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.cef.confbuilder.configuration.*;
import se.swedenconnect.eidas.cef.confbuilder.options.AppOptions;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main Bean to handle service requests based on CLI input data
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@Slf4j
@Component
public class ConfigHandler {

  @Value("${option.config-file}") String configFileProp;
  @Value("${option.target-dir}") String targetDirProp;

  private final EIDASNodeConfigBuilderProvider eidasNodeConfigBuilderProvider;

  private final BaseProperties baseProperties;
  private final MetadataProperties metadataProperties;
  private final ServicesProperties servicesProperties;
  private final KeystoreProperties keystoreProperties;
  private final IdpProperties idpProperties;
  private final SpProperties spProperties;

  @Autowired
  public ConfigHandler(
    EIDASNodeConfigBuilderProvider eidasNodeConfigBuilderProvider,
    BaseProperties baseProperties,
    MetadataProperties metadataProperties,
    ServicesProperties servicesProperties,
    KeystoreProperties keystoreProperties,
    IdpProperties idpProperties, SpProperties spProperties) {
    this.eidasNodeConfigBuilderProvider = eidasNodeConfigBuilderProvider;
    this.baseProperties = baseProperties;
    this.metadataProperties = metadataProperties;
    this.servicesProperties = servicesProperties;
    this.keystoreProperties = keystoreProperties;
    this.idpProperties = idpProperties;
    this.spProperties = spProperties;
  }

  public void run(String... args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    Options opt = AppOptions.getOptions();
    log.info("Log test on info level");
    try {
      CommandLine cmd = parser.parse(opt, args);
      validateInput(cmd);
    }
    catch (ParseException ex) {
      showHelp("Unable to parse arguments!");
    }

  }

  private void validateInput(CommandLine cmd) {

    File configFile = new File(configFileProp);
    System.out.println("Config file: " + configFile.getAbsolutePath());

    File targetDir = new File(targetDirProp);
    System.out.println("Target directory: " + targetDir.getAbsolutePath());

    if (!cmd.hasOption(AppOptions.OPTION_TEMPLATE_DIR)) {
      System.out.println("Template -" + AppOptions.OPTION_TEMPLATE_DIR + " must be set.");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar confbuilder.jar [options]", AppOptions.getOptions());
      return;
    }
    String templateDirStr = cmd.getOptionValue(AppOptions.OPTION_TEMPLATE_DIR);
    File templateDir;
    if (templateDirStr.startsWith("/")) {
      templateDir = new File(templateDirStr);
    }
    else {
      templateDir = new File(System.getProperty("user.dir"), templateDirStr);
    }
    if (!templateDir.exists() || !templateDir.isDirectory()) {
      System.out.println("No template directory " + templateDir.getAbsolutePath() + " exists");
      return;
    }
    System.out.println("Template data directory: " + templateDir.getAbsolutePath());

    String profile = "latest";
    if (cmd.hasOption(AppOptions.OPTION_PROFILE)) {
      profile = cmd.getOptionValue(AppOptions.OPTION_PROFILE);
    }
    System.out.println("Selected profile: " + profile);

    try {
      EIDASNodeConfigBuilder configBuilder = eidasNodeConfigBuilderProvider.getConfigBuilder(profile);
      configBuilder.buildConfiguration(configFile, templateDir, targetDir, baseProperties, metadataProperties,
        servicesProperties, keystoreProperties, idpProperties, spProperties);
    }
    catch (Exception e) {
      System.out.println("Error merging CA repositories: " + e.getMessage());
    }

  }

  private List<String> strList(List<BigInteger> bigIntegerList) {
    if (bigIntegerList == null) {
      return new ArrayList<>();
    }
    return bigIntegerList.stream()
      .map(bigInteger -> bigInteger.toString(16))
      .collect(Collectors.toList());
  }

  private void showHelp() {
    showHelp(null);
  }

  private void showHelp(String error) {
    if (error != null) {
      System.out.println("Error: " + error);
    }
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("java -jar repomigrate.jar [options]", AppOptions.getOptions());
  }

}
