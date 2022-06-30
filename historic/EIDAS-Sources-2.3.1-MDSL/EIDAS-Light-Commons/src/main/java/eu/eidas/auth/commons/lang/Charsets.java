/* 
#   Copyright (c) 2017 European Commission  
#   Licensed under the EUPL, Version 1.2 or – as soon they will be 
#   approved by the European Commission - subsequent versions of the 
#    EUPL (the "Licence"); 
#    You may not use this work except in compliance with the Licence. 
#    You may obtain a copy of the Licence at: 
#    * https://joinup.ec.europa.eu/page/eupl-text-11-12  
#    *
#    Unless required by applicable law or agreed to in writing, software 
#    distributed under the Licence is distributed on an "AS IS" basis, 
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
#    See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.eidas.auth.commons.lang;

import java.nio.charset.Charset;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Charsets.
 *
 * @since 1.1.1
 */
public final class Charsets {

    // Java 7+ Regexp for Unicode scripts which span multiple Unicode blocks:
    private static final Pattern IS_LATIN_OR_COMMON_PATTERN = Pattern.compile("[\\p{IsLatin}\\p{IsCommon}]*");

    public static final String ISO_LATIN_1_ENCODING = "ISO-8859-1";

    public static final String UTF8_ENCODING = "UTF-8";

    public static final Charset ISO_LATIN_1 = Charset.forName(ISO_LATIN_1_ENCODING);

    public static final Charset UTF8 = Charset.forName(UTF8_ENCODING);

    /**
     * Returns {@code true} if and only if the given value is in Latin script according to Unicode Common and Latin
     * scripts.
     *
     * @param value the value to test
     * @return {@code true} if and only if the given value is in Latin script according to Unicode Common and Latin
     * scripts.
     */
    public static boolean isLatinScript(@Nullable CharSequence value) {
        return null == value || IS_LATIN_OR_COMMON_PATTERN.matcher(value).matches();
    }

    private Charsets() {
    }
}
