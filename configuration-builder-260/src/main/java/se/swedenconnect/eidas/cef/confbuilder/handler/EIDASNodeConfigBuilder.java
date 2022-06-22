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

import se.swedenconnect.eidas.cef.confbuilder.configuration.*;

import java.io.File;
import java.io.IOException;

/**
 * Interface for an EIDAS node configurator processing property input and templates to build configuration data
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public interface EIDASNodeConfigBuilder {

  void buildConfiguration(
    File configDirectory, File templateDirectory, File targetDirectory,
    BaseProperties baseProperties, MetadataProperties metadataProperties,
    ServicesProperties servicesProperties, KeystoreProperties keystoreProperties,
    IdpProperties idpProperties, SpProperties spProperties
  ) throws IOException;

}
