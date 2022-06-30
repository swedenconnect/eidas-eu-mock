/*
 * Copyright (c) 2018 by European Commission
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be
 * approved by the European Commission - subsequent versions of the
 * EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/page/eupl-text-11-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package eu.eidas.node.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This class consists exclusively on static methods to sanitize content before inserting into logs.
 */
public class LoggingSanitizer {

    private static final String SANITIZED_SUFIX = " (Sanitized)";

    private LoggingSanitizer() {}

    /**
     * Removes CRLF characters by calling method {@link StringEscapeUtils#escapeJava(String)}.
     * Appends SANITIZED_SUFIX, to indicate when any escaping was applied.
     *
     * @param message the initial string to be removed the CRLF injection
     * @return the resulting string after CRLF injection removal
     */
     public static String removeCRLFInjection(String message) {
        if (StringUtils.isNotEmpty(message)) {
            String clean = StringEscapeUtils.escapeJava(message);
            if (!message.equals(clean)) {
                return clean + SANITIZED_SUFIX;
            }
        }

        return  message;
    }

}
