package eu.eidas.auth.commons;

import java.nio.charset.Charset;

/**
 * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
 */
@Deprecated
public final class Constants {

    /**
     * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
     */
    @Deprecated
    public static final String UTF8_ENCODING = "UTF-8";

    /**
     * @deprecated since 1.1.1 use {@link eu.eidas.auth.commons.lang.Charsets} instead.
     */
    @Deprecated
    public static final Charset UTF8 = Charset.forName(UTF8_ENCODING);

    private Constants() {
    }
}
