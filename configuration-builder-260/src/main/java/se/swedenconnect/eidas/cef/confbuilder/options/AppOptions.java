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
package se.swedenconnect.eidas.cef.confbuilder.options;

import org.apache.commons.cli.Options;

/**
 * CLI options
 *
 * @author Martin Lindström (martin@idsec.se)
 * @author Stefan Santesson (stefan@idsec.se)
 */
public class AppOptions {
    public static final String OPTION_TARGET = "o";
    public static final String OPTION_CONF = "c";
    public static final String OPTION_PROFILE = "p";
    public static final String OPTION_TEMPLATE_DIR = "t";
    public static final String OPTION_LOG = "log";
    public static final String OPTION_HELP = "help";

    private static final Options op;

    static{
        op = new Options();
        op.addOption(OPTION_TARGET, true, "Target directory where configuration data will be written.");
        op.addOption(OPTION_CONF, true, "Directory where configuration input data is provided");
        op.addOption(OPTION_TEMPLATE_DIR, true, "Directory where template files are located");
        op.addOption(OPTION_PROFILE, true, "Profile identifier defining the function of the builder (default CEF26)");
        op.addOption(OPTION_LOG, false, "Include log information in the console output");
        op.addOption(OPTION_HELP, false, "Print this message");
    }

    public static Options getOptions() {
        return op;
    }
}
