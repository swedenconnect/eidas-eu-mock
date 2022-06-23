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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Description
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class ConfigFileProcessor {

  String configData;
  File configFile;

  public ConfigFileProcessor(String fileName, File templateDir, File targetDir) throws IOException {
    this.configFile = new File(targetDir, fileName);
    this.configData = FileUtils.readFileToString(new File(templateDir, fileName), "UTF-8");
    save();
  }

  public void update(String id, int value) throws IOException {
    update(id, String.valueOf(value));
  }

  public void update(String id, boolean value) throws IOException {
    update(id, String.valueOf(value));
  }

  public void update(String id, String value) throws IOException {

    configData = configData.replace("${" + id + "}", value);
    save();
  }

  private void save() throws IOException {
    FileUtils.writeByteArrayToFile(configFile, configData.getBytes(StandardCharsets.UTF_8));
  }



}
