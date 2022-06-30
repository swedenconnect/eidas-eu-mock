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
