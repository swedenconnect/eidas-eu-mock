package eu.eidas.auth.commons;

import javax.annotation.Nonnull;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Base64;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public final class EidasStringUtil {

    /**
     * Contains the possible values of delimiters for strings with tokens.
     */
    private static final Pattern STRING_SPLITTER = Pattern.compile("[,;]");

    /**
     * Decodes the given {@link Base64} String into a byte array.
     *
     * @param base64String the BASE64 String to be decoded.
     * @return The decoded byte array.
     * @see Base64#decode
     */
    @Nonnull
    public static byte[] decodeBytesFromBase64(@Nonnull String base64String) {
        return Base64.decode(base64String);
    }

    /**
     * Decodes the given {@link Base64} String into a byte array.
     *
     * @param base64String the BASE64 String to be decoded.
     * @return The decoded byte array.
     * @see Base64#decode
     */
    @Nonnull
    public static String decodeStringFromBase64(@Nonnull String base64String) {
        return toString(decodeBytesFromBase64(base64String));
    }

    /**
     * {@link Base64} encodes the given byte array into a BASE64 string.
     *
     * @param bytes the byte array to be encoded.
     * @return The Base64 String of the encoded bytes.
     * @see Base64#encode
     */
    @Nonnull
    public static String encodeToBase64(@Nonnull byte[] bytes) {
        if (bytes.length == 0) {
            return StringUtils.EMPTY;
        }
        return toString(Base64.encode(bytes));
    }

    /**
     * {@link Base64} encodes the given normal string into a BASE64 string.
     *
     * @param bytes the value to be encoded.
     * @return The Base64 String of the encoded bytes coming from the given string.
     * @see Base64#encode
     */
    @Nonnull
    public static String encodeToBase64(@Nonnull String value) {
        return encodeToBase64(getBytes(value));
    }

    /**
     * @param value input String
     * @return the corresponding array of bytes encoded in UTF-8
     */
    @Nonnull
    public static byte[] getBytes(@Nonnull String value) {
        return value.getBytes(Constants.UTF8);
    }

    /**
     * @param bytes input byte array
     * @return a String created from the given bytes, encoded in UTF-8
     */
    @Nonnull
    public static String toString(@Nonnull byte[] bytes) {
        return new String(bytes, Constants.UTF8);
    }

    /**
     * Splits a string that contains several tokens separated by a delimiter defined in {@link EidasStringUtil#STRING_SPLITTER}
     *
     * @param tokens the string containing tokens
     * @return the {@link List<String>} containing the tokens or an empty list if no token is found.
     */
    public static List<String> getTokens (String tokens) {
        if (StringUtils.isNotEmpty(tokens)) {
            return Arrays.asList(STRING_SPLITTER.split(tokens));
        } else {
            return new ArrayList<>();
        }
    }

    private EidasStringUtil() {
    }


}
