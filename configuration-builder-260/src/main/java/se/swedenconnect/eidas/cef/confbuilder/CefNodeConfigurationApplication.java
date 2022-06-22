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

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import se.swedenconnect.eidas.cef.confbuilder.handler.ConfigHandler;
import se.swedenconnect.eidas.cef.confbuilder.options.AppOptions;

import java.io.File;
import java.io.IOException;

/**
 * Main application for this CLI Spring Boot application that is executed using java -jar repomigrate.jar [options]
 *
 * Help menu is available through java -jar repomigrate.jar -help
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@SpringBootApplication()
public class CefNodeConfigurationApplication implements CommandLineRunner {

  private final ConfigHandler configHandler;

  @Autowired
  public CefNodeConfigurationApplication(ConfigHandler configHandler) {
    this.configHandler = configHandler;
  }

  public static void main(String[] args) throws ParseException {

    CommandLineParser parser = new DefaultParser();
    Options opt = AppOptions.getOptions();
    CommandLine cmd = parser.parse(opt, args);

    if (cmd.hasOption(AppOptions.OPTION_HELP)){
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar confbuilder.jar [options]", AppOptions.getOptions());
      return;
    }

    if (cmd.hasOption(AppOptions.OPTION_LOG)){
      /*
        #logging.level.root=WARN
        #logging.level.se.swedenconnect.ca.tools.repomigration.CaRepositoryApplication = WARN
       */
      System.setProperty("logging.level.root", "INFO");
      System.setProperty("logging.level.se.swedenconnect.eidas.cef.confbuilder.CaRepositoryApplication", "INFO");
      System.setProperty("logging.level.se.swedenconnect.eidas.cef.confbuilder", "INFO");
    }

    if (!cmd.hasOption(AppOptions.OPTION_CONF)){
      System.out.println("Configuration directory -" + AppOptions.OPTION_CONF + " must be set.");
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("java -jar confbuilder.jar [options]", AppOptions.getOptions());
      return;
    }
    File configDir;
    final String confDirStr = cmd.getOptionValue(AppOptions.OPTION_CONF);
    if (confDirStr.startsWith("/")){
      configDir = new File(confDirStr);
    } else {
      configDir = new File(System.getProperty("user.dir"), confDirStr);
    }
    if (!configDir.exists() || !configDir.isDirectory()){
      System.out.println("provided config dir " + configDir.getAbsolutePath() + "does not exist");
      return;
    }
    System.setProperty("spring.config.additional-location", configDir.getAbsolutePath() + "/");

    File targetDir;
    if (cmd.hasOption(AppOptions.OPTION_TARGET)) {
      final String targetFileStr = cmd.getOptionValue(AppOptions.OPTION_TARGET);
      if (targetFileStr.startsWith("/")) {
        targetDir = new File(targetFileStr);
      }
      else {
        targetDir = new File(System.getProperty("user.dir"), targetFileStr);
      }
    }
    else {
      targetDir = new File(System.getProperty("user.dir"), "target");
    }
    if (targetDir.exists()) {
      try {
        FileUtils.forceDelete(targetDir);
      }
      catch (IOException e) {
        System.out.println("Unable to delete target dir: " + e.toString());
        return;
      }
    }
    if (!targetDir.mkdirs()) {
      System.out.println("Unable to create target dir");
      return;
    }
    System.setProperty("option.target-dir", targetDir.getAbsolutePath());


    // Run application
    SpringApplication.run(CefNodeConfigurationApplication.class, args);
  }

  @Override public void run(String... args) throws Exception {
    configHandler.run(args);
  }

}
