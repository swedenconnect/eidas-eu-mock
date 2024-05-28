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

import org.springframework.stereotype.Component;
import se.swedenconnect.eidas.cef.confbuilder.handler.impl.CEF26ConfigBuilder;
import se.swedenconnect.eidas.cef.confbuilder.handler.impl.CEF27ConfigBuilder;

/**
 * Provides a configuration builder
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
@Component
public class EIDASNodeConfigBuilderProvider {

  public EIDASNodeConfigBuilder getConfigBuilder(String profile) throws IllegalArgumentException {

    return switch (profile) {
      case "CEF26" -> new CEF26ConfigBuilder();
      case "CEF27", "latest" -> new CEF27ConfigBuilder();
      default -> throw new IllegalArgumentException("Unrecognized profile: " + profile);
    };

  }

}
