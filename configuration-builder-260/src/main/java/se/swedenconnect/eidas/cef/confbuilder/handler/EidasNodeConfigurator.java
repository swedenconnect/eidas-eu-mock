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
import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.cef.confbuilder.options.AppOptions;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
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
public class EidasNodeConfigurator {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");


  @Autowired
  public EidasNodeConfigurator() {
  }

  public void run(String... args) throws Exception {
    CommandLineParser parser = new DefaultParser();
    Options opt = AppOptions.getOptions();
    log.info("Log test on info level");
    try {
      CommandLine cmd = parser.parse(opt, args);
      validateInput(cmd);
    } catch (ParseException ex) {
      showHelp("Unable to parse arguments!");
    }

  }

  private void validateInput(CommandLine cmd) {
    if (cmd.hasOption(AppOptions.OPTION_HELP)) {
      showHelp();
      return;
    }
    try {
      System.out.println("Doing the main stuff here....");
    }
    catch (Exception e) {
      System.out.println("Error merging CA repositories");
      e.printStackTrace();
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
